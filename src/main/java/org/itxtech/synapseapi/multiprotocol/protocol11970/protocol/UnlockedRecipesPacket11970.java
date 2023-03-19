package org.itxtech.synapseapi.multiprotocol.protocol11970.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UnlockedRecipesPacket11970 extends Packet11970 {
    public static final int NETWORK_ID = ProtocolInfo.UNLOCKED_RECIPES_PACKET;

    public boolean newRecipes;
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
        putBoolean(newRecipes);

        putUnsignedVarInt(recipes.length);
        for (String recipe : recipes) {
            putString(recipe);
        }
    }
}
