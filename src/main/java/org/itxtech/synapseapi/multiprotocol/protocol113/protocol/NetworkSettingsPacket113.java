package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class NetworkSettingsPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.NETWORK_SETTINGS_PACKET;

    public static final int COMPRESS_NOTHING = 0;
    public static final int COMPRESS_EVERYTHING = 1;

    /**
     * 可选值 0-65535.
     */
    public int compressionThreshold = COMPRESS_EVERYTHING;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putLShort(this.compressionThreshold);
    }
}
