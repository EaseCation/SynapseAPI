package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sends the name of the Structure Feature the player is currently occupying to the client.
 * If the player is not in a structure, this packet contains an empty string.
 */
@ToString
public class CurrentStructureFeaturePacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.CURRENT_STRUCTURE_FEATURE_PACKET;

    /**
     * The identifier of the Structure Feature that the player is currently occupying.
     * If the player is not occupying a structure then this value is an empty string.
     */
    public String structureFeatureId = "";

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
        putString(structureFeatureId);
    }
}
