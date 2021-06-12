package org.itxtech.synapseapi.multiprotocol.protocol117.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.GameRulesChangedPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class GameRulesChangedPacket117 extends Packet117 {

    public static final int NETWORK_ID = ProtocolInfo.GAME_RULES_CHANGED_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public GameRules gameRules;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putGameRules(this.gameRules);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, GameRulesChangedPacket.class);

        GameRulesChangedPacket packet = (GameRulesChangedPacket) pk;
        this.gameRules = packet.gameRules;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return GameRulesChangedPacket.class;
    }
}
