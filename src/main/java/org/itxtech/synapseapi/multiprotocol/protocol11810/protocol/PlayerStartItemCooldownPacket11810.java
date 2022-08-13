package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerStartItemCooldownPacket11810 extends Packet11810 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_START_ITEM_COOLDOWN_PACKET;

    public String itemCategory;
    public int cooldownTicks;

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
        this.putString(this.itemCategory);
        this.putVarInt(this.cooldownTicks);
    }
}
