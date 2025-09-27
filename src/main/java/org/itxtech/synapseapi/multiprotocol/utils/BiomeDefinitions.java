package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.level.biome.*;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.Hash;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.event.server.BiomeRegistryChecksumChangedEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.biome.BiomeDefinitionData;
import org.itxtech.synapseapi.multiprotocol.protocol121100.protocol.BiomeDefinitionListPacket121100;
import org.itxtech.synapseapi.multiprotocol.protocol121110.protocol.BiomeDefinitionListPacket121110;
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

    private static long BIOME_REGISTRY_CHECKSUM;
    private static final AbstractProtocol BIOME_REGISTRY_CHECKSUM_VERSION = AbstractProtocol.FIRST_AVAILABLE_PROTOCOL;

    private static final IntList CUSTOM_BIOME_RUNTIME_TO_LEGACY_CLIENT = new IntArrayList();

    static {
        log.debug("Loading biome definitions...");

        Biomes.setNetworkManager(new BiomeNetworkManager() {
            @Override
            public void registerCustomBiome(int id, String name, String fullName, CustomBiome biome) {
                BiomeDefinitions.registerCustomBiome(id, name, fullName, biome);
            }

            @Override
            public void rebuildCache() {
                BiomeDefinitions.cachePackets();
            }

            @Override
            public int toClientId(int id, boolean legacy) {
                return BiomeDefinitions.toClientId(id, legacy);
            }
        });

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
            CompoundTag data121100 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions121100.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data121110 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions121110.nbt")), ByteOrder.LITTLE_ENDIAN, true);

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
            data.put(AbstractProtocol.PROTOCOL_121_100, data121100);
            data.put(AbstractProtocol.PROTOCOL_121_110, data121110);
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
            if (nbt.contains("id")) {
                definition.id = nbt.getShort("id");
            }
            definition.temperature = nbt.getFloat("temperature");
            definition.downfall = nbt.getFloat("downfall");
            definition.foliageSnow = nbt.getFloat("foliage_snow");
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
        DataPacket packet;
        DataPacket packetNe;
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_110.getProtocolStart()) {
            BiomeDefinitionListPacket121110 biomePacket = new BiomeDefinitionListPacket121110();
            biomePacket.biomes = loadNewPacket(protocol);
            biomePacket.setHelper(protocol.getHelper());
            biomePacket.tryEncode();
            packet = biomePacket;

            BiomeDefinitionListPacket121110 biomePacketNe = new BiomeDefinitionListPacket121110();
            biomePacketNe.biomes = biomePacket.biomes;
            biomePacketNe.setHelper(protocol.getHelper());
            biomePacketNe.neteaseMode = true;
            biomePacketNe.tryEncode();
            packetNe = biomePacketNe;
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_100.getProtocolStart()) {
            BiomeDefinitionListPacket121100 biomePacket = new BiomeDefinitionListPacket121100();
            biomePacket.biomes = loadNewPacket(protocol);
            biomePacket.setHelper(protocol.getHelper());
            biomePacket.tryEncode();
            packet = biomePacket;

            BiomeDefinitionListPacket121100 biomePacketNe = new BiomeDefinitionListPacket121100();
            biomePacketNe.biomes = biomePacket.biomes;
            biomePacketNe.setHelper(protocol.getHelper());
            biomePacketNe.neteaseMode = true;
            biomePacketNe.tryEncode();
            packetNe = biomePacketNe;
        } else {
            BiomeDefinitionListPacket12180 biomePacket = new BiomeDefinitionListPacket12180();
            biomePacket.biomes = loadNewPacket(protocol);
            biomePacket.setHelper(protocol.getHelper());
            biomePacket.tryEncode();
            packet = biomePacket;

            BiomeDefinitionListPacket12180 biomePacketNe = new BiomeDefinitionListPacket12180();
            biomePacketNe.biomes = biomePacket.biomes;
            biomePacketNe.setHelper(protocol.getHelper());
            biomePacketNe.neteaseMode = true;
            biomePacketNe.tryEncode();
            packetNe = biomePacketNe;
        }

        BatchPacket batch = packet.compress(protocol.getCompressor(), Deflater.BEST_COMPRESSION);
        batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

        BatchPacket batchNe = packetNe.compress(protocol.getCompressor(), Deflater.BEST_COMPRESSION);
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

            BatchPacket batch = packet.compress(protocol.getCompressor(), Deflater.BEST_COMPRESSION);
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            PACKETS.put(protocol, new BatchPacket[]{batch, batch});
        }

        recalculateBiomeRegistryChecksum();
    }

    private static void recalculateBiomeRegistryChecksum() {
        Object2IntMap<String> sort = new Object2IntRBTreeMap<>();
        for (String name : getData(BIOME_REGISTRY_CHECKSUM_VERSION).keySet()) {
            sort.put(name, Biomes.getIdByName(name));
        }

        BinaryStream stream = new BinaryStream();
        for (Object2IntMap.Entry<String> entry : sort.object2IntEntrySet()) {
            stream.putString(entry.getKey());
            stream.putLShort(entry.getIntValue());
        }

        long newChecksum = Hash.xxh64(stream.getBuffer());
        if (BIOME_REGISTRY_CHECKSUM == newChecksum) {
            return;
        }
        BIOME_REGISTRY_CHECKSUM = newChecksum;

        new BiomeRegistryChecksumChangedEvent(newChecksum).call();

        SynapseAPI.getInstance().getLogger().debug("BiomeRegistry checksum: {}", newChecksum);
    }

    public static long getBiomeRegistryChecksum() {
        return BIOME_REGISTRY_CHECKSUM;
    }

    @Nullable
    public static CompoundTag getData(AbstractProtocol protocol) {
        return data.get(protocol);
    }

    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS.get(protocol)[netease ? 1 : 0];
    }

    private static void registerCustomBiome(int id, String name, String fullName, CustomBiome biome) {
        Object2IntMap<String> sort = new Object2IntRBTreeMap<>();
        sort.put(name, id);
        for (Biome customBiome : Biomes.getCustomBiomes()) {
            sort.put(customBiome.getIdentifier(), customBiome.getId());
        }
        IntList converter = CUSTOM_BIOME_RUNTIME_TO_LEGACY_CLIENT;
        converter.clear();
        int clientIndex = 0;
        for (int runtimeId : sort.values()) {
            int runtimeIndex = runtimeId - 30000;
            while (converter.size() <= runtimeIndex) {
                converter.add(-1);
            }
            converter.set(runtimeIndex, 193 + clientIndex++); // 1.20.0-1.21.30: 193+
        }

        for (Entry<AbstractProtocol, CompoundTag> entry : data.entrySet()) {
            AbstractProtocol protocol = entry.getKey();
            if (protocol.getProtocolStart() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.getProtocolStart()) {
                continue;
            }

            CompoundTag nbt = new CompoundTag()
                    .putFloat("temperature", biome.getTemperature())
                    .putFloat("downfall", biome.getDownfall());

            if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_116.getProtocolStart()) {
                nbt.putFloat("ash", biome.getAsh());
                nbt.putFloat("white_ash", biome.getWhiteAsh());
                nbt.putFloat("blue_spores", biome.getBlueSpores());
                nbt.putFloat("red_spores", biome.getRedSpores());

                ListTag<StringTag> tags = new ListTag<>();
                biome.getTags().forEach(tags::addString);
                nbt.putList("tags", tags);

                if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
                    CompoundTag climate = new CompoundTag()
                            .putFloat("temperature", biome.getTemperature())
                            .putFloat("downfall", biome.getDownfall())
                            .putFloat("ash", biome.getAsh())
                            .putFloat("white_ash", biome.getWhiteAsh())
                            .putFloat("blue_spores", biome.getBlueSpores())
                            .putFloat("red_spores", biome.getRedSpores());

                    if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_30.getProtocolStart()) {
                        climate.putFloat("snow_accumulation_min", biome.getMinSnowAccumulation());
                        climate.putFloat("snow_accumulation_max", biome.getMaxSnowAccumulation());
                    }

                    nbt.putCompound("minecraft:climate", climate);
                }

                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
                    nbt.putFloat("depth", biome.getBaseHeight());
                    nbt.putFloat("height", biome.getHeightVariation());

                    nbt.putBoolean("rain", biome.canRain());

                    if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                        nbt.putFloat("waterTransparency", biome.getWaterColor().getAlpha() / 255f);

                        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_30.getProtocolStart()) {
                            nbt.putString("name_hash", name);
                        }
                    } else {
                        nbt.putShort("id", id);

                        if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
                            nbt.putFloat("waterColorA", biome.getWaterColor().getAlpha() / 255f);
                            nbt.putFloat("waterColorR", biome.getWaterColor().getRed() / 255f);
                            nbt.putFloat("waterColorG", biome.getWaterColor().getGreen() / 255f);
                            nbt.putFloat("waterColorB", biome.getWaterColor().getBlue() / 255f);
                        } else {
                            nbt.putInt("waterColorARGB", biome.getWaterColor().getRGB());
                        }
                    }
                }
            }

            entry.getValue().putCompound(protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart() ? fullName : name, nbt);
        }
    }

    private static int toClientId(int runtimeId, boolean legacy) {
        if (legacy && runtimeId >= 30000) {
            int runtimeIndex = runtimeId - 30000;
            if (runtimeIndex >= CUSTOM_BIOME_RUNTIME_TO_LEGACY_CLIENT.size()) {
                return BiomeID.OCEAN;
            }
            int clientId = CUSTOM_BIOME_RUNTIME_TO_LEGACY_CLIENT.getInt(runtimeIndex);
            if (clientId == -1) {
                return BiomeID.OCEAN;
            }
            return clientId;
        }
        return runtimeId;
    }

    public static void init() {
    }
}
