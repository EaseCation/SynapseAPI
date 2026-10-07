package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeModelTexturePacket17 extends Packet17 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_MODEL_TEXTURE;

	public static final int MODE_TEXTURE = 0;
	public static final int MODE_ENTITY_SKIN = 1;

	public long entityUniqueId;
	public String texture = "";
	public long skinEntityUniqueId = -1;
	public int mode = MODE_TEXTURE;

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
		putString(texture);
		putEntityUniqueId(mode == MODE_ENTITY_SKIN ? skinEntityUniqueId : entityUniqueId);
		putByte(mode);
	}
}
