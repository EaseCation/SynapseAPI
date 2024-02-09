package org.itxtech.synapseapi.multiprotocol.protocol12060.protocol;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerListPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.Packet116100;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ToString
public class PlayerListPacket12060 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_LIST_PACKET;

    public static final byte TYPE_ADD = 0;
    public static final byte TYPE_REMOVE = 1;

    public byte type;
    public Entry[] entries = new Entry[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.type);
        this.putUnsignedVarInt(this.entries.length);
        for (Entry entry : this.entries) {
            this.putUUID(entry.uuid);
            if (type == TYPE_ADD) {
                this.putVarLong(entry.entityId);
                this.putString(entry.name);
                this.putString(entry.xboxUserId);
                this.putString(entry.platformChatId);
                this.putLInt(entry.buildPlatform);
                this.putSkin(entry.skin);
                this.putBoolean(entry.isTeacher);
                this.putBoolean(entry.isHost);
                this.putBoolean(entry.isSubClient);
            }
        }
        if (type == TYPE_ADD) {
            for (Entry entry : this.entries) { // Biggest wtf
                this.putBoolean(entry.skin.isTrusted());
            }
        }
    }

    @ToString
    public static class Entry {
        public final UUID uuid;
        public long entityId = 0;
        public String name = "";
        public String xboxUserId = ""; //TODO
        public String platformChatId = ""; //TODO
        public int buildPlatform = -1;
        public Skin skin;
        public boolean isTeacher;
        public boolean isHost;
        public boolean isSubClient;

        public Entry(UUID uuid) {
            this.uuid = uuid;
        }

        public Entry(UUID uuid, long entityId, String name, Skin skin) {
            this(uuid, entityId, name, skin, "");
        }

        public Entry(UUID uuid, long entityId, String name, Skin skin, String xboxUserId) {
            this.uuid = uuid;
            this.entityId = entityId;
            this.name = name;
            this.skin = skin;
            this.xboxUserId = xboxUserId == null ? "" : xboxUserId;
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, PlayerListPacket.class);
        PlayerListPacket packet = (PlayerListPacket) pk;
        this.type = packet.type;
        List<Entry> entries = new ArrayList<>();
        for (PlayerListPacket.Entry entry: packet.entries) {
            Entry e = new Entry(entry.uuid, entry.entityId, entry.name, entry.skin, entry.xboxUserId);
            entries.add(e);
        }
        this.entries = entries.toArray(new Entry[0]);
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return PlayerListPacket.class;
    }
}
