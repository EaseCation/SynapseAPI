package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.math.Vector2f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

/**
 * Syncs LocatorBar changes on the server with the client.
 */
@ToString
public class LocatorBarPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.LOCATOR_BAR_PACKET;

    public Waypoint[] waypoints;

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
        putArray(waypoints, (stream, waypoint) -> {
            stream.putUUID(waypoint.groupHandle);

            stream.putLInt(waypoint.updateFlag);
            stream.putOptional(waypoint.visible, BinaryStream::putBoolean);
            stream.putOptional(waypoint.dimensionLocation, (bs, location) -> {
                bs.putVector3f(location.x, location.y, location.z);
                bs.putVarInt(location.dimension);
            });
            stream.putOptional(waypoint.textureId, BinaryStream::putLInt);
            stream.putOptional(waypoint.color, (bs, color) -> bs.putLInt(color.getRGB()));
            stream.putOptional(waypoint.clientPositionAuthority, BinaryStream::putBoolean);
            stream.putOptional(waypoint.entityUniqueId, BinaryStream::putEntityUniqueId);

            stream.putByte(waypoint.action);
        });
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Waypoint {
        public static final int ACTION_NONE = 0;
        public static final int ACTION_ADD = 1;
        public static final int ACTION_REMOVE = 2;
        public static final int ACTION_UPDATE = 3;

        public static final int FLAG_WORLD_POS = 1 << 0;
        public static final int FLAG_VISIBILITY = 1 << 1;
        public static final int FLAG_TEXTURE = 1 << 2;
        public static final int FLAG_COLOR = 1 << 3;
        public static final int FLAG_CLIENT_POSITION_AUTHORITY = 1 << 4;

        public static final int TEXTURE_SQUARE = 0;
        public static final int TEXTURE_CIRCLE = 1;
        public static final int TEXTURE_SMALL_SQUARE = 2;
        public static final int TEXTURE_SMALL_STAR = 3;
        public static final int TEXTURE_TINY_SQUARE = 4;
        public static final int TEXTURE_TINY_STAR = 5;

        public UUID groupHandle;

        public int updateFlag;
        @Nullable
        public Boolean visible;
        @Nullable
        public DimensionLocation dimensionLocation;
        /**
         * @deprecated 1.26.20
         */
        @Deprecated
        @Nullable
        public Integer textureId;
        /**
         * @since 1.26.20
         */
        @Nullable
        public String texturePath;
        /**
         * @since 1.26.20
         */
        @Nullable
        public Vector2f iconSize;
        @Nullable
        public Color color;
        @Nullable
        public Boolean clientPositionAuthority;
        @Nullable
        public Long entityUniqueId;

        public int action = ACTION_NONE;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DimensionLocation {
        public float x;
        public float y;
        public float z;
        public int dimension;
    }
}
