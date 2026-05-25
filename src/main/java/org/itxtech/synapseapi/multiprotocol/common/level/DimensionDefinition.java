package org.itxtech.synapseapi.multiprotocol.common.level;

import cn.nukkit.level.DimensionFullNames;
import cn.nukkit.level.DimensionID;
import cn.nukkit.level.generator.GeneratorID;
import cn.nukkit.utils.Utils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DimensionDefinition {
    public static final DimensionDefinition OVERWORLD = new DimensionDefinition(DimensionFullNames.OVERWORLD, 320, -64, GeneratorID.OVERWORLD, DimensionID.OVERWORLD);
    /**
     * 0-128
     */
    public static final DimensionDefinition NETHER = new DimensionDefinition(DimensionFullNames.NETHER, 0, 0, GeneratorID.NETHER, DimensionID.NETHER);
    /**
     * 0-256
     */
    public static final DimensionDefinition THE_END = new DimensionDefinition(DimensionFullNames.THE_END, 0, 0, GeneratorID.THE_END, DimensionID.THE_END);

    public static final int NETEASE_BUILTIN_DIMENSION_FIRST = 3;
    public static final int NETEASE_BUILTIN_DIMENSION_LAST = 20;

    /**
     * -64~320
     * @since 1.26.30
     */
    public static final DimensionDefinition[] BUILTIN_DIMENSIONS = Utils.make(() -> {
        DimensionDefinition[] netease = new DimensionDefinition[NETEASE_BUILTIN_DIMENSION_LAST - NETEASE_BUILTIN_DIMENSION_FIRST + 1];
        for (int i = 0; i < netease.length; i++) {
            netease[i] = new DimensionDefinition("netease:dm" + (i + NETEASE_BUILTIN_DIMENSION_FIRST), OVERWORLD.maximumHeight, OVERWORLD.minimumHeight, GeneratorID.VOID, i + DimensionID.CUSTOM_DIMENSION);
        }
        return netease;
    });

    public final String identifier;

    /**
     * -512~512, multiples of 16.
     */
    public final int maximumHeight;
    /**
     * -512~512, multiples of 16.
     */
    public final int minimumHeight;
    public final int generatorType;
    public final int dimensionType;
}
