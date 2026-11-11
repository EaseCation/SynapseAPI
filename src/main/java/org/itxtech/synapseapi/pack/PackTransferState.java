package org.itxtech.synapseapi.pack;

import cn.nukkit.resourcepacks.ResourcePack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * 跨服时传输到目标服务器当前玩家安装上的资源包.
 */
@Log4j2
public final class PackTransferState {
    public static void write(JsonObject extra, Map<String, ResourcePack> resourcePacks, Map<String, ResourcePack> behaviourPacks, @Nullable JsonObject previousExtra) {
        writePacks(extra, "res_packs", resourcePacks, previousExtra);
        writePacks(extra, "beh_packs", behaviourPacks, previousExtra);
    }

    private static void writePacks(JsonObject extra, String key, Map<String, ResourcePack> packs, @Nullable JsonObject previousExtra) {
        JsonObject previousSelections = previousExtra == null ? null : previousExtra.getAsJsonObject(key);
        JsonObject selections = new JsonObject();
        for (Map.Entry<String, ResourcePack> entry : packs.entrySet()) {
            String id = entry.getKey();
            ResourcePack pack = entry.getValue();
            if (pack != null) {
                JsonObject selection = new JsonObject();
                selection.addProperty("version", pack.getPackVersion());
                selection.addProperty("subpack", pack.getSubPackName());
                selections.add(id, selection);
            } else if (previousSelections != null && previousSelections.has(id)) {
                selections.add(id, previousSelections.get(id).deepCopy());
            } else {
                throw new IllegalStateException("Missing transferred resource pack state: " + id);
            }
        }
        extra.add(key, selections);
    }

    public static void restore(@Nullable JsonObject extra, Map<String, ResourcePack> resourcePacks, Map<String, ResourcePack> behaviourPacks, Function<String, ResourcePack> resolver) {
        resourcePacks.clear();
        behaviourPacks.clear();
        if (extra == null) {
            return;
        }
        restorePacks(extra, "res_packs", resourcePacks, resolver);
        restorePacks(extra, "beh_packs", behaviourPacks, resolver);
    }

    private static void restorePacks(JsonObject extra, String key, Map<String, ResourcePack> packs, Function<String, ResourcePack> resolver) {
        JsonObject selections = extra.getAsJsonObject(key);
        if (selections == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : selections.entrySet()) {
            String id = entry.getKey();
            JsonObject selection = entry.getValue().getAsJsonObject();
            String version = selection.get("version").getAsString();
            String subpack = selection.get("subpack").getAsString();
            ResourcePack pack = resolver.apply(id);
            if (pack != null) {
                if (!pack.getPackVersion().equals(version) || !subpack.isEmpty() && pack.getSubPacks().stream()
                        .noneMatch(candidate -> candidate.folderName().equals(subpack))) {
                    log.debug("Cannot resolve transferred resource pack {} version {} subpack {} against local version {}", id, version, subpack, pack.getPackVersion());
                    pack = null;
                } else {
                    pack = pack.withSubPack(subpack);
                }
            }
            packs.put(id, pack);
        }
    }

    private PackTransferState() {
    }
}
