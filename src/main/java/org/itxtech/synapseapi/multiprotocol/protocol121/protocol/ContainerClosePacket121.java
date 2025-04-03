package org.itxtech.synapseapi.multiprotocol.protocol121.protocol;

import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerType;
import lombok.ToString;

@ToString
public class ContainerClosePacket121 extends Packet121 {
    public static final int NETWORK_ID = ProtocolInfo.CONTAINER_CLOSE_PACKET;

    public int windowId;
    public int windowType = ContainerType.NONE;
    public boolean wasServerInitiated;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.windowId = this.getSingedByte();
        this.windowType = this.getSingedByte();
        this.wasServerInitiated = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) this.windowId);
        this.putByte((byte) this.windowType);
        this.putBoolean(this.wasServerInitiated);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ContainerClosePacket packet = (ContainerClosePacket) pk;
        this.windowId = packet.windowId;
        this.windowType = packet.windowType;
        this.wasServerInitiated = packet.wasServerInitiated;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ContainerClosePacket.class;
    }
}
