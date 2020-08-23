package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet116 extends IterationProtocolPacket {

	@Override
	public AbstractProtocol getAbstractProtocol() {
		return AbstractProtocol.PROTOCOL_116;
	}

	@Override
	public void reset() {
		super.superReset();
		this.putUnsignedVarInt(this.pid());
	}
}
