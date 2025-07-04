package org.itxtech.synapseapi.network.protocol.spp;

/**
 * Created by boybook on 16/6/24.
 */
public class HeartbeatPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.HEARTBEAT_PACKET;

    public float tps;
    public float load;
    public long upTime;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putFloat(this.tps);
        this.putFloat(this.load);
        this.putUnsignedVarLong(this.upTime);
    }

    @Override
    public void decode() {
        this.tps = this.getFloat();
        this.load = this.getFloat();
        this.upTime = this.getUnsignedVarLong();
    }
}
