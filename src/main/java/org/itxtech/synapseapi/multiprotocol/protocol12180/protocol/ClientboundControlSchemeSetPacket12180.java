package org.itxtech.synapseapi.multiprotocol.protocol12180.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.ControlScheme;

/**
 * The server sends this packet to clients upon client requests or the execution of control scheme commands.
 */
@ToString
public class ClientboundControlSchemeSetPacket12180 extends Packet12180 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_CONTROL_SCHEME_SET_PACKET;

    /**
     * @see ControlScheme
     */
    public int scheme = ControlScheme.LOCKED_PLAYER_RELATIVE_STRAFE;

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
        putByte(scheme);
    }
}
