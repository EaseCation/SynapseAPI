package org.itxtech.synapseapi.multiprotocol.protocol121120.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraInstruction;

@ToString
public class CameraInstructionPacket121120 extends Packet121120 {
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
                bs.putByte(ease.type);
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
            stream.putByte(fov.ease.type);
            stream.putBoolean(fov.clear);
        });

        putOptional(instruction.spline, (stream, spline) -> {
            stream.putLFloat(spline.totalTime);
            stream.putByte(spline.type.ordinal());
            stream.putUnsignedVarInt(spline.curve.length);
            for (Vector3f controlPoint : spline.curve) {
                stream.putVector3f(controlPoint);
            }
            stream.putUnsignedVarInt(spline.progressKeyFrames.length);
            for (FloatFloatPair progressKeyFrame : spline.progressKeyFrames) {
                stream.putLFloat(progressKeyFrame.leftFloat());
                stream.putLFloat(progressKeyFrame.rightFloat());
            }
            stream.putUnsignedVarInt(spline.rotationOption.length);
            for (ObjectFloatPair<Vector3f> rotationKeyFrame : spline.rotationOption) {
                stream.putVector3f(rotationKeyFrame.left());
                stream.putLFloat(rotationKeyFrame.rightFloat());
            }
        });

        putOptional(instruction.attachToEntity, (stream, target) -> {
            stream.putLLong(target.entityId);
        });

        putOptional(instruction.detachFromEntity, BinaryStream::putBoolean);
    }
}
