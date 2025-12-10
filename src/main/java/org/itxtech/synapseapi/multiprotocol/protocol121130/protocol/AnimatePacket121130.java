package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.AnimatePacket.Action;
import cn.nukkit.network.protocol.AnimatePacket.SwingSource;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import javax.annotation.Nullable;

@ToString
public class AnimatePacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.ANIMATE_PACKET;

    public long eid;
    public Action action;
    public float data;
    @Nullable
    public SwingSource swingSource;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = Action.fromId(this.getByte());
        this.eid = this.getEntityRuntimeId();
        this.data = this.getLFloat();
        this.swingSource = this.getOptionalEnum(SwingSource::byName);
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.action.getId());
        this.putEntityRuntimeId(this.eid);
        this.putLFloat(this.data);
        this.putOptionalEnum(this.swingSource, SwingSource::getName);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        AnimatePacket packet = (AnimatePacket) pk;
        this.eid = packet.eid;
        this.action = packet.action;
        this.data = packet.data;
        this.swingSource = packet.swingSource;
        return this;
    }

    @Override
    public DataPacket toDefault() {
        AnimatePacket pk = new AnimatePacket();
        pk.eid = this.eid;
        pk.action = this.action;
        pk.data = this.data;
        pk.swingSource = this.swingSource;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return AnimatePacket.class;
    }
}
