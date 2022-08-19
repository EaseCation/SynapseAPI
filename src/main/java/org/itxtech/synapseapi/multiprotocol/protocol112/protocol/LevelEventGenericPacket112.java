package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventGenericPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.io.IOException;
import java.nio.ByteOrder;

@ToString
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

        if (this.tag != null) {
            this.tag.getTags().forEach((name, tag) -> {
                byte[] nbt;
                try {
                    nbt = NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.put(nbt);
            });
        }
        this.putByte((byte) 0); // End tag
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, LevelEventGenericPacket.class);

        LevelEventGenericPacket packet = (LevelEventGenericPacket) pk;
        this.eventId = packet.eventId;
        this.tag = packet.tag;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return LevelEventGenericPacket.class;
    }
}
