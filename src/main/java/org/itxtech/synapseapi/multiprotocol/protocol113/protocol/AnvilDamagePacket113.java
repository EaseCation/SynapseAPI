package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AnvilDamagePacket113 extends Packet113 {
    public static final int NETWORK_ID = ProtocolInfo.ANVIL_DAMAGE_PACKET;

    public int damage;
    public int x;
    public int y;
    public int z;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.damage = this.getByte();
        BlockVector3 pos = this.getBlockVector3();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    @Override
    public void encode() {
    }
}
