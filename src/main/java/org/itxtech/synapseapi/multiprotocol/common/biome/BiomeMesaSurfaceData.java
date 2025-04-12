package org.itxtech.synapseapi.multiprotocol.common.biome;

import cn.nukkit.block.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeMesaSurfaceData {
    public Block clayMaterial;
    public Block hardClayMaterial;
    public boolean brycePillars;
    public boolean hasForest;
}
