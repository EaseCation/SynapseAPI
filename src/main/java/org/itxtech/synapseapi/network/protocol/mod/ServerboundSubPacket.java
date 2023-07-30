package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface ServerboundSubPacket<T extends ServerSubPacketHandler> extends SubPacket<T> {
    default Value pack() {
        throw new IllegalStateException("clientbound only");
    }
}
