package org.itxtech.synapseapi.multiprotocol.protocol110.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;

public class LecternUpdatePacket110 extends Packet110 {

    public static final byte NETWORK_ID = ProtocolInfo.LECTERN_UPDATE_PACKET;

    public int page;
    public BlockVector3 blockPosition;
    public boolean unknownBool;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        page = this.getByte();
        blockPosition = this.getBlockVector3();
        unknownBool = this.getBoolean();
    }

    @Override
    public void encode() {
    }

}
