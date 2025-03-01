package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet12170 extends IterationProtocolPacket {
    @Override
    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.PROTOCOL_121_70;
    }

    @Override
    public void reset() {
        super.superReset();
        this.putUnsignedVarInt(this.pid());
    }
}
