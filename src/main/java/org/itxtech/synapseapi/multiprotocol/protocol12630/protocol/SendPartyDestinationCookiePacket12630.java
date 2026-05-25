package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the server to a client with a party destination cookie.
 */
@ToString
public class SendPartyDestinationCookiePacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.SEND_PARTY_DESTINATION_COOKIE_PACKET;

    public static final int INTENT_NOTIFY = 0;
    public static final int INTENT_OPT_IN = 1;
    public static final int INTENT_OPT_OUT = 2;

    public String cookie;
    public int intent;
    public String destinationName;

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
        putString(cookie);
        putByte(intent);
        putString(destinationName);
    }
}
