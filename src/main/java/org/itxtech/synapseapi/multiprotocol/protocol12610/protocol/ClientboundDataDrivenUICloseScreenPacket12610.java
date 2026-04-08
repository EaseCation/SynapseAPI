package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * Allows the server to tell the client to close Data Driven UI screens.
 */
@ToString
public class ClientboundDataDrivenUICloseScreenPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_DRIVEN_UI_CLOSE_SCREEN_PACKET;

    @Nullable
    public Integer formId;

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
        putOptional(formId, BinaryStream::putLInt);
    }
}
