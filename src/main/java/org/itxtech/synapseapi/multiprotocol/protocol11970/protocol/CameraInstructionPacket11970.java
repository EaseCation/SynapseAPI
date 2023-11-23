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
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSetInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraSetInstruction.Ease;

import javax.annotation.Nullable;
import java.io.IOException;

@ToString
public class CameraInstructionPacket11970 extends Packet11970 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_INSTRUCTION_PACKET;

    @Nullable
    public CameraSetInstruction set;
    @Nullable
    public Boolean clear;
    @Nullable
    public CameraFadeInstruction fade;

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

        CompoundTag root = new CompoundTag();

        if (set != null) {
            CompoundTag tag = new CompoundTag()
                    .putInt("preset", set.preset.runtimeId);

            Ease ease = set.ease;
            if (ease != null) {
                tag.putCompound("ease", new CompoundTag()
                        .putString("type", lookupEaseName(ease.type))
                        .putFloat("time", ease.duration));
            }

            Vector3f pos = set.pos;
            if (pos != null) {
                tag.putList("pos", pos.toNbt());
            }

            Vector2f rot = set.rot;
            if (rot != null) {
                tag.putCompound("rot", new CompoundTag()
                        .putFloat("x", rot.x)
                        .putFloat("y", rot.y));
            }

            Vector3f facing = set.facing;
            if (facing != null) {
                tag.putList("facing", facing.toNbt()); //TODO: check
            }

            Boolean defaultPreset = set.defaultPreset;
            if (defaultPreset != null) {
                tag.putBoolean("default", defaultPreset);
            }

            root.putCompound("set", tag);
        }

        if (clear != null) {
            root.putBoolean("clear", clear);
        }

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

    private static String lookupEaseName(int type) {
        if (type < 0 || type >= EASE_TYPE_NAMES.length) {
            type = Ease.LINEAR;
        }
        return EASE_TYPE_NAMES[type];
    }

    private static final String[] EASE_TYPE_NAMES = new String[32];

    static {
        EASE_TYPE_NAMES[Ease.LINEAR] = "linear";
        EASE_TYPE_NAMES[Ease.SPRING] = "spring";
        EASE_TYPE_NAMES[Ease.IN_QUAD] = "in_quad";
        EASE_TYPE_NAMES[Ease.OUT_QUAD] = "out_quad";
        EASE_TYPE_NAMES[Ease.IN_OUT_QUAD] = "in_out_quad";
        EASE_TYPE_NAMES[Ease.IN_CUBIC] = "in_cubic";
        EASE_TYPE_NAMES[Ease.OUT_CUBIC] = "out_cubic";
        EASE_TYPE_NAMES[Ease.IN_OUT_CUBIC] = "in_out_cubic";
        EASE_TYPE_NAMES[Ease.IN_QUART] = "in_quart";
        EASE_TYPE_NAMES[Ease.OUT_QUART] = "out_quart";
        EASE_TYPE_NAMES[Ease.IN_OUT_QUART] = "in_out_quart";
        EASE_TYPE_NAMES[Ease.IN_QUINT] = "in_quint";
        EASE_TYPE_NAMES[Ease.OUT_QUINT] = "out_quint";
        EASE_TYPE_NAMES[Ease.IN_OUT_QUINT] = "in_out_quint";
        EASE_TYPE_NAMES[Ease.IN_SINE] = "in_sine";
        EASE_TYPE_NAMES[Ease.OUT_SINE] = "out_sine";
        EASE_TYPE_NAMES[Ease.IN_OUT_SINE] = "in_out_sine";
        EASE_TYPE_NAMES[Ease.IN_EXPO] = "in_expo";
        EASE_TYPE_NAMES[Ease.OUT_EXPO] = "out_expo";
        EASE_TYPE_NAMES[Ease.IN_OUT_EXPO] = "in_out_expo";
        EASE_TYPE_NAMES[Ease.IN_CIRC] = "in_circ";
        EASE_TYPE_NAMES[Ease.OUT_CIRC] = "out_circ";
        EASE_TYPE_NAMES[Ease.IN_OUT_CIRC] = "in_out_circ";
        EASE_TYPE_NAMES[Ease.IN_BOUNCE] = "in_bounce";
        EASE_TYPE_NAMES[Ease.OUT_BOUNCE] = "out_bounce";
        EASE_TYPE_NAMES[Ease.IN_OUT_BOUNCE] = "in_out_bounce";
        EASE_TYPE_NAMES[Ease.IN_BACK] = "in_back";
        EASE_TYPE_NAMES[Ease.OUT_BACK] = "out_back";
        EASE_TYPE_NAMES[Ease.IN_OUT_BACK] = "in_out_back";
        EASE_TYPE_NAMES[Ease.IN_ELASTIC] = "in_elastic";
        EASE_TYPE_NAMES[Ease.OUT_ELASTIC] = "out_elastic";
        EASE_TYPE_NAMES[Ease.IN_OUT_ELASTIC] = "in_out_elastic";
    }
}
