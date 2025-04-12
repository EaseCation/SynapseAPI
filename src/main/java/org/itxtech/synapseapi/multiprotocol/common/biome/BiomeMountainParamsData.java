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
public class BiomeMountainParamsData {
    public Block steepBlock;
    public boolean northSlopes;
    public boolean southSlopes;
    public boolean westSlopes;
    public boolean eastSlopes;
    public boolean topSlideEnabled;
}
