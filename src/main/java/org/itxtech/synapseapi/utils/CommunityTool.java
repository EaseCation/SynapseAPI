package org.itxtech.synapseapi.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.stream.NBTOutputStream;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.block.BlockPalette.BlockData;
import org.itxtech.synapseapi.multiprotocol.utils.block.RuntimeBlockMapper;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

/**
 * 生成数据用于阿尔尼亚RPG服的<a href="https://github.com/NetEasePE/Nukkit">社区旧版Nukkit</a>
 */
public final class CommunityTool {
    public static void remapResourcesToNetease(Path dirPath, int protocol) throws IOException {
        // block palette

        ListTag<CompoundTag> tag;
        try (InputStream stream = Files.newInputStream(dirPath.resolve("runtime_block_states.dat"))) {
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new BufferedInputStream(new GZIPInputStream(stream)), ByteOrder.BIG_ENDIAN, false);
        }

        BlockPalette netease = RuntimeBlockMapper.PALETTES.get(AbstractProtocol.fromRealProtocol(protocol))[1];
        for (CompoundTag entry : tag) {
            String name = entry.getString("name");
            CompoundTag states = entry.getCompound("states");
            netease.palette.stream()
                    .filter(data -> name.equals(data.name) && states.equalsTags(data.states))
                    .findFirst()
                    .ifPresent(data -> entry.putInt("runtimeId", data.runtimeId));
        }

        GzipParameters parameters = new GzipParameters();
        parameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
        try (NBTOutputStream stream = new NBTOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(dirPath.resolve("runtime_block_states.nbt")), parameters), ByteOrder.BIG_ENDIAN, false)) {
            Tag.writeNamedTag(tag, stream);
        }

        // creative content

        Map<String, List<Map<String, Object>>> creative = JsonUtil.TRUSTED_JSON_MAPPER.readValue(Files.newInputStream(dirPath.resolve("creative_items.json")), new TypeReference<>() {
        });

        BlockPalette microsoft = RuntimeBlockMapper.PALETTES.get(AbstractProtocol.fromRealProtocol(protocol))[0];
        for (Map<String, Object> entry : creative.get("items")) {
            if (!entry.containsKey("blockRuntimeId")) {
                continue;
            }
            BlockData block = microsoft.palette.get(((Number) entry.get("blockRuntimeId")).intValue());
            netease.palette.stream()
                    .filter(data -> block.name.equals(data.name) && block.states.equalsTags(data.states))
                    .findFirst()
                    .ifPresent(data -> entry.put("blockRuntimeId", data.runtimeId));
        }

        JsonUtil.PRETTY_JSON_MAPPER.writeValue(Files.newOutputStream(dirPath.resolve("creativeitems.json")), creative);
    }

    private CommunityTool() {
        throw new IllegalStateException();
    }
}
