package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.common.io.ByteStreams;
import org.itxtech.synapseapi.SynapseAPI;

import java.io.InputStream;

public class BiomeDefinitionListPacket18 extends Packet18 {
    public static final byte NETWORK_ID = ProtocolInfo.BIOME_DEFINITION_LIST_PACKET;

    private static final byte[] TAG;

    static {
        try {
            InputStream inputStream = SynapseAPI.class.getClassLoader().getResourceAsStream("biome_definitions112.dat");
            if (inputStream == null) {
                throw new AssertionError("Could not find biome_definitions.dat");
            }
            //noinspection UnstableApiUsage
            TAG = ByteStreams.toByteArray(inputStream);
        } catch (Exception e) {
            throw new AssertionError("Error whilst loading biome_definitions.dat", e);
        }
    }

    public byte[] tag = TAG;

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
        this.put(tag);
    }
}
