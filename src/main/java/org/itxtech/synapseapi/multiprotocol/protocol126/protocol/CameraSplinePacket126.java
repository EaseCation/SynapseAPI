package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSplineDefinition;

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
            stream.putLFloat(definition.totalTime);
            stream.putString(definition.type.toString());

            stream.putArray(definition.controlPoints, BinaryStream::putVector3f);

            stream.putArray(definition.progressKeyFrames, (bs, progress) -> {
                bs.putLFloat(progress.progress);
                bs.putLFloat(progress.time);
                bs.putOptional(progress.easing, (s, type) -> s.putByte(type.ordinal()));
            });

            stream.putArray(definition.rotationKeyFrames, (bs, rotation) -> {
                bs.putVector3f(rotation.rotationX, rotation.rotationY, rotation.rotationZ);
                bs.putLFloat(rotation.time);
                bs.putOptional(rotation.easing, (s, type) -> s.putByte(type.ordinal()));
            });
        });
    }
}
