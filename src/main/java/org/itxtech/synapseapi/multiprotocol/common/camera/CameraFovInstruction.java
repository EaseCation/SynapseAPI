package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.*;
import lombok.Builder.Default;

/**
 * @since 1.21.100
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CameraFovInstruction implements Cloneable {
    private static final CameraEase DEFAULT_EASE = new CameraEase(Float.intBitsToFloat(1));

    /**
     * 30~110
     */
    public float fieldOfView;
    @Default
    @NonNull
    public CameraEase ease = DEFAULT_EASE;
    public boolean clear;

    public CameraFovInstruction(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        this.ease = DEFAULT_EASE;
    }

    public CameraFovInstruction(float fieldOfView, @NonNull CameraEase ease) {
        this.fieldOfView = fieldOfView;
        this.ease = ease;
    }

    public CameraFovInstruction(boolean clear) {
        this.clear = clear;
        this.ease = DEFAULT_EASE;
    }

    public CameraFovInstruction(boolean clear, @NonNull CameraEase ease) {
        this.clear = clear;
        this.ease = ease;
    }

    @Override
    public CameraFovInstruction clone() {
        try {
            return (CameraFovInstruction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
