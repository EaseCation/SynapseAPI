package org.itxtech.synapseapi.filtertext;

import cn.nukkit.Player;

import javax.annotation.Nullable;

@FunctionalInterface
public interface TextFilter {
    @Nullable
    String filter(String text, Player player);

    default boolean isChatOnly() {
        return false;
    }
}
