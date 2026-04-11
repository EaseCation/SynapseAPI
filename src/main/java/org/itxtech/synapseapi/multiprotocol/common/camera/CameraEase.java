package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.*;
import lombok.Builder.Default;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraEase {
    @Default
    @NonNull
    public EasingType type = EasingType.LINEAR;
    public float duration;

    public CameraEase(float duration) {
        this.duration = duration;
        this.type = EasingType.LINEAR;
    }
}
