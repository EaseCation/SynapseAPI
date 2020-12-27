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
            byte[] data18 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_18.dat"));
            byte[] data19 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_19.dat"));
            byte[] data110 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_110.dat"));
            byte[] data111 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_111.dat"));
            byte[] data112 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_112.dat"));
            byte[] data113 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_113.dat"));
            byte[] data116 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116.dat"));
            byte[] data11620 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11620.dat"));
            byte[] data116100 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_116100.dat"));

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
            palettes.put(AbstractProtocol.PROTOCOL_116_100_NE, data11620);
            palettes.put(AbstractProtocol.PROTOCOL_116_100, data116100);
            palettes.put(AbstractProtocol.PROTOCOL_116_200, data116100);
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
