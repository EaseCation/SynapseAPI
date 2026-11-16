package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.AnimatePacket;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLogoutPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.synlib.Session;
import org.itxtech.synapseapi.network.synlib.SynapseClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 全包准入 M8：登录与登出任务在补充阶段执行；登录前到达的未解码批包只在玩家未初始化时放行，
 * 已初始化后保留屏障并上报 INITIALIZED。走真实 SynapseEntry 入队路径。
 */
class LoginLogoutAdmissionTest {
    @Test
    void loginBatchAndLogoutDrainBetweenTicksWhilePlayerIsUninitialized() {
        try (Fixture f = new Fixture(false)) {
            f.network.set(true);
            f.entry.handleDataPacket(f.login());
            f.entry.handleDataPacket(f.batch);
            f.network.set(false);
            assertEquals(2, f.dispatcher.drain(true, 1, Long.MAX_VALUE));
            assertEquals(1, f.players.constructed().size());
            assertEquals(0, f.dispatcher.snapshot().queuedTasks());
            assertEquals(0, f.dispatcher.snapshot().betweenTickBarriers());

            f.network.set(true);
            PlayerLogoutPacket logout = new PlayerLogoutPacket();
            logout.sessionId = f.sessionId;
            logout.reason = "disconnectionScreen.disconnected";
            f.entry.handleDataPacket(logout);
            f.network.set(false);
            assertEquals(1, f.dispatcher.drain(true, 2, Long.MAX_VALUE));
            verify(f.players.constructed().getFirst()).close("", "disconnectionScreen.disconnected", true);
        }
    }

    @Test
    void undecodedBatchWaitsForWorldTickOnceThePlayerIsInitialized() {
        try (Fixture f = new Fixture(true)) {
            f.network.set(true);
            f.entry.handleDataPacket(f.login());
            f.entry.handleDataPacket(f.batch);
            f.network.set(false);
            f.dispatcher.enableBarrierDiagnostics();
            assertEquals(1, f.dispatcher.drain(true, 1, Long.MAX_VALUE));
            ServerInputDispatcher.BarrierReport report = f.dispatcher.drainBarrierReport();
            assertEquals(1, report.types().size());
            assertEquals("UndecodedLoginBatch", report.types().getFirst().type());
            assertEquals("INITIALIZED", report.types().getFirst().reason());
            assertEquals(1, f.dispatcher.drainWorldTick(2));
            assertEquals(0, f.dispatcher.snapshot().queuedTasks());
        }
    }

    private static final class Fixture implements AutoCloseable {
        final AtomicBoolean network = new AtomicBoolean();
        final UUID actor = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final RedirectPacket batch = new RedirectPacket();
        final ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096,
                error -> fail(error));
        final MockedConstruction<SynapsePlayer116100> players;
        final SynapseEntry entry;
        private final MockedStatic<Server> servers;
        private final MockedStatic<SynapseAPI> globals;
        private final MockedStatic<PacketRegister> packets;
        private final MockedConstruction<SynapseInterface> interfaces;
        private final MockedConstruction<SynapseEntry.AsyncTicker> tickers;

        Fixture(boolean initialized) {
            Server server = mock(Server.class, RETURNS_DEEP_STUBS);
            SynapseAPI api = mock(SynapseAPI.class);
            when(api.getServer()).thenReturn(server);
            when(api.isMainThreadInput()).thenReturn(true);
            doReturn(new Player[0]).when(server).getOnlinePlayerList();
            doReturn(Compressor.SNAPPY).when(server).getCompressor();
            when(server.isPrimaryThread()).thenAnswer(call -> !network.get());
            when(server.getInputDispatcher()).thenReturn(dispatcher);
            Channel channel = mock(Channel.class);
            when(channel.isActive()).thenReturn(true);
            SynapseClient client = mock(SynapseClient.class);
            Session transport = mock(Session.class);
            when(client.getSession()).thenReturn(transport);
            when(transport.getChannel()).thenReturn(channel);
            AtomicReference<ServerInputDispatcher.Session> binding = new AtomicReference<>();
            batch.sessionId = sessionId;
            batch.protocol = 975;
            batch.mcpeBuffer = new byte[]{1};
            batch.receivedNanos = 123L;
            AnimatePacket swing = new AnimatePacket();
            swing.action = AnimatePacket.Action.SWING_ARM;
            servers = mockStatic(Server.class);
            servers.when(Server::getInstance).thenReturn(server);
            globals = mockStatic(SynapseAPI.class);
            globals.when(SynapseAPI::getInstance).thenReturn(api);
            packets = mockStatic(PacketRegister.class);
            packets.when(() -> PacketRegister.getFullPacket(batch.mcpeBuffer, 975)).thenReturn(swing);
            interfaces = mockConstruction(SynapseInterface.class, (source, context) -> when(source.getClient()).thenReturn(client));
            tickers = mockConstruction(SynapseEntry.AsyncTicker.class);
            players = mockConstruction(SynapsePlayer116100.class, (player, context) -> {
                when(player.getUniqueId()).thenReturn(actor);
                when(player.getSessionId()).thenReturn(sessionId);
                when(player.isAcceptingInputPackets()).thenReturn(true);
                when(player.isInitialized()).thenReturn(initialized);
                when(player.getInputSession()).thenAnswer(call -> binding.get());
                doAnswer(call -> { binding.set(call.getArgument(0)); return null; }).when(player).bindInputSession(any(), any());
            });
            entry = new SynapseEntry(api, "127.0.0.1", 10325, false, "0123456789abcdef", "Login admission contract");
        }

        PlayerLoginPacket login() {
            PlayerLoginPacket login = new PlayerLoginPacket();
            login.protocol = 975;
            login.uuid = actor;
            login.sessionId = sessionId;
            login.cachedLoginPacket = new byte[]{1};
            login.address = "127.0.0.1";
            login.port = 19132;
            return login;
        }

        @Override
        public void close() {
            players.close();
            tickers.close();
            interfaces.close();
            packets.close();
            globals.close();
            servers.close();
        }
    }
}
