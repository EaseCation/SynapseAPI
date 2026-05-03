package org.itxtech.synapseapi.multiprotocol.protocol121120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * Send debug drawing shape info to the client for rendering
 */
@ToString
public class DebugDrawerPacket121120 extends Packet121120 {
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
        putUnsignedVarInt(entries.length);
        for (Entry entry : entries) {
            putUnsignedVarLong(entry.id);
            putOptional(entry.type, (stream, type) -> stream.putByte(type.ordinal()));
            putOptional(entry.location, BinaryStream::putVector3f);
            putOptional(entry.scale, BinaryStream::putLFloat);
            putOptional(entry.rotation, BinaryStream::putVector3f);
            putOptional(entry.totalTimeLeft, BinaryStream::putLFloat);
            putOptional(entry.color, BinaryStream::putLInt);
            putVarInt(entry.dimension);

            putUnsignedVarInt(Type.getPayloadType(entry.type));
            switch (entry.type) {
                case null -> {}
                case ARROW -> {
                    putOptional(entry.lineEndLocation, BinaryStream::putVector3f);
                    putOptional(entry.arrowHeadLength, BinaryStream::putLFloat);
                    putOptional(entry.arrowHeadRadius, BinaryStream::putLFloat);
                    putOptional(entry.numSegments, BinaryStream::putByte);
                }
                case TEXT -> putString(entry.text);
                case BOX -> putVector3f(entry.boxBound);
                case LINE -> putVector3f(entry.lineEndLocation);
                case SPHERE, CIRCLE -> putByte(entry.numSegments);
            }
        }
    }
}
