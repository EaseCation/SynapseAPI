package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeModelPacket16 extends Packet16 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_MODEL;

	public long entityUniqueId;
	public String modelName = "";

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	@Override
	public void decode() {
	}

	@Override
	public void encode() {
		reset();
		putEntityUniqueId(entityUniqueId);
		putString(modelName);
	}
}
