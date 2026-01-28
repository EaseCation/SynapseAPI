package org.itxtech.synapseapi.multiprotocol.protocol11970.protocol;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BlockColor;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraFadeInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraFadeInstruction.TimeData;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSetInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraEase;

import java.io.IOException;

@ToString
public class CameraInstructionPacket11970 extends Packet11970 {
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

        //TODO: helper.putCameraInstruction(this, instruction);

        CompoundTag root = new CompoundTag();

        CameraSetInstruction set = instruction.set;
        if (set != null) {
            CompoundTag tag = new CompoundTag()
                    .putInt("preset", set.preset.runtimeId);

            CameraEase ease = set.ease;
            if (ease != null) {
                tag.putCompound("ease", new CompoundTag()
                        .putString("type", ease.type.toString())
                        .putFloat("time", ease.duration));
            }

            Vector3f pos = set.pos;
            if (pos != null) {
                tag.putCompound("pos", new CompoundTag()
                        .putList("pos", pos.toNbt()));
            }

            Vector2f rot = set.rot;
            if (rot != null) {
                tag.putCompound("rot", new CompoundTag()
                        .putFloat("x", rot.x)
                        .putFloat("y", rot.y));
            }

            Vector3f facing = set.facing;
            if (facing != null) {
                tag.putCompound("facing", new CompoundTag()
                        .putList("pos", facing.toNbt()));
            }

            Boolean defaultPreset = set.defaultPreset;
            if (defaultPreset != null) {
                tag.putBoolean("default", defaultPreset);
            }

            root.putCompound("set", tag);
        }

        Boolean clear = instruction.clear;
        if (clear != null) {
            root.putBoolean("clear", clear);
        }

        CameraFadeInstruction fade = instruction.fade;
        if (fade != null) {
            CompoundTag tag = new CompoundTag();

            TimeData time = fade.time;
            if (time != null) {
                tag.putCompound("time", new CompoundTag()
                        .putFloat("fadeIn", time.fadeInTime)
                        .putFloat("hold", time.stayTime)
                        .putFloat("fadeOut", time.fadeOutTime));
            }

            BlockColor color = fade.color;
            if (color != null) {
                tag.putCompound("color", new CompoundTag()
                        .putFloat("r", color.getRed() / 255f)
                        .putFloat("g", color.getBlue() / 255f) // Microjang is sending blue as green and green as blue :(
                        .putFloat("b", color.getGreen() / 255f));
            }

            root.putCompound("fade", tag);
        }

        byte[] bytes;
        try {
            bytes = NBTIO.writeNetwork(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        put(bytes);
    }
}
