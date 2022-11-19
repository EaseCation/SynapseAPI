package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

/**
 * Created by boybook on 16/7/18.
 */
public class LevelUtil {

    public static void addParticle(Level level, Vector3 pos, String identifier) {
        addParticle(level, pos, identifier, level.getChunkPlayers(pos.getFloorX() >> 4, pos.getFloorZ() >> 4).values().toArray(new Player[0]));
    }

    public static void addParticle(Level level, Vector3 pos, String identifier, Player[] players) {
        if (players == null) {
            players = level.getChunkPlayers(pos.getChunkX(), pos.getChunkZ()).values().toArray(new Player[0]);
        }
        for (Player player : players) {
            player.spawnParticleEffect(pos.asVector3f(), identifier, -1, level.getDimension());
        }
    }

}
