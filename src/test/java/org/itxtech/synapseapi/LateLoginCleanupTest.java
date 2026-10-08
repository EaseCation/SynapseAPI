package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.MainLogger;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.event.player.SynapsePlayerCreationEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLogoutPacket;
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LateLoginCleanupTest {
    @BeforeAll
    static void initializeProtocol() {
        Block.init();
        Item.init();
        Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            assertTrue(AbstractProtocol.PROTOCOL_121_20.getProtocolStart() > 0);
            PacketRegister.init();
        }
    }

    @Test
    void deferredDuplicateCleanupCannotCloseTheReplacementPlayer() {
        try (Fixture fixture = new Fixture()) {
            fixture.login();
            fixture.drain();
            assertEquals(1, fixture.created.size());
            RecordingPlayer previous = fixture.created.getFirst();
            fixture.login();
            assertFalse(previous.isInputSessionActive());
            Runnable delayed = fixture.deferred.removeFirst();
            previous.close("", "Test retirement", true);
            fixture.login();
            fixture.drain();
            assertEquals(2, fixture.created.size());
            RecordingPlayer current = fixture.created.getLast();
            assertTrue(current.isAcceptingInputPackets());

            delayed.run();

            assertEquals(0, current.closeCount);
            assertTrue(current.isAcceptingInputPackets());
            assertTrue(fixture.entry.outgoing.isEmpty());
        }
    }

    @Test
    void duplicateBeforeCreationReleasesTheRetiredInputKey() {
        try (Fixture fixture = new Fixture()) {
            fixture.login();
            fixture.login();
            fixture.deferred.removeFirst().run();
            assertEquals(1, fixture.entry.outgoing.size());
            fixture.login();
            fixture.drain();

            assertEquals(1, fixture.created.size());
            assertTrue(fixture.created.getFirst().isAcceptingInputPackets());
            assertTrue(fixture.deferred.isEmpty());
        }
    }

    @Test
    void replacedConnectionKeepsItsNewPlayerDespiteBothOldCleanupTasks() {
        try (Fixture fixture = new Fixture()) {
            fixture.login();
            fixture.drain();
            RecordingPlayer previous = fixture.created.getFirst();
            fixture.login();
            fixture.entry.invalidateInputSessions();
            fixture.entry.source = mock(Channel.class);
            when(fixture.entry.source.isActive()).thenReturn(true);
            fixture.login();
            fixture.drain();
            assertEquals(2, fixture.created.size());
            RecordingPlayer current = fixture.created.getLast();
            assertTrue(current.isAcceptingInputPackets());

            for (Runnable delayed : List.copyOf(fixture.deferred)) delayed.run();

            assertEquals(1, previous.closeCount);
            assertEquals(0, current.closeCount);
            assertTrue(current.isAcceptingInputPackets());
            assertTrue(fixture.entry.outgoing.isEmpty());
        }
    }

    @Test
    void replacementCreatedInsideQuitCallbackKeepsItsInputAndDoesNotReceiveOldLogout() {
        try (Fixture fixture = new Fixture()) {
            fixture.login();
            fixture.drain();
            RecordingPlayer previous = fixture.created.getFirst();
            previous.afterClose = () -> { fixture.login(); fixture.drain(); };
            fixture.login();

            fixture.deferred.removeFirst().run();

            assertEquals(2, fixture.created.size());
            RecordingPlayer current = fixture.created.getLast();
            assertTrue(current.isAcceptingInputPackets());
            fixture.redirect();
            assertEquals(1, current.incomingCount);
            assertTrue(fixture.entry.outgoing.isEmpty());
        }
    }

    @Test
    void matchingDuplicateCleanupRetainsConflictRejectionOnce() {
        try (Fixture fixture = new Fixture()) {
            fixture.login();
            fixture.drain();
            RecordingPlayer player = fixture.created.getFirst();
            fixture.login();
            Runnable delayed = fixture.deferred.removeFirst();

            delayed.run();
            delayed.run();

            assertEquals(1, player.closeCount);
            assertFalse(player.isAcceptingInputPackets());
            assertEquals(1, fixture.entry.outgoing.size());
            PlayerLogoutPacket logout = assertInstanceOf(PlayerLogoutPacket.class, fixture.entry.outgoing.getFirst());
            assertEquals(fixture.sessionId, logout.sessionId);
            assertEquals("disconnectionScreen.serverIdConflict", logout.reason);
        }
    }

    // 构造器提前停在无效配置出口，仅离线初始化原Session/FIFO；不连接、不启动线程或验证认证。
    private static final class ProbeEntry extends SynapseEntry {
        private final List<SynapseDataPacket> outgoing = new ArrayList<>();
        private Channel source = mock(Channel.class);

        private ProbeEntry(SynapseAPI plugin) {
            super(plugin, "127.0.0.1", 10325, false, "", "Offline input contract");
            when(source.isActive()).thenReturn(true);
        }

        @Override
        public boolean isCurrentInputConnection(Channel channel) {
            return channel == source && channel.isActive();
        }

        @Override
        public void sendDataPacket(SynapseDataPacket packet) {
            outgoing.add(packet);
        }
    }

    // 通过原创建事件提供标准构造器；只记录生命周期，不执行游戏登录或真实退出。
    public static class RecordingPlayer extends SynapsePlayer {
        private int closeCount;
        private int incomingCount;
        private Runnable afterClose = () -> {};

        public RecordingPlayer(SourceInterface source, SynapseEntry entry, Long clientId, InetSocketAddress address) {
            super(source, entry, clientId, address);
        }

        @Override
        public void handleLoginPacket(PlayerLoginPacket packet) {
            this.connected = true;
            this.protocol = packet.protocol;
        }

        @Override
        public void close(String message, String reason, boolean notify) {
            closeCount++;
            this.connected = false;
            this.closed = true;
            this.getSynapseEntry().handlePlayerQuit(this);
            afterClose.run();
        }

        @Override
        public void handleDataPacket(DataPacket packet) {
            incomingCount++;
        }

        @Override
        public boolean isNetEaseClient() {
            return false;
        }

        @Override
        public boolean isOnline() {
            return this.connected && !this.closed;
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final Server server = mock(Server.class);
        private final SynapseAPI plugin = mock(SynapseAPI.class);
        private final ServerScheduler scheduler = mock(ServerScheduler.class);
        private final PluginManager plugins = mock(PluginManager.class);
        private final List<Runnable> deferred = new ArrayList<>();
        private final List<RecordingPlayer> created = new ArrayList<>();
        private final UUID sessionId = UUID.randomUUID();
        private final UUID playerId = UUID.randomUUID();
        private final ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 64, 4096, 64, 4096,
                exception -> fail(exception));
        private final MockedStatic<Server> global = mockStatic(Server.class);
        private final MockedStatic<SynapseAPI> globalPlugin = mockStatic(SynapseAPI.class);
        private final ProbeEntry entry;

        private Fixture() {
            global.when(Server::getInstance).thenReturn(server);
            globalPlugin.when(SynapseAPI::getInstance).thenReturn(plugin);
            when(plugin.getServer()).thenReturn(server);
            when(plugin.getLogger()).thenReturn(mock(PluginLogger.class));
            when(plugin.isMainThreadInput()).thenReturn(true);
            when(server.getInputDispatcher()).thenReturn(dispatcher);
            when(server.getNetwork()).thenReturn(mock(Network.class));
            when(server.getLogger()).thenReturn(mock(MainLogger.class));
            when(server.getScheduler()).thenReturn(scheduler);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));
            when(server.getDefaultLevel()).thenReturn(mock(Level.class));
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getOnlinePlayerList()).thenAnswer(ignored -> created.stream().filter(Player::isOnline).toArray(Player[]::new));
            doAnswer(call -> { deferred.add(call.getArgument(1)); return mock(TaskHandler.class); })
                    .when(scheduler).scheduleTask(eq(plugin), any(Runnable.class));
            doAnswer(call -> {
                SynapsePlayerCreationEvent event = call.getArgument(0);
                event.setPlayerClass(RecordingPlayer.class);
                return null;
            }).when(plugins).callEvent(any(SynapsePlayerCreationEvent.class));
            doAnswer(call -> { created.add(call.getArgument(1)); return null; })
                    .when(server).addPlayer(any(InetSocketAddress.class), any(Player.class));
            entry = new ProbeEntry(plugin);
        }

        private void login() {
            PlayerLoginPacket packet = new PlayerLoginPacket();
            packet.sessionId = sessionId;
            packet.uuid = playerId;
            packet.address = "127.0.0.1";
            packet.port = 50000;
            packet.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
            packet.cachedLoginPacket = new byte[0];
            packet.receivedChannel = entry.source;
            entry.handleDataPacket(packet);
        }

        private void redirect() {
            NetworkStackLatencyPacket19 latency = new NetworkStackLatencyPacket19();
            latency.timestamp = 123;
            latency.isFromServer = false;
            latency.tryEncode();
            RedirectPacket redirect = new RedirectPacket();
            redirect.sessionId = sessionId;
            redirect.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
            redirect.mcpeBuffer = latency.getBuffer();
            assertInstanceOf(NetworkStackLatencyPacket19.class, PacketRegister.getFullPacket(redirect.mcpeBuffer, redirect.protocol));
            entry.handleDataPacket(redirect);
        }

        private void drain() {
            dispatcher.drain(false, 1, Long.MAX_VALUE);
        }

        @Override
        public void close() {
            dispatcher.close();
            globalPlugin.close();
            global.close();
        }
    }
}
