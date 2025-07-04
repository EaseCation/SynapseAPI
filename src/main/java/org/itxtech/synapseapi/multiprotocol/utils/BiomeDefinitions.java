package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.DataPacket;
import com.google.common.io.ByteStreams;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.biome.BiomeDefinitionData;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.BiomeDefinitionListPacket12180;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.BiomeDefinitionListPacket18;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.Deflater;

@Log4j2
public final class BiomeDefinitions {
    private static final boolean ENABLE_CLIENT_CHUNK_GEN = false;

    private static final Map<AbstractProtocol, CompoundTag> data = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, BatchPacket[]> PACKETS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading biome definitions...");

        try {
/*
            //TODO: 1.8-1.11
            CompoundTag data112 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions112.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data116 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data116210 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116210.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11740 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11740.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data118 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions118.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11810 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11810.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data119 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions119.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11920 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11920.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11930 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11930.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11940 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11940.nbt")), ByteOrder.LITTLE_ENDIAN, true);
*/
            CompoundTag data11980 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11980.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12120 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12120.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12140 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12140.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12150 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12150.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12160 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12160.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12170 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12170.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12180 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions12180.nbt")), ByteOrder.LITTLE_ENDIAN, true);

/*
            data.put(AbstractProtocol.PROTOCOL_112, data112);
            data.put(AbstractProtocol.PROTOCOL_113, data112);
            data.put(AbstractProtocol.PROTOCOL_114, data112);
            data.put(AbstractProtocol.PROTOCOL_114_60, data112);
            data.put(AbstractProtocol.PROTOCOL_116, data116);
            data.put(AbstractProtocol.PROTOCOL_116_20, data116);
            data.put(AbstractProtocol.PROTOCOL_116_100_NE, data116);
            data.put(AbstractProtocol.PROTOCOL_116_100, data116);
            data.put(AbstractProtocol.PROTOCOL_116_200, data116);
            data.put(AbstractProtocol.PROTOCOL_116_210, data116210);
            data.put(AbstractProtocol.PROTOCOL_116_220, data116210);
            data.put(AbstractProtocol.PROTOCOL_117, data116210);
            data.put(AbstractProtocol.PROTOCOL_117_10, data116210);
            data.put(AbstractProtocol.PROTOCOL_117_30, data116210);
            data.put(AbstractProtocol.PROTOCOL_117_40, data11740);
            data.put(AbstractProtocol.PROTOCOL_118, data118);
            data.put(AbstractProtocol.PROTOCOL_118_10, data11810);
            data.put(AbstractProtocol.PROTOCOL_118_30, data11810);
            data.put(AbstractProtocol.PROTOCOL_118_30_NE, data11920);
            data.put(AbstractProtocol.PROTOCOL_119, data119);
            data.put(AbstractProtocol.PROTOCOL_119_10, data119);
            data.put(AbstractProtocol.PROTOCOL_119_20, data11920);
            data.put(AbstractProtocol.PROTOCOL_119_21, data11920);
            data.put(AbstractProtocol.PROTOCOL_119_30, data11930);
            data.put(AbstractProtocol.PROTOCOL_119_40, data11940);
            data.put(AbstractProtocol.PROTOCOL_119_50, data11940);
            data.put(AbstractProtocol.PROTOCOL_119_60, data11940);
            data.put(AbstractProtocol.PROTOCOL_119_63, data11940);
            data.put(AbstractProtocol.PROTOCOL_119_70, data11940);
            data.put(AbstractProtocol.PROTOCOL_119_80, data11980);
            data.put(AbstractProtocol.PROTOCOL_120, data11980);
*/
            data.put(AbstractProtocol.PROTOCOL_120_10, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_30, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_40, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_50, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_60, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_70, data11980);
            data.put(AbstractProtocol.PROTOCOL_120_80, data11980);
            data.put(AbstractProtocol.PROTOCOL_121, data11980);
            data.put(AbstractProtocol.PROTOCOL_121_2, data11980);
            data.put(AbstractProtocol.PROTOCOL_121_20, data12120);
            data.put(AbstractProtocol.PROTOCOL_121_30, data12120);
            data.put(AbstractProtocol.PROTOCOL_121_40, data12140);
            data.put(AbstractProtocol.PROTOCOL_121_50, data12150);
            data.put(AbstractProtocol.PROTOCOL_121_60, data12160);
            data.put(AbstractProtocol.PROTOCOL_121_70, data12170);
            data.put(AbstractProtocol.PROTOCOL_121_80, data12180);
            data.put(AbstractProtocol.PROTOCOL_121_90, data12180);
            data.put(AbstractProtocol.PROTOCOL_121_93, data12180);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load biome_definitions.dat");
        }

