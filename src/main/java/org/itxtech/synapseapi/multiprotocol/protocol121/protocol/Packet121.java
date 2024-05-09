package org.itxtech.synapseapi.multiprotocol.protocol121.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet121 extends IterationProtocolPacket {
    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_121;
    }

    @Override
    public void reset() {
        super.superReset();
        this.putUnsignedVarInt(this.pid());
    }
}
