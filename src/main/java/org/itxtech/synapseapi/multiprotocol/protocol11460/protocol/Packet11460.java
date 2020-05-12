package org.itxtech.synapseapi.multiprotocol.protocol11460.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet11460 extends IterationProtocolPacket {

	@Override
	public AbstractProtocol getAbstractProtocol() {
		return AbstractProtocol.PROTOCOL_114_60;
	}

	@Override
	public void reset() {
		super.superReset();
		this.putUnsignedVarInt(this.pid());
	}
}
