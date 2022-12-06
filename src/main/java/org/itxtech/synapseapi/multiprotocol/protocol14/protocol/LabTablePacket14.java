package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class LabTablePacket14 extends Packet14 {
    public static final int NETWORK_ID = ProtocolInfo.LAB_TABLE_PACKET;

    public static final int ACTION_START_COMBINE = 0;
    public static final int ACTION_START_REACTION = 1;
    public static final int ACTION_RESET = 2;

    public static final int REACTION_TYPE_NONE = 0;
    public static final int REACTION_TYPE_ICE_BOMB = 1;
    public static final int REACTION_TYPE_BLEACH = 2;
    public static final int REACTION_TYPE_ELEPHANT_TOOTHPASTE = 3;
    public static final int REACTION_TYPE_FERTILIZER = 4;
    public static final int REACTION_TYPE_HEAT_BLOCK = 5;
    public static final int REACTION_TYPE_MAGNESIUM_SALTS = 6;
    public static final int REACTION_TYPE_MISC_FIRE = 7;
    public static final int REACTION_TYPE_MISC_EXPLOSION = 8;
    public static final int REACTION_TYPE_MISC_LAVAL = 9;
    public static final int REACTION_TYPE_MISC_MYSTICAL = 10;
    public static final int REACTION_TYPE_MISC_SMOKE = 11;
    public static final int REACTION_TYPE_MISC_LARGE_SMOKE = 12;

    public int action;
    public int x;
    public int y;
    public int z;
    public int reactionType;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = this.getByte();
        BlockVector3 v = this.getSignedBlockPosition();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.reactionType = this.getByte();
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) this.action);
        this.putSignedBlockPosition(this.x, this.y, this.z);
        this.putByte((byte) this.reactionType);
    }
}
