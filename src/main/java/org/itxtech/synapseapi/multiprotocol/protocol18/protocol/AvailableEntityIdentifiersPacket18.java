package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString(exclude = "tag")
public class AvailableEntityIdentifiersPacket18 extends Packet18 {
    public static final int NETWORK_ID = ProtocolInfo.AVAILABLE_ACTOR_IDENTIFIERS_PACKET;

    /**
     * CompoundTag.
     */
    public byte[] tag;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.put(this.tag);
    }
}
