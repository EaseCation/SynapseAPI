package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Binary;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class AddItemEntityPacket17 extends Packet17 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_ITEM_ACTOR_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public long entityUniqueId;
    public long entityRuntimeId;
    public Item item;
    public float x;
    public float y;
    public float z;
    public float speedX;
    public float speedY;
    public float speedZ;
    public EntityMetadata metadata = new EntityMetadata();
    public boolean isFromFishing = false;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putSlot(this.item);
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.speedX, this.speedY, this.speedZ);
        this.put(Binary.writeMetadata(metadata));
        this.putBoolean(this.isFromFishing);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.AddItemEntityPacket.class);
        cn.nukkit.network.protocol.AddItemEntityPacket packet = (cn.nukkit.network.protocol.AddItemEntityPacket) pk;
        this.entityUniqueId = packet.entityUniqueId;
        this.entityRuntimeId = packet.entityRuntimeId;
        this.item = packet.item;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.speedX = packet.speedX;
        this.speedY = packet.speedY;
        this.speedZ = packet.speedZ;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AddItemEntityPacket.class;
    }
}
