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
public class BiomeSurfaceMaterialData {
    public Block topBlock;
    public Block midBlock;
    public Block seaFloorBlock;
    public Block foundationBlock;
    public Block seaBlock;
    public int seaFloorDepth;
}
