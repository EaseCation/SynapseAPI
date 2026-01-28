package org.itxtech.synapseapi.multiprotocol.protocol11710.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SimulationTypePacket11710 extends Packet11710 {
    public static final int NETWORK_ID = ProtocolInfo.SIMULATION_TYPE_PACKET;

    public static final int TYPE_GAME = 0;
    public static final int TYPE_EDITOR = 1;
    public static final int TYPE_TEST = 2;

    public int type = TYPE_GAME;

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
        putByte(type);
    }
}
