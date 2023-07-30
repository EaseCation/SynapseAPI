package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface SubPacket<T extends SubPacketHandler> {
    void handle(T handler);

    Value pack();
}
