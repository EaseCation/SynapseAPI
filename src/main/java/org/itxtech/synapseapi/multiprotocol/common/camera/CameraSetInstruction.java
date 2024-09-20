package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraSetInstruction {

    public CameraPreset preset;
    @Nullable
    public Ease ease;

    @Nullable
    public Vector3f pos;
    @Nullable
    public Vector2f rot;
    @Nullable
    public Vector3f facing;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Vector2f viewOffset;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Vector3f entityOffset;
    @Nullable
    public Boolean defaultPreset;

    /**
     * @see <a href="https://easings.net/">Easing Functions Cheat Sheet</a>
     */
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Ease {
        public static final byte LINEAR = 0;
        public static final byte SPRING = 1;
        public static final byte IN_QUAD = 2;
        public static final byte OUT_QUAD = 3;
        public static final byte IN_OUT_QUAD = 4;
        public static final byte IN_CUBIC = 5;
        public static final byte OUT_CUBIC = 6;
        public static final byte IN_OUT_CUBIC = 7;
        public static final byte IN_QUART = 8;
        public static final byte OUT_QUART = 9;
        public static final byte IN_OUT_QUART = 10;
        public static final byte IN_QUINT = 11;
        public static final byte OUT_QUINT = 12;
        public static final byte IN_OUT_QUINT = 13;
        public static final byte IN_SINE = 14;
        public static final byte OUT_SINE = 15;
        public static final byte IN_OUT_SINE = 16;
        public static final byte IN_EXPO = 17;
        public static final byte OUT_EXPO = 18;
        public static final byte IN_OUT_EXPO = 19;
        public static final byte IN_CIRC = 20;
        public static final byte OUT_CIRC = 21;
        public static final byte IN_OUT_CIRC = 22;
        public static final byte IN_BOUNCE = 23;
        public static final byte OUT_BOUNCE = 24;
        public static final byte IN_OUT_BOUNCE = 25;
        public static final byte IN_BACK = 26;
        public static final byte OUT_BACK = 27;
        public static final byte IN_OUT_BACK = 28;
        public static final byte IN_ELASTIC = 29;
        public static final byte OUT_ELASTIC = 30;
        public static final byte IN_OUT_ELASTIC = 31;

        public byte type = LINEAR;
        public float duration;
    }
}
