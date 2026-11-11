package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.math.Mth;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSplineDefinition;
import org.itxtech.synapseapi.multiprotocol.common.camera.EasingType;
import org.itxtech.synapseapi.multiprotocol.common.camera.SplineType;

/**
 * Camera custom spline data sent from server to client.
 */
@ToString
public class CameraSplinePacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_SPLINE_PACKET;

    public CameraSplineDefinition[] splines = new CameraSplineDefinition[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putArray(splines, (stream, definition) -> {
            stream.putString(definition.name);
            if (!Float.isFinite(definition.totalTime)) throw new IllegalArgumentException("total time is invalid");
            stream.putLFloat(Math.max(definition.totalTime, 0));
            stream.putEnum(definition.type, SplineType::getName);

            stream.putArray(definition.controlPoints, BinaryStream::putVector3f);

            stream.putArray(definition.progressKeyFrames, (bs, progress) -> {
                if (!Float.isFinite(progress.progress)) throw new IllegalArgumentException("progress is invalid");
                bs.putLFloat(Mth.clamp(progress.progress, 0, 1));
                if (!Float.isFinite(progress.time)) throw new IllegalArgumentException("time is invalid");
                bs.putLFloat(Math.max(progress.time, 0));
                bs.putOptionalEnum(progress.easing, EasingType::getName);
            });

            stream.putArray(definition.rotationKeyFrames, (bs, rotation) -> {
                bs.putVector3f(rotation.rotationX, rotation.rotationY, rotation.rotationZ);
                if (!Float.isFinite(rotation.time)) throw new IllegalArgumentException("time is invalid");
                bs.putLFloat(Math.max(rotation.time, 0));
                bs.putOptionalEnum(rotation.easing, EasingType::getName);
            });
        });
    }
}
