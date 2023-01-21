package org.itxtech.synapseapi.multiprotocol.protocol11730.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.EntityPickRequestPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class EntityPickRequestPacket11730 extends Packet11730 {
    public static final int NETWORK_ID = ProtocolInfo.ACTOR_PICK_REQUEST_PACKET;

    public long entityId;
    public int hotbarSlot;
    public boolean withData;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.entityId = this.getLLong();
        this.hotbarSlot = this.getByte();
        this.withData = this.getBoolean();
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, EntityPickRequestPacket.class);

        EntityPickRequestPacket packet = (EntityPickRequestPacket) pk;
        this.entityId = packet.entityId;
        this.hotbarSlot = packet.hotbarSlot;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return EntityPickRequestPacket.class;
    }
}
