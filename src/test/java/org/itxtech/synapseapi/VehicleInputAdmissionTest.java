package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.event.player.PlayerVehicleInputEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleInputAdmissionTest {
    @Test void predictedVehicleInputsAndFollowerKeepFifoWithoutExtraWorldUpdates() { check("normal", true); }
    @Test void wrongPredictedVehicleIdDoesNotReachVehicleMovement() { check("wrong-id", true); }
    @Test void passengerCannotControlVehicle() { check("passenger", true); }
    @Test void excessiveDistanceKeepsOriginalRejection() { check("far", true); }
    @Test void packetCancellationCannotBeReopenedByValidator() { check("cancel", true); }
    @Test void validatorRejectionStopsVehicleInput() { check("reject", true); }
    @Test void epochChangeInJumpCallbackStopsRemainingInput() { check("jump-epoch", true); }
    @Test void epochChangeInPacketEventRemainsCancelled() { check("event-epoch", true); }
    @Test void directionalInputsKeepOriginalCallbackAndEvent() { check("direction", true); }
    @Test void disabledModeKeepsOriginalPredictedVehiclePath() { check("normal", false); }

    private void check(String variant, boolean enabled) {
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Entity vehicle = mock(Entity.class, withSettings().extraInterfaces(EntityRideable.class));
        EntityRideable rideable = (EntityRideable) vehicle;
        player.configure(server, vehicle);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(false).when(player).isClosed();
        doReturn(false).when(player).isUsingItem();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        when(vehicle.getId()).thenReturn(12L);
        when(vehicle.isControlling(player)).thenReturn(!variant.equals("passenger"));
        when(vehicle.getMountedOffset(player)).thenReturn(new Vector3f());
        List<String> phases = new ArrayList<>();
        doAnswer(call -> {
            if (call.getArgument(0) instanceof DataPacketReceiveEvent event) {
                phases.add("event");
                if (variant.equals("cancel")) event.setCancelled();
                if (variant.equals("event-epoch")) doReturn(false).when(player).isCurrentInputPosition();
            }
            return null;
        }).when(plugins).callEvent(any());
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals("event", phases.getLast());
            phases.add("validate");
            assertEquals(variant.equals("cancel") || variant.equals("event-epoch"), call.getArgument(2));
            return variant.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED;
        });
        doAnswer(call -> {
            if (variant.equals("jump-epoch")) doReturn(false).when(player).isCurrentInputPosition();
            return null;
        }).when(rideable).updatePlayerJump(player, false);
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        BitSet flags = new BitSet();
        if (!variant.equals("direction")) flags.set(PlayerAuthInputFlags.IN_CLIENT_PREDICTED_IN_VEHICLE);
        when(packet.getInputFlags()).thenReturn(flags);
        when(packet.hasFlag(anyInt())).thenCallRealMethod();
        when(packet.getPredictedVehicleEntityUniqueId()).thenReturn(variant.equals("wrong-id") ? 13L : 12L);
        when(packet.getX()).thenReturn(variant.equals("far") ? 50f : 1f);
        when(packet.getMoveVecX()).thenReturn(variant.equals("direction") ? 0.5f : 0f);
        boolean normal = variant.equals("normal") || variant.equals("direction");
        int frames = normal ? 3 : 1;
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            if (enabled) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                assertTrue(BetweenTickPackets.supports(packet));
                for (int i = 0; i < frames; i++) {
                    dispatcher.offer(session, new ServerInputTask() {
                        @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                        @Override public void run(InboundContext context) {
                            assertTrue(context.betweenTicks());
                            player.handleDataPacket(packet);
                        }
                    }, 1, true);
                }
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> phases.add("follower"), 1, true);
                assertEquals(frames + 1, dispatcher.drain(true, 42, Long.MAX_VALUE));
                assertEquals(0, dispatcher.snapshot().queuedTasks());
                assertEquals("follower", phases.getLast());
            } else {
                for (int i = 0; i < frames; i++) player.handleDataPacket(packet);
            }
        }
        verify(plugins, times(frames)).callEvent(any(DataPacketReceiveEvent.class));
        verify(processor, times(enabled ? frames : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        verify(rideable, times(variant.equals("normal") ? frames : 0)).onPlayerInput(eq(player), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(rideable, times(variant.equals("direction") ? frames : 0)).onPlayerInput(eq(player), anyDouble(), anyDouble());
        verify(plugins, times(variant.equals("direction") ? frames : 0)).callEvent(any(PlayerVehicleInputEvent.class));
        verify(player, never()).onUpdate(anyInt());
        verify(vehicle, never()).onUpdate(anyInt());
        verify(player, never()).commitReceivedMovement();
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Entity vehicle) {
            this.server = server;
            this.isSynapseLogin = true;
            this.spawned = true;
            this.riding = vehicle;
        }
    }
}
