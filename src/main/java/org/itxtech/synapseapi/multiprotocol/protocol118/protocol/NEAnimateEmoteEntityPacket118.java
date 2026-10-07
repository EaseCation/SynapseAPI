package org.itxtech.synapseapi.multiprotocol.protocol118.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NEAnimateEmoteEntityPacket118 extends Packet118 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_ANIMATE_EMOTE_ENTITY;

	public String animation;
	public String nextState;
	public String stopExpression;
	public int stopExpressionVersion;
	public String controller;
	public float blendOutTime;
	public long[] entityRuntimeIds;

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
		putString(animation);
		putString(nextState);
		putString(stopExpression);
		putLInt(stopExpressionVersion);
		putString(controller);
		putLFloat(blendOutTime);
		putUnsignedVarInt(entityRuntimeIds.length);
		for (long entityRuntimeId : entityRuntimeIds) {
			putEntityRuntimeId(entityRuntimeId);
		}
	}
}
