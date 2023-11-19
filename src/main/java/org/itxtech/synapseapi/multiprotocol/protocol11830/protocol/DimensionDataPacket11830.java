package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.RequiredArgsConstructor;

/**
 * Data-driven dimensions.
 * 添加自定义维度目前仅在内部开发版本可用, 零售版本暂时仅支持修改主世界的高度和已存在维度的生成器类型为虚空.
 * see DimensionManager.cpp, DimensionDefinitionGroup.cpp, DimensionDocument.cpp
 */
public class DimensionDataPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.DIMENSION_DATA_PACKET;

    public static final DimensionDefinition VANILLA_OVERWORLD = new DimensionDefinition("minecraft:overworld", 320, -64, Generator.TYPE_INFINITE);
    /**
     * 暂不支持修改高度, 高低边界需全设为0.
     */
    public static final DimensionDefinition VANILLA_NETHER = new DimensionDefinition("minecraft:nether", 128, 0, Generator.TYPE_NETHER);
    /**
     * 暂不支持修改高度, 高低边界需全设为0.
     */
    public static final DimensionDefinition VANILLA_THE_END = new DimensionDefinition("minecraft:the_end", 256, 0, Generator.TYPE_END);

    /**
     * 暂不支持添加自定义维度.
     */
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

        /**
         * -512~512, multiples of 16.
         */
        public final int maximumHeight;
        /**
         * -512~512, multiples of 16.
         */
        public final int minimumHeight;
        /**
         * 暂时仅支持虚空生成器.
         */
        public final int generatorType;
    }
}
