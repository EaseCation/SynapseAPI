package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;

public class ResourcePackClientResponsePacket16 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET;

    public static final byte STATUS_REFUSED = 1;
    public static final byte STATUS_SEND_PACKS = 2;
    public static final byte STATUS_HAVE_ALL_PACKS = 3;
    public static final byte STATUS_COMPLETED = 4;

    public byte responseStatus;
    public Entry[] packEntries;

    @Override
    public void decode() {
        this.responseStatus = (byte) this.getByte();
        this.packEntries = new Entry[this.getLShort()];
        for (int i = 0; i < this.packEntries.length; i++) {
            String[] entry = this.getString().split("_");
            this.packEntries[i] = new Entry(entry[0], entry[1]);
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putByte(this.responseStatus);
        this.putLShort(this.packEntries.length);
        for (Entry entry : this.packEntries) {
            this.putString(entry.uuid + '_' + entry.version);
        }
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static class Entry {
        public final String uuid;
        public final String version;

        public Entry(String uuid, String version) {
            this.uuid = uuid;
            this.version = version;
        }
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.ResourcePackClientResponsePacket.class;
    }
}
