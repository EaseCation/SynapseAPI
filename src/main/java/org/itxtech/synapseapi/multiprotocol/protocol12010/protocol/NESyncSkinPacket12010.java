package org.itxtech.synapseapi.multiprotocol.protocol12010.protocol;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

import java.util.UUID;

@ToString
public class NESyncSkinPacket12010 extends Packet12010 {
	public static final int NETWORK_ID = ProtocolInfo.PACKET_SYNC_SKIN;

	public Entry[] entries;
	public Skin skin;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	@Override
	public void decode() {
		entries = getArray(new Entry[0], stream -> {
			Entry entry = new Entry();
			entry.confirmed = stream.getBoolean();
			entry.uuid = stream.getUUID();
			entry.skinBytes = stream.getByteArray();
			return entry;
		});
        for (Entry entry : entries) {
            entry.udid = getString();
        }
        for (Entry entry : entries) {
            entry.extraData = getString();
        }
        for (Entry entry : entries) {
            entry.iid = getString();
        }
		skin = getSkin();
	}

	@Override
	public void encode() {
		reset();
		putUnsignedVarInt(entries.length);
		for (Entry entry : entries) {
			putBoolean(entry.confirmed);
			putUUID(entry.uuid);
			putByteArray(entry.skinBytes);
		}
		for (Entry entry : entries) {
			putString(entry.udid);
		}
		for (Entry entry : entries) {
			putString(entry.extraData);
		}
		for (Entry entry : entries) {
			putString(entry.iid);
		}
		putSkin(skin);
	}

	@ToString
	public static class Entry {
		public boolean confirmed;
		public UUID uuid;
		public byte[] skinBytes = new byte[0];
		public String udid = "";
		public String extraData = "";
		public String iid = "";
	}
}
