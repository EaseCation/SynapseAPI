package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.event.player.SynapsePlayerJavaCustomPayloadEvent;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadEnvelope;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadMessenger;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

class JavaPayloadEventContinuationTest {
    @Test
    void retiredByChannelCallbackDoesNotStartGenericEvent() {
        verifyEvent(true, true, false);
    }

    @Test
    void normalChannelCallbackKeepsOneGenericEvent() {
        verifyEvent(true, false, true);
    }

    @Test
    void disabledModeKeepsGenericEventAfterChannelRetirement() {
        verifyEvent(false, true, true);
    }

    private void verifyEvent(boolean inputMode, boolean retire, boolean eventExpected) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(plugins);
        doReturn(server).when(player).getServer();
        player.configure(server);
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doReturn(true).when(player).isJavaClient();
        doReturn(inputMode).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isAcceptingInputPackets();
        SynapseAPI api = mock(SynapseAPI.class);
        JavaCustomPayloadMessenger messenger = mock(JavaCustomPayloadMessenger.class);
        when(api.getJavaCustomPayloadMessenger()).thenReturn(messenger);
        doAnswer(call -> {
            if (retire) doReturn(false).when(player).isAcceptingInputPackets();
            return null;
        }).when(messenger).dispatchIncomingMessage(eq(player), eq("networklab:payload"), any());
        ScriptMessagePacket11810 packet = new ScriptMessagePacket11810();
        packet.messageId = JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID;
        packet.value = JavaCustomPayloadEnvelope.encode("networklab:payload", new byte[]{1, 2, 3}).orElseThrow();
        try (MockedStatic<SynapseAPI> apis = mockStatic(SynapseAPI.class)) {
            apis.when(SynapseAPI::getInstance).thenReturn(api);
            player.handleDataPacket(packet);
        }
        verify(messenger, times(1)).dispatchIncomingMessage(eq(player), eq("networklab:payload"), any());
        verify(plugins, times(eventExpected ? 1 : 0)).callEvent(any(SynapsePlayerJavaCustomPayloadEvent.class));
    }

    static class ProbePlayer extends SynapsePlayer {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
    }
}
