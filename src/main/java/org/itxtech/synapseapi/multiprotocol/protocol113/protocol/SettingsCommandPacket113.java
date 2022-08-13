package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SettingsCommandPacket113 extends Packet113 {
    public static final int NETWORK_ID = ProtocolInfo.SETTINGS_COMMAND_PACKET;

    public String command;
    public boolean suppressingOutput;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.command = this.getString();
        this.suppressingOutput = this.getBoolean();
    }

    @Override
    public void encode() {
    }
}
