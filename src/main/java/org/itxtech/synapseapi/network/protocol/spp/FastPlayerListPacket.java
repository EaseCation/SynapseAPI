package org.itxtech.synapseapi.network.protocol.spp;

import java.util.UUID;

/**
 * @author Nukkit Project Team
 */
public class FastPlayerListPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.FAST_PLAYER_LIST_PACKET;

    public static final byte TYPE_ADD = 0;
    public static final byte TYPE_REMOVE = 1;

    public UUID sendTo;
    public byte type;
    public FastPlayerListPacket.Entry[] entries = new FastPlayerListPacket.Entry[0];

    @Override
    public void decode() {
        this.sendTo = this.getUUID();
        this.type = (byte) this.getByte();
        int len = (int) this.getUnsignedVarInt();
        this.entries = new FastPlayerListPacket.Entry[len];
        if (this.type == TYPE_ADD) {
            for (int i = 0; i < len; i++) {
                this.entries[i] = new Entry(this.getUUID(), this.getUnsignedVarLong(), this.getString());
            }
        } else {
            for (int i = 0; i < len; i++) {
                this.entries[i] = new Entry(this.getUUID());
            }
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putUUID(this.sendTo);
        this.putByte(this.type);
        this.putUnsignedVarInt(this.entries.length);
        if (type == TYPE_ADD) {
            for (FastPlayerListPacket.Entry entry : this.entries) {
                this.putUUID(entry.uuid);
                this.putUnsignedVarLong(entry.entityId);
                this.putString(entry.name);
            }
        } else {
            for (FastPlayerListPacket.Entry entry : this.entries) {
                this.putUUID(entry.uuid);
            }
        }
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    public static class Entry {

        public final UUID uuid;
        public final long entityId;
        public final String name;

        public Entry(UUID uuid) {
            this(uuid, 0, "");
        }

        public Entry(UUID uuid, long entityId, String name) {
            this.uuid = uuid;
            this.entityId = entityId;
            this.name = name;
        }
    }

}
