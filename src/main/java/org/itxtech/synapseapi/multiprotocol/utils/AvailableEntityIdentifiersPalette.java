package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.DataPacket;
import com.google.common.io.ByteStreams;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.AvailableEntityIdentifiersPacket18;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.Deflater;

@Log4j2
public final class AvailableEntityIdentifiersPalette {

    private static final Map<AbstractProtocol, CompoundTag> palettes = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, BatchPacket> PACKETS = new EnumMap<>(AbstractProtocol.class);

    private static int CUSTOM_ENTITY_RUNTIME_ID_ALLOCATOR = 1000;

    static {
        log.debug("Loading entity identifiers...");

        try {
/*
            CompoundTag data18 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_18.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data19 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_19.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data110 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_110.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data111 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_111.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data112 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_112.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data113 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_113.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data116 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11620 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11620.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data116100 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116100.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data117 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_117.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11740 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11740.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data118 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_118.dat")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data119 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_119.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11910 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11910.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11960 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11960.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data11970 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11970.nbt")), ByteOrder.LITTLE_ENDIAN, true);
*/
            CompoundTag data11980 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11980.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12040 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12040.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12060 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12060.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12070 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12070.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12080 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12080.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data121 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_121.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12120 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12120.nbt")), ByteOrder.LITTLE_ENDIAN, true);
            CompoundTag data12150 = NBTIO.read(ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12150.nbt")), ByteOrder.LITTLE_ENDIAN, true);

/*
            palettes.put(AbstractProtocol.PROTOCOL_18, data18);
            palettes.put(AbstractProtocol.PROTOCOL_19, data19);
            palettes.put(AbstractProtocol.PROTOCOL_110, data110);
            palettes.put(AbstractProtocol.PROTOCOL_111, data111);
            palettes.put(AbstractProtocol.PROTOCOL_112, data112);
            palettes.put(AbstractProtocol.PROTOCOL_113, data113);
            palettes.put(AbstractProtocol.PROTOCOL_114, data113);
            palettes.put(AbstractProtocol.PROTOCOL_114_60, data113);
            palettes.put(AbstractProtocol.PROTOCOL_116, data116);
            palettes.put(AbstractProtocol.PROTOCOL_116_20, data11620);
            palettes.put(AbstractProtocol.PROTOCOL_116_100_NE, data116);
            palettes.put(AbstractProtocol.PROTOCOL_116_100, data116100);
            palettes.put(AbstractProtocol.PROTOCOL_116_200, data116100);
            palettes.put(AbstractProtocol.PROTOCOL_116_210, data116100);
            palettes.put(AbstractProtocol.PROTOCOL_116_220, data116100);
            palettes.put(AbstractProtocol.PROTOCOL_117, data117);
            palettes.put(AbstractProtocol.PROTOCOL_117_10, data117);
            palettes.put(AbstractProtocol.PROTOCOL_117_30, data117);
            palettes.put(AbstractProtocol.PROTOCOL_117_40, data11740);
            palettes.put(AbstractProtocol.PROTOCOL_118, data118);
            palettes.put(AbstractProtocol.PROTOCOL_118_10, data118);
            palettes.put(AbstractProtocol.PROTOCOL_118_30, data118);
            palettes.put(AbstractProtocol.PROTOCOL_118_30_NE, data118);
            palettes.put(AbstractProtocol.PROTOCOL_119, data119);
            palettes.put(AbstractProtocol.PROTOCOL_119_10, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_20, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_21, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_30, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_40, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_50, data11910);
            palettes.put(AbstractProtocol.PROTOCOL_119_60, data11960);
            palettes.put(AbstractProtocol.PROTOCOL_119_63, data11960);
            palettes.put(AbstractProtocol.PROTOCOL_119_70, data11970);
            palettes.put(AbstractProtocol.PROTOCOL_119_80, data11980);
            palettes.put(AbstractProtocol.PROTOCOL_120, data11980);
*/
            palettes.put(AbstractProtocol.PROTOCOL_120_10, data11980);
            palettes.put(AbstractProtocol.PROTOCOL_120_30, data11980);
            palettes.put(AbstractProtocol.PROTOCOL_120_40, data12040);
            palettes.put(AbstractProtocol.PROTOCOL_120_50, data12040);
            palettes.put(AbstractProtocol.PROTOCOL_120_60, data12060);
            palettes.put(AbstractProtocol.PROTOCOL_120_70, data12070);
            palettes.put(AbstractProtocol.PROTOCOL_120_80, data12080);
            palettes.put(AbstractProtocol.PROTOCOL_121, data121);
            palettes.put(AbstractProtocol.PROTOCOL_121_2, data121);
            palettes.put(AbstractProtocol.PROTOCOL_121_20, data12120);
            palettes.put(AbstractProtocol.PROTOCOL_121_30, data12120);
            palettes.put(AbstractProtocol.PROTOCOL_121_40, data12120);
            palettes.put(AbstractProtocol.PROTOCOL_121_50, data12150);
            palettes.put(AbstractProtocol.PROTOCOL_121_60, data12150);
            palettes.put(AbstractProtocol.PROTOCOL_121_70, data12150);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load entity_identifiers.dat");
        }

        cachePackets();
    }

    public static void cachePackets() {
        log.debug("cache entity identifiers...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_18.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            CompoundTag tag = palettes.get(protocol);
            if (tag == null) {
                throw new AssertionError("Missing entity_identifiers.nbt: " + protocol);
            }

            byte[] data;
            try {
                data = NBTIO.writeNetwork(tag);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            AvailableEntityIdentifiersPacket18 packet = new AvailableEntityIdentifiersPacket18();
            packet.tag = data;
            packet.setHelper(protocol.getHelper());
            packet.tryEncode();

            BatchPacket batch = packet.compress(Deflater.BEST_COMPRESSION, protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_116.getProtocolStart());
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            PACKETS.put(protocol, batch);
        }
    }

    @Nullable
    public static CompoundTag getData(AbstractProtocol protocol) {
        return palettes.get(protocol);
    }

    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol) {
        return PACKETS.get(protocol);
    }

    public static void registerCustomEntity(String id) {
        registerCustomEntity(id, "");
    }

    public static void registerCustomEntity(String id, String baseId) {
        registerCustomEntity(id, baseId, CUSTOM_ENTITY_RUNTIME_ID_ALLOCATOR++);
    }

    public static void registerCustomEntity(String id, String baseId, int runtimeId) {
        palettes.forEach((protocol, data) -> {
            ListTag<CompoundTag> idList = data.getList("idlist", CompoundTag.class);

            for (CompoundTag entry : idList) {
                String existsId = entry.getString("id");
                if (id.equals(existsId)) {
                    return;
                }
                if (runtimeId == entry.getInt("rid")) {
                    throw new IllegalArgumentException(protocol + " | entity (" + existsId + ") already registered with rid: " + runtimeId + " (" + id + ")");
                }
            }

            idList.add(new CompoundTag()
                    .putString("id", id)
                    .putString("bid", baseId)
                    .putInt("rid", runtimeId)
                    .putBoolean("hasspawnegg", false)
                    .putBoolean("summonable", false)
                    .putBoolean("experimental", false));
        });
    }

    public static void init() {
    }
}
