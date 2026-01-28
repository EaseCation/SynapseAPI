package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.*;

@ToString
public class CameraInstructionPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_INSTRUCTION_PACKET;

    public CameraInstruction instruction;

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

        putOptional(instruction.set, (stream, set) -> {
            stream.putLInt(set.preset.runtimeId);
            stream.putOptional(set.ease, (bs, ease) -> {
                bs.putByte(ease.type.ordinal());
                bs.putLFloat(ease.duration);
            });
            stream.putOptional(set.pos, BinaryStream::putVector3f);
            stream.putOptional(set.rot, BinaryStream::putVector2f);
            stream.putOptional(set.facing, BinaryStream::putVector3f);
            stream.putOptional(set.viewOffset, BinaryStream::putVector2f);
            stream.putOptional(set.entityOffset, BinaryStream::putVector3f);
            stream.putOptional(set.defaultPreset, BinaryStream::putBoolean);
            stream.putBoolean(set.removeIgnoreStartingValuesComponent);
        });

        putOptional(instruction.clear, BinaryStream::putBoolean);

        putOptional(instruction.fade, (stream, fade) -> {
            stream.putOptional(fade.time, (bs, time) -> {
                bs.putLFloat(time.fadeInTime);
                bs.putLFloat(time.stayTime);
                bs.putLFloat(time.fadeOutTime);
            });
            stream.putOptional(fade.color, (bs, color) -> {
                bs.putLFloat(color.getRed() / 255f);
                bs.putLFloat(color.getGreen() / 255f);
                bs.putLFloat(color.getBlue() / 255f);
            });
        });

        putOptional(instruction.target, (stream, target) -> {
            stream.putOptional(target.centerOffset, BinaryStream::putVector3f);
            stream.putLLong(target.entityId);
        });

        putOptional(instruction.removeTarget, BinaryStream::putBoolean);

        putOptional(instruction.fieldOfView, (stream, fov) -> {
            stream.putLFloat(fov.fieldOfView);
            stream.putLFloat(fov.ease.duration);
            stream.putByte(fov.ease.type.ordinal());
            stream.putBoolean(fov.clear);
        });

        putOptional(instruction.spline, (stream, spline) -> {
            stream.putLFloat(spline.totalTime);
            stream.putByte(spline.type.ordinal());
            stream.putUnsignedVarInt(spline.controlPoints.length);
            for (Vector3f controlPoint : spline.controlPoints) {
                stream.putVector3f(controlPoint);
            }
            stream.putUnsignedVarInt(spline.progressKeyFrames.length);
            for (CameraSplineProgressKeyFrame progressKeyFrame : spline.progressKeyFrames) {
                stream.putLFloat(progressKeyFrame.progress);
                stream.putLFloat(progressKeyFrame.time);
                stream.putByte((progressKeyFrame.easing != null ? progressKeyFrame.easing : EasingType.LINEAR).ordinal());
            }
            stream.putUnsignedVarInt(spline.rotationKeyFrames.length);
            for (CameraSplineRotationKeyFrame rotationKeyFrame : spline.rotationKeyFrames) {
                stream.putVector3f(rotationKeyFrame.rotationX, rotationKeyFrame.rotationY, rotationKeyFrame.rotationZ);
                stream.putLFloat(rotationKeyFrame.time);
                stream.putByte((rotationKeyFrame.easing != null ? rotationKeyFrame.easing : EasingType.LINEAR).ordinal());
            }

            stream.putString(spline.name);
            stream.putBoolean(!spline.name.isEmpty()); // loadFromBehaviorPack(CameraSplinePacket)
        });

        putOptional(instruction.attachToEntity, (stream, target) -> {
            stream.putLLong(target.entityId);
        });

        putOptional(instruction.detachFromEntity, BinaryStream::putBoolean);
    }
}
