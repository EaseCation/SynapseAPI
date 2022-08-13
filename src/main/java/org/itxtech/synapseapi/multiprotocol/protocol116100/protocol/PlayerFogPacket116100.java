package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerFogPacket116100 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_FOG_PACKET;

    public String[] fogStack = new String[0];

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
        this.putUnsignedVarInt(this.fogStack.length);
        for (String fog : this.fogStack) {
            this.putString(fog);
        }
    }
}
