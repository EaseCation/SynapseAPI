package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the server to provide PresenceConfiguration to the client.
 */
@ToString
public class ServerPresenceInfoPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_PRESENCE_INFO_PACKET;

    /**
     * The name of the experience.
     */
    public String experienceName;
    /**
     * The name of the world.
     */
    public String worldName;

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
        putString(experienceName);
        putString(worldName);
    }
}
