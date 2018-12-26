package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

/**
 * Created on 15-10-13.
 */
public class ScriptCustomEventPacket17 extends Packet17 {

	public static final int NETWORK_ID = ProtocolInfo.SCRIPT_CUSTOM_EVENT_PACKET;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public String eventName;
	public String eventData;

	@Override
	public void decode() {

	}

	@Override
	public void encode() {
		this.reset();
		this.putString(this.eventName);
		this.putString(this.eventData);
	}

}
