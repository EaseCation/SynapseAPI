package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * Sent from the client to the server to message to the server about the state of the loading screen.
 */
@ToString
public class ServerboundLoadingScreenPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_LOADING_SCREEN_PACKET;

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_START_LOADING_SCREEN = 1;
    public static final int TYPE_END_LOADING_SCREEN = 2;

    public int type = TYPE_UNKNOWN;
    /**
     * This will be set if the server gives us a value.
     */
    @Nullable
    public Integer loadingScreenId;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        type = getVarInt();
        if (getBoolean()) {
            loadingScreenId = getLInt();
        }
    }

    @Override
    public void encode() {
    }
}
