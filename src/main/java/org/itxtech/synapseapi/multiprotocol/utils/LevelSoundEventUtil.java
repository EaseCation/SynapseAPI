package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.network.protocol.LevelSoundEventPacket;

import java.util.Arrays;

public final class LevelSoundEventUtil {
    private static final boolean[] C2S_SOUNDS = new boolean[2048];

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
