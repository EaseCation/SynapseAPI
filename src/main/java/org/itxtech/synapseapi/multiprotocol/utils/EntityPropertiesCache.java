package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.entity.EntityFullNames;
import cn.nukkit.entity.property.EntityProperties;
import cn.nukkit.entity.property.EntityPropertyRegistry;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.SyncEntityPropertyPacket117;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

@Log4j2
public class EntityPropertiesCache {
    private static BatchPacket PACKETS;
    private static byte[] PLAYER_PROPERTIES = CompoundTag.EMPTY;

    static {
        rebuildNetworkCache();
    }

    public static void rebuildNetworkCache() {
        log.debug("cache entity properties...");

        Map<String, byte[]> compiled = new HashMap<>();
        for (Map.Entry<String, EntityProperties> entry : EntityPropertyRegistry.getRegistry().entrySet()) {
            CompoundTag tag = entry.getValue().toNBT();

            if (log.isTraceEnabled()) {
                log.trace("Client synced entity properties: {}", tag);
            }

            try {
                compiled.put(entry.getKey(), NBTIO.writeNetwork(tag));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        byte[] playerProperties = compiled.get(EntityFullNames.PLAYER);
        if (playerProperties != null) {
            PLAYER_PROPERTIES = playerProperties;
        }

        List<DataPacket> packets = new ArrayList<>();
        for (byte[] nbt : compiled.values()) {
            SyncEntityPropertyPacket117 packet = new SyncEntityPropertyPacket117();
            packet.nbt = nbt;
            packets.add(packet);
        }
        PACKETS = BatchPacket.compress(Deflater.BEST_COMPRESSION, true, packets.toArray(new DataPacket[0]));
    }

    /**
     * @return batch packet
     */
    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol, boolean netease) {
        return PACKETS;
    }

    public static byte[] getCompiledPlayerProperties(AbstractProtocol protocol, boolean netease) {
        return PLAYER_PROPERTIES;
    }

    public static void init() {
    }

    private EntityPropertiesCache() {
        throw new IllegalStateException();
    }
}
