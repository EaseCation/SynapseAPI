package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
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
        this.packEntries = this.getArrayLShort(Entry.class, stream -> {
            String[] entry = this.getString().split("_", 3);
            return new Entry(entry[0], entry[1]);
        });
    }

    @Override
    public void encode() {
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @ToString
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
