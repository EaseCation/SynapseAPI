package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * Send debug drawing shape info to the client for rendering
 */
@ToString
public class PrimitiveShapesPacket12630 extends Packet12630 {
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
            putOptional(entry.maximumRenderDistance, BinaryStream::putLFloat);
            putOptional(entry.color, BinaryStream::putLInt);
            putOptional(entry.dimension, BinaryStream::putVarInt);
            putOptional(entry.attachedEntityRuntimeId, BinaryStream::putEntityRuntimeId);

            putUnsignedVarInt(Type.getPayloadType(entry.type, helper.getProtocol().ordinal()));
            switch (entry.type) {
                case ARROW -> {
                    putOptional(entry.lineEndLocation, BinaryStream::putVector3f);
                    putOptional(entry.arrowHeadLength, BinaryStream::putLFloat);
                    putOptional(entry.arrowHeadRadius, BinaryStream::putLFloat);
                    putOptional(entry.numSegments, BinaryStream::putByte);
                }
                case TEXT -> {
                    putString(entry.text);
                    putBoolean(entry.textRotation);
                    putOptional(entry.backgroundColor, BinaryStream::putLInt);
                    putBoolean(entry.depthTest);
                    putBoolean(entry.showBackface);
                    putBoolean(entry.showTextBackface);
                }
                case BOX -> putVector3f(entry.boxBound);
                case LINE -> putVector3f(entry.lineEndLocation);
                case SPHERE, CIRCLE -> putByte(entry.numSegments);
                case CYLINDER -> {
                    putVector2f(entry.radii);
                    putVector2f(entry.radii2);
                    putLFloat(entry.height);
                    putByte(entry.numSegments);
                }
                case PYRAMID -> {
                    putLFloat(entry.width);
                    putOptional(entry.depth, BinaryStream::putLFloat);
                    putLFloat(entry.height);
                }
                case ELLIPSOID -> {
                    putVector3f(entry.radii3);
                    putByte(entry.numSegments);
                }
                case CONE -> {
                    putVector2f(entry.radii);
                    putLFloat(entry.height);
                    putByte(entry.numSegments);
                }
                case null, default -> {}
            }
        }
    }
}
