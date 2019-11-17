package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class ClientCacheStatusPacket112 extends Packet112 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENT_CACHE_STATUS_PACKET;

    public boolean supported;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.supported = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.supported);
    }
}
