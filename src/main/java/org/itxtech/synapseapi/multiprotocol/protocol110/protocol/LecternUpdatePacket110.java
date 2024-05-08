package org.itxtech.synapseapi.multiprotocol.protocol110.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class LecternUpdatePacket110 extends Packet110 {

    public static final int NETWORK_ID = ProtocolInfo.LECTERN_UPDATE_PACKET;

    public int page;
    public int x;
    public int y;
    public int z;
    public boolean droppingBook;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        page = this.getByte();
        BlockVector3 blockPosition = this.getBlockVector3();
        x = blockPosition.x;
        y = blockPosition.y;
        z = blockPosition.z;
        droppingBook = this.getBoolean();
    }

    @Override
    public void encode() {
    }

}
