package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet19 extends IterationProtocolPacket {

	@Override
	public AbstractProtocol getAbstractProtocol() {
		return AbstractProtocol.PROTOCOL_19;
	}

	@Override
	public void reset() {
		super.superReset();
		this.putUnsignedVarInt(this.pid());
	}
}
