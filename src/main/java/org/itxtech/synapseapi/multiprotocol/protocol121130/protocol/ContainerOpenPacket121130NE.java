package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ContainerOpenPacket121130NE extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.CONTAINER_OPEN_PACKET;

    public int windowId;
    public int type;
    public int x;
    public int y;
    public int z;
    public long entityId = -1;

    /**
     * @since 1.21.120-netease
     */
    public boolean isIgnoreBlock;
    /**
     * @since 1.21.130-netease
     */
    public String screenName = "";
    /**
     * @since 1.21.130-netease
     */
    public String customDescription = "";
    /**
     * @since 1.21.130-netease
     */
    public boolean isClosedDropItem = true;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.windowId);
        this.putByte(this.type);
        this.putBlockVector3(this.x, this.y, this.z);
        this.putEntityUniqueId(this.entityId);

        if (neteaseMode) {
            this.putBoolean(this.isIgnoreBlock);
            this.putString(this.screenName);
            this.putString(this.customDescription);
            this.putBoolean(this.isClosedDropItem);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ContainerOpenPacket packet = (ContainerOpenPacket) pk;
        this.windowId = packet.windowId;
        this.type = packet.type;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.entityId = packet.entityId;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ContainerOpenPacket.class;
    }
}
