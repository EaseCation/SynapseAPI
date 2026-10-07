package org.itxtech.synapseapi.multiprotocol.protocol116200.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEWithdrawFurnaceXpPacket116200 extends Packet116200 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_WITHDRAW_FURNACE_XP;

	public int x;
	public int y;
	public int z;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	@Override
	public void decode() {
		BlockVector3 pos = getSignedBlockPosition();
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	@Override
	public void encode() {
	}
}
