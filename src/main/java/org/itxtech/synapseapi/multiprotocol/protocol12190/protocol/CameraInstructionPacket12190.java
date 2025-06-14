package org.itxtech.synapseapi.multiprotocol.protocol12190.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraFadeInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSetInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraTargetInstruction;

import javax.annotation.Nullable;

@ToString
public class CameraInstructionPacket12190 extends Packet12190 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_INSTRUCTION_PACKET;

    @Nullable
    public CameraSetInstruction set;
    @Nullable
    public Boolean clear;
    @Nullable
    public CameraFadeInstruction fade;
    @Nullable
    public CameraTargetInstruction target;
    @Nullable
    public Boolean removeTarget;

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

        putOptional(set, (stream, set) -> {
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

        putOptional(clear, BinaryStream::putBoolean);

        putOptional(fade, (stream, fade) -> {
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

        putOptional(target, (stream, target) -> {
            stream.putOptional(target.centerOffset, BinaryStream::putVector3f);
            stream.putLLong(target.entityId);
        });

        putOptional(removeTarget, BinaryStream::putBoolean);
    }
}
