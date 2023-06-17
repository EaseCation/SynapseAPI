package org.itxtech.synapseapi.multiprotocol.protocol11930.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NetworkSettingsPacket11930 extends Packet11930 {
    public static final int NETWORK_ID = ProtocolInfo.NETWORK_SETTINGS_PACKET;

    public static final int COMPRESS_NOTHING = 0;
    public static final int COMPRESS_EVERYTHING = 1;
    public static final int COMPRESS_MAXIMUM = 65535;

    public static final int ALGORITHM_ZLIB = 0;
    public static final int ALGORITHM_SNAPPY = 1;

    public int compressionThreshold = COMPRESS_EVERYTHING;
    public int compressionAlgorithm = ALGORITHM_ZLIB;

    public boolean enableClientThrottling;
    public byte clientThrottleThreshold;
    public int clientThrottleScalar;

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
        this.putLShort(this.compressionAlgorithm);
        this.putBoolean(this.enableClientThrottling);
        this.putByte(this.clientThrottleThreshold);
        this.putLFloat(this.clientThrottleScalar);
    }
}
