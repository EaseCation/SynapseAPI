package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

import java.io.IOException;

public final class EntityProperties {
    /**
     * @since 1.19.70
     */
    public static final byte[] BEE;
    /**
     * @since 1.20.80
     */
    public static final byte[] ARMADILLO;

    static {
        try {
            BEE = NBTIO.writeNetwork(new CompoundTag()
                    .putString("type", "minecraft:bee")
                    .putList(new ListTag<>("properties")
                            .add(new CompoundTag()
                                    .putString("name", "minecraft:has_nectar")
                                    .putInt("type", 2)
                            )
                    )
            );

            ARMADILLO = NBTIO.writeNetwork(new CompoundTag()
                    .putString("type", "minecraft:armadillo")
                    .putList("properties", new ListTag<>()
                            .addCompound(new CompoundTag()
                                    .putString("name", "minecraft:armadillo_state")
                                    .putInt("type", 3)
                                    .putList("enum", new ListTag<>()
                                            .addString("unrolled")
                                            .addString("rolled_up")
                                            .addString("rolled_up_peeking")
                                            .addString("rolled_up_relaxing")
                                            .addString("rolled_up_unrolling")
                                    )
                            )
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
    }

    private EntityProperties() {
        throw new IllegalStateException();
    }
}
