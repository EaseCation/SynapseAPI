package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NetworkChunkPublisherUpdatePacket18 extends Packet18 {

    public BlockVector3 position;
    public int radius;

    @Override
    public int pid() {
        return ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putSignedBlockPosition(position);
        this.putUnsignedVarInt(radius);
    }
}
