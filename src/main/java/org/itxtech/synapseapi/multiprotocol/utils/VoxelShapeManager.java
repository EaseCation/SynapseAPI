package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.JsonUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.block.VoxelShape;
import org.itxtech.synapseapi.multiprotocol.protocol126.protocol.VoxelShapesPacket126;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.VoxelShapesPacket12610;
import tools.jackson.core.type.TypeReference;

import javax.annotation.Nullable;
import java.util.*;
import java.util.zip.Deflater;

@Log4j2
public class VoxelShapeManager {
    private static final List<VoxelShape> SHAPES = new ArrayList<>();
    private static final Object2IntMap<String> NAME_MAP = new Object2IntOpenHashMap<>();

    private static final Map<AbstractProtocol, BatchPacket> PACKETS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("load voxel shape manager...");

        List<Entry> shapes = JsonUtil.TRUSTED_JSON_MAPPER.readValue(SynapseAPI.getInstance().getResource("shapes.json"), new TypeReference<>(){});
        for (int i = 0; i < shapes.size(); i++) {
            Entry entry = shapes.get(i);
            SHAPES.add(VoxelShape.builder()
                    .xSize(entry.xSize())
                    .ySize(entry.ySize())
                    .zSize(entry.zSize())
                    .storage(entry.storage())
                    .xCoordinates(entry.xCoords())
                    .yCoordinates(entry.yCoords())
                    .zCoordinates(entry.zCoords())
                    .build());
            for (String name : entry.names) {
                NAME_MAP.put(name, i);
            }
            log.trace("register voxel shape {} : {}", i, entry);
        }

        rebuildNetworkCache();
    }

    public static void rebuildNetworkCache() {
        log.debug("cache voxel shapes...");

        VoxelShape[] shapes = SHAPES.toArray(new VoxelShape[0]);
        ObjectIntPair<String>[] nameMap = NAME_MAP.object2IntEntrySet().stream()
                .map(entry -> ObjectIntPair.of(entry.getKey(), entry.getIntValue()))
                .toArray(ObjectIntPair[]::new);

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            DataPacket packet;
            if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
                VoxelShapesPacket12610 pk = new VoxelShapesPacket12610();
                pk.shapes = shapes;
                pk.nameMap = nameMap;
                packet = pk;
            } else {
                VoxelShapesPacket126 pk = new VoxelShapesPacket126();
                pk.shapes = shapes;
                pk.nameMap = nameMap;
                packet = pk;
            }

            packet.setHelper(protocol.getHelper());
            packet.tryEncode();

            BatchPacket batch = packet.compress(protocol.getCompressor(), Deflater.BEST_COMPRESSION);
            batch.tracks = new Track[]{new Track(packet.pid(), packet.getCount())};

            PACKETS.put(protocol, batch);
        }
    }

    /**
     * @return batch packet
     */
    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS.get(protocol);
    }

    public static void init() {
    }

    private VoxelShapeManager() {
    }

    private record Entry(
            String[] names,
            byte xSize,
            byte ySize,
            byte zSize,
            byte[] storage,
            float[] xCoords,
            float[] yCoords,
            float[] zCoords
    ) {
        @Override
        public String toString() {
            return "Entry(names=" + Arrays.toString(this.names) + ", xSize=" + this.xSize + ", ySize=" + this.ySize + ", zSize=" + this.zSize + ", storage=" + Arrays.toString(this.storage) + ", xCoords=" + Arrays.toString(this.xCoords) + ", yCoords=" + Arrays.toString(this.yCoords) + ", zCoords=" + Arrays.toString(this.zCoords) + ")";
        }
    }
}
