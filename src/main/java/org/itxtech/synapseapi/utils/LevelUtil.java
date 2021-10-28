package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.SpawnParticleEffectPacket18;

/**
 * Created by boybook on 16/7/18.
 */
public class LevelUtil {

    public static void addParticle(Level level, Vector3 pos, String identifier) {
        addParticle(level, pos, identifier, level.getChunkPlayers(pos.getFloorX() >> 4, pos.getFloorZ() >> 4).values().toArray(new Player[0]));
    }

    public static void addParticle(Level level, Vector3 pos, String identifier, Player[] players) {
        SpawnParticleEffectPacket18 pk = new SpawnParticleEffectPacket18();
        pk.dimension = level.getDimension();
        pk.position = pos.asVector3f();
        pk.identifier = identifier;
        Server.broadcastPacket(players, pk);
    }

}
