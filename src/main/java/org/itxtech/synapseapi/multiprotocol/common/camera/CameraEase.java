package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraEase {
    @Default
    public EasingType type = EasingType.LINEAR;
    public float duration;

    public CameraEase(float duration) {
        this.duration = duration;
    }
}
