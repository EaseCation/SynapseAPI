package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class NENetEaseJsonPacket16 extends Packet16 {
    public static final int NETWORK_ID = ProtocolInfo.PACKET_NETEASE_JSON;

    public String json;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.json = this.getString();
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(json);
    }

}
