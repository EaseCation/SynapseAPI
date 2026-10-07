package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeModelOffsetPacket113 extends Packet113 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_MODEL_OFFSET;

	public long entityUniqueId;
	public float x;
    public float y;
	public float z;

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
		putLDouble(x);
		putLDouble(y);
		putLDouble(z);
	}
}
