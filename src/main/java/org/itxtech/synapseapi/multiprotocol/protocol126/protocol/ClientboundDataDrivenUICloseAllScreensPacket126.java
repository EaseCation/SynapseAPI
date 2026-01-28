package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Allows the server to tell the client to close all the Data Driven UI screens.
 */
@ToString
public class ClientboundDataDrivenUICloseAllScreensPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_DRIVEN_UI_CLOSE_ALL_SCREENS_PACKET;

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
    }
}
