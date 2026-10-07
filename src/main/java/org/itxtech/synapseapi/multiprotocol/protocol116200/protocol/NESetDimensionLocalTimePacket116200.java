package org.itxtech.synapseapi.multiprotocol.protocol116200.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NESetDimensionLocalTimePacket116200 extends Packet116200 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_SET_DIMENSION_LOCAL_TIME;

	public boolean useLocalTime;
	public int localTime;
	public boolean doDayNightCycle;

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
		if (!useLocalTime) {
			return;
		}
		putVarInt(localTime);
		putBoolean(doDayNightCycle);
	}
}
