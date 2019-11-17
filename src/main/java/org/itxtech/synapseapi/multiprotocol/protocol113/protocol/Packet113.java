package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet113 extends IterationProtocolPacket {

	@Override
	public AbstractProtocol getAbstractProtocol() {
		return AbstractProtocol.PROTOCOL_113;
	}

	@Override
	public void reset() {
		super.superReset();
		this.putUnsignedVarInt(this.pid());
	}
}
