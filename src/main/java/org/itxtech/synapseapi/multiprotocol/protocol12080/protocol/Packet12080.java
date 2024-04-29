package org.itxtech.synapseapi.multiprotocol.protocol12080.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet12080 extends IterationProtocolPacket {
    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_120_80;
    }

    @Override
    public void reset() {
        super.superReset();
        this.putUnsignedVarInt(this.pid());
    }
}
