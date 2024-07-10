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
public final class AvailableEntityIdentifiersPalette {

    private static final Map<AbstractProtocol, byte[]> palettes = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading entity identifiers...");

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
            byte[] data117 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_117.dat"));
            byte[] data11740 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11740.dat"));
            byte[] data118 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_118.dat"));
            byte[] data119 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_119.nbt"));
            byte[] data11910 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11910.nbt"));
            byte[] data11960 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11960.nbt"));
            byte[] data11970 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11970.nbt"));
            byte[] data11980 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_11980.nbt"));
            byte[] data12040 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12040.nbt"));
            byte[] data12060 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12060.nbt"));
            byte[] data12070 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12070.nbt"));
            byte[] data12080 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_12080.nbt"));
            byte[] data121 = ByteStreams.toByteArray(SynapseAPI.getInstance().getResource("entity_identifiers_121.nbt"));

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
            palettes.put(AbstractProtocol.PROTOCOL_120_10, data11980);
            palettes.put(AbstractProtocol.PROTOCOL_120_30, data11980);
            palettes.put(AbstractProtocol.PROTOCOL_120_40, data12040);
            palettes.put(AbstractProtocol.PROTOCOL_120_50, data12040);
            palettes.put(AbstractProtocol.PROTOCOL_120_60, data12060);
            palettes.put(AbstractProtocol.PROTOCOL_120_70, data12070);
            palettes.put(AbstractProtocol.PROTOCOL_120_80, data12080);
            palettes.put(AbstractProtocol.PROTOCOL_121, data121);
            palettes.put(AbstractProtocol.PROTOCOL_121_2, data121);
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load entity_identifiers.dat");
        }

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_18.getProtocolStart()) {
                continue;
            }
            if (palettes.get(protocol) == null) {
                throw new AssertionError("Missing entity_identifiers.nbt: " + protocol);
            }
        }
    }

    public static byte[] getData(AbstractProtocol protocol) {
        return palettes.get(protocol);
    }

    public static void init() {
        if (!SynapseSharedConstants.CHECK_RESOURCE_DATA) {
            return;
        }
        palettes.forEach((protocol, data) -> {
            try {
                NBTIO.read(data, ByteOrder.LITTLE_ENDIAN, true); //检查数据
            } catch (Exception e) {
                SynapseAPI.getInstance().getLogger().error(protocol.toString() +" 的实体标识符无效", e);
            }
        });
    }
}
