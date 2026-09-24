package org.itxtech.synapseapi.multiprotocol.protocol12010.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEChangeBiomePacket12010 extends Packet12010 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_CHANGE_BIOME;

	public String biome;
	public float minSnowAccumulation;
	public float maxSnowAccumulation;
	public float temperature;
	public float downfall;
	public boolean rain;

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
		putString(biome);
		putLFloat(minSnowAccumulation);
		putLFloat(maxSnowAccumulation);
		putLFloat(temperature);
		putLFloat(downfall);
		putBoolean(rain);
	}
}
