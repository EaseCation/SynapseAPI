package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobArmorEquipmentPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class MobArmorEquipmentPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET;

    public long eid;
    public Item[] slots = new Item[4];
    /**
     * an extra armor slot for entities like Horses (horse/wolf armor and llama carpet),
     * limited to a single armor item but needing the effects of a full armor set.
     */
    public Item body;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        this.slots = new Item[4];
        this.slots[0] = this.getSlot();
        this.slots[1] = this.getSlot();
        this.slots[2] = this.getSlot();
        this.slots[3] = this.getSlot();
        this.body = this.getSlot();
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        this.putSlot(this.slots[0]);
        this.putSlot(this.slots[1]);
        this.putSlot(this.slots[2]);
        this.putSlot(this.slots[3]);
        this.putSlot(this.body);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, MobArmorEquipmentPacket.class);
        MobArmorEquipmentPacket packet = (MobArmorEquipmentPacket) pk;

        this.eid = packet.eid;
        this.slots = packet.slots;

        Item body = packet.body;
        if (body != null) {
            this.body = body;

            if (!this.slots[1].isNull()) {
                this.slots[1] = Items.air();
            }
        } else {
            this.body = Items.air();
        }

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return MobArmorEquipmentPacket.class;
    }
}
