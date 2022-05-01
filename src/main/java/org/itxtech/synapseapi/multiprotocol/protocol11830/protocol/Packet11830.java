package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet11830 extends IterationProtocolPacket {

    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_118_30;
    }

    @Override
    public void reset() {
        super.superReset();
        this.putUnsignedVarInt(this.pid());
    }
}
