package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Allows the server to tell the client to show a Data Driven UI screen.
 */
@ToString
public class ClientboundDataDrivenUIShowScreenPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_DRIVEN_UI_SHOW_SCREEN_PACKET;

    /**
     * The ID of the screen to show.
     */
    public String screenId;

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
        putString(screenId);
    }
}
