package org.itxtech.synapseapi.multiprotocol.protocol12160.protocol;

import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ItemRegistryPacket12160 extends Packet12160 {
    public static final int NETWORK_ID = ProtocolInfo.ITEM_REGISTRY_PACKET;

    public ItemData[] entries = new ItemData[0];

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
        for (ItemData entry : entries) {
            this.putString(entry.name);
            this.putLShort(entry.id);
            this.putBoolean(entry.componentBased);
            this.putVarInt(entry.version);
            this.put(entry.components);
        }
    }

    @ToString(exclude = "components")
    public static class ItemData {
        public String name;
        public int id;
        public boolean componentBased;
        public int version;
        /**
         * CompoundTag.
         */
        public byte[] components;

        public ItemData(String name, int id) {
            this(name, id, false, 0, CompoundTag.EMPTY);
        }

        public ItemData(String name, int id, byte[] components) {
            this(name, id, true, 0, components);
        }

        public ItemData(String name, int id, int version, byte[] components) {
            this(name, id, true, version, components);
        }

        public ItemData(String name, int id, boolean componentBased, int version, byte[] components) {
            this.name = name;
            this.id = id;
            this.componentBased = componentBased;
            this.version = version;
            this.components = components;
        }
    }
}
