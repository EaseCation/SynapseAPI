package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.level.particle.Particle;

import java.util.HashMap;
import java.util.Map;

public final class ParticleIdTranslator {

    public final static Map<Integer, Integer> v12ToV112Book = new HashMap<>();

    public static final int TYPE_BUBBLE = 1;
    // 2 same as 1
    public static final int TYPE_CRITICAL = 3;
    public static final int TYPE_BLOCK_FORCE_FIELD = 4;
    public static final int TYPE_SMOKE = 5;
    public static final int TYPE_EXPLODE = 6;
    public static final int TYPE_EVAPORATION = 7;
    public static final int TYPE_FLAME = 8;
    public static final int TYPE_LAVA = 9;
    public static final int TYPE_LARGE_SMOKE = 10;
    public static final int TYPE_REDSTONE = 11;
    public static final int TYPE_RISING_RED_DUST = 12;
    // 62 same as 12
    public static final int TYPE_ITEM_BREAK = 13;
    public static final int TYPE_SNOWBALL_POOF = 14;
    public static final int TYPE_HUGE_EXPLODE = 15;
    // 60 same as 15
    public static final int TYPE_HUGE_EXPLODE_SEED = 16;
    public static final int TYPE_MOB_FLAME = 17;
    public static final int TYPE_HEART = 18;
    public static final int TYPE_TERRAIN = 19;
    public static final int TYPE_SUSPENDED_TOWN = 20, TYPE_TOWN_AURA = 20;
    // 61 same as 20
    public static final int TYPE_PORTAL = 21;
    // 22 same as 21
    public static final int TYPE_SPLASH = 23, TYPE_WATER_SPLASH = 23;
    // 24 same as 23
    public static final int TYPE_WATER_WAKE = 25;
    public static final int TYPE_DRIP_WATER = 26;
    public static final int TYPE_DRIP_LAVA = 27;
    public static final int TYPE_FALLING_DUST = 28, TYPE_DUST = 28;
    public static final int TYPE_MOB_SPELL = 29;
    public static final int TYPE_MOB_SPELL_AMBIENT = 30;
    public static final int TYPE_MOB_SPELL_INSTANTANEOUS = 31;
    public static final int TYPE_INK = 32;
    public static final int TYPE_SLIME = 33;
    public static final int TYPE_RAIN_SPLASH = 34;
    public static final int TYPE_VILLAGER_ANGRY = 35;
    // 59 same as 35
    public static final int TYPE_VILLAGER_HAPPY = 36;
    public static final int TYPE_ENCHANTMENT_TABLE = 37;
    public static final int TYPE_TRACKING_EMITTER = 38;
    public static final int TYPE_NOTE = 39;
    public static final int TYPE_WITCH_SPELL = 40;
    public static final int TYPE_CARROT = 41;
    // 42 unknown
    public static final int TYPE_END_ROD = 43;
    // 58 same as 43
    public static final int TYPE_DRAGONS_BREATH = 44;
    public static final int TYPE_SPIT = 45;
    public static final int TYPE_TOTEM = 46;
    public static final int TYPE_FOOD = 47;
    public static final int TYPE_FIREWORKS_STARTER = 48;
    public static final int TYPE_FIREWORKS_SPARK = 49;
    public static final int TYPE_FIREWORKS_OVERLAY = 50;
    public static final int TYPE_BALLOON_GAS = 51;
    public static final int TYPE_COLORED_FLAME = 52;
    public static final int TYPE_SPARKLER = 53;
    public static final int TYPE_CONDUIT = 54;
    public static final int TYPE_BUBBLE_COLUMN_UP = 55;
    public static final int TYPE_BUBBLE_COLUMN_DOWN = 56;
    public static final int TYPE_SNEEZE = 57;
    
