package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryMismatchAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void deadPendingTransactionsKeepOriginalCloseAndSyncOrder() {
        for (Pending pending : Pending.values()) {
            check(pending, true, false, false);
            check(pending, false, false, false);
        }
    }

    @Test void modernLifecycleSyncUsesOriginalEventAndValidation() {
        for (String state : List.of("dead", "unspawned", "offline", "no-sequence", "no-processor")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                if (state.equals("dead")) f.player.makeDead();
                if (state.equals("unspawned")) f.player.spawned = false;
                if (state.equals("offline")) doReturn(false).when(f.player).isOnline();
                if (state.equals("no-sequence")) doReturn(false).when(f.player).supportsPacketSequences();
                if (state.equals("no-processor")) when(f.server.getPlayerInputProcessor()).thenReturn(null);
                doNothing().when(f.player).sendAllInventories();
                InventoryTransactionPacket116 packet = request();
                f.deliver(packet);
                verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                verify(f.player).sendAllInventories();
                verify(f.processor, times(state.equals("no-processor") ? 0 : 1)).validateReceivedPacket(f.player, packet, false);
                verify(f.player, never()).onUpdate(anyInt());
            }
        }
    }

    @Test void modernCancellationRejectionAndEntryGuardsPreventSync() {
        for (String state : List.of("cancel", "validation", "uninitialized", "retired")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                f.player.makeDead();
                if (state.equals("cancel")) f.cancel = true;
                if (state.equals("validation")) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                if (state.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
                if (state.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
                f.deliver(request());
                verify(f.player, never()).sendAllInventories();
                if (state.equals("uninitialized") || state.equals("retired")) verifyNoInteractions(f.plugins, f.processor);
            }
        }
    }

    @Test
    void emptySyncAndFollowerRunInOriginalOrder() {
        check(Pending.NONE, true, false);
    }

    @Test
    void cancellationDoesNotSyncOrAffectFollower() {
        check(Pending.NONE, true, true);
    }

    @Test
    void pendingCraftingIsNotExecutedOrCleared() {
        check(Pending.CRAFTING, true, false);
    }

    @Test
    void pendingRepairIsNotExecutedOrCleared() {
        check(Pending.REPAIR, true, false);
    }

    @Test
    void pendingEnchantKeepsOriginalCloseAndTwoSyncs() {
        check(Pending.ENCHANTING, true, false);
    }

    @Test
    void cancelledPendingEnchantCannotCloseOrSync() {
        check(Pending.ENCHANTING, true, true);
    }

    @Test
    void disabledNormalAndPendingTransactionsKeepOriginalHandlers() {
        for (Pending pending : Pending.values()) {
            check(pending, false, false);
        }
    }

    @Test
    void cancelledDisabledRequestCannotSync() {
        check(Pending.NONE, false, true);
    }

    @Test
    void nonemptyOrSpecialTransactionPayloadDoesNotBorrowEmptySyncAdmission() {
        InventoryTransactionPacket116 packet = request();
        assertTrue(BetweenTickPackets.supports(packet));
        packet.actions = new NetworkInventoryAction[]{new NetworkInventoryAction()};
        assertFalse(BetweenTickPackets.supports(packet));
        packet.actions = null;
        assertFalse(BetweenTickPackets.supports(packet));
        packet.actions = new NetworkInventoryAction[0];
        packet.isCraftingPart = true;
        assertFalse(BetweenTickPackets.supports(packet));
        packet.isCraftingPart = false;
        packet.isEnchantingPart = true;
        assertFalse(BetweenTickPackets.supports(packet));
        packet.isEnchantingPart = false;
        packet.isRepairItemPart = true;
        assertFalse(BetweenTickPackets.supports(packet));
        packet.isRepairItemPart = false;
        packet.transactionType = InventoryTransactionPacket116.TYPE_NORMAL;
        assertFalse(BetweenTickPackets.supports(packet));
    }

    private static InventoryTransactionPacket116 request() {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.transactionType = InventoryTransactionPacket116.TYPE_MISMATCH;
        packet.actions = new NetworkInventoryAction[0];
        return packet;
    }

    private void check(Pending pending, boolean enabled, boolean cancelled) {
        check(pending, enabled, cancelled, true);
    }

    private void check(Pending pending, boolean enabled, boolean cancelled, boolean alive) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        player.configure(server, pending);
        when(server.getLogger()).thenReturn(mock(MainLogger.class));
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isSpectator();
        doReturn(true).when(player).isOnline();
        doReturn(alive).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).supportsPacketSequences();
        // 原同步不读取物理姿态，也不要求已有多包事务先结束。
        doReturn(true).when(player).isGliding();
        List<String> order = new ArrayList<>();
        doAnswer(call -> { order.add("event"); return !cancelled; })
                .when(player).callPacketReceiveEvent(any());
        doAnswer(call -> { order.add("sync"); return null; })
                .when(player).sendAllInventories();
        doAnswer(call -> { order.add("close"); return null; })
                .when(player).removeAllWindows(false);
        InventoryTransactionPacket116 packet = request();
        if (enabled) {
            assertTrue(BetweenTickPackets.supports(packet));
            assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                    16, 1024, 16, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            dispatcher.offer(session, context -> {
                assertTrue(context.betweenTicks());
                player.handleDataPacket(packet);
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
        } else {
            player.handleDataPacket(packet);
            order.add("follower");
        }
        assertEquals(cancelled ? List.of("event", "follower")
                        : pending == Pending.ENCHANTING ? List.of("event", "close", "sync", "sync", "follower")
                        : List.of("event", "sync", "follower"), order);
        assertEquals(pending != Pending.NONE && (pending != Pending.ENCHANTING || cancelled),
                player.hasPending(pending));
        player.verifyNoTransactionExecution();
        verify(player, times(1)).callPacketReceiveEvent(packet);
        verify(player, never()).onUpdate(anyInt());
    }

    private enum Pending {
        NONE,
        CRAFTING,
        ENCHANTING,
        REPAIR
    }

    static class ProbePlayer extends SynapsePlayer116 {
        private CraftingTransaction originalCraft;
        private EnchantTransaction originalEnchant;
        private RepairItemTransaction originalRepair;

        ProbePlayer() {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
        }

        void configure(Server server, Pending pending) {
            this.server = server;
            spawned = true;
            isSynapseLogin = true;
            if (pending == Pending.CRAFTING) craftingTransaction = originalCraft = mock(CraftingTransaction.class);
            if (pending == Pending.ENCHANTING) enchantTransaction = originalEnchant = mock(EnchantTransaction.class);
            if (pending == Pending.REPAIR) repairItemTransaction = originalRepair = mock(RepairItemTransaction.class);
        }

        boolean hasPending(Pending pending) {
            return switch (pending) {
                case NONE -> false;
                case CRAFTING -> craftingTransaction == originalCraft;
                case ENCHANTING -> enchantTransaction == originalEnchant;
                case REPAIR -> repairItemTransaction == originalRepair;
            };
        }

        void verifyNoTransactionExecution() {
            if (originalCraft != null) verify(originalCraft, never()).execute();
            if (originalEnchant != null) verify(originalEnchant, never()).execute();
            if (originalRepair != null) verify(originalRepair, never()).execute();
        }
    }
}
