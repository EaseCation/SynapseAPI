package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfirmSkinPacket113 extends Packet113 {

	public static final int NETWORK_ID = ProtocolInfo.CONFIRM_SKIN_PACKET;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public List<UUID> uuids = new ArrayList<>();

	@Override
	public void decode() {

	}

	@Override
	public void encode() {
		this.reset();
		this.putUnsignedVarInt(this.uuids.size());
		for (UUID uuid : this.uuids) {
			this.putBoolean(true);
			this.putUUID(uuid);
			this.putString("");
		}
	}

}
