package org.itxtech.synapseapi;

import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemUseSlotTest {
    private TestPlayer player;
    private PlayerInventory inventory;
    private Item snowballs;

    @BeforeEach
    void prepare() {
        player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        inventory = mock(PlayerInventory.class);
        player.configure(inventory);
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(false).when(player).isJavaClient();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isClosed();
        doReturn(true).when(player).dataPacket(any());
        doReturn(true).when(player).isInputSessionActive();
        snowballs = new Item(ItemID.SNOWBALL, 0, 8);
        when(inventory.getHotbarSize()).thenReturn(9);
        when(inventory.getItem(1)).thenAnswer(call -> snowballs.clone());
        doAnswer(call -> {
            doReturn(1).when(inventory).getHeldItemIndex();
            return true;
        }).when(inventory).equipItem(1);
    }

    @Test
    void useBeforeEquipmentSelectsServerOwnedSlotAndRejectsAConsumedReplay() {
        assertTrue(player.prepareItemUseSlot(1, snowballs.clone(), ItemUseHand.MAIN_HAND));
        Item replay = snowballs.clone();
        snowballs.setCount(7);
        assertFalse(player.prepareItemUseSlot(1, replay, ItemUseHand.MAIN_HAND));
        verify(inventory, times(1)).equipItem(1);
        verify(inventory).sendContents(player);
        verify(player).dataPacket(isA(PlayerHotbarPacket.class));
        verify(inventory, never()).setItemInHand(any());
    }

    @Test
    void invalidSlotItemOrCountCannotSelectOrCreateInventory() {
        assertFalse(player.prepareItemUseSlot(-1, snowballs, ItemUseHand.MAIN_HAND));
        assertFalse(player.prepareItemUseSlot(9, snowballs, ItemUseHand.MAIN_HAND));
        assertFalse(player.prepareItemUseSlot(1, new Item(ItemID.ENDER_PEARL, 0, 8), ItemUseHand.MAIN_HAND));
        assertFalse(player.prepareItemUseSlot(1, new Item(ItemID.SNOWBALL, 0, 64), ItemUseHand.MAIN_HAND));
        assertFalse(player.prepareItemUseSlot(1, null, ItemUseHand.MAIN_HAND));
        verify(inventory, never()).equipItem(anyInt());
        verify(inventory, never()).setItemInHand(any());
    }

    @Test
    void cancelledSlotChangeDoesNotAllowUse() {
        doReturn(false).when(inventory).equipItem(1);
        assertFalse(player.prepareItemUseSlot(1, snowballs, ItemUseHand.MAIN_HAND));
        verify(inventory).sendContents(player);
    }

    @Test
    void eventInventoryReplacementCannotBecomeTheUseItem() {
        doAnswer(call -> {
            doReturn(1).when(inventory).getHeldItemIndex();
            snowballs.setCount(0);
            return true;
        }).when(inventory).equipItem(1);
        assertFalse(player.prepareItemUseSlot(1, snowballs.clone(), ItemUseHand.MAIN_HAND));
    }

    @Test
    void eventTeleportOrDisconnectPreventsActionAfterSelection() {
        doAnswer(call -> {
            doReturn(1).when(inventory).getHeldItemIndex();
            doReturn(1L).when(player).getMovementEpoch();
            return true;
        }).when(inventory).equipItem(1);
        assertFalse(player.prepareItemUseSlot(1, snowballs, ItemUseHand.MAIN_HAND));
        doAnswer(call -> {
            doReturn(false).when(player).isOnline();
            return true;
        }).when(inventory).equipItem(1);
        assertFalse(player.prepareItemUseSlot(1, snowballs, ItemUseHand.MAIN_HAND));
    }

    @Test
    void invalidatedSessionCannotUseOrSendCorrection() {
        doReturn(false).when(player).isInputSessionActive();
        assertFalse(player.prepareItemUseSlot(1, snowballs, ItemUseHand.MAIN_HAND));
        verifyNoInteractions(inventory);
        verify(player, never()).dataPacket(any());
    }

    @Test
    void disabledModeAndExplicitJavaHandKeepExistingRouting() {
        doReturn(false).when(player).isMainThreadInputEnabled();
        assertTrue(player.prepareItemUseSlot(-1, null, ItemUseHand.MAIN_HAND));
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isJavaClient();
        assertTrue(player.prepareItemUseSlot(-1, null, ItemUseHand.MAIN_HAND));
        doReturn(false).when(player).isJavaClient();
        assertTrue(player.prepareItemUseSlot(-1, null, ItemUseHand.OFF_HAND));
        verifyNoInteractions(inventory);
    }

    static class TestPlayer extends SynapsePlayer {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void configure(PlayerInventory inventory) {
            this.inventory = inventory;
            this.spawned = true;
        }
    }
}
