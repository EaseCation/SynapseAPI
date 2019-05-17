package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.IterationProtocolPacket;

public abstract class Packet111 extends IterationProtocolPacket {

	@Override
	public AbstractProtocol getAbstractProtocol() {
		return AbstractProtocol.PROTOCOL_111;
	}

	@Override
	public void reset() {
		super.superReset();
		this.putUnsignedVarInt(this.pid());
	}
}
