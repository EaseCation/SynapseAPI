package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;

import java.io.IOException;

public final class EntityProperties {
    public static final byte[] BEE;

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
