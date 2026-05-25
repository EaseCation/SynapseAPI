package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the client to the server with a party destination cookie response.
 */
@ToString
public class PartyDestinationCookieResponsePacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.PARTY_DESTINATION_COOKIE_RESPONSE_PACKET;

    public String cookie;
    public boolean accepted;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        cookie = getString();
        accepted = getBoolean();
    }

    @Override
    public void encode() {
    }
}
