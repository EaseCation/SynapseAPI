package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

import static cn.nukkit.Player.MAX_VIEW_DISTANCE;

@ToString
public class NetworkChunkPublisherUpdatePacket11920 extends Packet11920 {

    public static final int NETWORK_ID = ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET;

    public static final int MAX_SAVED_CHUNKS = MAX_VIEW_DISTANCE * MAX_VIEW_DISTANCE;

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

        this.putLInt(0); // savedChunks
    }
}
