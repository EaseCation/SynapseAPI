package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;
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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
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
