package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface ServerboundSubPacket extends SubPacket {
    default Value pack() {
        throw new IllegalStateException("clientbound only");
    }
}
