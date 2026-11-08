package org.itxtech.synapseapi.locator;

import cn.nukkit.math.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Waypoint {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_ADD = 1;
    public static final int ACTION_REMOVE = 2;
    public static final int ACTION_UPDATE = 3;

    public static final String DEFAULT_TEXTURE = "textures/ui/locator_bar_dot_0";

    public static final int TEXTURE_SQUARE = 0;
    public static final int TEXTURE_CIRCLE = 1;
    public static final int TEXTURE_SMALL_SQUARE = 2;
    public static final int TEXTURE_SMALL_STAR = 3;
    public static final int TEXTURE_TINY_SQUARE = 4;
    public static final int TEXTURE_TINY_STAR = 5;

    public UUID groupHandle;

    @Default
    public EnumSet<UpdateFlag> updateFlag = EnumSet.noneOf(UpdateFlag.class);
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

    public enum UpdateFlag {
        WORLD_POS(0),
        VISIBILITY(1),
        TEXTURE(2),
        ICON_SIZE(-1),
        COLOR(3),
        CLIENT_POSITION_AUTHORITY(4);

        private final int legacyId;

        UpdateFlag(int legacyId) {
            this.legacyId = legacyId;
        }

        public int getLegacyId() {
            return legacyId;
        }
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
