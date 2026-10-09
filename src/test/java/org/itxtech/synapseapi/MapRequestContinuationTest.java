package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.event.player.PlayerMapInfoRequestEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MapInfoRequestPacket;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.ServerScheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.MapInfoRequestPacket11920;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapRequestContinuationTest {
    @BeforeAll
    static void initialize() {
        Block.init(); Item.init(); Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
        }
    }

    @Test
    void retiredInRequestEventStartsNoImageTask() { verifyCases(true, "event-retire", 0, 0); }

    @Test
    void recipientRetiredBeforeWorkerStartsCannotInvokeVirtualMapSender() { verifyCases(true, "worker-retire", 1, 0); }

    @Test
    void disabledModeKeepsOriginalContinuation() { verifyCases(false, "event-retire", 1, 1); }

    @Test
    void removalAfterAcceptedRequestKeepsMapCacheNotification() { verifyCases(true, "remove", 1, 1); }

    @Test
    void legalPositionChangeDoesNotCancelMapNotification() { verifyCases(true, "position", 1, 1); }

    @Test
    void requestCancellationStillStartsNoImageTask() { verifyCases(true, "cancel", 0, 0); }

    private void verifyCases(boolean mode, String variant, int tasks, int sends) {
        for (AbstractProtocol protocol : new AbstractProtocol[]{AbstractProtocol.PROTOCOL_117_10,
                AbstractProtocol.PROTOCOL_121_124, AbstractProtocol.PROTOCOL_121_130, AbstractProtocol.PROTOCOL_126_20}) {
            try (Fixture fixture = new Fixture(mode, protocol)) {
                doAnswer(call -> {
                    PlayerMapInfoRequestEvent event = call.getArgument(0);
                    if (variant.equals("event-retire")) doReturn(false).when(fixture.player).isAcceptingInputPackets();
                    if (variant.equals("cancel")) event.setCancelled();
                    if (variant.equals("position")) doReturn(2L).when(fixture.player).getMovementEpoch();
                    return null;
                }).when(fixture.plugins).callEvent(any(PlayerMapInfoRequestEvent.class));
                fixture.player.handleDataPacket(fixture.request());
                assertEquals(tasks, fixture.tasks.size(), protocol.name());
                if (variant.equals("worker-retire")) doReturn(false).when(fixture.player).isAcceptingInputPackets();
                if (variant.equals("remove")) when(fixture.inventory.getContents()).thenReturn(Int2ObjectMaps.emptyMap());
                for (AsyncTask<?> task : fixture.tasks) task.onRun();
                verify(fixture.map, times(sends)).sendImage(fixture.player);
                verify(fixture.player, never()).getMovementEpoch();
            }
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final Server server = mock(Server.class);
        private final PluginManager plugins = mock(PluginManager.class);
        private final ServerScheduler scheduler = mock(ServerScheduler.class);
        private final ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        private final PlayerInventory inventory = mock(PlayerInventory.class);
        private final PlayerOffhandInventory offhand = mock(PlayerOffhandInventory.class);
        private final ItemMap map = mock(ItemMap.class);
        private final List<AsyncTask<?>> tasks = new ArrayList<>();
        private final AbstractProtocol protocol;
        private final MockedStatic<SynapseAPI> apis;

        private Fixture(boolean mode, AbstractProtocol protocol) {
            this.protocol = protocol;
            player.configure(server, mock(Level.class), inventory, offhand);
            doReturn(true).when(player).isInitialized();
            doReturn(mode).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).callPacketReceiveEvent(any(DataPacket.class));
            doReturn(protocol.getProtocolStart()).when(player).getProtocol();
            when(map.getMapId()).thenReturn(17L);
            when(inventory.getContents()).thenReturn(Int2ObjectMaps.singleton(0, map));
            when(offhand.getContents()).thenReturn(Int2ObjectMaps.emptyMap());
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getScheduler()).thenReturn(scheduler);
            doAnswer(call -> { tasks.add(call.getArgument(1)); return null; }).when(scheduler).scheduleAsyncTask(any(), any(AsyncTask.class));
            apis = mockStatic(SynapseAPI.class);
            apis.when(SynapseAPI::getInstance).thenReturn(mock(SynapseAPI.class));
        }

        private DataPacket request() {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
                MapInfoRequestPacket packet = new MapInfoRequestPacket(); packet.mapId = 17; return packet;
            }
            MapInfoRequestPacket11920 packet = new MapInfoRequestPacket11920(); packet.mapId = 17; return packet;
        }

        @Override public void close() { apis.close(); }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, PlayerInventory inventory, PlayerOffhandInventory offhand) {
            this.server = server; this.level = level; this.inventory = inventory; this.offhandInventory = offhand; this.isSynapseLogin = true;
        }
    }
}
