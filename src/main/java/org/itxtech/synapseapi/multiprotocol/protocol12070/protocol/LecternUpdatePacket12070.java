package org.itxtech.synapseapi.multiprotocol.protocol12070.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class LecternUpdatePacket12070 extends Packet12070 {
    public static final int NETWORK_ID = ProtocolInfo.LECTERN_UPDATE_PACKET;

    public int page;
    public int totalPages;
    public int x;
    public int y;
    public int z;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        page = this.getByte();
        totalPages = this.getByte();
        BlockVector3 blockPosition = this.getBlockVector3();
        x = blockPosition.x;
        y = blockPosition.y;
        z = blockPosition.z;
    }

    @Override
    public void encode() {
    }
}
