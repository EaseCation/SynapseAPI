package org.itxtech.synapseapi.multiprotocol.common.biome;

import cn.nukkit.block.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeCappedSurfaceData {
    public Collection<Block> floorBlocks;
    public Collection<Block> ceilingBlocks;
    @Nullable
    public Block seaBlock;
    @Nullable
    public Block foundationBlock;
    @Nullable
    public Block beachBlock;
}
