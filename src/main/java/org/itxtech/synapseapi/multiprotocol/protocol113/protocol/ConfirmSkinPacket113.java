package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@ToString
public class ConfirmSkinPacket113 extends Packet113 {

	public static final int NETWORK_ID = ProtocolInfo.PACKET_CONFIRM_SKIN;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public List<UUID> uuids = new ObjectArrayList<>();

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
