package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportReentryTest {
    @Test void changedEpochInPlayerEventStopsOuterTeleport() { check(true, false); }
    @Test void changedEpochInLevelEventStopsOuterTeleport() { check(false, false); }
    @Test void retiredSourceInPlayerEventStopsOuterTeleport() { check(true, true); }
    @Test void retiredSourceInLevelEventStopsOuterTeleport() { check(false, true); }

    private void check(boolean playerEvent, boolean retired) {
        TeleportCancellationTest.ProbePlayer player = mock(TeleportCancellationTest.ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        Level origin = mock(Level.class);
        Level target = mock(Level.class);
        player.configure(server, origin);
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(new Long2ObjectOpenHashMap<>()).when(player).getDummyBossBars();
        when(origin.getEntities()).thenReturn(new Entity[0]);
        when(server.getPluginManager()).thenReturn(plugins);
        AtomicLong epoch = new AtomicLong();
        doAnswer(call -> epoch.get()).when(player).getMovementEpoch();
        doAnswer(call -> {
            Object event = call.getArgument(0);
            if (playerEvent && event instanceof PlayerTeleportEvent
                    || !playerEvent && event instanceof EntityLevelChangeEvent) {
                if (retired) doReturn(false).when(player).isAcceptingInputPackets();
                else { epoch.incrementAndGet(); player.x = 3; }
            }
            return null;
        }).when(plugins).callEvent(any());
        assertFalse(player.teleport(new Location(128, 64, 128, 0, 0, target), PlayerTeleportEvent.TeleportCause.PLUGIN));
        assertSame(origin, player.getLevel());
        assertEquals(retired ? 0 : 3, player.x);
        assertFalse(player.isLevelChange());
        verify(origin, never()).removeEntity(player);
        verify(target, never()).addEntity(player);
        verify(plugins).callEvent(any(PlayerTeleportEvent.class));
        verify(plugins, playerEvent ? never() : times(1)).callEvent(any(EntityLevelChangeEvent.class));
    }
}
