package org.itxtech.synapseapi.multiprotocol.protocol12.protocol;

import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

/**
 * @author CreeperFace
 */
public abstract class Packet12 extends IterationProtocolPacket {

    public DataPacket toDefault() {
        return this;
    }

    public DataPacket fromDefault(DataPacket pk) {
        return this;
    }

    static Class<? extends DataPacket> getDefaultPacket() {
        return null;
    }

    public boolean needConversion() {
        return true;
    }

    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_12;
    }

}
