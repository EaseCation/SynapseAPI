package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * Send debug drawing shape info to the client for rendering
 */
@ToString
public class DebugDrawerPacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.PRIMITIVE_SHAPES_PACKET;

    public Entry[] entries = new Entry[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putArray(entries, (stream, entry) -> {
            stream.putUnsignedVarLong(entry.id);
            stream.putOptional(entry.type, (bs, type) -> bs.putByte(type.ordinal()));
            stream.putOptional(entry.location, BinaryStream::putVector3f);
            stream.putOptional(entry.scale, BinaryStream::putLFloat);
            stream.putOptional(entry.rotation, BinaryStream::putVector3f);
            stream.putOptional(entry.totalTimeLeft, BinaryStream::putLFloat);
            stream.putOptional(entry.color, BinaryStream::putLInt);
            stream.putOptional(entry.dimension, BinaryStream::putVarInt);

            stream.putUnsignedVarInt(Type.getPayloadType(entry.type, helper.getProtocol().ordinal()));
            switch (entry.type) {
                case ARROW -> {
                    stream.putOptional(entry.lineEndLocation, BinaryStream::putVector3f);
                    stream.putOptional(entry.arrowHeadLength, BinaryStream::putLFloat);
                    stream.putOptional(entry.arrowHeadRadius, BinaryStream::putLFloat);
                    stream.putOptional(entry.numSegments, BinaryStream::putByte);
                }
                case TEXT -> stream.putString(entry.text);
                case BOX -> stream.putVector3f(entry.boxBound);
                case LINE -> stream.putVector3f(entry.lineEndLocation);
                case SPHERE, CIRCLE -> stream.putByte(entry.numSegments);
                case null, default -> {}
            }
        }, 1048576);
    }
}
