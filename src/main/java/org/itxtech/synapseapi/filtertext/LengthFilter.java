package org.itxtech.synapseapi.filtertext;

import cn.nukkit.Player;

public class LengthFilter implements TextFilter {
    @Override
    public String filter(String text, Player player) {
        if (text.length() > 512 * 3) {
            return null;
        }
        return text;
    }

    @Override
    public boolean isChatOnly() {
        return true;
    }
}
