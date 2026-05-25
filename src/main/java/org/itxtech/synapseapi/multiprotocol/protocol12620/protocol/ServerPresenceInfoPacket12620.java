package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.PresenceConfig;

import javax.annotation.Nullable;

/**
 * Sent by the server to provide PresenceConfiguration to the client.
 */
@ToString
public class ServerPresenceInfoPacket12620 extends Packet12620 {
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
            putString(config.experienceName != null ? config.experienceName : " ");
            putString(config.worldName != null ? config.worldName : " ");
        });
    }
}
