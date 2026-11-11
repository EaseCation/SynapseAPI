package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBread;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.EntityEventPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.EntityEventPacket12620;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EatingPresentationAdmissionTest {
    @Test void nativeFoodPresentationUsesServerHeldItem() { check(false, "normal", true); }
    @Test void modernFoodPresentationUsesServerHeldItem() { check(true, "normal", true); }
    @Test void nativeWrongEntityCannotPublish() { check(false, "wrong-id", true); }
    @Test void modernWrongEntityCannotPublish() { check(true, "wrong-id", true); }
    @Test void nativeZeroDataCannotPublish() { check(false, "zero", true); }
    @Test void modernZeroDataCannotPublish() { check(true, "zero", true); }
    @Test void nativeNonFoodCannotPublish() { check(false, "nonfood", true); }
    @Test void modernNonFoodCannotPublish() { check(true, "nonfood", true); }
    @Test void nativeCancellationStopsOriginalResetAndBroadcast() { check(false, "cancel", true); }
    @Test void modernCancellationStopsOriginalResetAndBroadcast() { check(true, "cancel", true); }
    @Test void validatorRejectionStopsBroadcast() { check(true, "reject", true); }
    @Test void deadPlayerKeepsOriginalRejection() { check(false, "dead", true); }
    @Test void disabledModeKeepsOriginalBroadcast() { check(false, "normal", false); }
    @Test void disabledModernModeKeepsOriginalBroadcast() { check(true, "normal", false); }

    @Test void onlyEatingSubtypeIsEligibleAndReadinessStillApplies() {
        for (boolean modern : new boolean[]{false, true}) {
            for (int subtype : new int[]{EntityEventPacket.EATING_ITEM, EntityEventPacket.ENCHANT, EntityEventPacket.HURT_ANIMATION}) {
                assertEquals(subtype == EntityEventPacket.EATING_ITEM, BetweenTickPackets.supports(packet(modern, subtype, 8, 1)));
            }
            ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
            player.spawned(false);
            assertFalse(BetweenTickPackets.canRunBetweenTicks(player, packet(modern, EntityEventPacket.EATING_ITEM, 8, 1)));
        }
    }

    private void check(boolean modern, String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        player.configure(server, inventory);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        doReturn(modern ? 975 : 860).when(player).getProtocol();
        doReturn(true).when(player).isInitialized();
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(!variant.equals("dead")).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(false).when(player).isClosed();
        doReturn(8L).when(player).getId();
        doReturn(Map.<Long, Player>of()).when(player).getViewers();
        doReturn(true).when(player).dataPacket(any());
        when(inventory.getItemInHand()).thenReturn(variant.equals("nonfood") ? new Item(ItemID.DIAMOND_SWORD) : new ItemBread(0, 8));
        List<String> order = new ArrayList<>();
        doAnswer(call -> { order.add("event"); if (variant.equals("cancel")) ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled(); return null; }).when(plugins).callEvent(any());
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals(List.of("event"), order); assertEquals(variant.equals("cancel"), call.getArgument(2));
            order.add("validate"); return variant.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED;
        });
        DataPacket packet = packet(modern, EntityEventPacket.EATING_ITEM, variant.equals("wrong-id") ? 9 : 8, variant.equals("zero") ? 0 : -1);
        boolean publish = variant.equals("normal");
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            if (enabled && !variant.equals("dead")) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) { assertTrue(context.betweenTicks()); player.handleDataPacket(packet); }
                }, 1, true);
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> order.add("follower"), 1, true);
                assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
                assertEquals("follower", order.getLast());
            } else player.handleDataPacket(packet);
            global.verify(() -> Server.broadcastPacket(anyCollection(), any(EntityEventPacket.class)), times(publish ? 1 : 0));
        }
        verify(plugins).callEvent(any(DataPacketReceiveEvent.class));
        verify(processor, times(enabled ? 1 : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        if (publish) {
            ArgumentCaptor<EntityEventPacket> out = ArgumentCaptor.forClass(EntityEventPacket.class);
            verify(player).dataPacket(out.capture());
            assertEquals(EntityEventPacket.EATING_ITEM, out.getValue().event);
            assertEquals(ItemID.BREAD << 16, out.getValue().data);
            assertEquals(8, out.getValue().eid);
        } else verify(player, never()).dataPacket(any());
        assertEquals(variant.equals("cancel") || variant.equals("reject") || variant.equals("dead") ? Player.CRAFTING_BIG : Player.CRAFTING_SMALL, player.craftingType);
        verify(inventory, never()).setItemInHand(any());
        verify(player, never()).setUsingItem(anyBoolean());
        verify(player, never()).getFoodData();
        verify(player, never()).onUpdate(anyInt());
    }

    private static DataPacket packet(boolean modern, int event, long eid, int data) {
        if (modern) { EntityEventPacket12620 p = new EntityEventPacket12620(); p.event=event; p.eid=eid; p.data=data; return p; }
        EntityEventPacket116100 p = new EntityEventPacket116100(); p.event=event; p.eid=eid; p.data=data; return p;
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, PlayerInventory inventory) { this.server=server; this.inventory=inventory; this.spawned=true; this.isSynapseLogin=true; this.craftingType=Player.CRAFTING_BIG; }
    }
}
