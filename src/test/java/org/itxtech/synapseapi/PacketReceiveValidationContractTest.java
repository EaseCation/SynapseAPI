package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import java.util.BitSet;
import java.util.UUID;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacketReceiveValidationContractTest {
    @Test
    void normalInputValidatesOnceAfterAllPacketListenersComplete() {
        Fixture fixture = new Fixture(true);
        List<String> phases = new ArrayList<>();
        doAnswer(call -> {
            phases.add("listeners-start");
            phases.add("listeners-end");
            return null;
        }).when(fixture.plugins).callEvent(any());
        when(fixture.processor.validateReceivedPacket(fixture.player, fixture.packet, false))
                .thenAnswer(call -> {
                    assertEquals(List.of("listeners-start", "listeners-end"), phases);
                    phases.add("validator");
                    return InputValidationResult.ACCEPTED;
                });

        assertTrue(fixture.player.callPacketReceiveEvent(fixture.packet));
        assertEquals(List.of("listeners-start", "listeners-end", "validator"), phases);
        verify(fixture.plugins, times(1)).callEvent(any(DataPacketReceiveEvent.class));
        verify(fixture.processor, times(1)).validateReceivedPacket(fixture.player, fixture.packet, false);
    }

    @Test
    void eventCancellationCannotBeReopenedByAnAcceptingValidator() {
        Fixture fixture = new Fixture(true);
        doAnswer(call -> {
            ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled();
            return null;
        }).when(fixture.plugins).callEvent(any());
        when(fixture.processor.validateReceivedPacket(fixture.player, fixture.packet, true))
                .thenReturn(InputValidationResult.ACCEPTED_GROUND_JUMP);

        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor, times(1)).validateReceivedPacket(fixture.player, fixture.packet, true);
    }

    @Test
    void validatorRejectionStopsAnOtherwiseUncancelledPacket() {
        Fixture fixture = new Fixture(true);
        when(fixture.processor.validateReceivedPacket(fixture.player, fixture.packet, false))
                .thenReturn(InputValidationResult.REJECTED);

        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor, times(1)).validateReceivedPacket(fixture.player, fixture.packet, false);
    }

    @Test
    void positionBoundaryChangedDuringPacketEventStaysCancelled() {
        Fixture fixture = new Fixture(true);
        doAnswer(call -> {
            doReturn(false).when(fixture.player).isCurrentInputPosition();
            return null;
        }).when(fixture.plugins).callEvent(any());
        when(fixture.processor.validateReceivedPacket(fixture.player, fixture.packet, true))
                .thenReturn(InputValidationResult.ACCEPTED);

        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor).validateReceivedPacket(fixture.player, fixture.packet, true);
    }

    @Test
    void retirementDuringPacketEventDoesNotStartANewValidation() {
        Fixture fixture = new Fixture(true);
        doAnswer(call -> {
            doReturn(false).when(fixture.player).isAcceptingInputPackets();
            return null;
        }).when(fixture.plugins).callEvent(any());

        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor, never()).validateReceivedPacket(any(), any(), anyBoolean());
    }

    @Test
    void closureDuringValidationCannotReleaseThePacket() {
        Fixture fixture = new Fixture(true);
        when(fixture.processor.validateReceivedPacket(fixture.player, fixture.packet, false))
                .thenAnswer(call -> {
                    doReturn(true).when(fixture.player).isClosed();
                    return InputValidationResult.ACCEPTED;
                });

        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor, times(1)).validateReceivedPacket(fixture.player, fixture.packet, false);
    }

    @Test
    void legacyModeUsesTheOriginalEventWithoutCallingTheNewProcessor() {
        Fixture fixture = new Fixture(false);
        assertTrue(fixture.player.callPacketReceiveEvent(fixture.packet));
        doAnswer(call -> {
            ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled();
            return null;
        }).when(fixture.plugins).callEvent(any());
        assertFalse(fixture.player.callPacketReceiveEvent(fixture.packet));
        verify(fixture.processor, never()).validateReceivedPacket(any(), any(), anyBoolean());
    }

    @Test void deadInputReachesRejectingValidatorWithoutBlockingFollower() { checkDeadInput("reject"); }
    @Test void deadInputCannotApplyPoseEvenIfValidatorAccepts() { checkDeadInput("accept"); }
    @Test void cancelledDeadInputCannotBeReopened() { checkDeadInput("cancel"); }
    @Test void respawnEpochDuringEventKeepsOldInputCancelled() { checkDeadInput("respawn"); }
    @Test void retirementDuringDeadInputStopsValidation() { checkDeadInput("retire-event"); }
    @Test void invalidatedDeadSessionIsDiscardedBeforeEligibility() { checkDeadInput("retired-session"); }

    private void checkDeadInput(String variant) {
        Fixture fixture = new Fixture(true);
        ProbePlayer player = fixture.player;
        player.spawned = true;
        doReturn(true).when(player).isOnline();
        doReturn(false).when(player).isAlive();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isUsingItem();
        when(fixture.packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        BitSet flags = new BitSet();
        flags.set(PlayerAuthInputFlags.START_SPRINTING);
        when(fixture.packet.getInputFlags()).thenReturn(flags);
        when(fixture.packet.hasFlag(PlayerAuthInputFlags.START_SPRINTING)).thenReturn(true);
        List<String> phases = new ArrayList<>();
        doAnswer(call -> {
            phases.add("event");
            if (variant.equals("cancel")) ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled();
            if (variant.equals("respawn")) {
                doReturn(true).when(player).isAlive();
                doReturn(false).when(player).isCurrentInputPosition();
            }
            if (variant.equals("retire-event")) doReturn(false).when(player).isAcceptingInputPackets();
            return null;
        }).when(fixture.plugins).callEvent(any());
        when(fixture.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals(List.of("event"), phases);
            assertEquals(variant.equals("cancel") || variant.equals("respawn"), call.getArgument(2));
            phases.add("validate");
            return variant.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED_GROUND_JUMP;
        });
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        ServerInputDispatcher.Session other = new ServerInputDispatcher.Session(UUID.randomUUID());
        assertTrue(BetweenTickPackets.supports(fixture.packet));
        dispatcher.offer(session, new ServerInputTask() {
            @Override public boolean isValid() { return player.isAcceptingInputPackets(); }
            @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, fixture.packet); }
            @Override public void run(InboundContext context) {
                assertTrue(context.betweenTicks());
                player.handleDataPacket(fixture.packet);
            }
        }, 1, true);
        dispatcher.offer(other, context -> phases.add("follower"), 1, true);
        if (variant.equals("retired-session")) dispatcher.invalidate(session);
        assertEquals(variant.equals("retired-session") ? 1 : 2, dispatcher.drain(true, 42, Long.MAX_VALUE));
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        int validations = variant.equals("retired-session") || variant.equals("retire-event") ? 0 : 1;
        verify(fixture.processor, times(validations)).validateReceivedPacket(any(), any(), anyBoolean());
        verify(fixture.plugins, times(variant.equals("retired-session") ? 0 : 1)).callEvent(any());
        assertEquals("follower", phases.getLast());
        verify(player, never()).onClientTickUpdated(anyLong());
        verify(player, never()).setSprinting(anyBoolean());
        verify(player, never()).commitReceivedMovement();
        verify(player, never()).onUpdate(anyInt());
    }

    private static final class Fixture {
        final ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        final PluginManager plugins = mock(PluginManager.class);
        final PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        final PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);

        Fixture(boolean enabled) {
            Server server = mock(Server.class);
            player.configure(server);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getPlayerInputProcessor()).thenReturn(processor);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isCurrentInputPosition();
            doReturn(false).when(player).isClosed();
        }
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
        }

        void configure(Server server) {
            this.server = server;
            this.isSynapseLogin = true;
        }
    }
}
