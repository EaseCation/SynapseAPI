package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class AirUseCancellationTest {
    @BeforeAll
    static void initializeItems() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_30.getProtocolStart();
        }
    }

    @Test
    void modernCancellationAfterSelectionChangeResyncsOriginalInventory() {
        verifyCancellation(true, true);
    }

    @Test
    void legacyCancellationAfterSelectionChangeResyncsOriginalInventory() {
        verifyCancellation(false, true);
    }

    @Test
    void disabledModeKeepsOriginalHeldItemResync() {
        verifyCancellation(false, false);
    }

    private void verifyCancellation(boolean modern, boolean inputMode) {
        BlockPlacementInventoryResyncTest.TestPlayer player = mock(BlockPlacementInventoryResyncTest.TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(plugins);
        PlayerInventory inventory = mock(PlayerInventory.class);
        InventorySlotReference source = mock(InventorySlotReference.class);
        int protocol = modern ? AbstractProtocol.PROTOCOL_126_30.getProtocolStart()
                : AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        player.configure(server, mock(Level.class), inventory, protocol);
        doReturn(inputMode).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isInitialized();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isInputSessionActive();
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isClosed();
        doReturn(false).when(player).isUsingItem();
        doReturn(ItemUseHand.MAIN_HAND).when(player).getUsingItemHand();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isNetEaseClient();
        doReturn(protocol).when(player).getProtocol();
        doReturn(true).when(player).dataPacket(any());
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        Item item = Item.get(ItemID.SNOWBALL, 0, 8);
        when(inventory.getItemInHand()).thenReturn(item);
        Item storedItem = item.clone();
        when(inventory.getItem(0)).thenReturn(storedItem);
        when(inventory.getHotbarSize()).thenReturn(9);
        when(inventory.equipItem(0)).thenReturn(true);
        when(inventory.captureHeldItem()).thenReturn(source);
        doAnswer(call -> {
            PlayerInteractEvent event = call.getArgument(0);
            when(inventory.getHeldItemIndex()).thenReturn(1);
            event.setCancelled();
            return null;
        }).when(plugins).callEvent(any(PlayerInteractEvent.class));
        UseItemData data = new UseItemData();
        data.actionType = InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR;
        data.hotbarSlot = 0;
        data.itemInHand = item.clone();
        DataPacket packet;
        if (modern) {
            InventoryTransactionPacket12630 transaction = new InventoryTransactionPacket12630();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        } else {
            InventoryTransactionPacket116 transaction = new InventoryTransactionPacket116();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        }
        player.handleDataPacket(packet);
        verify(plugins).callEvent(any(PlayerInteractEvent.class));
        verify(source, never()).setItem(any());
        verify(inventory, never()).setItemInHand(any());
        if (inputMode) {
            verify(source).sendContents(player);
            verify(player).dataPacket(isA(PlayerHotbarPacket.class));
            verify(inventory, never()).sendHeldItem(player);
        } else {
            verify(inventory).sendHeldItem(player);
            verify(inventory, never()).captureHeldItem();
        }
    }
}