        cachePackets();
    }

    private static Map<String, BiomeDefinitionData> loadNewPacket(AbstractProtocol protocol) {
        CompoundTag tag = data.get(protocol);
        if (tag == null) {
            throw new AssertionError("Missing biome_definitions.nbt: " + protocol);
        }
        Map<String, BiomeDefinitionData> biomes = new HashMap<>();
        for (Entry<String, Tag> entry : tag.entrySet()) {
            CompoundTag nbt = (CompoundTag) entry.getValue();
            BiomeDefinitionData definition = new BiomeDefinitionData();
            definition.temperature = nbt.getFloat("temperature");
            definition.downfall = nbt.getFloat("downfall");
            definition.redSporeDensity = nbt.getFloat("red_spores");
            definition.blueSporeDensity = nbt.getFloat("blue_spores");
            definition.ashDensity = nbt.getFloat("ash");
            definition.whiteAshDensity = nbt.getFloat("white_ash");
            definition.depth = nbt.getFloat("depth");
            definition.scale = nbt.getFloat("height");
            definition.mapWaterColorARGB = nbt.getInt("waterColorARGB");
            definition.rain = nbt.getBoolean("rain");
            if (nbt.contains("tags")) {
                Set<String> tags = new HashSet<>();
                for (StringTag stringTag : nbt.getList("tags", StringTag.class)) {
                    tags.add(stringTag.data);
                }
                definition.tags = tags;
            }
            definition.chunkGenData = null;
            biomes.put(entry.getKey(), definition);
        }
        return biomes;
    }

    public static void rebuildPackets() {
        if (!ENABLE_CLIENT_CHUNK_GEN) {
            return;
        }
        log.debug("cache biome new definitions...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
                continue;
            }
            cacheNewPacket(protocol);
        }
    }

    private static void cacheNewPacket(AbstractProtocol protocol) {
        BiomeDefinitionListPacket12180 packet = new BiomeDefinitionListPacket12180();
        packet.biomes = loadNewPacket(protocol);
        packet.setHelper(protocol.getHelper());
        packet.tryEncode();

        BiomeDefinitionListPacket12180 packetNe = new BiomeDefinitionListPacket12180();
        packetNe.biomes = packet.biomes;
        packetNe.setHelper(protocol.getHelper());
        packetNe.neteaseMode = true;
        packetNe.tryEncode();

        BatchPacket batch = packet.compress(Deflater.BEST_COMPRESSION, true);
        batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

        BatchPacket batchNe = packetNe.compress(Deflater.BEST_COMPRESSION, true);
        batchNe.tracks = new Track[]{new Track(packetNe.pid(), packetNe.getCount())};

        PACKETS.put(protocol, new BatchPacket[]{batch, batchNe});
    }

    private static void cachePackets() {
        log.debug("cache biome definitions...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_112.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
                cacheNewPacket(protocol);
                continue;
            }

            CompoundTag tag = data.get(protocol);
            if (tag == null) {
                throw new AssertionError("Missing biome_definitions.nbt: " + protocol);
            }

            byte[] data;
            try {
                data = NBTIO.writeNetwork(tag);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            BiomeDefinitionListPacket18 packet = new BiomeDefinitionListPacket18();
            packet.tag = data;
            packet.setHelper(protocol.getHelper());
            packet.tryEncode();

            BatchPacket batch = packet.compress(Deflater.BEST_COMPRESSION, protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_116.getProtocolStart());
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            PACKETS.put(protocol, new BatchPacket[]{batch, batch});
        }
    }

    @Nullable
    public static CompoundTag getData(AbstractProtocol protocol) {
        return data.get(protocol);
    }

    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS.get(protocol)[netease ? 1 : 0];
    }

    public static void init() {
    }
}
