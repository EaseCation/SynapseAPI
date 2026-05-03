package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Sent by the server to provide ClientStoreEntryPointConfiguration to the client.
 */
@ToString
public class ServerStoreInfoPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_STORE_INFO_PACKET;

    /**
     * The unique identifier for the store.
     */
    public String storeId;
    /**
     * The name of the store.
     */
    public String storeName;

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
        putString(storeId);
        putString(storeName);
    }
}
