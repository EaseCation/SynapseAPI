package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NESetDimensionLocalWeatherPacket16 extends Packet16 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_SET_DIMENSION_LOCAL_WEATHER;

	public boolean useLocalWeather;
	public float rainLevel;
	public int rainTime;
	public float lightningLevel;
	public int lightningTime;
	public boolean doWeatherCycle;

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
		putBoolean(useLocalWeather);
		putLFloat(rainLevel);
		putVarInt(rainTime);
		putLFloat(lightningLevel);
		putVarInt(lightningTime);
		putBoolean(doWeatherCycle);
	}
}
