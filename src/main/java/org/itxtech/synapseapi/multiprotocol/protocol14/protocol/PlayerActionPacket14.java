package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * @author Nukkit Project Team
 */
@ToString
public class PlayerActionPacket14 extends Packet14 {

    public static final int NETWORK_ID = ProtocolInfo.PLAYER_ACTION_PACKET;

    public static final int ACTION_START_BREAK = 0;
    public static final int ACTION_ABORT_BREAK = 1;
    public static final int ACTION_STOP_BREAK = 2;
    public static final int ACTION_GET_UPDATED_BLOCK = 3;
    public static final int ACTION_DROP_ITEM = 4;
    public static final int ACTION_START_SLEEPING = 5;
    public static final int ACTION_STOP_SLEEPING = 6;
    public static final int ACTION_RESPAWN = 7;
    public static final int ACTION_JUMP = 8;
    public static final int ACTION_START_SPRINT = 9;
    public static final int ACTION_STOP_SPRINT = 10;
    public static final int ACTION_START_SNEAK = 11;
    public static final int ACTION_STOP_SNEAK = 12;
    public static final int ACTION_DIMENSION_CHANGE_REQUEST = 13; //sent when dying in different dimension
    public static final int ACTION_CREATIVE_PLAYER_DESTROY_BLOCK = 13; // 1.16.100+
    public static final int ACTION_DIMENSION_CHANGE_ACK = 14; //sent when spawning in a different dimension to tell the server we spawned
    public static final int ACTION_START_GLIDE = 15;
    public static final int ACTION_STOP_GLIDE = 16;
    public static final int ACTION_BUILD_DENIED = 17;
    public static final int ACTION_CONTINUE_BREAK = 18;
    public static final int ACTION_CHANGE_SKIN = 19;
    public static final int ACTION_SET_ENCHANTMENT_SEED = 20;
    public static final int ACTION_START_SWIMMING = 21;
    public static final int ACTION_STOP_SWIMMING = 22;
    public static final int ACTION_START_SPIN_ATTACK = 23;
    public static final int ACTION_STOP_SPIN_ATTACK = 24;
    public static final int ACTION_BLOCK_INTERACT = 25;
    public static final int ACTION_BLOCK_PREDICT_DESTROY = 26;
    public static final int ACTION_BLOCK_CONTINUE_DESTROY = 27;
    /**
     * @since 1.19.0
     */
    public static final int ACTION_ITEM_USE_ON_START = 28;
    /**
     * @since 1.19.0
     */
    public static final int ACTION_ITEM_USE_ON_STOP = 29;

    public long entityId;
    public int action;
    public int x;
    public int y;
    public int z;
    public int data;

    @Override
    public void decode() {
        this.entityId = this.getEntityRuntimeId();
        this.action = this.getVarInt();
        BlockVector3 v = this.getBlockVector3();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.data = this.getVarInt();
    }

    @Override
    public void encode() {
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.PlayerActionPacket.class;
    }

    @Override
    public DataPacket toDefault() {
        PlayerActionPacket pk = new PlayerActionPacket();
        pk.action = this.action;
        pk.entityId = this.entityId;
        pk.data = this.data;
        pk.x = this.x;
        pk.y = this.y;
        pk.z = this.z;
        return pk;
    }
}
