package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class InventorySlotPacket12120 extends Packet12120 {

    public static final int NETWORK_ID = ProtocolInfo.INVENTORY_SLOT_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public int inventoryId;
    public int slot;
    /**
     * ID of the container if it is dynamic, zero otherwise.
     */
    public int dynamicContainerId;
    public Item item;

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.inventoryId);
        this.putUnsignedVarInt(this.slot);
        this.putUnsignedVarInt(this.dynamicContainerId);
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
