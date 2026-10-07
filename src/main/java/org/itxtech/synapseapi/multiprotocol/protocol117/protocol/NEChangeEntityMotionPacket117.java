package org.itxtech.synapseapi.multiprotocol.protocol117.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeEntityMotionPacket117 extends Packet117 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_ACTOR_MOTION;

	public static final int ACTION_RESET = 0;

	public long entityUniqueId;
	public int action = ACTION_RESET;

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
		putByte(action);
	}
}
