package org.itxtech.synapseapi.multiprotocol.protocol12140.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Tells clients to change their ServerAuthMovementMode.
 * For preview-only use during flights of server authoritative movement.
 * Server was asked to change the movement authority mode.
 */
@ToString
public class SetMovementAuthorityPacket12140 extends Packet12140 {
    public static final int NETWORK_ID = ProtocolInfo.SET_MOVEMENT_AUTHORITY_PACKET;

    public static final int MODE_LEGACY_CLIENT_AUTHORITATIVE_V1 = 0;
    public static final int MODE_CLIENT_AUTHORITATIVE_V2 = 1;
    public static final int MODE_SERVER_AUTHORITATIVE_V3 = 2;

    public int mode = MODE_CLIENT_AUTHORITATIVE_V2;

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
        putByte(mode);
    }
}
