package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * @author Nukkit Project Team
 */
@ToString
public class AddPaintingPacket112 extends Packet112 {

    public static final int NETWORK_ID = ProtocolInfo.ADD_PAINTING_PACKET;

    public long entityUniqueId;
    public long entityRuntimeId;
    public float x;
    public float y;
    public float z;
    public int direction;
    public String title;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);

        this.putVector3f(this.x, this.y, this.z);
        this.putVarInt(this.direction);
        this.putString(this.title);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.AddPaintingPacket.class);
        cn.nukkit.network.protocol.AddPaintingPacket packet = (cn.nukkit.network.protocol.AddPaintingPacket) pk;

        this.entityUniqueId = packet.entityUniqueId;
        this.entityRuntimeId = packet.entityRuntimeId;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.direction = packet.direction;
        this.title = packet.title;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AddPaintingPacket.class;
    }

}
