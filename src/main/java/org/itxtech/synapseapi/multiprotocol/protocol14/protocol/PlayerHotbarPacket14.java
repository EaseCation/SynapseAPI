package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerIds;
import org.itxtech.synapseapi.utils.ClassUtils;

public class PlayerHotbarPacket14 extends Packet14 {

    public int selectedHotbarSlot;
    public int windowId = ContainerIds.INVENTORY;

    public boolean selectHotbarSlot = true;

    @Override
    public int pid() {
        return ProtocolInfo.PLAYER_HOTBAR_PACKET;
    }

    @Override
    public void decode() {
        this.selectedHotbarSlot = (int) this.getUnsignedVarInt();
        this.windowId = this.getByte();
        this.selectHotbarSlot = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.selectedHotbarSlot);
        this.putByte((byte) this.windowId);
        this.putBoolean(this.selectHotbarSlot);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.PlayerHotbarPacket.class);
        cn.nukkit.network.protocol.PlayerHotbarPacket packet = (cn.nukkit.network.protocol.PlayerHotbarPacket) pk;
        this.selectedHotbarSlot = packet.selectedHotbarSlot;
        this.windowId = packet.windowId;
        this.selectHotbarSlot = packet.selectHotbarSlot;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.PlayerHotbarPacket.class;
    }

}
