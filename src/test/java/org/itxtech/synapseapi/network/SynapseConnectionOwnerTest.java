package org.itxtech.synapseapi.network;

import cn.nukkit.Server;
import cn.nukkit.utils.MainLogger;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.SynapseEntry;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.synlib.Session;
import org.itxtech.synapseapi.network.synlib.SynapseClient;
import org.itxtech.synapseapi.runnable.SynapseEntryPutPacketThread;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

class SynapseConnectionOwnerTest {
    @Test
    void disconnectedNewModeDoesNotDecodeItsQueuedGamePacket() {
        checkDisconnected(true, false);
    }

    @Test
    void disabledModeKeepsTheOriginalQueueContinuation() {
        checkDisconnected(false, true);
    }

    private void checkDisconnected(boolean enabled, boolean delivered) {
        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(mock(MainLogger.class));
        SynapseEntry entry = mock(SynapseEntry.class);
        when(entry.isMainThreadInputEnabled()).thenReturn(enabled);
        try (MockedStatic<Server> global = mockStatic(Server.class);
             MockedConstruction<SynapseClient> clients = mockConstruction(SynapseClient.class);
             MockedConstruction<SynapseEntryPutPacketThread> writers = mockConstruction(SynapseEntryPutPacketThread.class)) {
            global.when(Server::getInstance).thenReturn(server);
            SynapseInterface source = new SynapseInterface(entry, "127.0.0.1", 10325);
            SynapseClient client = source.getClient();
            Session session = mock(Session.class);
            Channel channel = mock(Channel.class);
            when(client.getSession()).thenReturn(session);
            when(session.getChannel()).thenReturn(channel);
            when(channel.isActive()).thenReturn(true);
            when(client.isConnected()).thenReturn(true);
            source.process();
            when(client.isConnected()).thenReturn(false);
            when(channel.isActive()).thenReturn(false);
            RedirectPacket packet = mock(RedirectPacket.class);
            when(client.readThreadToMainPacket()).thenReturn(packet, null);
            source.process();
            verify(packet, times(delivered ? 1 : 0)).decode();
            verify(entry, times(delivered ? 1 : 0)).handleDataPacket(packet);
            verify(entry).invalidateInputSessions();
        }
    }

    @Test
    void oldQueuedPacketIsRejectedAfterAReplacementWhileNewOrderContinues() {
        Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(mock(MainLogger.class));
        SynapseEntry entry = mock(SynapseEntry.class);
        when(entry.isMainThreadInputEnabled()).thenReturn(true);
        try (MockedStatic<Server> global = mockStatic(Server.class);
             MockedConstruction<SynapseClient> clients = mockConstruction(SynapseClient.class);
             MockedConstruction<SynapseEntryPutPacketThread> writers = mockConstruction(SynapseEntryPutPacketThread.class)) {
            global.when(Server::getInstance).thenReturn(server);
            SynapseInterface source = new SynapseInterface(entry, "127.0.0.1", 10325);
            SynapseClient client = source.getClient();
            Session session = mock(Session.class);
            Channel old = mock(Channel.class);
            Channel current = mock(Channel.class);
            when(client.getSession()).thenReturn(session);
            when(client.isConnected()).thenReturn(true);
            when(session.getChannel()).thenReturn(old);
            when(old.isActive()).thenReturn(true);
            source.process();
            when(session.getChannel()).thenReturn(current);
            when(current.isActive()).thenReturn(true);
            RedirectPacket stale = mock(RedirectPacket.class);
            stale.receivedChannel = old;
            RedirectPacket first = mock(RedirectPacket.class);
            first.receivedChannel = current;
            RedirectPacket second = mock(RedirectPacket.class);
            second.receivedChannel = current;
            when(client.readThreadToMainPacket()).thenReturn(stale, first, second, null);
            source.process();
            verify(stale, never()).decode();
            InOrder order = inOrder(entry, first, second);
            order.verify(entry).invalidateInputSessions();
            order.verify(first).decode();
            order.verify(entry).handleDataPacket(first);
            order.verify(second).decode();
            order.verify(entry).handleDataPacket(second);
            verify(entry, never()).handleDataPacket(stale);
        }
    }
}
