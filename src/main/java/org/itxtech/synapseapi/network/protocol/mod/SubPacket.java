package org.itxtech.synapseapi.network.protocol.mod;

import org.msgpack.value.Value;

public interface SubPacket<H extends SubPacketHandler<?>> {
    default void dispatch(SubPacketHandler<?> handler) {
        Class<H> handlerClass = getHandlerClass();
        if (!handlerClass.isInstance(handler)) return;
        H realHandler = handlerClass.cast(handler);
        handle(realHandler);
    }

    Class<H> getHandlerClass();

    void handle(H handler);

    Value pack();
}
