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
    @Nullable
    public Byte audioListener;
    @Nullable
    public Boolean playerEffects;
    /**
     * @since 1.21.40
     */
    @Nullable
    public Boolean alignTargetAndCameraForward;
}
