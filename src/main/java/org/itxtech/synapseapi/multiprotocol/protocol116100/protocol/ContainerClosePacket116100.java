package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ContainerClosePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class ContainerClosePacket116100 extends Packet116100 {

    public static final int NETWORK_ID = ProtocolInfo.CONTAINER_CLOSE_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public int windowId;
    /**
     * 是否是由服务端发起. 响应客户端的ContainerClosePacket需要为false. 设置不当会收到PacketViolationWarningPacket.
     * 这个垃圾也许是Microjang用来修那个物品栏消失bug的? 然而并没有什么用.
     */
    public boolean wasServerInitiated;

    @Override
    public void decode() {
        this.windowId = this.getSingedByte();
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
        this.wasServerInitiated = packet.wasServerInitiated;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ContainerClosePacket.class;
    }
}
