package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface ServerboundSubPacket<H extends ServerSubPacketHandler<?>> extends SubPacket<H> {
    default Value pack() {
        throw new IllegalStateException("clientbound only");
    }
}
