package org.itxtech.synapseapi.multiprotocol.protocol11980.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;

/**
 * This packet is only sent by the server when client-side chunk generation is enabled in vanilla.
 * It contains NBT data for biomes, similar to the BiomeDefinitionListPacket,
 * but with a large amount of extra data for client-side chunk generation.
 */
public class CompressedBiomeDefinitionListPacket11980 extends Packet11980 {
    public static final int NETWORK_ID = ProtocolInfo.COMPRESSED_BIOME_DEFINITION_LIST_PACKET;

    public static final byte[] MAGIC = new byte[]{0x43, 0x4f, 0x4d, 0x50, 0x52, 0x45, 0x53, 0x53, 0x45, 0x44};

    /**
     * The data is compressed with a cursed home-brewed compression format :(
     */
    public byte[] payload;

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
        putByteArray(payload);
    }
}
