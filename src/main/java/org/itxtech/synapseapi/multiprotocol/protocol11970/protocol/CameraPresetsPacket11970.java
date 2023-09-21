package org.itxtech.synapseapi.multiprotocol.protocol11970.protocol;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraPreset;

import java.io.IOException;

@ToString
public class CameraPresetsPacket11970 extends Packet11970 {
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

        ListTag<CompoundTag> list = new ListTag<>();
        for (CameraPreset preset : presets) {
            CompoundTag entry = new CompoundTag(7)
                    .putString("identifier", preset.name)
                    .putString("inherit_from", preset.parent);

            if (preset.x != null) {
                entry.putFloat("pos_x", preset.x);
            }
            if (preset.y != null) {
                entry.putFloat("pos_y", preset.y);
            }
            if (preset.z != null) {
                entry.putFloat("pos_z", preset.z);
            }
            if (preset.pitch != null) {
                entry.putFloat("rot_x", preset.pitch);
            }
            if (preset.yaw != null) {
                entry.putFloat("rot_y", preset.yaw);
            }

            list.add(entry);
        }

        CompoundTag root = new CompoundTag(1)
                .putList("presets", list);
        byte[] bytes;
        try {
            bytes = NBTIO.writeNetwork(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        put(bytes);
    }
}
