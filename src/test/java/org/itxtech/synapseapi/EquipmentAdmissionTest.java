package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.network.Network;
import cn.nukkit.utils.BinaryStream;
import java.util.Arrays;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void deadEquipmentPreservesOriginalIgnoreBeforeInventoryRead() {
        for (boolean enabled : new boolean[]{true, false}) {
            try (Fixture f = new Fixture(enabled)) {
                f.player.makeDead();
                f.deliver(equipment(1, ItemID.STONE));
                assertEquals(0, f.slots.getHeldItemIndex());
                verify(f.slots, never()).getItem(anyInt());
                verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                verify(f.plugins, never()).callEvent(any(PlayerItemHeldEvent.class));
                verifyNoInteractions(f.processor);
            }
        }
    }

    @Test void hotbarPreservesOriginalDeadEventAndModeSpecificCommit() {
        for (boolean enabled : new boolean[]{true, false}) {
            try (Fixture f = new Fixture(enabled)) {
                f.player.makeDead();
                f.deliver(hotbar(1));
                assertEquals(enabled ? 0 : 1, f.slots.getHeldItemIndex());
                verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                verify(f.plugins).callEvent(any(PlayerItemHeldEvent.class));
                verifyNoInteractions(f.processor);
            }
        }
    }

    @Test void sourceMismatchResynchronizesWithoutSelectingOrEndingUse() {
        try (Fixture f = new Fixture(true)) {
            f.deliver(equipment(1, ItemID.DIRT));
            verify(f.slots).sendContents(f.player);
            assertEquals(0, f.slots.getHeldItemIndex());
            verify(f.plugins, never()).callEvent(any(PlayerItemHeldEvent.class));
            verify(f.player, never()).setUsingItem(anyBoolean());
        }
    }

    @Test void actualSelectionAndEchoKeepUseAndEventCounts() {
        for (boolean enabled : new boolean[]{true, false}) {
            for (boolean hotbar : new boolean[]{true, false}) {
                try (Fixture f = new Fixture(enabled)) {
                    DataPacket p = hotbar ? hotbar(1) : equipment(1, ItemID.STONE);
                    f.deliver(p);
                    f.deliver(p);
                    assertEquals(1, f.slots.getHeldItemIndex());
                    verify(f.plugins, times(enabled ? 1 : 2)).callEvent(any(PlayerItemHeldEvent.class));
                    verify(f.player, times(enabled ? 1 : 0)).setUsingItem(false);
                    verifyNoInteractions(f.processor);
                    verify(f.player, never()).onUpdate(anyInt());
                }
            }
        }
    }

    @Test void cancelledPacketAndHeldEventCannotChangeSlot() {
        for (boolean hotbar : new boolean[]{true, false}) {
            for (boolean packetCancel : new boolean[]{true, false}) {
                try (Fixture f = new Fixture(true)) {
                    doAnswer(call -> {
                        Object event = call.getArgument(0);
                        if (packetCancel && event instanceof DataPacketReceiveEvent e) e.setCancelled();
                        if (!packetCancel && event instanceof PlayerItemHeldEvent e) e.setCancelled();
                        return null;
                    }).when(f.plugins).callEvent(any());
                    f.deliver(hotbar ? hotbar(1) : equipment(1, ItemID.STONE));
                    assertEquals(0, f.slots.getHeldItemIndex());
                    verify(f.player, never()).setUsingItem(anyBoolean());
                }
            }
        }
    }

    @Test void lifecycleAndNestedSlotChangeDuringHeldEventCannotOverwriteNewState() {
        for (String change : List.of("dead", "epoch", "slot")) {
            try (Fixture f = new Fixture(true)) {
                doAnswer(call -> {
                    if (call.getArgument(0) instanceof PlayerItemHeldEvent) {
                        if (change.equals("dead")) f.player.makeDead();
                        if (change.equals("epoch")) doReturn(1L).when(f.player).getMovementEpoch();
                        if (change.equals("slot")) f.slots.setHeldItemIndex(2, false);
                    }
                    return null;
                }).when(f.plugins).callEvent(any());
                f.deliver(equipment(1, ItemID.STONE));
                assertEquals(change.equals("slot") ? 2 : 0, f.slots.getHeldItemIndex());
                verify(f.player, never()).setUsingItem(anyBoolean());
                verify(f.player, never()).setDataFlag(Entity.DATA_FLAG_ACTION, false);
            }
        }
    }

    @Test void modernEntryGuardsAndUnspawnedEquipmentRemain() {
        for (String state : List.of("uninitialized", "retired", "unspawned")) {
            try (Fixture f = new Fixture(true)) {
                if (state.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
                if (state.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
                if (state.equals("unspawned")) f.player.spawned = false;
                f.deliver(equipment(1, ItemID.STONE));
                assertEquals(0, f.slots.getHeldItemIndex());
                verify(f.slots, never()).getItem(anyInt());
                verify(f.plugins, times(state.equals("unspawned") ? 1 : 0)).callEvent(any(DataPacketReceiveEvent.class));
            }
        }
    }

    @Test void invalidSlotAndOffhandUseRetainOriginalSelectionRules() {
        try (Fixture f = new Fixture(true)) {
            f.deliver(hotbar(99));
            assertEquals(0, f.slots.getHeldItemIndex());
            verify(f.slots).sendContents(f.player);
            doReturn(ItemUseHand.OFF_HAND).when(f.player).getUsingItemHand();
            f.deliver(hotbar(1));
            assertEquals(1, f.slots.getHeldItemIndex());
            verify(f.player, never()).setUsingItem(anyBoolean());
        }
    }

    @Test void nonInventoryAndNullEquipmentRemainStaticBarriers() {
        MobEquipmentPacket equipment = equipment(1, ItemID.STONE);
        equipment.windowId = ContainerIds.OFFHAND;
        assertFalse(BetweenTickPackets.supports(equipment));
        equipment.windowId = ContainerIds.INVENTORY;
        equipment.item = null;
        assertFalse(BetweenTickPackets.supports(equipment));
        PlayerHotbarPacket hotbar = hotbar(1);
        hotbar.windowId = ContainerIds.OFFHAND;
        assertFalse(BetweenTickPackets.supports(hotbar));
    }

    @Test void registeredTargetDecodersPreserveEquipmentAndHotbarPayloads() {
        try (Fixture f = new Fixture(true);
             MockedStatic<SynapseAPI> apiGlobal = mockStatic(SynapseAPI.class)) {
            SynapseAPI api = mock(SynapseAPI.class, RETURNS_DEEP_STUBS);
            apiGlobal.when(SynapseAPI::getInstance).thenReturn(api);
            Network network = mock(Network.class);
            when(f.server.getNetwork()).thenReturn(network);
            when(network.getPacket(ProtocolInfo.MOB_EQUIPMENT_PACKET)).thenAnswer(call -> new MobEquipmentPacket());
            when(network.getPacket(ProtocolInfo.PLAYER_HOTBAR_PACKET)).thenAnswer(call -> new PlayerHotbarPacket());
            for (int protocol : new int[]{859, 860, 898, 975}) {
                for (DataPacket packet : List.of(equipment(2, ItemID.STONE), hotbar(2))) {
                    packet.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());
                    packet.tryEncode();
                    // 原生出站编码保留旧三字节头，目标客户端实际入站使用现代变长头。
                    BinaryStream wire = new BinaryStream();
                    wire.putUnsignedVarInt(packet.pid());
                    wire.put(Arrays.copyOfRange(packet.getBuffer(), 3, packet.getCount()));
                    DataPacket decoded = PacketRegister.getFullPacket(wire.getBuffer(), protocol, false);
                    assertNotNull(decoded);
                    assertEquals(packet.getClass(), decoded.getClass());
                    assertEquals(wire.getCount(), decoded.getOffset());
                    assertTrue(BetweenTickPackets.supports(decoded));
                    if (decoded instanceof MobEquipmentPacket equipment) {
                        assertEquals(2, equipment.hotbarSlot);
                        assertEquals(ItemID.STONE, equipment.item.getId());
                        assertEquals(4, equipment.item.getCount());
                    } else {
                        assertEquals(2, ((PlayerHotbarPacket) decoded).selectedHotbarSlot);
                    }
                }
            }
        }
    }

    static MobEquipmentPacket equipment(int slot, int id) {
        MobEquipmentPacket packet = new MobEquipmentPacket();
        packet.windowId = ContainerIds.INVENTORY;
        packet.hotbarSlot = slot;
        packet.item = Item.get(id, 0, 4);
        return packet;
    }

    static PlayerHotbarPacket hotbar(int slot) {
        PlayerHotbarPacket packet = new PlayerHotbarPacket();
        packet.selectedHotbarSlot = slot;
        return packet;
    }

    static class Fixture extends PlayerActionAdmissionTest.Fixture {
        final PlayerInventory slots = mock(PlayerInventory.class, CALLS_REAL_METHODS);
        Fixture(boolean enabled) {
            super(enabled, true);
            player.configure(slots);
            doReturn(true).when(player).supportsPacketSequences();
            doReturn(slots).when(player).getWindowById(ContainerIds.INVENTORY);
            doReturn(player).when(slots).getHolder();
            doReturn(Item.get(ItemID.STONE, 0, 4)).when(slots).getItem(anyInt());
            doReturn(9).when(slots).getHotbarSize();
            doReturn(Set.of()).when(slots).getViewers();
            doNothing().when(slots).sendHeldItem(any(Collection.class));
            doNothing().when(slots).sendContents(any(Player.class));
            doNothing().when(slots).sendContents(any(Collection.class));
            doReturn(ItemUseHand.MAIN_HAND).when(player).getUsingItemHand();
            when(level.getServer()).thenReturn(server);
        }
    }
}
