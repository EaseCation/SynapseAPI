package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * Created on 15-10-13.
 */
@ToString
public class TextPacket14 extends Packet14 {

	public static final int NETWORK_ID = ProtocolInfo.TEXT_PACKET;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public static final byte TYPE_RAW = 0;
	public static final byte TYPE_CHAT = 1;
	public static final byte TYPE_TRANSLATION = 2;
	public static final byte TYPE_POPUP = 3;
	public static final byte JUKE_BOX_POPUP = 4;
	public static final byte TYPE_TIP = 5;
	public static final byte TYPE_SYSTEM = 6;
	public static final byte TYPE_WHISPER = 7;
	public static final byte TYPE_ANNOUNCEMENT = 8;

	public byte type;
	public boolean isLocalized = false;

	public String message = "";
	public String[] parameters = new String[0];
	public String primaryName = "";
	public String thirdPartyName = "";
	public int platformId = -1;

	public String sendersXUID = "";
	public String platformIdString = "";

	@Override
	public void decode() {
		this.type = (byte) getByte();
		this.isLocalized = this.getBoolean();

		switch (this.type) {
		case TYPE_RAW:
			message = getString();
			break;
		case TYPE_CHAT:
			primaryName = getString();
			thirdPartyName = getString();
			platformId = getVarInt();
			message = getString();
			break;
		case TYPE_TRANSLATION:
			message = getString();
			parameters = getArray(String.class, BinaryStream::getString);
			break;
		case TYPE_POPUP:
			message = getString();
			parameters = getArray(String.class, BinaryStream::getString);
			break;
		case JUKE_BOX_POPUP:
			message = getString();
			parameters = getArray(String.class, BinaryStream::getString);
			break;
		case TYPE_TIP:
			message = getString();
			break;
		case TYPE_SYSTEM:
			message = getString();
			break;
		case TYPE_WHISPER:
			primaryName = getString();
			thirdPartyName = getString();
			platformId = getVarInt();
			message = getString();
			break;
		case TYPE_ANNOUNCEMENT:
			primaryName = getString();
			thirdPartyName = getString();
			platformId = getVarInt();
			message = getString();
			break;

		default:
			break;
		}
		sendersXUID = getString();
		platformIdString = getString();
	}

	@Override
	public void encode() {
		this.reset();
		this.putByte(this.type);
		this.putBoolean(this.isLocalized);
		switch (this.type) {
		case TYPE_RAW:
			this.putString(message);
			break;
		case TYPE_CHAT:
			this.putString(primaryName);
			this.putString(thirdPartyName);
			this.putVarInt(platformId);
			this.putString(message);
			break;
		case TYPE_TRANSLATION:
			this.putString(this.message);
			this.putUnsignedVarInt(this.parameters.length);
			for (String parameter : this.parameters) {
				this.putString(parameter);
			}
			break;
		case TYPE_POPUP:
			this.putString(this.message);
			this.putUnsignedVarInt(this.parameters.length);
			for (String parameter : this.parameters) {
				this.putString(parameter);
			}
			break;
		case JUKE_BOX_POPUP:
			this.putString(this.message);
			this.putUnsignedVarInt(this.parameters.length);
			for (String parameter : this.parameters) {
				this.putString(parameter);
			}
			break;
		case TYPE_TIP:
			this.putString(message);
			break;
		case TYPE_SYSTEM:
			this.putString(this.message);
			break;
		case TYPE_WHISPER:
			this.putString(primaryName);
			this.putString(thirdPartyName);
			this.putVarInt(platformId);
			this.putString(message);
			break;
		case TYPE_ANNOUNCEMENT:
			this.putString(primaryName);
			this.putString(thirdPartyName);
			this.putVarInt(platformId);
			this.putString(message);
			break;

		default:
			break;
		}
		this.putString(sendersXUID);
		this.putString(platformIdString);
	}

	@Override
	public DataPacket fromDefault(DataPacket pk) {
		ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.TextPacket.class);
		cn.nukkit.network.protocol.TextPacket packet = (cn.nukkit.network.protocol.TextPacket) pk;
		this.type = packet.type;
		this.isLocalized = packet.isLocalized;

		this.message = packet.message;
		this.parameters = packet.parameters;
		this.primaryName = packet.primaryName;

		this.sendersXUID = packet.sendersXUID;
		this.platformIdString = packet.platformIdString;
		return this;
	}

	public static Class<? extends DataPacket> getDefaultPacket() {
		return cn.nukkit.network.protocol.TextPacket.class;
	}

}
