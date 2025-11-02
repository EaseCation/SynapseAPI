package org.itxtech.synapseapi.multiprotocol.protocol12190.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Used by Scripting to send new, removed or modified debug shapes information to the client to be used for rendering.
 */
@ToString
public class ServerScriptDebugDrawerPacket12190 extends Packet12190 {
    public static final int NETWORK_ID = ProtocolInfo.SERVER_SCRIPT_DEBUG_DRAWER_PACKET;

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
            putOptional(entry.text, BinaryStream::putString);
            putOptional(entry.boxBound, BinaryStream::putVector3f);
            putOptional(entry.lineEndLocation, BinaryStream::putVector3f);
            putOptional(entry.arrowHeadLength, BinaryStream::putLFloat);
            putOptional(entry.arrowHeadRadius, BinaryStream::putLFloat);
            putOptional(entry.numSegments, BinaryStream::putByte);
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Entry {
        public long id;
        public Type type;
        public Vector3f location;
        public Float scale;
        public Vector3f rotation;
        public Float totalTimeLeft;
        public Integer color;
        /**
         * @since 1.21.120
         */
        public int dimension;
        public String text;
        public Vector3f boxBound;
        public Vector3f lineEndLocation;
        public Float arrowHeadLength;
        public Float arrowHeadRadius;
        public Byte numSegments;

        public Entry(long id, int dimension) {
            this.id = id;
            this.dimension = dimension;
        }
    }

    public enum Type {
        LINE(4),
        BOX(3),
        SPHERE(5),
        CIRCLE(5),
        TEXT(2),
        ARROW(1),
        ;

        private final int payloadType;

        Type(int payloadType) {
            this.payloadType = payloadType;
        }

        public int getPayloadType() {
            return payloadType;
        }

        public static int getPayloadType(Type type) {
            return type == null ? 0 : type.getPayloadType();
        }
    }
}
