package org.itxtech.synapseapi.multiprotocol.protocol12050.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerToggleCrafterSlotRequestPacket12050 extends Packet12050 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_TOGGLE_CRAFTER_SLOT_REQUEST_PACKET;

    public int x;
    public int y;
    public int z;
    public int slot;
    public boolean disabled;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        x = (int) getUnsignedVarInt(); //mj, wtf unsigned???
        y = (int) getUnsignedVarInt();
        z = (int) getUnsignedVarInt();
        slot = getByte();
        if (slot >= 128) {
            throw new IndexOutOfBoundsException("The slot index is too big");
        }
        disabled = getBoolean();
    }

    @Override
    public void encode() {
    }
}
