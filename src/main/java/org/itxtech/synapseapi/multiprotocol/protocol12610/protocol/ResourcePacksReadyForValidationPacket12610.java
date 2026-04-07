package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Used to inform the server that the client has finished loading all resource packs.
 */
@ToString
public class ResourcePacksReadyForValidationPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_READY_FOR_VALIDATION_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
    }
}
