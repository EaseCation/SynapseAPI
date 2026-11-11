package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.event.player.SynapsePlayerJavaCustomPayloadEvent;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadEnvelope;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadMessenger;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScriptMessageAdmissionTest {
    @Test
    void originalBarrierVersusAdmissionKeepsWholeHandlerBeforeFollowingLatency() {
        for (boolean baseline : new boolean[]{true, false}) {
            Fixture fixture = new Fixture(true);
            fixture.offer(packet(), !baseline && BetweenTickPackets.supports(packet()));
            fixture.offer(new NetworkStackLatencyPacket19(), true);
            assertEquals(baseline ? 0 : 2, fixture.dispatcher.drain(true, 7, Long.MAX_VALUE));
            if (baseline) assertEquals(2, fixture.dispatcher.drainWorldTick(8));
            assertEquals(List.of("receive", "channel", "generic", "latency"), fixture.order);
            assertEquals(List.of(!baseline, !baseline), fixture.phases);
        }
    }

    @Test
    void earlierWorldBarrierStillBlocksScriptAndFollowingLatency() {
        Fixture fixture = new Fixture(true);
        fixture.offer(new TextPacket(), false);
        fixture.offer(packet(), BetweenTickPackets.supports(packet()));
        fixture.offer(new NetworkStackLatencyPacket19(), true);
        assertEquals(0, fixture.dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(3, fixture.dispatcher.drainWorldTick(8));
        assertEquals(List.of("barrier", "receive", "channel", "generic", "latency"), fixture.order);
    }

    @Test
    void exhaustedSupplementalBudgetDefersWithoutDroppingOrCappingWorldDrain() {
        Fixture fixture = new Fixture(true);
        for (int i = 0; i < 256; i++) fixture.offer(packet(), BetweenTickPackets.supports(packet()));
        assertEquals(0, fixture.dispatcher.drain(true, 7, 0));
        assertEquals(256, fixture.dispatcher.drainWorldTick(8));
        assertEquals(256 * 3, fixture.order.size());
        assertEquals(0, fixture.dispatcher.snapshot().queuedTasks());
        assertEquals(0, fixture.dispatcher.snapshot().discardedTasks());
    }

    @Test
    void retiredSessionDiscardsWithoutCallingPayloadListeners() {
        Fixture fixture = new Fixture(true);
        fixture.offer(packet(), BetweenTickPackets.supports(packet()));
        fixture.dispatcher.invalidate(fixture.session);
        fixture.dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()),
                context -> fixture.order.add("other-session"), 1, true);
        fixture.dispatcher.drain(true, 7, Long.MAX_VALUE);
        assertEquals(List.of("other-session"), fixture.order);
        verifyNoInteractions(fixture.messenger);
    }

    @Test
    void cancelledCameraAndCustomMessagesKeepOriginalReceiveEventOnly() {
        for (boolean mode : new boolean[]{false, true}) {
            for (String messageId : List.of(JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID,
                    "easecation:spectator_camera_v1", "easecation:spectator_camera_v2")) {
                Fixture fixture = new Fixture(mode);
                fixture.cancel = true;
                ScriptMessagePacket11810 packet = packet();
                packet.messageId = messageId;
                fixture.handle(packet);
                assertEquals(List.of("receive"), fixture.order);
                verifyNoInteractions(fixture.messenger);
                verify(fixture.plugins, never()).callEvent(any(SynapsePlayerJavaCustomPayloadEvent.class));
            }
        }
    }

    @Test
    void nativeClientAndInvalidEnvelopeKeepOriginalIgnoreInBothModes() {
        for (boolean mode : new boolean[]{false, true}) {
            for (boolean nativeClient : new boolean[]{false, true}) {
                Fixture fixture = new Fixture(mode);
                doReturn(!nativeClient).when(fixture.player).isJavaClient();
                ScriptMessagePacket11810 packet = packet();
                if (!nativeClient) packet.value = "!";
                fixture.handle(packet);
                assertEquals(List.of("receive"), fixture.order);
                verifyNoInteractions(fixture.messenger);
            }
        }
    }

    @Test
    void normalPayloadKeepsOneCallbackAndEventInDisabledMode() {
        Fixture fixture = new Fixture(false);
        fixture.handle(packet());
        assertEquals(List.of("receive", "channel", "generic"), fixture.order);
        verify(fixture.messenger).dispatchIncomingMessage(eq(fixture.player), eq("networklab:payload"), eq(new byte[]{1, 2, 3}));
    }

    private static ScriptMessagePacket11810 packet() {
        ScriptMessagePacket11810 packet = new ScriptMessagePacket11810();
        packet.messageId = JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID;
        packet.value = JavaCustomPayloadEnvelope.encode("networklab:payload", new byte[]{1, 2, 3}).orElseThrow();
        return packet;
    }

    private static final class Fixture {
        final JavaPayloadEventContinuationTest.ProbePlayer player = mock(JavaPayloadEventContinuationTest.ProbePlayer.class, CALLS_REAL_METHODS);
        final SynapseAPI api = mock(SynapseAPI.class);
        final JavaCustomPayloadMessenger messenger = mock(JavaCustomPayloadMessenger.class);
        final PluginManager plugins = mock(PluginManager.class);
        final List<String> order = new ArrayList<>();
        final List<Boolean> phases = new ArrayList<>();
        final Thread mainThread = Thread.currentThread();
        final ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                512, 65536, 512, 65536, error -> fail(error));
        final ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        boolean cancel;

        Fixture(boolean mode) {
            Server server = mock(Server.class);
            when(server.getPluginManager()).thenReturn(plugins);
            doReturn(server).when(player).getServer();
            player.configure(server);
            doReturn(true).when(player).isJavaClient();
            doReturn(mode).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doAnswer(call -> { order.add("receive"); return !cancel; }).when(player).callPacketReceiveEvent(any());
            when(api.getJavaCustomPayloadMessenger()).thenReturn(messenger);
            doAnswer(call -> { order.add("channel"); return null; }).when(messenger).dispatchIncomingMessage(any(), any(), any());
            doAnswer(call -> { order.add("generic"); return null; }).when(plugins).callEvent(any(SynapsePlayerJavaCustomPayloadEvent.class));
        }

        void handle(DataPacket packet) {
            try (MockedStatic<SynapseAPI> apis = mockStatic(SynapseAPI.class)) {
                apis.when(SynapseAPI::getInstance).thenReturn(api);
                player.handleDataPacket(packet);
            }
        }

        void offer(DataPacket packet, boolean supported) {
            assertEquals(ServerInputDispatcher.OfferResult.ACCEPTED, dispatcher.offer(session, new ServerInputTask() {

                @Override
                public void run(InboundContext context) {
                    assertSame(mainThread, Thread.currentThread());
                    phases.add(context.betweenTicks());
                    if (packet instanceof ScriptMessagePacket11810) handle(packet);
                    else order.add(packet instanceof TextPacket ? "barrier" : "latency");
                }
            }, 1, supported));
        }
    }
}
