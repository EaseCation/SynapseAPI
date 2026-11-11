package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.PlayerFood;
import cn.nukkit.inventory.ArmorInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.observability.ConnectionObservation;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.event.player.PlayerJoinEvent;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionSpawnObservationTest {
    @BeforeAll static void initializeProtocol() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            assertTrue(AbstractProtocol.PROTOCOL_121_20.getProtocolStart() > 0);
        }
    }

    @Test void modernSpawnEmitsWorldReadyAfterJoinExactlyOnce() {
        SpawnProbe player = SpawnProbe.create();
        List<String> order = new ArrayList<>();
        PluginManager plugins = player.getServer().getPluginManager();
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof PlayerJoinEvent) order.add("join");
            return null;
        }).when(plugins).callEvent(any());
        ConnectionObservation.setObserver((observed, type, data) -> {
            assertSame(player, observed);
            assertEquals(Map.of("evidence", "server_spawn_completed"), data);
            order.add(type);
        });
        try {
            player.doFirstSpawn();
            player.doFirstSpawn();
            assertEquals(List.of("join", "world_initialized"), order);
            assertEquals(60, player.noDamageTicks);
            verify(player.inventoryForTest(), times(1)).sendContents(player);
        } finally { ConnectionObservation.setObserver(null); }
    }

    @Test void failingObservationDoesNotAbortSpawn() {
        SpawnProbe player = SpawnProbe.create();
        ConnectionObservation.setObserver((observed, type, data) -> { throw new IllegalStateException("Simulated observer failure"); });
        try {
            assertDoesNotThrow(player::doFirstSpawn);
            assertEquals(60, player.noDamageTicks);
            verify(player).spawnToAll();
        } finally { ConnectionObservation.setObserver(null); }
    }

    /**
     * 使用真实现代出生方法，其余网络与世界依赖均为测试替身，不启动游戏服务器。
     */
    static class SpawnProbe extends SynapsePlayer116100 {
        SpawnProbe(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) { super(source, entry, id, address); }
        static SpawnProbe create() {
            SpawnProbe player = mock(SpawnProbe.class);
            Server server = mock(Server.class);
            PluginManager plugins = mock(PluginManager.class);
            when(server.getPluginManager()).thenReturn(plugins);
            Level level = mock(Level.class);
            when(level.getSafeSpawn(player)).thenReturn(new Position(0, 64, 0, level));
            player.server = server;
            player.level = level;
            player.inventory = mock(PlayerInventory.class);
            player.armorInventory = mock(ArmorInventory.class);
            player.offhandInventory = mock(PlayerOffhandInventory.class);
            player.usedChunks = new Long2BooleanOpenHashMap();
            when(player.getServer()).thenReturn(server);
            when(player.getLevel()).thenReturn(level);
            when(player.getHealth()).thenReturn(20f);
            when(player.getDisplayName()).thenReturn("ObservationTest");
            when(player.getFoodData()).thenReturn(mock(PlayerFood.class));
            when(player.inventoryForTest()).thenReturn(player.inventory);
            doCallRealMethod().when(player).doFirstSpawn();
            doCallRealMethod().when(player).observeConnection(anyString(), anyMap());
            return player;
        }
        PlayerInventory inventoryForTest() { return inventory; }
    }
}
