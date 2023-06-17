package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

public class DeathInfoPacket11910 extends Packet11910 {
    public static final int NETWORK_ID = ProtocolInfo.DEATH_INFO_PACKET;

    public String messageTranslationKey = "";
    public String[] messageParameters = new String[0];

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

        putString(messageTranslationKey);

        putUnsignedVarInt(messageParameters.length);
        for (String parameter : messageParameters) {
            putString(parameter);
        }
    }
}
