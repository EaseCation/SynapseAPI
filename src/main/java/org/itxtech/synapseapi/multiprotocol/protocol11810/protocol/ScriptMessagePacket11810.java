package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ScriptMessagePacket11810 extends Packet11810 {
    public static final int NETWORK_ID = ProtocolInfo.SCRIPT_MESSAGE_PACKET;

    public String messageId;
    public String value;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        messageId = getString();
        value = getString();
    }

    @Override
    public void encode() {
        reset();
        putString(messageId);
        putString(value);
    }
}
