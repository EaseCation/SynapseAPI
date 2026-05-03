package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class InventorySlotPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.INVENTORY_SLOT_PACKET;

    public int inventoryId;
    public int slot;
    /**
     * Used to reference a specific container within a given screen container context.
     */
    public int containerSlotType;
    /**
     * ID of the container if it is dynamic.
     */
    @Nullable
    public Integer dynamicContainerId;
    /**
     * Optional storage item to set into. Only the item type is relevant, not any stack information.
     */
    @Nullable
    public Item storageItem;
    public Item item;

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
        this.putByte(this.inventoryId);
        this.putUnsignedVarInt(this.slot);
        this.putOptional(this.dynamicContainerId, (stream, dynamicContainerId) -> {
            stream.putByte(this.containerSlotType);
            stream.putLInt(dynamicContainerId);
        });
        this.putOptional(this.storageItem, BinaryStream::putItemStack);
        this.putItemStack(this.item);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
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
