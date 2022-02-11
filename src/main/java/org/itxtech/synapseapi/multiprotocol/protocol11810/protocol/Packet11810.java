package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet11810 extends IterationProtocolPacket {

    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_118_10;
    }

    @Override
    public void reset() {
        super.superReset();
        this.putUnsignedVarInt(this.pid());
    }
}
