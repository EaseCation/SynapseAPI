package org.itxtech.synapseapi.filtertext;

import cn.nukkit.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class TextFilterUtil {
    public static Map<String, String> readWordList(InputStream stream, boolean base64) {
        Map<String, String> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(base64 ? Base64.getDecoder().wrap(stream) : stream))) {
            String word;
            while ((word = reader.readLine()) != null) {
                if (word.isBlank()) {
                    continue;
                }
                result.put(word, "#".repeat(word.length()));
            }
        } catch (IOException e) {
            Server.getInstance().getLogger().error("Unable to load word list", e);
        }
        return result;
    }

    private TextFilterUtil() {
    }
}
