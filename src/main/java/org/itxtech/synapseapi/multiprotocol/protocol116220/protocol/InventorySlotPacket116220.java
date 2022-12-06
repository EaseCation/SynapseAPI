package org.itxtech.synapseapi.multiprotocol.protocol116220.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class InventorySlotPacket116220 extends Packet116220 {

    public static final byte NETWORK_ID = ProtocolInfo.INVENTORY_SLOT_PACKET;

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
        this.putSlot(this.item);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, InventorySlotPacket.class);

        InventorySlotPacket packet = (InventorySlotPacket) pk;
        this.inventoryId = packet.inventoryId;
        this.slot = packet.slot;
        this.item = packet.item;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InventorySlotPacket.class;
    }
}
