package org.itxtech.synapseapi.multiprotocol.common.biome;

import cn.nukkit.block.Block;
import it.unimi.dsi.fastutil.floats.FloatList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BiomeNoiseGradientSurfaceData {
    public Collection<Block> nonReplaceableBlocks;
    public Collection<Block> gradientBlocks;
    public String noiseSeedString;
    public int firstOctave;
    public FloatList amplitudes;
}
