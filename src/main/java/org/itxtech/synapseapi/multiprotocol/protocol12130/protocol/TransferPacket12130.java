package org.itxtech.synapseapi.multiprotocol.protocol12130.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.TransferPacket;
import lombok.ToString;

@ToString
public class TransferPacket12130 extends Packet12130 {
    public static final int NETWORK_ID = ProtocolInfo.TRANSFER_PACKET;

    public String address;
    public int port = 19132;
    public boolean reloadWorld;

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
        putString(address);
        putLShort(port);
        putBoolean(reloadWorld);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        TransferPacket packet = (TransferPacket) pk;
        address = packet.address;
        port = packet.port;
        reloadWorld = packet.reloadWorld;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return TransferPacket.class;
    }
}
