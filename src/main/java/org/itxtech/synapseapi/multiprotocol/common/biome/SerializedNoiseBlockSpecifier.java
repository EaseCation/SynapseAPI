package org.itxtech.synapseapi.multiprotocol.common.biome;

import cn.nukkit.block.Block;
import cn.nukkit.math.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SerializedNoiseBlockSpecifier {
    public String noise;
    public float threshold;
    public Vector2f range;
    public Block block;
}
