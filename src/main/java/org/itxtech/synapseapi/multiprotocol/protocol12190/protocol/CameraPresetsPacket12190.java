package org.itxtech.synapseapi.multiprotocol.protocol12190.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraPreset;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.Packet12180;

@ToString
public class CameraPresetsPacket12190 extends Packet12180 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_PRESETS_PACKET;

    public CameraPreset[] presets = CameraPreset.EMPTY_PRESETS;

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
        putUnsignedVarInt(presets.length);
        for (CameraPreset preset : presets) {
            putString(preset.name);
            putString(preset.parent);

            putOptional(preset.x, BinaryStream::putLFloat);
            putOptional(preset.y, BinaryStream::putLFloat);
            putOptional(preset.z, BinaryStream::putLFloat);
            putOptional(preset.pitch, BinaryStream::putLFloat);
            putOptional(preset.yaw, BinaryStream::putLFloat);

            putOptional(preset.rotationSpeed, BinaryStream::putLFloat);
            putOptional(preset.snapToTarget, BinaryStream::putBoolean);
            putOptional(preset.horizontalRotationLimit, BinaryStream::putVector2f);
            putOptional(preset.verticalRotationLimit, BinaryStream::putVector2f);
            putOptional(preset.continueTargeting, BinaryStream::putBoolean);
            putOptional(preset.blockListeningRadius, BinaryStream::putLFloat);
            putOptional(preset.viewOffset, BinaryStream::putVector2f);
            putOptional(preset.entityOffset, BinaryStream::putVector3f);
            putOptional(preset.radius, BinaryStream::putLFloat);
            putOptional(preset.yawLimitMin, BinaryStream::putLFloat);
            putOptional(preset.yawLimitMax, BinaryStream::putLFloat);

            putOptional(preset.audioListener, BinaryStream::putByte);
            putOptional(preset.playerEffects, BinaryStream::putBoolean);

            putOptional(preset.aimAssist, (stream, aimAssist) -> {
                putOptional(aimAssist.presetId, BinaryStream::putString);
                putOptional(aimAssist.targetMode, BinaryStream::putLInt);
                putOptional(aimAssist.angle, BinaryStream::putVector2f);
                putOptional(aimAssist.distance, BinaryStream::putLFloat);
            });

            putOptional(preset.controlScheme, BinaryStream::putLInt);
        }
    }
}
