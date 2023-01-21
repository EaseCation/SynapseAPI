package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
public class ItemComponentPacket116100 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.ITEM_COMPONENT_PACKET;

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
        this.putUnsignedVarInt(entries.length);
        for (Entry entry : entries) {
            this.putString(entry.name);
            this.put(entry.tag);
        }
    }

    @ToString(exclude = "tag")
    @AllArgsConstructor
    public static class Entry {
        public String name;
        /**
         * CompoundTag.
         */
        public byte[] tag;
    }
}
