package org.itxtech.synapseapi.multiprotocol.protocol12050.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SetPlayerInventoryOptionsPacket12050 extends Packet12050 {
    public static final int NETWORK_ID = ProtocolInfo.SET_PLAYER_INVENTORY_OPTIONS_PACKET;

    public static final int LEFT_NONE = 0;
    public static final int LEFT_RECIPE_CONSTRUCTION = 1;
    public static final int LEFT_RECIPE_EQUIPMENT = 2;
    public static final int LEFT_RECIPE_ITEMS = 3;
    public static final int LEFT_RECIPE_NATURE = 4;
    public static final int LEFT_RECIPE_SEARCH = 5;
    public static final int LEFT_SURVIVAL = 6;

    public static final int RIGHT_NONE = 0;
    public static final int RIGHT_FULL_SCREEN = 1;
    public static final int RIGHT_CRAFTING = 2;
    public static final int RIGHT_ARMOR = 3;

    public static final int LAYOUT_NONE = 0;
    public static final int LAYOUT_SURVIVAL = 1;
    public static final int LAYOUT_RECIPE_BOOK = 2;
    public static final int LAYOUT_CREATIVE = 3;

    public int leftTab = LEFT_NONE;
    public int rightTab = RIGHT_NONE;
    public boolean filtering;
    public int layout = LAYOUT_NONE;
    public int craftingLayout = LAYOUT_NONE;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        leftTab = getVarInt();
        rightTab = getVarInt();
        filtering = getBoolean();
        layout = getVarInt();
        craftingLayout = getVarInt();
    }

    @Override
    public void encode() {
        reset();
        putVarInt(leftTab);
        putVarInt(rightTab);
        putBoolean(filtering);
        putVarInt(layout);
        putVarInt(craftingLayout);
    }
}
