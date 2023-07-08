package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface SubPacket {
    void handle(SubPacketHandler handler);

    Value pack();
}
