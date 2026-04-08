package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.InputLock;
import lombok.ToString;

@ToString
public class UpdateClientInputLocksPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_CLIENT_INPUT_LOCKS_PACKET;

    public int flags = InputLock.NONE;

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
        putUnsignedVarInt(flags);
    }
}
