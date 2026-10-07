package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeModelBindPacket113 extends Packet113 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_MODEL_BIND;

	public long entityUniqueId;
	public long bindEntityUniqueId = -1;

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
		putEntityUniqueId(bindEntityUniqueId);
	}
}
