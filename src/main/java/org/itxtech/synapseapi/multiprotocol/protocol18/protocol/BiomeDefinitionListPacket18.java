package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class BiomeDefinitionListPacket18 extends Packet18 {
    public static final int NETWORK_ID = ProtocolInfo.BIOME_DEFINITION_LIST_PACKET;

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
        this.put(tag);
    }
}
