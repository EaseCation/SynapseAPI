package org.itxtech.synapseapi.multiprotocol.utils;

import com.google.common.io.ByteStreams;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.io.IOException;
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
        } catch (NullPointerException | IOException e) {
            throw new AssertionError("Unable to load entity_identifiers.dat");
        }
    }

    public static byte[] getData(AbstractProtocol protocol) {
        return palettes.get(protocol);
    }
}
