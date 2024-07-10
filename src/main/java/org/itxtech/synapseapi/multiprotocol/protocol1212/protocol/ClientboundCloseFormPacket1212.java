package org.itxtech.synapseapi.multiprotocol.protocol1212.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent from the server to client to force close all server forms on the stack and return to the HUD screen.
 */
@ToString
public class ClientboundCloseFormPacket1212 extends Packet1212 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_CLOSE_FORM_PACKET;

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
