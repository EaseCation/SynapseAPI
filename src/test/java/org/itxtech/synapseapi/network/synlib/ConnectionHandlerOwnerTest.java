package org.itxtech.synapseapi.network.synlib;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.itxtech.synapseapi.SynapseAPI;
import cn.nukkit.plugin.PluginLogger;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionHandlerOwnerTest {
    @Test
    void handlerBindsOnlyUnboundDiagnosticInputAndPreservesAnOldSource() throws Exception {
        SynapseClient client = mock(SynapseClient.class);
        when(client.isRecordInputTime()).thenReturn(true);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        Channel current = mock(Channel.class);
        Channel old = mock(Channel.class);
        when(context.channel()).thenReturn(current);
        SynapseClientHandler handler = new SynapseClientHandler(client);
        RedirectPacket unbound = new RedirectPacket();
        handler.channelRead(context, unbound);
        assertSame(current, unbound.receivedChannel);
        RedirectPacket stale = new RedirectPacket();
        stale.receivedChannel = old;
        handler.channelRead(context, stale);
        assertSame(old, stale.receivedChannel);
        verify(client).pushThreadToMainPacket(unbound);
        verify(client).pushThreadToMainPacket(stale);
    }

    @Test
    void lateInactiveOfOldChannelCannotDisconnectTheReplacement() throws Exception {
        checkInactive(true, 0);
    }

    @Test
    void disabledModeKeepsOriginalInactiveHandling() throws Exception {
        checkInactive(false, 1);
    }

    private void checkInactive(boolean enabled, int mutations) throws Exception {
        SynapseClient client = mock(SynapseClient.class);
        when(client.isRecordInputTime()).thenReturn(enabled);
        Session session = mock(Session.class);
        when(client.getSession()).thenReturn(session);
        when(session.getChannel()).thenReturn(mock(Channel.class));
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(mock(Channel.class));
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getLogger()).thenReturn(mock(PluginLogger.class));
        try (MockedStatic<SynapseAPI> global = mockStatic(SynapseAPI.class)) {
            global.when(SynapseAPI::getInstance).thenReturn(api);
            new SynapseClientHandler(client).channelInactive(context);
        }
        verify(client, times(mutations)).setConnected(false);
        verify(client, times(mutations)).reconnect();
    }
}
