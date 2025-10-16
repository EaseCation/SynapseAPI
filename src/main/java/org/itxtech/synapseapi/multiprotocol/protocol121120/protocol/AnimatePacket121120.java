package org.itxtech.synapseapi.multiprotocol.protocol121120.protocol;

import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.AnimatePacket.Action;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

@ToString
public class AnimatePacket121120 extends Packet121120 {
    public static final int NETWORK_ID = ProtocolInfo.ANIMATE_PACKET;

    public long eid;
    public Action action;
    public float data;
    public float rowingTime;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = Action.fromId(this.getVarInt());
        this.eid = this.getEntityRuntimeId();
        this.data = this.getLFloat();
        if (this.action == Action.ROW_RIGHT || this.action == Action.ROW_LEFT) {
            this.rowingTime = this.getLFloat();
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putVarInt(this.action.getId());
        this.putEntityRuntimeId(this.eid);
        this.putLFloat(this.data);
        if (this.action == Action.ROW_RIGHT || this.action == Action.ROW_LEFT) {
            this.putLFloat(this.rowingTime);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        AnimatePacket packet = (AnimatePacket) pk;
        this.eid = packet.eid;
        this.action = packet.action;
        this.data = packet.data;
        this.rowingTime = packet.rowingTime;
        return this;
    }

    @Override
    public DataPacket toDefault() {
        AnimatePacket pk = new AnimatePacket();
        pk.eid = this.eid;
        pk.action = this.action;
        pk.data = this.data;
        pk.rowingTime = this.rowingTime;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return AnimatePacket.class;
    }
}
