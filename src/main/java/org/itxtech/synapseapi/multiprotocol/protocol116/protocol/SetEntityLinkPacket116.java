package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SetEntityLinkPacket116 extends Packet116 {

    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_LINK_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static final byte TYPE_REMOVE = 0;
    public static final byte TYPE_RIDE = 1;
    public static final byte TYPE_PASSENGER = 2;

    public long vehicleUniqueId; //from
    public long riderUniqueId; //to
    public byte type;
    public byte immediate;
    public boolean riderInitiated = false;

    @Override
    public void decode() {
        this.vehicleUniqueId = this.getEntityUniqueId();
        this.riderUniqueId = this.getEntityUniqueId();
        this.type = (byte) this.getByte();
        this.immediate = (byte) this.getByte();
        this.riderInitiated = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.vehicleUniqueId);
        this.putEntityUniqueId(this.riderUniqueId);
        this.putByte(this.type);
        this.putByte(this.immediate);
        this.putBoolean(this.riderInitiated);
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

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetEntityLinkPacket.class;
    }
}
