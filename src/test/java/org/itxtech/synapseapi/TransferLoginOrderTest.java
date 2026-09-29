package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.MainLogger;
import com.google.gson.JsonObject;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LoginPacket14;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.stubbing.OngoingStubbing;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferLoginOrderTest {
    @BeforeAll
    static void initializeProtocol() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            assertTrue(AbstractProtocol.PROTOCOL_121_20.getProtocolStart() > 0);
        }
    }

    @Test
    void matchingRegistriesHandleLoginWithoutReadingChain() {
        for (Class<? extends SynapsePlayer> type : types()) {
            SynapsePlayer player = player(type);
            doReturn(true).when(player).checkTransferExtra();
            PlayerLoginPacket login = login(type);
            try (MockedStatic<ClientChainData12NetEase> chains = mockStatic(ClientChainData12NetEase.class)) {
                player.handleLoginPacket(login);
                InOrder order = inOrder(player);
                order.verify(player).checkTransferExtra();
                order.verify(player).handleDataPacket(login.decodedLoginPacket);
                verify(player, never()).rejoinGame(anyString());
                chains.verifyNoInteractions();
            }
        }
    }

    @Test
    void mismatchReadsAndSetsChainBeforeRejoiningWithoutHandlingLogin() {
        for (Class<? extends SynapsePlayer> type : types()) {
            SynapsePlayer player = player(type);
            doReturn(false).when(player).checkTransferExtra();
            PlayerLoginPacket login = login(type);
            ClientChainData12NetEase chain = mock(ClientChainData12NetEase.class);
            try (MockedStatic<ClientChainData12NetEase> chains = mockStatic(ClientChainData12NetEase.class)) {
                read(chains, login).thenReturn(chain);
                player.handleLoginPacket(login);
                InOrder order = inOrder(player);
                order.verify(player).checkTransferExtra();
                order.verify(player).setLoginChainData(chain);
                order.verify(player).rejoinGame("disconnectionScreen.blockMismatch");
                assertSame(chain, player.getLoginChainData());
                verify(player, never()).handleDataPacket(any());
                verify(player, never()).close(anyString(), anyString());
            }
        }
    }

    @Test
    void brokenChainClosesWithoutRejoiningOrHandlingLogin() {
        for (Class<? extends SynapsePlayer> type : types()) {
            SynapsePlayer player = player(type);
            doReturn(false).when(player).checkTransferExtra();
            PlayerLoginPacket login = login(type);
            try (MockedStatic<ClientChainData12NetEase> chains = mockStatic(ClientChainData12NetEase.class);
                 MockedStatic<MainLogger> loggers = mockStatic(MainLogger.class)) {
                loggers.when(MainLogger::getLogger).thenReturn(mock(MainLogger.class));
                read(chains, login).thenThrow(new IllegalArgumentException("Invalid login chain"));
                player.handleLoginPacket(login);
                verify(player).close("", "disconnectionScreen.internalError.cantConnect");
                verify(player, never()).handleDataPacket(any());
                verify(player, never()).rejoinGame(anyString());
            }
        }
    }

    @Test
    void combinedCheckAcceptsMissingFieldsAndRejectsEachMismatchWithoutRejoining() {
        assertTrue(player(Legacy.class).checkTransferExtra());
        List<String> keys = List.of("DataVersion", "blocks_checksum", "items_checksum", "biomes_checksum", "entities_checksum", "cameras_checksum");
        for (int index = 0; index < keys.size(); index++) {
            SynapsePlayer player = player(Legacy.class);
            player.cachedExtra = new JsonObject();
            for (String key : keys) player.cachedExtra.addProperty(key, 1);
            doReturn(index != 0).when(player).checkDataVersion(1);
            doReturn(index != 1).when(player).checkBlockRegistryChecksum(1);
            doReturn(index != 2).when(player).checkItemRegistryChecksum(1);
            doReturn(index != 3).when(player).checkBiomeRegistryChecksum(1);
            doReturn(index != 4).when(player).checkEntityRegistryChecksum(1);
            doReturn(index != 5).when(player).checkCameraRegistryChecksum(1);
            assertFalse(player.checkTransferExtra(), keys.get(index));
            verify(player, never()).rejoinGame(anyString());
        }
    }

    private OngoingStubbing<ClientChainData12NetEase> read(MockedStatic<ClientChainData12NetEase> chains, PlayerLoginPacket login) {
        if (login.decodedLoginPacket instanceof LoginPacket legacy) {
            return chains.when(() -> ClientChainData12NetEase.read(legacy));
        }
        return chains.when(() -> ClientChainData12NetEase.read((LoginPacket14) login.decodedLoginPacket));
    }

    private static List<Class<? extends SynapsePlayer>> types() {
        return List.of(Legacy.class, Version14.class, Version16.class, Modern.class);
    }

    private SynapsePlayer player(Class<? extends SynapsePlayer> type) {
        SynapsePlayer player = mock(type, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        ((Configurable) player).configure(server);
        player.isSynapseLogin = true;
        doNothing().when(player).handleDataPacket(any());
        doNothing().when(player).rejoinGame(anyString());
        doNothing().when(player).close(anyString(), anyString());
        return player;
    }

    private PlayerLoginPacket login(Class<? extends SynapsePlayer> type) {
        PlayerLoginPacket login = new PlayerLoginPacket();
        login.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        login.uuid = UUID.randomUUID();
        login.extra.addProperty("username", "Foobar");
        login.decodedLoginPacket = type == Legacy.class ? new LoginPacket() : new LoginPacket14();
        return login;
    }

    interface Configurable { void configure(Server server); }

    static class Legacy extends SynapsePlayer implements Configurable {
        Legacy(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) { super(source, entry, id, address); }
        public void configure(Server server) { this.server = server; }
    }

    static class Version14 extends SynapsePlayer14 implements Configurable {
        Version14(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) { super(source, entry, id, address); }
        public void configure(Server server) { this.server = server; }
    }

    static class Version16 extends SynapsePlayer16 implements Configurable {
        Version16(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) { super(source, entry, id, address); }
        public void configure(Server server) { this.server = server; }
    }

    static class Modern extends SynapsePlayer116100 implements Configurable {
        Modern(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) { super(source, entry, id, address); }
        public void configure(Server server) { this.server = server; this.level = mock(Level.class); }
    }
}
