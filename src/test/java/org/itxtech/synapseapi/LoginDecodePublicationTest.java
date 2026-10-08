package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.synlib.Session;
import org.itxtech.synapseapi.network.synlib.SynapseClient;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginDecodePublicationTest {
    @Test
    void publishedIdentityCannotBeDecodedUntilTheLoginTaskFinishes() {
        check(false);
    }

    @Test
    void closedLoginCannotPublishOrDecodeItsPendingFirstFrame() {
        check(true);
    }

    private void check(boolean closed) {
        Server server = mock(Server.class, RETURNS_DEEP_STUBS);
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getServer()).thenReturn(server);
        doReturn(new Player[0]).when(server).getOnlinePlayerList();
        when(api.isMainThreadInput()).thenReturn(true);
        AtomicBoolean primary = new AtomicBoolean(true);
        when(server.isPrimaryThread()).thenAnswer(call -> primary.get());
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096,
                error -> fail(error));
        when(server.getInputDispatcher()).thenReturn(dispatcher);
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true);
        SynapseClient client = mock(SynapseClient.class);
        Session transport = mock(Session.class);
        when(client.getSession()).thenReturn(transport);
        when(transport.getChannel()).thenReturn(channel);
        DataPacket data = mock(DataPacket.class);
        when(data.pid()).thenReturn(ProtocolInfo.NETWORK_STACK_LATENCY_PACKET);
        AtomicReference<SynapseEntry> entry = new AtomicReference<>();
        AtomicReference<ServerInputDispatcher.Session> binding = new AtomicReference<>();
        UUID actor = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        RedirectPacket redirect = new RedirectPacket();
        redirect.sessionId = sessionId;
        redirect.protocol = 975;
        redirect.mcpeBuffer = new byte[]{1};
        redirect.receivedNanos = 123L;

        doReturn(Compressor.SNAPPY).when(server).getCompressor();
        try (MockedStatic<Server> globalServer = servers(server);
             MockedStatic<SynapseAPI> global = mockStatic(SynapseAPI.class);
             MockedStatic<PacketRegister> packets = mockStatic(PacketRegister.class);
             MockedConstruction<SynapseInterface> interfaces = mockConstruction(SynapseInterface.class,
                     (source, context) -> when(source.getClient()).thenReturn(client));
             MockedConstruction<SynapseEntry.AsyncTicker> tickers = mockConstruction(SynapseEntry.AsyncTicker.class);
             MockedConstruction<SynapsePlayer116100> players = mockConstruction(SynapsePlayer116100.class,
                     (player, context) -> {
                         when(player.getUniqueId()).thenReturn(actor);
                         when(player.getSessionId()).thenReturn(sessionId);
                         when(player.isAcceptingInputPackets()).thenReturn(true);
                         when(player.getInputSession()).thenAnswer(call -> binding.get());
                         doAnswer(call -> { binding.set(call.getArgument(0)); return null; })
                                 .when(player).bindInputSession(any(), any());
                         doAnswer(call -> {
                             // 原登录控制任务尚未完成时，异步阶段收到后续完整包。
                             primary.set(false);
                             try {
                                 entry.get().handleDataPacket(redirect);
                                 packets.verify(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975), never());
                             } finally {
                                 primary.set(true);
                             }
                             doReturn(closed).when(player).isClosed();
                             return null;
                         }).when(player).handleLoginPacket(any());
                     })) {
            global.when(SynapseAPI::getInstance).thenReturn(api);
            packets.when(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975)).thenReturn(data);
            entry.set(new SynapseEntry(api, "127.0.0.1", 10325, false, "0123456789abcdef", "test"));
            PlayerLoginPacket login = new PlayerLoginPacket();
            login.protocol = 975;
            login.uuid = actor;
            login.sessionId = sessionId;
            login.cachedLoginPacket = new byte[]{1};
            login.address = "127.0.0.1";
            login.port = 19132;
            entry.get().handleDataPacket(login);
            assertEquals(1, dispatcher.snapshot().queuedTasks());
            dispatcher.drain(false, 1, Long.MAX_VALUE);
            assertEquals(1, players.constructed().size());
            if (closed) {
                packets.verify(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975), never());
                verify(players.constructed().getFirst(), never()).handleInputDataPacket(any(), anyLong());
                assertFalse(binding.get().isActive());
            } else {
                packets.verify(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975), times(1));
                verify(players.constructed().getFirst(), times(1)).handleInputDataPacket(data, 0L);
            }
            assertEquals(0, dispatcher.snapshot().queuedTasks());
        }
    }
    private static MockedStatic<Server> servers(Server server) {
        MockedStatic<Server> global = mockStatic(Server.class);
        global.when(Server::getInstance).thenReturn(server);
        return global;
    }
}
