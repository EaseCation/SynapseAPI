package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.nbt.NBTIO;
import com.google.common.io.ByteStreams;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

@Log4j2
public final class BiomeDefinitions {

    private static final Map<AbstractProtocol, byte[]> data = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading biome definitions...");

        try {
            //TODO: 1.8-1.11
            byte[] data112 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions112.dat"));
            byte[] data116 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116.dat"));
            byte[] data116210 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions116210.dat"));
            byte[] data11740 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11740.dat"));
            byte[] data118 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions118.dat"));
            byte[] data11810 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11810.dat"));
            byte[] data119 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions119.nbt"));
            byte[] data11920 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11920.nbt"));
            byte[] data11930 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11930.nbt"));
            byte[] data11940 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("biome_definitions11940.nbt"));

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
            data.put(AbstractProtocol.PROTOCOL_119, data119);
            data.put(AbstractProtocol.PROTOCOL_119_10, data119);
            data.put(AbstractProtocol.PROTOCOL_119_20, data11920);
            data.put(AbstractProtocol.PROTOCOL_119_21, data11920);
            data.put(AbstractProtocol.PROTOCOL_119_30, data11930);
            data.put(AbstractProtocol.PROTOCOL_119_40, data11940);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load biome_definitions.dat");
        }
    }

    public static byte[] getData(AbstractProtocol protocol) {
        return data.get(protocol);
    }

    public static void init() {
        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
            return;
        }
        data.forEach((protocol, data) -> {
            try {
                NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true); //检查数据
            } catch (Exception e) {
                SynapseAPI.getInstance().getLogger().error(protocol.toString() +" 的生物群系定义无效", e);
            }
        });
    }
}
