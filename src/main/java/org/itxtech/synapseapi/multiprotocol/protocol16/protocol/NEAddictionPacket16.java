package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.JsonUtil;
import lombok.ToString;

@ToString
public class NEAddictionPacket16 extends Packet16 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_ADDICTION;

	public double blockMultiplier = 1;
	public double expMultiplier = 1;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	@Override
	public void decode() {
		AddictionData data = JsonUtil.UNTRUSTED_JSON_MAPPER.readValue(get(), AddictionData.class);
		if (data != null) {
			if (data.blockMultiplier != null) {
				blockMultiplier = data.blockMultiplier;
			}
			if (data.expMultiplier != null) {
				expMultiplier = data.expMultiplier;
			}
		}
	}

	@Override
	public void encode() {
	}

	private record AddictionData(
			Double blockMultiplier,
			Double expMultiplier
	) {
	}
}
