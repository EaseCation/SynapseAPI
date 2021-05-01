package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import com.google.common.io.ByteStreams;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public final class BiomeDefinitions {

    private static final Map<AbstractProtocol, byte[]> data = new HashMap<>();

    static {
        try {
            //TODO: 1.8-1.11
            byte[] data112 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions112.dat"));
            byte[] data116 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116.dat"));
            byte[] data116210 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116210.dat"));

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
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load biome_definitions.dat");
        }
    }

    public static byte[] getData(AbstractProtocol protocol) {
        return data.get(protocol);
    }

    public static void init() {
        data.forEach((protocol, data) -> {
            try {
                NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true); //检查数据
            } catch (Exception e) {
                SynapseAPI.getInstance().getLogger().error(protocol.toString() +" 的生物群系定义无效", e);
            }
        });
    }
}
