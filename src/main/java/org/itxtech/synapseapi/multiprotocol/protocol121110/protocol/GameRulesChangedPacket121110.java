package org.itxtech.synapseapi.multiprotocol.protocol121110.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.GameRulesChangedPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class GameRulesChangedPacket121110 extends Packet121110 {
    public static final int NETWORK_ID = ProtocolInfo.GAME_RULES_CHANGED_PACKET;

    public GameRules gameRules;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putGameRules(gameRules, false);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        GameRulesChangedPacket packet = (GameRulesChangedPacket) pk;
        gameRules = packet.gameRules;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return GameRulesChangedPacket.class;
    }
}
