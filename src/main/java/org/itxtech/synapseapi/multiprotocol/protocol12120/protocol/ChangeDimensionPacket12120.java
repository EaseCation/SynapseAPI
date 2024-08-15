package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ChangeDimensionPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import javax.annotation.Nullable;

@ToString
public class ChangeDimensionPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.CHANGE_DIMENSION_PACKET;

    public int dimension;
    public float x;
    public float y;
    public float z;
    public boolean respawn;
    /**
     * Leave empty if there is no loading screen expected on the client.
     * This id needs to be unique and not conflict with any other active loading screens.
     * This is implemented with an unsigned integer incrementing forever,
     * and that is expected to not have collisions when it wraps around back to 0 if that could be a possibility.
     */
    @Nullable
    public Integer loadingScreenId;

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
        this.putVarInt(this.dimension);
        this.putVector3f(this.x, this.y, this.z);
        this.putBoolean(this.respawn);
        this.putOptional(this.loadingScreenId, BinaryStream::putLInt);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, ChangeDimensionPacket.class);

        ChangeDimensionPacket packet = (ChangeDimensionPacket) pk;
        this.dimension = packet.dimension;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.respawn = packet.respawn;
        this.loadingScreenId = packet.loadingScreenId;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ChangeDimensionPacket.class;
    }
}

