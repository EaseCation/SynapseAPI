package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Utils;
import lombok.ToString;

/**
 * This packet contains a copy of the behavior pack jigsaw structure rules.
 * Sends the serialized jigsaw rule JSON to the client as it's needed on both the client and server.
 */
@ToString(exclude = "nbt")
public class JigsawStructureDataPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.JIGSAW_STRUCTURE_DATA_PACKET;

    private static final byte[] DEFAULT = Utils.make(() -> {
        try {
            return NBTIO.writeNetwork(new CompoundTag()
                    .putList("jigsaws", new ListTag<>())
                    .putList("processors", new ListTag<>())
                    .putList("structure_sets", new ListTag<>())
                    .putList("template_pools", new ListTag<>())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });

    /**
     * CompoundTag.
     */
    public byte[] nbt = DEFAULT;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        put(nbt);
    }
}
