package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraSplineDefinition {
    /**
     * The identifier for this spline, in "namespace:name" format.
     */
    public String name;
    /**
     * The total time, in seconds, to play back the spline.
     */
    public float totalTime;
    /**
     * The spline interpolation type.
     */
    @Default
    public SplineType type = SplineType.HERMITE;
    /**
     * Array of control point positions, each as [x, y, z] in world space.
     */
    @Default
    public Vector3f[] controlPoints = new Vector3f[0];
    /**
     * Key frames where each entry defines a progress alpha [0–1] and a time (in seconds) when the spline should reach that progress.
     */
    @Default
    public CameraSplineProgressKeyFrame[] progressKeyFrames = new CameraSplineProgressKeyFrame[0];
    /**
     * Array of rotation key frames, each with a rotation value and the time (in seconds) when it should be applied.
     */
    @Default
    public CameraSplineRotationKeyFrame[] rotationKeyFrames = new CameraSplineRotationKeyFrame[0];
}
