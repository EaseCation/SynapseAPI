package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SetEntityLinkPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_LINK_PACKET;

    public static final byte TYPE_REMOVE = 0;
    public static final byte TYPE_RIDE = 1;
    public static final byte TYPE_PASSENGER = 2;

    public long vehicleUniqueId;
    public long riderUniqueId;
    public byte type;
    public byte immediate;
    public boolean riderInitiated;
    public float vehicleAngularVelocity;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.vehicleUniqueId = this.getEntityUniqueId();
        this.riderUniqueId = this.getEntityUniqueId();
        this.type = (byte) this.getByte();
        this.immediate = (byte) this.getByte();
        this.riderInitiated = this.getBoolean();
        this.vehicleAngularVelocity = this.getLFloat();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.vehicleUniqueId);
        this.putEntityUniqueId(this.riderUniqueId);
        this.putByte(this.type);
        this.putByte(this.immediate);
        this.putBoolean(this.riderInitiated);
        this.putLFloat(this.vehicleAngularVelocity);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, SetEntityLinkPacket.class);

        SetEntityLinkPacket packet = (SetEntityLinkPacket) pk;
        this.vehicleUniqueId = packet.vehicleUniqueId;
        this.riderUniqueId = packet.riderUniqueId;
        this.type = packet.type;
        this.immediate = packet.immediate;
        this.riderInitiated = packet.riderInitiated;
        this.vehicleAngularVelocity = packet.vehicleAngularVelocity;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetEntityLinkPacket.class;
    }
}
