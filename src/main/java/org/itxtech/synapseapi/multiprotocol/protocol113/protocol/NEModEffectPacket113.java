package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEModEffectPacket113 extends Packet113 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_MOD_EFFECT;

	public ModEffect[] effects;

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
		putArray(effects, (stream, effect) -> {
			stream.putVarInt(effect.id);
			stream.putLFloat(effect.red);
			stream.putLFloat(effect.green);
			stream.putLFloat(effect.blue);
			stream.putString(effect.name);
			stream.putString(effect.icon);
		});
	}

	public record ModEffect(
			int id,
			float red,
			float green,
			float blue,
			String name,
			String icon
	) {
	}
}
