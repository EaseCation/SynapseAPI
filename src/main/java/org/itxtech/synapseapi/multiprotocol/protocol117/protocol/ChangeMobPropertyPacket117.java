package org.itxtech.synapseapi.multiprotocol.protocol117.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ChangeMobPropertyPacket117 extends Packet117 {

    public static final int NETWORK_ID = ProtocolInfo.CHANGE_MOB_PROPERTY_PACKET;

    public long uniqueEntityId;
    public String property;
    public boolean boolValue;
    public String stringValue;
    public int intValue;
    public float floatValue;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.putLong(this.uniqueEntityId);
        this.putString(this.property);
        this.putBoolean(this.boolValue);
        this.putString(this.stringValue == null ? "" : this.stringValue);
        this.putVarInt(this.intValue);
        this.putLFloat(this.floatValue);
    }
}