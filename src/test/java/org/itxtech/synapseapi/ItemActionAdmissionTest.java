package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemActionAdmissionTest {
    @Test
    void sustainedUseDoesNotBlockItsOwnSlotAndReleaseDependencies() {
        TestPlayer player = player();
        doReturn(true).when(player).isUsingItem();

        assertFalse(player.canProcessMovementBetweenTicks());
        assertTrue(player.canProcessInputStateBetweenTicks());
        assertTrue(player.canProcessItemActionBetweenTicks());
    }

    @Test
    void ordinaryMovementAndReleaseConsumeOneFifoWithoutAdvancingTheWorldTick() {
        TestPlayer player = player();
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                16, 1024, 16, 1024, exception -> fail(exception));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        PlayerAuthInputPacket116220 movement = new PlayerAuthInputPacket116220();
        InventoryTransactionPacket116 release = new InventoryTransactionPacket116();
        dispatcher.offer(session, task(player, movement, () -> {
            assertTrue(dispatcher.getCurrentContext().betweenTicks());
            assertEquals(40, dispatcher.getCurrentContext().serverTick());
            order.add("movement");
        }), 1, true);
        dispatcher.offer(session, task(player, release, () -> {
            assertTrue(dispatcher.getCurrentContext().betweenTicks());
            assertEquals(40, dispatcher.getCurrentContext().serverTick());
            order.add("release");
        }), 1, true);

        dispatcher.drain(true, 40, Long.MAX_VALUE);

        assertEquals(List.of("movement", "release"), order);
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        // 输入唤醒不调用世界周期的持续使用回调，也不重启使用计时。
        verify(player, never()).onUpdate(anyInt());
        verify(player, never()).setUsingItem(anyBoolean());
    }

    @Test
    void sustainedUseCannotCarryReleasePastAnUnmigratedMovementBoundary() {
        TestPlayer player = player();
        doReturn(true).when(player).isUsingItem();
        doReturn(true).when(player).isGliding();
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                16, 1024, 16, 1024, exception -> fail(exception));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        dispatcher.offer(session, task(player, new PlayerAuthInputPacket116220(),
                () -> order.add("movement")), 1, true);
        dispatcher.offer(session, task(player, new InventoryTransactionPacket116(),
                () -> order.add("release")), 1, true);

        dispatcher.drain(true, 40, Long.MAX_VALUE);
        assertTrue(order.isEmpty());
        assertEquals(2, dispatcher.snapshot().queuedTasks());

        dispatcher.drain(false, 41, Long.MAX_VALUE);
        assertEquals(List.of("movement", "release"), order);
    }

    private static ServerInputTask task(TestPlayer player, DataPacket packet, Runnable action) {
        return new ServerInputTask() {
            @Override public void run(InboundContext context) { action.run(); }
            @Override public boolean canRunBetweenTicks() {
                return BetweenTickPackets.canRunBetweenTicks(player, packet);
            }
        };
    }

    @Test
    void pendingTeleportAllowsStateButKeepsWorldActionsBehindItsBoundary() {
        TestPlayer player = player();
        player.awaitTeleport();

        assertTrue(player.canProcessInputStateBetweenTicks());
        assertFalse(player.canProcessItemActionBetweenTicks());
    }

    @Test
    void missedSwingSoundNeedsCurrentWorldActionEligibility() {
        TestPlayer player = player();
        PlayerAuthInputPacket116220 input = mock(PlayerAuthInputPacket116220.class);
        doReturn(true).when(input).hasFlag(PlayerAuthInputFlags.MISSED_SWING);
        doReturn(false).when(player).isServerAuthoritativeSoundEnabled();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, input));
        doReturn(true).when(player).isServerAuthoritativeSoundEnabled();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, input));
        player.awaitTeleport();
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, input));
        doReturn(false).when(player).isServerAuthoritativeSoundEnabled();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, input));
        player.clearTeleport();
        doReturn(true).when(player).isUsingItem();
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, input));
    }

    @Test
    void embeddedWorldActionNeedsBothMovementAndItemLifecycleEligibility() {
        TestPlayer player = player();
        PlayerAuthInputPacket116220 input = mock(PlayerAuthInputPacket116220.class);
        doReturn(null).when(input).getUseItemData();
        player.awaitTeleport();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, input));
        doReturn(new UseItemData()).when(input).getUseItemData();
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, input));
        player.clearTeleport();
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, input));
        doReturn(true).when(player).isUsingItem();
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, input));
    }

    @Test
    void deathAndRetiredSessionCannotAcquireAnyNewActionEligibility() {
        TestPlayer player = player();
        doReturn(false).when(player).isAlive();
        assertFalse(player.canProcessInputStateBetweenTicks());
        assertFalse(player.canProcessItemActionBetweenTicks());
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isAcceptingInputPackets();
        assertFalse(player.canProcessInputStateBetweenTicks());
        assertFalse(player.canProcessItemActionBetweenTicks());
    }

    @Test
    void unmigratedMovementStateDoesNotPreventAnOrderedSlotChange() {
        TestPlayer player = player();
        doReturn(true).when(player).isGliding();

        assertTrue(player.canProcessInputStateBetweenTicks());
        assertFalse(player.canProcessItemActionBetweenTicks());
    }

    private static TestPlayer player() {
        TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        player.prepare(server);
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isSwimming();
        doReturn(false).when(player).isGliding();
        doReturn(false).when(player).isCrawling();
        doReturn(false).when(player).getDataFlag(anyInt());
        doReturn(mock(AdventureSettings.class)).when(player).getAdventureSettings();
        return player;
    }

    static class TestPlayer extends SynapsePlayer116100 {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void prepare(Server server) {
            this.server = server;
            this.spawned = true;
        }

        void awaitTeleport() {
            this.teleportPosition = new Vector3();
        }

        void clearTeleport() {
            this.teleportPosition = null;
        }
    }
}
