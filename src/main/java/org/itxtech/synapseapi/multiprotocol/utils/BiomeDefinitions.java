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
            byte[] data116100 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116100.dat"));

            data.put(AbstractProtocol.PROTOCOL_112, data112);
            data.put(AbstractProtocol.PROTOCOL_113, data112);
            data.put(AbstractProtocol.PROTOCOL_114, data112);
            data.put(AbstractProtocol.PROTOCOL_114_60, data112);
            data.put(AbstractProtocol.PROTOCOL_116, data112);
            data.put(AbstractProtocol.PROTOCOL_116_20, data112);
            data.put(AbstractProtocol.PROTOCOL_116_100, data116100);
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
