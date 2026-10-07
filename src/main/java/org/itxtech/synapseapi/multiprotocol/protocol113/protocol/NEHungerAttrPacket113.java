package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.JsonUtil;
import lombok.ToString;

@ToString
public class NEHungerAttrPacket113 extends Packet113 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_HUNGER_ATTR;

	public float maxExhaustion = 4;

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
		put(JsonUtil.TRUSTED_JSON_MAPPER.writeValueAsBytes(new HungerAttr(maxExhaustion)));
	}

	private record HungerAttr(
			float maxExhaustion
	) {
	}
}
