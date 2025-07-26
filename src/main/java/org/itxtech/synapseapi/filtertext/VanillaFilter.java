package org.itxtech.synapseapi.filtertext;

import cn.nukkit.Player;
import org.itxtech.synapseapi.SynapseAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VanillaFilter implements TextFilter {
    private static final VanillaFilter INSTANCE = new VanillaFilter(TextFilterUtil.readWordList(SynapseAPI.getInstance().getResource("profanity_filter.wlist"), true));

    private final Map<String, String> words = new HashMap<>();

    public VanillaFilter(Map<String, String> words) {
        this.words.putAll(words);
    }

    @Override
    public String filter(String text, Player player) {
        boolean filtered = false;
        String[] split = text.split(" ", 512);
        WORD:
        for (int i = 0; i < split.length; i++) {
            for (Entry<String, String> entry : words.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(split[i])) {
                    split[i] = entry.getValue();
                    filtered = true;
                    continue WORD;
                }
            }
        }
        if (!filtered) {
            return text;
        }
        return String.join(" ", split);
    }

    public static VanillaFilter getInstance() {
        return INSTANCE;
    }
}
