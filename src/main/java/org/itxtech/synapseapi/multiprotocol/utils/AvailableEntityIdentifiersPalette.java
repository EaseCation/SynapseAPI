package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import com.google.common.io.ByteStreams;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public final class AvailableEntityIdentifiersPalette {

    private static final Map<AbstractProtocol, byte[]> palettes = new HashMap<>();

    static {
        try {
            palettes.put(AbstractProtocol.PROTOCOL_18, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_18.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_19, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_19.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_110, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_110.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_111, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_111.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_112, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_112.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_113, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_113.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_114, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_113.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_114_60, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_113.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_116, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_116_20, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11620.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_116_100, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116100.dat")));
            palettes.put(AbstractProtocol.PROTOCOL_116_200, ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116100.dat")));
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load entity_identifiers.dat");
        }
    }

    public static byte[] getData(AbstractProtocol protocol) {
        return palettes.get(protocol);
    }

    public static void init() {
        palettes.forEach((protocol, data) -> {
            try {

                NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true); //检查数据
            } catch (Exception e) {
                SynapseAPI.getInstance().getLogger().error(protocol.toString() +" 的实体标识符无效", e);
            }
        });
    }
}
