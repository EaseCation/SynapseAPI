package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the client to provide additional client metadata.
 */
@ToString
public class PartyChangedPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.PARTY_CHANGED_PACKET;

    public String partyId;
    public boolean partyLeader;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        partyId = getString();
        partyLeader = getBoolean();
    }

    @Override
    public void encode() {
    }
}
