package org.itxtech.synapseapi.multiprotocol.protocol12130.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class InventoryContentPacket12130 extends Packet12130 {
    public static final int NETWORK_ID = ProtocolInfo.INVENTORY_CONTENT_PACKET;

    public int inventoryId;
    public Item[] slots = new Item[0];
    /**
     * Used to reference a specific container within a given screen container context.
     */
    public int containerSlotType;
    /**
     * ID of the particular container instance if this is a dynamic container.
     */
    @Nullable
    public Integer dynamicContainerId;
    /**
     * Size of the particular container instance if this is a dynamic container, otherwise zero.
     */
    public int dynamicContainerSize;

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
        this.putUnsignedVarInt(this.inventoryId);
        this.putUnsignedVarInt(this.slots.length);
        for (Item slot : this.slots) {
            this.putSlot(slot);
        }
        this.putByte(this.containerSlotType);
        this.putOptional(this.dynamicContainerId, BinaryStream::putLInt);
        this.putUnsignedVarInt(this.dynamicContainerSize);
    }

    @Override
    public DataPacket clean() {
        this.slots = new Item[0];
        return super.clean();
    }

    @Override
    public InventoryContentPacket12130 clone() {
        InventoryContentPacket12130 pk = (InventoryContentPacket12130) super.clone();
        pk.slots = this.slots.clone();
        return pk;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        InventoryContentPacket packet = (InventoryContentPacket) pk;
        this.inventoryId = packet.inventoryId;
        this.slots = packet.slots;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InventoryContentPacket.class;
    }
}
