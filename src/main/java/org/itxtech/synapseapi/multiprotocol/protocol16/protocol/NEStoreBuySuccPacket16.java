package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NEStoreBuySuccPacket16 extends Packet16 {
    public static final int NETWORK_ID = ProtocolInfo.PACKET_STORE_BUY_SUCC;

    public String data;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        if (!feof()) {
            data = getString();
        }
    }

    @Override
    public void encode() {
    }

}
