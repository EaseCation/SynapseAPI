package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class InventorySlotPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.INVENTORY_SLOT_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public int inventoryId;
    public int slot;
    public Item item;

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.inventoryId);
        this.putUnsignedVarInt(this.slot);
        this.putVarInt(this.item.getId());
        this.putSlot(this.item);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.InventorySlotPacket.class);

        cn.nukkit.network.protocol.InventorySlotPacket packet = (cn.nukkit.network.protocol.InventorySlotPacket) pk;
        this.inventoryId = packet.inventoryId;
        this.slot = packet.slot;
        this.item = packet.item;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.InventorySlotPacket.class;
    }
}
