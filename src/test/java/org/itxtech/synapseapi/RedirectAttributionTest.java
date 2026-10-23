package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.ServerInputDispatcher;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import cn.nukkit.network.protocol.AnimatePacket;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.synlib.Session;
import org.itxtech.synapseapi.network.synlib.SynapseClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedirectAttributionTest {
    private static MockedStatic<Server> preparedServer(Server server) {
        // 独立运行此用例时，协议枚举同样需要已绑定的服务器。
        MockedStatic<Server> scope = mockStatic(Server.class);
        scope.when(Server::getInstance).thenReturn(server);
        return scope;
    }

    @Test
    void actualRedirectRecordsFirstDecisionWithoutDiagnosticRecheck() {
        Server server = mock(Server.class, RETURNS_DEEP_STUBS);
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getServer()).thenReturn(server);
        when(api.isMainThreadInput()).thenReturn(true);
        doReturn(new Player[0]).when(server).getOnlinePlayerList();
        doReturn(Compressor.SNAPPY).when(server).getCompressor();
        AtomicBoolean primary = new AtomicBoolean(true);
        when(server.isPrimaryThread()).thenAnswer(call -> primary.get());
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096, error -> fail(error));
        when(server.getInputDispatcher()).thenReturn(dispatcher);
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true);
        SynapseClient client = mock(SynapseClient.class);
        Session transport = mock(Session.class);
        when(client.getSession()).thenReturn(transport);
        when(transport.getChannel()).thenReturn(channel);
        AtomicReference<ServerInputDispatcher.Session> binding = new AtomicReference<>();
        AtomicReference<InputBlockReason> decision = new AtomicReference<>(InputBlockReason.DEAD);
        AtomicInteger evaluations = new AtomicInteger();
        UUID actor = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AnimatePacket animation = new AnimatePacket();
        animation.action = AnimatePacket.Action.SWING_ARM;
        RedirectPacket redirect = new RedirectPacket();
        redirect.sessionId = sessionId;
        redirect.protocol = 975;
        redirect.mcpeBuffer = new byte[]{1};
        redirect.receivedNanos = 123L;
        try (MockedStatic<Server> servers = preparedServer(server);
             MockedStatic<SynapseAPI> globals = mockStatic(SynapseAPI.class);
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
                         doAnswer(call -> { binding.set(call.getArgument(0)); return null; }).when(player).bindInputSession(any(), any());
                         when(player.inputStateBlockReason()).thenAnswer(call -> { evaluations.incrementAndGet(); return decision.get(); });
                     })) {
            servers.when(Server::getInstance).thenReturn(server);
            globals.when(SynapseAPI::getInstance).thenReturn(api);
            packets.when(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975)).thenReturn(animation);
            SynapseEntry entry = new SynapseEntry(api, "127.0.0.1", 10325, false, "0123456789abcdef", "Attribution contract");
            PlayerLoginPacket login = new PlayerLoginPacket();
            login.protocol = 975;
            login.uuid = actor;
            login.sessionId = sessionId;
            login.cachedLoginPacket = new byte[]{1};
            login.address = "127.0.0.1";
            login.port = 19132;
            entry.handleDataPacket(login);
            assertEquals(1, dispatcher.drainWorldTick(1));
            primary.set(false);
            entry.handleDataPacket(redirect);
            assertEquals(1, dispatcher.snapshot().queuedTasks());
            dispatcher.enableBarrierDiagnostics();
            assertEquals(0, dispatcher.drain(true, 2, Long.MAX_VALUE));
            assertEquals(1, evaluations.get());
            decision.set(InputBlockReason.OFFLINE);
            ServerInputDispatcher.BarrierReport report = dispatcher.drainBarrierReport();
            assertEquals(1, report.types().size());
            assertEquals("AnimatePacket", report.types().getFirst().type());
            assertEquals("DEAD", report.types().getFirst().reason());
            assertEquals("DEAD", report.activeReason());
            assertEquals(1, evaluations.get());
            dispatcher.drainBarrierReport();
            assertEquals(1, evaluations.get());
            assertEquals(0, dispatcher.drain(true, 2, Long.MAX_VALUE));
            assertEquals(2, evaluations.get());
            assertEquals("DEAD", dispatcher.drainBarrierReport().activeReason());
            assertEquals(2, evaluations.get());
            decision.set(InputBlockReason.NONE);
            primary.set(true);
            assertEquals(1, dispatcher.drain(true, 3, Long.MAX_VALUE));
            assertEquals(3, evaluations.get());
            verify(players.constructed().getFirst()).handleInputDataPacket(animation, 0L);
            assertEquals(0, dispatcher.snapshot().queuedTasks());
        }
    }
}
