package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CreativeContentPacket116 extends Packet116 {

    public static final int NETWORK_ID = ProtocolInfo.CREATIVE_CONTENT_PACKET;

    public Item[] entries = new Item[0];

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
        for (int i = 0; i < entries.length; i++) {
            this.putUnsignedVarInt(i + 1);
            this.putItemInstance(entries[i]);
        }
    }
}
