package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class InventoryContentPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.INVENTORY_CONTENT_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static final int SPECIAL_INVENTORY = 0;
    public static final int SPECIAL_OFFHAND = 0x77;
    public static final int SPECIAL_ARMOR = 0x78;
    public static final int SPECIAL_CREATIVE = 0x79;
    public static final int SPECIAL_HOTBAR = 0x7a;
    public static final int SPECIAL_FIXED_INVENTORY = 0x7b;

    public int inventoryId;
    public Item[] slots = new Item[0];

    @Override
    public DataPacket clean() {
        this.slots = new Item[0];
        return super.clean();
    }

    @Override
    public void decode() {
        this.inventoryId = (int) this.getUnsignedVarInt();
        int count = (int) this.getUnsignedVarInt();
        this.slots = new Item[count];

        for (int s = 0; s < count && !this.feof(); ++s) {
            this.slots[s] = this.getSlot();
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.inventoryId);
        this.putUnsignedVarInt(this.slots.length);
        for (Item slot : this.slots) {
            this.putVarInt(slot.getId());
            this.putSlot(slot);
        }
    }

    @Override
    public InventoryContentPacket116 clone() {
        InventoryContentPacket116 pk = (InventoryContentPacket116) super.clone();
        pk.slots = this.slots.clone();
        return pk;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.InventoryContentPacket.class);

        cn.nukkit.network.protocol.InventoryContentPacket packet = (cn.nukkit.network.protocol.InventoryContentPacket) pk;

        this.inventoryId = packet.inventoryId;
        this.slots = packet.slots;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.InventoryContentPacket.class;
    }
}
