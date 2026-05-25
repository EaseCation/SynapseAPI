package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet12630 extends IterationProtocolPacket {
    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_126_30;
    }

    @Override
    public void reset() {
        superReset();
        putUnsignedVarInt(pid());
    }
}
