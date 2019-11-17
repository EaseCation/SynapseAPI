package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;

import java.io.IOException;
import java.nio.ByteOrder;

public class LevelEventGenericPacket112 extends Packet112 {
    public static final int NETWORK_ID = ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET;

    public int eventId;
    public CompoundTag tag;

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
        this.putVarInt(eventId);
        try {
            this.put(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
