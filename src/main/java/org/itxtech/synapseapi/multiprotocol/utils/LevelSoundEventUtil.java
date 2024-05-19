package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.block.Block;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.Arrays;

import static cn.nukkit.network.protocol.LevelSoundEventPacket.*;

/**
 * LevelSoundEventPacket V3 utils
 */
public final class LevelSoundEventUtil {
    private static final boolean[] C2S_SOUNDS = new boolean[2048];

    public static int translateTo18ExtraData(int sound, int extraData, int pitch, AbstractProtocol protocol, boolean netease) {
        switch (sound) {
            case SOUND_PLACE:
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_STEP:
            case SOUND_HEAVY_STEP:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
            case SOUND_PRESSURE_PLATE_CLICK_OFF:
            case SOUND_PRESSURE_PLATE_CLICK_ON:
            case SOUND_BUTTON_CLICK_OFF:
            case SOUND_BUTTON_CLICK_ON:
            case SOUND_DOOR_OPEN:
            case SOUND_DOOR_CLOSE:
            case SOUND_TRAPDOOR_OPEN:
            case SOUND_TRAPDOOR_CLOSE:
            case SOUND_FENCE_GATE_OPEN:
            case SOUND_FENCE_GATE_CLOSE:
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, extraData >> Block.BLOCK_META_BITS, extraData & Block.BLOCK_META_MASK);
            case SOUND_NOTE:
                return (extraData << 8) | (pitch & 0b11111111);
        }
        return extraData;
    }

    public static int translateExtraDataFromClient(int sound, int extraData, AbstractProtocol protocol, boolean netease) {
        switch (sound) {
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_STEP:
            case SOUND_HEAVY_STEP:
            case SOUND_PLACE:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
            case SOUND_PRESSURE_PLATE_CLICK_OFF:
            case SOUND_PRESSURE_PLATE_CLICK_ON:
            case SOUND_BUTTON_CLICK_OFF:
            case SOUND_BUTTON_CLICK_ON:
            case SOUND_DOOR_OPEN:
            case SOUND_DOOR_CLOSE:
            case SOUND_TRAPDOOR_OPEN:
            case SOUND_TRAPDOOR_CLOSE:
            case SOUND_FENCE_GATE_OPEN:
            case SOUND_FENCE_GATE_CLOSE:
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_114.getProtocolStart()) {
                    int legacyId = AdvancedGlobalBlockPalette.getLegacyId(protocol, netease, extraData);
                    if (legacyId == -1) {
                        return 0;
                    }
                    return legacyId;
                }
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_16.getProtocolStart()) {
                    int legacyId = AdvancedGlobalBlockPalette.getLegacyId(protocol, netease, extraData);
                    if (legacyId == -1) {
                        return 0;
                    }
                    return ((legacyId >> 6) << Block.BLOCK_META_BITS) | (legacyId & 0x3f);
                }
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_14.getProtocolStart()) {
                    int legacyId = GlobalBlockPalette.getLegacyId(extraData);
                    if (legacyId == -1) {
                        return 0;
                    }
                    return ((legacyId >> 4) << Block.BLOCK_META_BITS) | (legacyId & 0xf);
                }
                return ((extraData >> 4) << Block.BLOCK_META_BITS) | (extraData & 0xf);
        }
        return extraData;
    }

    public static boolean isC2SSound(int sound) {
        if (sound < 0 || sound >= C2S_SOUNDS.length) {
            return false;
        }
        return C2S_SOUNDS[sound];
    }

    static {
        //TODO
        Arrays.fill(C2S_SOUNDS, true);
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_THUNDER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_13] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_CAT] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_BLOCKS] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_CHIRP] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_FAR] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_MALL] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_MELLOHI] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_STAL] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_STRAD] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_WARD] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_11] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_RECORD_WAIT] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_STOP_RECORD] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_BLAZE] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_CAVE_SPIDER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_CREEPER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ELDER_GUARDIAN] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ENDER_DRAGON] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ENDERMAN] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_EVOCATION_ILLAGER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_GHAST] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_HUSK] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ILLUSION_ILLAGER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_MAGMA_CUBE] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_POLAR_BEAR] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_SHULKER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_SILVERFISH] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_SKELETON] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_SLIME] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_SPIDER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_STRAY] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_VEX] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_VINDICATION_ILLAGER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_WITCH] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_WITHER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_WITHER_SKELETON] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_WOLF] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ZOMBIE] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ZOMBIE_PIGMAN] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_IMITATE_ZOMBIE_VILLAGER] = false;
        C2S_SOUNDS[LevelSoundEventPacket.SOUND_BLOCK_END_PORTAL_SPAWN] = false;
    }

    private LevelSoundEventUtil() {
        throw new IllegalStateException();
    }
}
