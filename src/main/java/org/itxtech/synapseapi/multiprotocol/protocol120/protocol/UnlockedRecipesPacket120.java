package org.itxtech.synapseapi.multiprotocol.protocol120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UnlockedRecipesPacket120 extends Packet120 {
    public static final int NETWORK_ID = ProtocolInfo.UNLOCKED_RECIPES_PACKET;

    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_INITIALLY_UNLOCKED = 1;
    public static final int TYPE_NEWLY_UNLOCKED = 2;
    public static final int TYPE_REMOVE = 3;
    public static final int TYPE_REMOVE_ALL = 4;

    public int type = TYPE_EMPTY;
    public String[] recipes = new String[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putLInt(type);

        putUnsignedVarInt(recipes.length);
        for (String recipe : recipes) {
            putString(recipe);
        }
    }
}
