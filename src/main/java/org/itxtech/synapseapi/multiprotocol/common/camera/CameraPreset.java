package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import lombok.*;
import lombok.Builder.Default;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraPreset {
    public static final CameraPreset FIRST_PERSON_PRESET = CameraPreset.builder()
            .name("minecraft:first_person")
            .build();
    /**
     * @since 1.21.70
     */
    public static final CameraPreset FIXED_BOOM_PRESET = CameraPreset.builder()
            .name("minecraft:fixed_boom")
            .viewOffset(new Vector2f(0, 0))
            .entityOffset(new Vector3f(-0, 0, 0))
            .alignTargetAndCameraForward(false)
            .build();
    /**
     * @since 1.21.40
     */
    public static final CameraPreset FOLLOW_ORBIT_PRESET = CameraPreset.builder()
            .name("minecraft:follow_orbit")
            .viewOffset(new Vector2f(0, 0))
            .entityOffset(new Vector3f(-0, 0, 0))
            .radius(10f)
            .build();
    /**
     * @since 1.22.
     */
    public static final CameraPreset CONTROL_SCHEME_CAMERA_PRESET = CameraPreset.builder()
            .name("minecraft:control_scheme_camera")
            .parent(FOLLOW_ORBIT_PRESET.name)
            .controlScheme(ControlScheme.CAMERA_RELATIVE)
            .build();
    public static final CameraPreset FREE_PRESET = CameraPreset.builder()
            .name("minecraft:free")
            .x(0f)
            .y(0f)
            .z(0f)
            .pitch(0f)
            .yaw(0f)
            .build();
    public static final CameraPreset THIRD_PERSON_PRESET = CameraPreset.builder()
            .name("minecraft:third_person")
            .build();
    public static final CameraPreset THIRD_PERSON_FRONT_PRESET = CameraPreset.builder()
            .name("minecraft:third_person_front")
            .build();

    public static final CameraPreset[] DEFAULT_PRESETS = new CameraPreset[]{
            FIRST_PERSON_PRESET,
            FREE_PRESET,
            THIRD_PERSON_PRESET,
            THIRD_PERSON_FRONT_PRESET,
    };
    public static final CameraPreset[] EMPTY_PRESETS = new CameraPreset[0];

    static {
        for (int i = 0; i < DEFAULT_PRESETS.length; i++) {
            DEFAULT_PRESETS[i].runtimeId = i;
        }
    }

    public static final byte AUDIO_LISTENER_CAMERA = 0;
    public static final byte AUDIO_LISTENER_PLAYER = 1;

    public CameraPreset(String name) {
        this.name = name;
    }

    public CameraPreset(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

    public int runtimeId;
    public String name;
    @Default
    public String parent = "";
    @Nullable
    public Float x;
    @Nullable
    public Float y;
    @Nullable
    public Float z;
    @Nullable
    public Float pitch;
    @Nullable
    public Float yaw;
    /**
     * @since 1.21.30
     */
    @Nullable
    public Float rotationSpeed;
    /**
     * @since 1.21.30
     */
    @Nullable
    public Boolean snapToTarget;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Vector2f horizontalRotationLimit;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Vector2f verticalRotationLimit;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Boolean continueTargeting;
    /**
     * @since 1.21.50
     */
    @Nullable
    public Float blockListeningRadius;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Vector2f viewOffset;
    /**
     * Changing the camera's pivot point from the center of the entity.
     * @since 1.21.30
     */
    @Nullable
    public Vector3f entityOffset;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Float radius;
    /**
     * @since 1.21.60
     */
    @Nullable
    public Float yawLimitMin;
    /**
     * @since 1.21.60
     */
    @Nullable
    public Float yawLimitMax;
    @Nullable
    public Byte audioListener;
    @Nullable
    public Boolean playerEffects;
    /**
     * @since 1.21.40
     * @deprecated 1.21.90 - use ControlScheme to switch between control schemes instead
     */
    @Nullable
    public Boolean alignTargetAndCameraForward;
    /**
     * @since 1.21.50
     */
    @Nullable
    public AimAssist aimAssist;
    /**
     * @see ControlScheme
     * @since 1.21.80
     */
    @Nullable
    public Integer controlScheme;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class AimAssist {
        public String presetId;
        @Nullable
        public Integer targetMode;
        @Nullable
        public Vector2f angle;
        @Nullable
        public Float distance;
    }
}
