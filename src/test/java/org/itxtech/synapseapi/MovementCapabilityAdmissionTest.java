package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementCapabilityAdmissionTest {
    @Test void authorityDisabledStillRunsOriginalViolationBeforeAnyInputState() { check("authority"); }
    @Test void missingSequencesStillValidatesAndReportsFinalCommit() { check("no-sequence"); }
    @Test void sequenceAbsenceDoesNotBypassValidatorRejection() { check("reject"); }
    @Test void sequenceAbsenceDoesNotReopenPacketCancellation() { check("cancel-packet"); }
    @Test void missingProcessorStillCommitsThroughOriginalCore() { check("no-processor"); }
    @Test void missingProcessorRetainsOriginalCancelledMoveResponse() { check("cancel-move"); }
    @Test void missingProcessorDoesNotReopenPacketCancellation() { check("cancel-no-processor"); }

    private void check(String variant) {
        boolean missingProcessor = variant.equals("no-processor") || variant.equals("cancel-move") || variant.equals("cancel-no-processor");
        boolean cancelled = variant.equals("cancel-packet") || variant.equals("cancel-no-processor");
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        Level level = mock(Level.class);
        BaseFullChunk chunk = mock(BaseFullChunk.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        player.configure(server, level, chunk);
        doReturn(new EntityMetadata()).when(player).getDataProperties();
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(missingProcessor ? null : processor);
        when(server.isPrimaryThread()).thenReturn(true);
        when(chunk.isGenerated()).thenReturn(true);
        when(level.getDifficulty()).thenReturn(1);
        doReturn(true).when(player).isInitialized();
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(!variant.equals("authority")).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(missingProcessor).when(player).supportsPacketSequences();
        doReturn(false).when(player).isClosed();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).isFoodEnabled();
        doReturn(false).when(player).isInsideOfWater();
        doReturn(false).when(player).isSprinting();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        doNothing().when(player).recalculateBoundingBox(anyBoolean());
        doNothing().when(player).addMovement(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        doNothing().when(player).onPacketViolation(any(), anyString());
        doNothing().when(player).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
        List<String> order = new ArrayList<>();
        doAnswer(call -> {
            if (call.getArgument(0) instanceof DataPacketReceiveEvent event) {
                order.add("packet");
                if (cancelled) event.setCancelled();
            } else if (call.getArgument(0) instanceof PlayerMoveEvent event) {
                order.add("move");
                if (variant.equals("cancel-move")) event.setCancelled();
            }
            return null;
        }).when(plugins).callEvent(any());
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals(List.of("packet"), order);
            assertEquals(cancelled, call.getArgument(2));
            order.add("validate");
            return variant.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED;
        });
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        when(packet.getInputFlags()).thenReturn(new BitSet());
        when(packet.getY()).thenReturn(1.8f * 0.9f);
        when(packet.getYaw()).thenReturn(40f);
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
        dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), new ServerInputTask() {
            @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
            @Override public void run(InboundContext context) {
                assertTrue(context.betweenTicks());
                player.handleInputDataPacket(packet, player.getMovementEpoch());
            }
        }, 1, true);
        dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> order.add("follower"), 1, true);
        assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        assertEquals("follower", order.getLast());
        verify(plugins).callEvent(any(DataPacketReceiveEvent.class));
        verify(processor, times(missingProcessor ? 0 : 1)).validateReceivedPacket(any(), any(), anyBoolean());
        boolean reachesMovement = !variant.equals("authority") && !variant.equals("reject") && !cancelled;
        verify(plugins, times(reachesMovement ? 1 : 0)).callEvent(any(PlayerMoveEvent.class));
        verify(processor, times(!missingProcessor && reachesMovement ? 1 : 0)).onMovementCommitted(any(), any());
        verify(player, times(variant.equals("cancel-move") ? 1 : 0)).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
        verify(player, times(variant.equals("authority") ? 1 : 0)).onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "auth_input");
        if (!reachesMovement) verify(player, never()).onClientTickUpdated(anyLong());
        verify(player, times(reachesMovement && !variant.equals("cancel-move") ? 1 : 0)).addMovement(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(player, never()).onUpdate(anyInt());
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, BaseFullChunk chunk) {
            this.server = server; this.level = level; this.chunk = chunk;
            this.spawned = true; this.connected = true; this.isSynapseLogin = true;
            this.scale = 1;
            this.boundingBox = new SimpleAxisAlignedBB(-0.3, 0, -0.3, 0.3, 1.8, 0.3);
            this.setSessionId(UUID.randomUUID());
        }
    }
}
