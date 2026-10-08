package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.ClientboundMapItemDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.network.protocol.PacketSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapPublicationLifetimeTest {
    @BeforeAll
    static void initialize() {
        Block.init(); Item.init(); Attribute.init();
        Server server = mock(Server.class); when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server); AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }
    }

    @Test
    void retiredRecipientStartsNoMapSendEvent() {
        Fixture fixture = new Fixture(true); fixture.player.bindInputSession(null);
        assertFalse(fixture.player.dataPacket(map(17)));
        verify(fixture.plugins, never()).callEvent(any(DataPacketSendEvent.class));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void sendCallbackRetirementDropsMapBeforeTransport() {
        Fixture fixture = new Fixture(true); fixture.onPacket(event -> fixture.player.bindInputSession(null));
        assertFalse(fixture.player.dataPacket(map(17)));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void nestedMapReplacementIsDiscardedOnce() {
        Fixture fixture = new Fixture(true); AtomicInteger dropped = new AtomicInteger();
        fixture.onPacket(event -> {
            fixture.player.bindInputSession(null);
            event.setPacket(new PacketSequence(List.of(new PacketSequence(List.of(map(18)), () -> {}, dropped::incrementAndGet)), () -> {}, dropped::incrementAndGet));
        });
        assertFalse(fixture.player.dataPacket(map(17))); assertEquals(2, dropped.get());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void mapIntroducedByIndependentReplacementStillRequiresLiveRecipient() {
        Fixture fixture = new Fixture(true);
        fixture.onPacket(event -> { fixture.player.bindInputSession(null); event.setPacket(map(18)); });
        assertFalse(fixture.player.dataPacket(new TextPacket()));
    }

    @Test
    void independentTextReplacementKeepsExplicitNotificationContract() {
        Fixture fixture = new Fixture(true);
        fixture.onPacket(event -> { fixture.player.bindInputSession(null); event.setPacket(new TextPacket()); });
        assertTrue(fixture.player.dataPacket(map(17)));
        verify(fixture.transport).putPacket(eq(fixture.player), any(TextPacket.class));
    }

    @Test
    void activeEpochChangeKeepsMapCacheNotification() {
        Fixture fixture = new Fixture(true); fixture.onPacket(event -> fixture.player.observedEpoch++);
        assertTrue(fixture.player.dataPacket(map(17)));
        verify(fixture.transport).putPacket(eq(fixture.player), any(ClientboundMapItemDataPacket.class));
    }

    @Test
    void disabledModeKeepsOriginalMapPublication() {
        Fixture fixture = new Fixture(false); fixture.player.bindInputSession(null);
        assertTrue(fixture.player.dataPacket(map(17)));
        verify(fixture.transport).putPacket(eq(fixture.player), any(ClientboundMapItemDataPacket.class));
    }

    private static ClientboundMapItemDataPacket map(long id) { ClientboundMapItemDataPacket packet = new ClientboundMapItemDataPacket(); packet.mapId = id; return packet; }

    private static final class Fixture {
        private final Server server = mock(Server.class);
        private final PluginManager plugins = mock(PluginManager.class);
        private final SourceInterface transport = mock(SourceInterface.class);
        private final ProbePlayer player;
        private Fixture(boolean enabled) {
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class)); when(server.getDefaultLevel()).thenReturn(mock(Level.class));
            when(server.getPluginManager()).thenReturn(plugins); when(server.isPrimaryThread()).thenReturn(false); when(server.getViewDistance()).thenReturn(16);
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                global.when(Server::getInstance).thenReturn(server); player = new ProbePlayer(transport, enabled);
                player.bindInputSession(new ServerInputDispatcher.Session(player.getSessionId()));
            }
        }
        private void onPacket(Consumer<DataPacketSendEvent> callback) {
            doAnswer(call -> { callback.accept(call.getArgument(0)); return null; }).when(plugins).callEvent(any(DataPacketSendEvent.class));
        }
    }
    private static final class ProbePlayer extends SynapsePlayer {
        private final boolean enabled; private long observedEpoch;
        private ProbePlayer(SourceInterface transport, boolean enabled) {
            super(transport, null, 0L, InetSocketAddress.createUnresolved("localhost", 0)); this.enabled = enabled; this.isSynapseLogin = true;
            this.sessionId = UUID.randomUUID(); this.setUniqueId(UUID.randomUUID()); this.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }
        @Override public boolean isMainThreadInputEnabled() { return enabled; }
        @Override public long getMovementEpoch() { return observedEpoch; }
        @Override public boolean isOnline() { return true; }
        @Override public boolean isNetEaseClient() { return false; }
    }
}
