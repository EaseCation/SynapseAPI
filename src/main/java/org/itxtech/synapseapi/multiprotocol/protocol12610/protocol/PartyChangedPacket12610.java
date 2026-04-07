package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the client to provide additional client metadata.
 */
@ToString
public class PartyChangedPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.PARTY_CHANGED_PACKET;

    private String partyId;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        partyId = getString();
    }

    @Override
    public void encode() {
    }
}
