package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Allows the server to tell the client to reload the Data Driven UI.
 */
@ToString
public class ClientboundDataDrivenUIReloadPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_DRIVEN_UI_RELOAD_PACKET;

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
