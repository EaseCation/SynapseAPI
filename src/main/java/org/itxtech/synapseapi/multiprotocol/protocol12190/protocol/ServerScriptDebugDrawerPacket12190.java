package org.itxtech.synapseapi.multiprotocol.protocol12190.protocol;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

/**
 * Used by Scripting to send new, removed or modified debug shapes information to the client to be used for rendering.
 */
@ToString
public class ServerScriptDebugDrawerPacket12190 extends Packet12190 {
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
            Type type = entry.type;
            if (type != null && helper.getProtocol().ordinal() < type.protocol.ordinal()) {
                type = null;
            }
            putOptional(type, (stream, t) -> stream.putByte(t.ordinal()));
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
        /**
         * @since 1.26.20
         */
        public Float maximumRenderDistance;
        public Integer color;
        /**
         * @since 1.21.120
         */
        public int dimension;
        /**
         * @since 1.26.0
         */
        public Long attachedEntityRuntimeId;

        public String text;
        /**
         * @since 1.26.20
         */
        public boolean textRotation;
        /**
         * @since 1.26.20
         */
        public Integer backgroundColor;
        /**
         * @since 1.26.20
         */
        public boolean depthTest;
        /**
         * @since 1.26.20
         */
        @Default
        public boolean showBackface = true;
        /**
         * @since 1.26.20
         */
        @Default
        public boolean showTextBackface = true;
        public Vector3f boxBound;
        public Vector3f lineEndLocation;
        public Float arrowHeadLength;
        public Float arrowHeadRadius;
        public Byte numSegments;
        /**
         * @since 1.26.30
         */
        public float width;
        /**
         * @since 1.26.30
         */
        public Float depth;
        /**
         * @since 1.26.30
         */
        public float height;
        /**
         * @since 1.26.30
         */
        public Vector2f radii;
        /**
         * @since 1.26.30
         */
        public Vector2f radii2;
        /**
         * @since 1.26.30
         */
        public Vector3f radii3;

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
        /**
         * @since 1.26.30
         */
        CYLINDER(6, AbstractProtocol.PROTOCOL_126_30),
        /**
         * @since 1.26.30
         */
        PYRAMID(7, AbstractProtocol.PROTOCOL_126_30),
        /**
         * @since 1.26.30
         */
        ELLIPSOID(8, AbstractProtocol.PROTOCOL_126_30),
        /**
         * @since 1.26.30
         */
        CONE(9, AbstractProtocol.PROTOCOL_126_30),
        ;

        private final int payloadType;
        private final AbstractProtocol protocol;

        Type(int payloadType) {
            this(payloadType, AbstractProtocol.PROTOCOL_121_90);
        }

        Type(int payloadType, AbstractProtocol protocol) {
            this.payloadType = payloadType;
            this.protocol = protocol;
        }

        public int getPayloadType() {
            return payloadType;
        }

        public static int getPayloadType(Type type, int protocolOrdinal) {
            return type == null || protocolOrdinal < type.protocol.ordinal() ? 0 : type.getPayloadType();
        }
    }
}
