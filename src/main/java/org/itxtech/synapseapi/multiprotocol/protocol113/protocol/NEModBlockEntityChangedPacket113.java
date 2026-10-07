package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEModBlockEntityChangedPacket113 extends Packet113 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_MOD_BLOCK_ACTOR_CHANGED;

	public int x;
	public int y;
	public int z;

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
		putSignedBlockPosition(x, y, z);
	}
}
