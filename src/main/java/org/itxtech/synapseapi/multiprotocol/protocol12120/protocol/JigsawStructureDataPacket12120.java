package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * This packet contains a copy of the behavior pack jigsaw structure rules.
 * Sends the serialized jigsaw rule JSON to the client as it's needed on both the client and server.
 */
@ToString(exclude = "nbt")
public class JigsawStructureDataPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.JIGSAW_STRUCTURE_DATA_PACKET;

    /**
     * CompoundTag.
     */
    public byte[] nbt;

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
        put(nbt);
    }
}