    static {
        v12ToV112Book.put(Particle.TYPE_BUBBLE, TYPE_BUBBLE);
        v12ToV112Book.put(Particle.TYPE_CRITICAL, TYPE_CRITICAL);
        v12ToV112Book.put(Particle.TYPE_BLOCK_FORCE_FIELD, TYPE_BLOCK_FORCE_FIELD);
        v12ToV112Book.put(Particle.TYPE_SMOKE, TYPE_SMOKE);
        v12ToV112Book.put(Particle.TYPE_EXPLODE, TYPE_EXPLODE);
        v12ToV112Book.put(Particle.TYPE_EVAPORATION, TYPE_EVAPORATION);
        v12ToV112Book.put(Particle.TYPE_FLAME, TYPE_FLAME);
        v12ToV112Book.put(Particle.TYPE_LAVA, TYPE_LAVA);
        v12ToV112Book.put(Particle.TYPE_LARGE_SMOKE, TYPE_LARGE_SMOKE);
        v12ToV112Book.put(Particle.TYPE_REDSTONE, TYPE_REDSTONE);
        v12ToV112Book.put(Particle.TYPE_RISING_RED_DUST, TYPE_RISING_RED_DUST);
        v12ToV112Book.put(Particle.TYPE_ITEM_BREAK, TYPE_ITEM_BREAK);
        v12ToV112Book.put(Particle.TYPE_SNOWBALL_POOF, TYPE_SNOWBALL_POOF);
        v12ToV112Book.put(Particle.TYPE_HUGE_EXPLODE, TYPE_HUGE_EXPLODE);
        v12ToV112Book.put(Particle.TYPE_HUGE_EXPLODE_SEED, TYPE_HUGE_EXPLODE_SEED);
        v12ToV112Book.put(Particle.TYPE_MOB_FLAME, TYPE_MOB_FLAME);
        v12ToV112Book.put(Particle.TYPE_HEART, TYPE_HEART);
        v12ToV112Book.put(Particle.TYPE_TERRAIN, TYPE_TERRAIN);
        v12ToV112Book.put(Particle.TYPE_SUSPENDED_TOWN, TYPE_SUSPENDED_TOWN);
        v12ToV112Book.put(Particle.TYPE_PORTAL, TYPE_PORTAL);
        v12ToV112Book.put(Particle.TYPE_SPLASH, TYPE_SPLASH);
        v12ToV112Book.put(Particle.TYPE_WATER_WAKE, TYPE_WATER_WAKE);
        v12ToV112Book.put(Particle.TYPE_DRIP_WATER, TYPE_DRIP_WATER);
        v12ToV112Book.put(Particle.TYPE_DRIP_LAVA, TYPE_DRIP_LAVA);
        v12ToV112Book.put(Particle.TYPE_FALLING_DUST, TYPE_FALLING_DUST);
        v12ToV112Book.put(Particle.TYPE_MOB_SPELL, TYPE_MOB_SPELL);
        v12ToV112Book.put(Particle.TYPE_MOB_SPELL_AMBIENT, TYPE_MOB_SPELL_AMBIENT);
        v12ToV112Book.put(Particle.TYPE_MOB_SPELL_INSTANTANEOUS, TYPE_MOB_SPELL_INSTANTANEOUS);
        v12ToV112Book.put(Particle.TYPE_INK, TYPE_INK);
        v12ToV112Book.put(Particle.TYPE_SLIME, TYPE_SLIME);
        v12ToV112Book.put(Particle.TYPE_RAIN_SPLASH, TYPE_RAIN_SPLASH);
        v12ToV112Book.put(Particle.TYPE_VILLAGER_ANGRY, TYPE_VILLAGER_ANGRY);
        v12ToV112Book.put(Particle.TYPE_VILLAGER_HAPPY, TYPE_VILLAGER_HAPPY);
        v12ToV112Book.put(Particle.TYPE_ENCHANTMENT_TABLE, TYPE_ENCHANTMENT_TABLE);
        v12ToV112Book.put(Particle.TYPE_TRACKING_EMITTER, TYPE_TRACKING_EMITTER);
        v12ToV112Book.put(Particle.TYPE_NOTE, TYPE_NOTE);
        v12ToV112Book.put(Particle.TYPE_WITCH_SPELL, TYPE_WITCH_SPELL);
        v12ToV112Book.put(Particle.TYPE_CARROT, TYPE_CARROT);
        v12ToV112Book.put(Particle.TYPE_END_ROD, TYPE_END_ROD);
        v12ToV112Book.put(Particle.TYPE_DRAGONS_BREATH, TYPE_DRAGONS_BREATH);
        //v12ToV112Book.put(Particle.TYPE_SPIT, TYPE_SPIT);
        //v12ToV112Book.put(Particle.TYPE_TOTEM, TYPE_TOTEM);
        //v12ToV112Book.put(Particle.TYPE_FOOD, TYPE_FOOD);
        //v12ToV112Book.put(Particle.TYPE_FIREWORKS_STARTER, TYPE_FIREWORKS_STARTER);
        v12ToV112Book.put(Particle.TYPE_FIREWORKS_SPARK, TYPE_FIREWORKS_SPARK);
        //v12ToV112Book.put(Particle.TYPE_FIREWORKS_OVERLAY, TYPE_FIREWORKS_OVERLAY);
        //v12ToV112Book.put(Particle.TYPE_BALLOON_GAS, TYPE_BALLOON_GAS);
        //v12ToV112Book.put(Particle.TYPE_COLORED_FLAME, TYPE_COLORED_FLAME);
        //v12ToV112Book.put(Particle.TYPE_SPARKLER, TYPE_SPARKLER);
        //v12ToV112Book.put(Particle.TYPE_CONDUIT, TYPE_CONDUIT);
        //v12ToV112Book.put(Particle.TYPE_BUBBLE_COLUMN_UP, TYPE_BUBBLE_COLUMN_UP);
        //v12ToV112Book.put(Particle.TYPE_BUBBLE_COLUMN_DOWN, TYPE_BUBBLE_COLUMN_DOWN);
        //v12ToV112Book.put(Particle.TYPE_SNEEZE, TYPE_SNEEZE);
    }

    public static int translateTo112(int particleId) {
        return v12ToV112Book.getOrDefault(particleId, particleId);
    }
}
