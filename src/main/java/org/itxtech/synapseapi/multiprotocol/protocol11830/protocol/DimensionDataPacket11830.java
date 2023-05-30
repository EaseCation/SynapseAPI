package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.RequiredArgsConstructor;

/**
 * Data-driven dimensions.
 * currently only "minecraft:overworld" is supported by behavior pack dimension data.
 * see DimensionManager.cpp, DimensionDefinitionGroup.cpp, DimensionDocument.cpp
 */
public class DimensionDataPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.DIMENSION_DATA_PACKET;

    public static final DimensionDefinition VANILLA_OVERWORLD = new DimensionDefinition("minecraft:overworld", 319, -64, Generator.TYPE_INFINITE);
    public static final DimensionDefinition VANILLA_NETHER = new DimensionDefinition("minecraft:nether", 127, 0, Generator.TYPE_NETHER);
    public static final DimensionDefinition VANILLA_THE_END = new DimensionDefinition("minecraft:the_end", 255, 0, Generator.TYPE_END);

    public DimensionDefinition[] definitions = new DimensionDefinition[0];

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

        putUnsignedVarInt(definitions.length);
        for (DimensionDefinition definition : definitions) {
            putString(definition.identifier);
            putVarInt(definition.maximumHeight);
            putVarInt(definition.minimumHeight);
            putVarInt(definition.generatorType);
        }
    }

    /**
     * see DimensionDefinitionGroup::DimensionDefinition
     */
    @RequiredArgsConstructor
    public static class DimensionDefinition {
        public final String identifier;

        public final int maximumHeight;
        public final int minimumHeight;
        public final int generatorType;
    }
}
