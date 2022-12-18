package org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NetworkChunkPublisherUpdatePacket11830NE extends Packet11830NE {

    public static final int NETWORK_ID = ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET;

    public int x;
    public int y;
    public int z;
    public int radius;

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
        this.putSignedBlockPosition(x, y, z);
        this.putUnsignedVarInt(radius);

        if (!neteaseMode) {
            return;
        }

        this.putLInt(0); // savedChunks
    }
}
