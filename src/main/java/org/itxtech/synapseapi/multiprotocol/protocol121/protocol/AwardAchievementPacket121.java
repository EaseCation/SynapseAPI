package org.itxtech.synapseapi.multiprotocol.protocol121.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AwardAchievementPacket121 extends Packet121 {
    public static final int NETWORK_ID = ProtocolInfo.AWARD_ACHIEVEMENT_PACKET;

    public int achievement;

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
        putLInt(achievement);
    }
}
