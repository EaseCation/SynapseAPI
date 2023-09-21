package org.itxtech.synapseapi.multiprotocol.protocol12030.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraPreset;

@ToString
public class CameraPresetsPacket12030 extends Packet12030 {
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

            putOptional(preset.audioListener, BinaryStream::putByte);
            putOptional(preset.playerEffects, BinaryStream::putBoolean);
        }
    }
}
