package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector3f;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @since 1.21.120
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraSplineInstruction {
    /// TimeSeconds
    public float totalTime;
    @Default
    public SplineType type = SplineType.LINE;
    /// ControlPoint[]
    @Default
    public Vector3f[] curve = new Vector3f[0];
    /// Pair<Alpha, TimeSeconds>[]
    @Default
    public FloatFloatPair[] progressKeyFrames = new FloatFloatPair[0];
    /// Pair<Rotation, TimeSeconds>[]
    @Default
    public ObjectFloatPair<Vector3f>[] rotationOption = new ObjectFloatPair[0];

    public enum SplineType {
        LINE,
        HERMITE,
    }
}
