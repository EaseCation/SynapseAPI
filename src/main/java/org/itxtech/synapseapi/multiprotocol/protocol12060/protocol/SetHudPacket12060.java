package org.itxtech.synapseapi.multiprotocol.protocol12060.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SetHudPacket12060 extends Packet12060 {
    public static final int NETWORK_ID = ProtocolInfo.SET_HUD_PACKET;

    public static final int ELEMENT_PAPER_DOLL = 0;
    public static final int ELEMENT_ARMOR = 1;
    public static final int ELEMENT_TOOL_TIPS = 2;
    public static final int ELEMENT_TOUCH_CONTROLS = 3;
    public static final int ELEMENT_CROSSHAIR = 4;
    public static final int ELEMENT_HOTBAR = 5;
    public static final int ELEMENT_HEALTH = 6;
    public static final int ELEMENT_XP = 7;
    public static final int ELEMENT_FOOD = 8;
    public static final int ELEMENT_AIR_BUBBLES = 9;
    public static final int ELEMENT_HORSE_HEALTH = 10;

    public static final int VISIBILITY_HIDE = 0;
    public static final int VISIBILITY_RESET = 1;

    public int[] elements = new int[0];
    public int visibility = VISIBILITY_RESET;

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

        putUnsignedVarInt(elements.length);
        for (int element : elements) {
            putUnsignedVarInt(element);
        }

        putByte((byte) visibility);
    }
}
