package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.utils.PacketUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadinessInputAdmissionTest {
    @Test void uninitializedInputUsesOriginalModernGuardWithoutInventoryOrValidation() { check("uninitialized", true); }
    @Test void initializedOfflineInputValidatesOnceButDoesNotWriteMovement() { check("offline", true); }
    @Test void initializedUnspawnedInputUsesOriginalRevertWithoutCommit() { check("unspawned", true); }
    @Test void cancelledUnspawnedInputCannotBeReopened() { check("cancel", true); }
    @Test void rejectedUnspawnedInputCannotWriteMovement() { check("reject", true); }
    @Test void changedPositionEpochCannotReleaseUnspawnedInput() { check("epoch", true); }
    @Test void legacyModeKeepsOriginalModernPreInitializationGuard() { check("uninitialized", false); }
    @Test void legacyModeKeepsOriginalUnspawnedRevert() { check("unspawned", false); }

    @Test
    void allDeclaredProtocolTargetsUseTheGuardedModernClass() {
        for (int protocol : new int[]{859, 860, 898, 975}) {
            assertEquals(SynapsePlayer116100.class, AbstractProtocol.fromRealProtocol(protocol).getPlayerClass());
        }
        assertFalse(PacketUtil.canBeSentBeforeLogin(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET));
        assertTrue(PacketUtil.canBeSentBeforeLogin(ProtocolInfo.LOGIN_PACKET));
    }

    @Test
    void olderNonTargetEntryKeepsOriginalReadinessBarrier() {
        PacketReceiveValidationContractTest.ProbePlayer player = mock(PacketReceiveValidationContractTest.ProbePlayer.class, CALLS_REAL_METHODS);
        player.configure(mock(Server.class));
        assertEquals(InputBlockReason.NOT_SPAWNED, player.movementBlockReason());
        player.spawned = true;
        doReturn(false).when(player).isOnline();
        assertEquals(InputBlockReason.OFFLINE, player.movementBlockReason());
    }

    private void check(String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(!variant.equals("uninitialized")).when(player).isInitialized();
        doReturn(!variant.equals("offline") && !variant.equals("uninitialized")).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(false).when(player).isClosed();
        doReturn(false).when(player).isUsingItem();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        doNothing().when(player).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        when(packet.getInputFlags()).thenReturn(new BitSet());
        when(packet.getX()).thenReturn(1f);
        List<String> phases = new ArrayList<>();
        doAnswer(call -> {
            phases.add("event");
            if (variant.equals("cancel")) ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled();
            if (variant.equals("epoch")) doReturn(false).when(player).isCurrentInputPosition();
            return null;
        }).when(plugins).callEvent(any());
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals(List.of("event"), phases);
            assertEquals(variant.equals("cancel") || variant.equals("epoch"), call.getArgument(2));
            phases.add("validator");
            return variant.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED;
        });
        if (enabled) {
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
            assertTrue(BetweenTickPackets.supports(packet));
            dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), new ServerInputTask() {
                @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                @Override public void run(InboundContext context) {
                    assertTrue(context.betweenTicks());
                    player.handleDataPacket(packet);
                }
            }, 1, true);
            dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> phases.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
            assertEquals(0, dispatcher.snapshot().queuedTasks());
            assertEquals("follower", phases.getLast());
        } else {
            player.handleDataPacket(packet);
        }
        int events = variant.equals("uninitialized") ? 0 : 1;
        verify(plugins, times(events)).callEvent(any(DataPacketReceiveEvent.class));
        verify(processor, times(enabled ? events : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        verify(processor, never()).onMovementCommitted(any(), any());
        assertFalse(player.hasPendingMovement());
        assertEquals(0.0, player.x);
        verify(player, never()).getInventory();
        verify(player, never()).onUpdate(anyInt());
        if (variant.equals("unspawned")) {
            verify(player).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
        }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
        boolean hasPendingMovement() { return this.newPosition != null; }
    }
}
