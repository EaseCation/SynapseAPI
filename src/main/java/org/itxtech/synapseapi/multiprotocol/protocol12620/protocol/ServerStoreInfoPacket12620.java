package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.StoreConfig;

import javax.annotation.Nullable;

/**
 * Sent by the server to provide ClientStoreEntryPointConfiguration to the client.
 */
@ToString
public class ServerStoreInfoPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_STORE_INFO_PACKET;

    @Nullable
    public StoreConfig config;

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
            putString(config.storeId);
            putString(config.storeName);
        });
    }
}
