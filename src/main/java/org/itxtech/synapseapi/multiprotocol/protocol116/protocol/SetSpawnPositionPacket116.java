package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetSpawnPositionPacket;
import org.itxtech.synapseapi.utils.ClassUtils;

public class SetSpawnPositionPacket116 extends Packet116 {

    public static final int NETWORK_ID = ProtocolInfo.SET_SPAWN_POSITION_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static final int TYPE_PLAYER_SPAWN = 0;
    public static final int TYPE_WORLD_SPAWN = 1;

    public int spawnType;
    public int y;
    public int z;
    public int x;
    public int dimension = 0;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putVarInt(this.spawnType);
        this.putBlockVector3(this.x, this.y, this.z);
        this.putVarInt(dimension);
        this.putBlockVector3(this.x, this.y, this.z);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, SetSpawnPositionPacket.class);

        SetSpawnPositionPacket packet = (SetSpawnPositionPacket) pk;
        this.spawnType = packet.spawnType;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetSpawnPositionPacket.class;
    }
}
