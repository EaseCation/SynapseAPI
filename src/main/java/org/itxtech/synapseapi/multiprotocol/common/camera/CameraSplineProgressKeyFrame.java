package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraSplineProgressKeyFrame {
    /**
     * Normalized progress of this frame along the spline, from 0.0 (start) to 1.0 (end).
     */
    public float progress;
    /**
     * Time (in seconds) at which this progress should be applied.
     */
    public float time;
    /**
     * Optional easing function used to interpolate towards this progress key frame.
     * Defaults to {@link EasingType#LINEAR} if omitted.
     * @since 1.26.0
     */
    @Nullable
    public EasingType easing;
}
