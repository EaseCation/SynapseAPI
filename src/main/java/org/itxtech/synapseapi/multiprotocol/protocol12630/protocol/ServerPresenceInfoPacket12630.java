package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.PresenceConfig;

import javax.annotation.Nullable;

/**
 * Sent by the server to provide PresenceConfiguration to the client.
 */
@ToString
public class ServerPresenceInfoPacket12630 extends Packet12630 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_PRESENCE_INFO_PACKET;

    @Nullable
    public PresenceConfig config;

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
        putOptional(config, (stream, config) -> {
            putOptional(config.experienceName, (bs, experienceName) -> bs.putString(experienceName, 40, true));
            putOptional(config.worldName, (bs, worldName) -> bs.putString(worldName, 40, true));
            putString(config.richPresenceId, 40);
        });
    }
}
