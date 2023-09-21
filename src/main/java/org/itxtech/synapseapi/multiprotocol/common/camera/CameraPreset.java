package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraPreset {
    public static final CameraPreset[] DEFAULT_PRESETS = new CameraPreset[]{
            CameraPreset.builder()
                    .name("minecraft:first_person")
                    .build(),
            CameraPreset.builder()
                    .name("minecraft:free")
                    .x(0f)
                    .y(0f)
                    .z(0f)
                    .pitch(0f)
                    .yaw(0f)
                    .build(),
            CameraPreset.builder()
                    .name("minecraft:third_person")
                    .build(),
            CameraPreset.builder()
                    .name("minecraft:third_person_front")
                    .build(),
    };
    public static final CameraPreset[] EMPTY_PRESETS = new CameraPreset[0];

    public static final byte AUDIO_LISTENER_CAMERA = 0;
    public static final byte AUDIO_LISTENER_PLAYER = 1;

    public CameraPreset(String name) {
        this.name = name;
    }

    public CameraPreset(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

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
    @Nullable
    public Byte audioListener;
    @Nullable
    public Boolean playerEffects;
}
