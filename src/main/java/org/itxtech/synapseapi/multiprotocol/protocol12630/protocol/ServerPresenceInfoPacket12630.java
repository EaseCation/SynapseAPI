package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
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
            putOptional(config.experienceName, BinaryStream::putString);
            putOptional(config.worldName, BinaryStream::putString);
            putString(config.richPresenceId);
        });
    }
}
