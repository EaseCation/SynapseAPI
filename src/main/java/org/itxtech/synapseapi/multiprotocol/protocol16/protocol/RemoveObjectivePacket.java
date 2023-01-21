package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RemoveObjectivePacket extends Packet16 {

    public String objectiveName;

    @Override
    public int pid() {
        return ProtocolInfo.REMOVE_OBJECTIVE_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.objectiveName);
    }
}
