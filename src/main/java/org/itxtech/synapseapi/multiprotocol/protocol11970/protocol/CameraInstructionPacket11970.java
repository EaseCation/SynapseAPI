package org.itxtech.synapseapi.multiprotocol.protocol11970.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CameraInstructionPacket11970 extends Packet11970 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_INSTRUCTION_PACKET;

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
