package org.itxtech.synapseapi.network.protocol.mod;

public interface SubPacketHandler<P extends SubPacket<?>> {
    default void dispatch(SubPacket<? extends SubPacketHandler<?>> subPacket) {
        subPacket.dispatch(this);
    }
}
