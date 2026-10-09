package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.SetSpawnPositionPacket;
import cn.nukkit.plugin.PluginManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportCancellationTest {
    @Test void playerCancellationPreservesViewInNewMode() { check(true, true); }
    @Test void entityCancellationPreservesViewAndMotionInNewMode() { check(true, false); }
    @Test void disabledPlayerCancellationPreservesLegacyPath() { check(false, true); }
    @Test void disabledEntityCancellationPreservesLegacyPath() { check(false, false); }

    private void check(boolean inputMode, boolean playerEvent) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        Level origin = mock(Level.class);
        Level target = mock(Level.class);
        Entity visible = mock(Entity.class);
        player.configure(server, origin);
        when(server.getPluginManager()).thenReturn(plugins);
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(inputMode).when(player).isMainThreadInputEnabled();
        doReturn(new Long2ObjectOpenHashMap<>()).when(player).getDummyBossBars();
        when(origin.getEntities()).thenReturn(new Entity[]{visible});
        int loaderId = player.getLoaderId();
        when(visible.getViewers()).thenReturn(Map.of(loaderId, player));
        when(target.getSpawnLocation(any())).thenReturn(new Position(128, 64, 128, target));
        doReturn(true).when(player).dataPacket(any());
        doReturn(true).when(player).setMotion(any(Vector3.class));
        if (playerEvent) {
            doAnswer(call -> {
                ((PlayerTeleportEvent) call.getArgument(0)).setCancelled();
                return null;
            }).when(plugins).callEvent(any(PlayerTeleportEvent.class));
        } else {
            doAnswer(call -> {
                ((EntityLevelChangeEvent) call.getArgument(0)).setCancelled();
                return null;
            }).when(plugins).callEvent(any(EntityLevelChangeEvent.class));
        }
        assertFalse(player.teleport(new Location(128, 64, 128, 0, 0, target), PlayerTeleportEvent.TeleportCause.PLUGIN));
        assertSame(origin, player.getLevel());
        verify(plugins).callEvent(any(PlayerTeleportEvent.class));
        verify(plugins, playerEvent ? never() : times(1)).callEvent(any(EntityLevelChangeEvent.class));
        assertEquals(!inputMode, player.isLevelChange());
        verify(visible, inputMode ? never() : times(1)).despawnFrom(player);
        verify(player, inputMode || playerEvent ? never() : times(1)).setMotion(any(Vector3.class));
        verify(player, inputMode || playerEvent ? never() : times(1)).dataPacket(any(SetSpawnPositionPacket.class));
    }

    static class ProbePlayer extends SynapsePlayer {
        ProbePlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }
        void configure(Server server, Level level) {
            this.server = server;
            this.level = level;
            this.temporalVector = new Vector3();
            this.y = 64;
            this.spawned = true;
        }
    }
}
