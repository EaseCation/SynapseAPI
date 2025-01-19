package org.itxtech.synapseapi.multiprotocol.protocol12160.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
public class CreativeContentPacket12160 extends Packet12160 {
    public static final int NETWORK_ID = ProtocolInfo.CREATIVE_CONTENT_PACKET;

    public Group[] groups = new Group[0];
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

        this.putUnsignedVarInt(groups.length);
        for (Group group : groups) {
            this.putLInt(group.category);
            this.putString(group.name);
            this.putItemInstance(group.icon);
        }

        this.putUnsignedVarInt(entries.length);
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            this.putUnsignedVarInt(i + 1); // netId
            this.putItemInstance(entry.item);
            this.putUnsignedVarInt(entry.groupIndex);
        }
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Group {
        public static final int CATEGORY_CONSTRUCTION = 1;
        public static final int CATEGORY_NATURE = 2;
        public static final int CATEGORY_EQUIPMENT = 3;
        public static final int CATEGORY_ITEMS = 4;
        public static final int CATEGORY_ITEM_COMMAND_ONLY = 5;

        public int category;
        public String name;
        public Item icon;
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        public Item item;
        public int groupIndex;
    }
}
