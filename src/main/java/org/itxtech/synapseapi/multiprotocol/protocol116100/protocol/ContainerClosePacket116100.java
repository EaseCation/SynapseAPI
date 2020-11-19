package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

public class ContainerClosePacket116100 extends Packet116100 {

    public static final int NETWORK_ID = ProtocolInfo.CONTAINER_CLOSE_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public int windowId;
    public boolean wasServerInitiated;

    @Override
    public void decode() {
        this.windowId = (byte) this.getByte();
        this.wasServerInitiated = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) this.windowId);
        this.putBoolean(this.wasServerInitiated);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, ContainerClosePacket.class);

        ContainerClosePacket packet = (ContainerClosePacket) pk;
        this.windowId = packet.windowId;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ContainerClosePacket.class;
    }
}
