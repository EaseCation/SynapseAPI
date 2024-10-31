package org.itxtech.synapseapi.network.protocol.spp;

public class ConnectionStatusPacket extends SynapseDataPacket {
    public static final int NETWORK_ID = SynapseInfo.CONNECTION_STATUS_PACKET;

    public static final byte TYPE_LOGIN_SUCCESS = 0;
    public static final byte TYPE_LOGIN_FAILED = 1;

    public byte type;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.type);
    }

    @Override
    public void decode() {
        this.type = (byte) this.getByte();
    }
}
