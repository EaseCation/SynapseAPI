package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public record EncryptedPacket(String modName, String systemName, String eventName, String data, Function<String, String> encMethod) implements ClientboundSubPacket {
    @Override
    public void handle(SubPacketHandler handler) {
        handler.handle(this);
    }

    @Override
    public Value pack() {
        return ValueFactory.newArray(
                ValueFactory.newBinary("ModEventS2C".getBytes(StandardCharsets.UTF_8)),
                ValueFactory.newArray(
                        ValueFactory.newBinary(modName.getBytes(StandardCharsets.UTF_8)),
                        ValueFactory.newBinary(systemName.getBytes(StandardCharsets.UTF_8)),
                        ValueFactory.newBinary(eventName.getBytes(StandardCharsets.UTF_8)),
                        ValueFactory.newMapBuilder()
                                .put(ValueFactory.newBinary("data".getBytes(StandardCharsets.UTF_8)),
                                        ValueFactory.newBinary(encMethod.apply(data).getBytes(StandardCharsets.UTF_8)))
                                .build()
                ),
                ValueFactory.newNil()
        );
    }
}
