package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.level.particle.Particle;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.Arrays;

public final class ParticleIdTranslator {

    public final static int[] v12ToV112Book = new int[Particle.TYPE_UNDEFINED];
    public final static int[] v12ToV114Book = new int[Particle.TYPE_UNDEFINED];
    public final static int[] v12ToV116220Book = new int[Particle.TYPE_UNDEFINED];
    public final static int[] v12ToV11710Book = new int[Particle.TYPE_UNDEFINED];

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

    public static final int V114_BUBBLE = 1;
    // 2 same as 1
    public static final int V114_CRITICAL = 3;
    public static final int V114_BLOCK_FORCE_FIELD = 4;
    public static final int V114_SMOKE = 5;
    public static final int V114_EXPLODE = 6;
    public static final int V114_EVAPORATION = 7;
    public static final int V114_FLAME = 8;
    public static final int V114_LAVA = 9;
    public static final int V114_LARGE_SMOKE = 10;
    public static final int V114_REDSTONE = 11;
    public static final int V114_RISING_RED_DUST = 12;
    public static final int V114_ITEM_BREAK = 13;
    public static final int V114_SNOWBALL_POOF = 14;
    public static final int V114_HUGE_EXPLODE = 15;
    public static final int V114_HUGE_EXPLODE_SEED = 16;
    public static final int V114_MOB_FLAME = 17;
    public static final int V114_HEART = 18;
    public static final int V114_TERRAIN = 19;
    public static final int V114_SUSPENDED_TOWN = 20, V114_TOWN_AURA = 20;
    // 61 same as 20
    public static final int V114_PORTAL = 21;
    // 22 same as 21
    public static final int V114_SPLASH = 23, V114_WATER_SPLASH = 23;
    // 24 same as 23
    public static final int V114_WATER_WAKE = 25;
    public static final int V114_DRIP_WATER = 26;
    public static final int V114_DRIP_LAVA = 27;
    public static final int V114_DRIP_HONEY = 28;
    public static final int V114_FALLING_DUST = 29, V114_DUST = 29;
    public static final int V114_MOB_SPELL = 30;
    public static final int V114_MOB_SPELL_AMBIENT = 31;
    public static final int V114_MOB_SPELL_INSTANTANEOUS = 32;
    public static final int V114_NOTE_AND_DUST = 33;
    public static final int V114_SLIME = 34;
    public static final int V114_RAIN_SPLASH = 35;
    public static final int V114_VILLAGER_ANGRY = 36;
    // 60 same as 36
    public static final int V114_VILLAGER_HAPPY = 37;
    public static final int V114_ENCHANTMENT_TABLE = 38;
    public static final int V114_TRACKING_EMITTER = 39;
    public static final int V114_NOTE = 40;
    public static final int V114_WITCH_SPELL = 41;
    public static final int V114_CARROT = 42;
    // 43 unknown
    public static final int V114_END_ROD = 44;
    // 59 same as 44
    public static final int V114_RISING_DRAGONS_BREATH = 45;
    public static final int V114_SPIT = 46;
    public static final int V114_TOTEM = 47;
    public static final int V114_FOOD = 48;
    public static final int V114_FIREWORKS_STARTER = 49;
    public static final int V114_FIREWORKS_SPARK = 50;
    public static final int V114_FIREWORKS_OVERLAY = 51;
    public static final int V114_BALLOON_GAS = 52;
    public static final int V114_COLORED_FLAME = 53;
    public static final int V114_SPARKLER = 54;
    public static final int V114_CONDUIT = 55;
    public static final int V114_BUBBLE_COLUMN_UP = 56;
    public static final int V114_BUBBLE_COLUMN_DOWN = 57;
    public static final int V114_SNEEZE = 58;
    public static final int V114_LARGE_EXPLOSION = 61;
    public static final int V114_INK = 62;
    public static final int V114_FALLING_RED_DUST = 63;
    public static final int V114_CAMPFIRE_SMOKE = 64;
    //65 same as 64
    public static final int V114_FALLING_DRAGONS_BREATH = 66;
    public static final int V114_DRAGONS_BREATH = 67;

    public static final int V116220_BUBBLE = 1;
    public static final int V116220_BUBBLE_MANUAL = 2;
    public static final int V116220_CRITICAL = 3;
    public static final int V116220_BLOCK_FORCE_FIELD = 4;
    public static final int V116220_SMOKE = 5;
    public static final int V116220_EXPLODE = 6;
    public static final int V116220_EVAPORATION = 7;
    public static final int V116220_FLAME = 8;
    public static final int V116220_LAVA = 9;
    public static final int V116220_LARGE_SMOKE = 10;
    public static final int V116220_REDSTONE = 11;
    public static final int V116220_RISING_RED_DUST = 12;
    public static final int V116220_ITEM_BREAK = 13;
    public static final int V116220_SNOWBALL_POOF = 14;
    public static final int V116220_HUGE_EXPLODE = 15;
    public static final int V116220_HUGE_EXPLODE_SEED = 16;
    public static final int V116220_MOB_FLAME = 17;
    public static final int V116220_HEART = 18;
    public static final int V116220_TERRAIN = 19;
    public static final int V116220_SUSPENDED_TOWN = 20, V116220_TOWN_AURA = 20;
    public static final int V116220_PORTAL = 21;
    // 22 same as 21
    public static final int V116220_SPLASH = 23, V116220_WATER_SPLASH = 23;
    public static final int V116220_WATER_SPLASH_MANUAL = 24;
    public static final int V116220_WATER_WAKE = 25;
    public static final int V116220_DRIP_WATER = 26;
    public static final int V116220_DRIP_LAVA = 27;
    public static final int V116220_DRIP_HONEY = 28;
    public static final int V116220_STALACTITE_DRIP_WATER = 29;
    public static final int V116220_STALACTITE_DRIP_LAVA = 30;
    public static final int V116220_FALLING_DUST = 31, V116220_DUST = 31;
    public static final int V116220_MOB_SPELL = 32;
    public static final int V116220_MOB_SPELL_AMBIENT = 33;
    public static final int V116220_MOB_SPELL_INSTANTANEOUS = 34;
    public static final int V116220_INK = 35;
    public static final int V116220_SLIME = 36;
    public static final int V116220_RAIN_SPLASH = 37;
    public static final int V116220_VILLAGER_ANGRY = 38;
    public static final int V116220_VILLAGER_HAPPY = 39;
    public static final int V116220_ENCHANTMENT_TABLE = 40;
    public static final int V116220_TRACKING_EMITTER = 41;
    public static final int V116220_NOTE = 42;
    public static final int V116220_WITCH_SPELL = 43;
    public static final int V116220_CARROT = 44;
    public static final int V116220_MOB_APPEARANCE  = 45;
    public static final int V116220_END_ROD = 46;
    public static final int V116220_RISING_DRAGONS_BREATH = 47;
    public static final int V116220_SPIT = 48;
    public static final int V116220_TOTEM = 49;
    public static final int V116220_FOOD = 50;
    public static final int V116220_FIREWORKS_STARTER = 51;
    public static final int V116220_FIREWORKS_SPARK = 52;
    public static final int V116220_FIREWORKS_OVERLAY = 53;
    public static final int V116220_BALLOON_GAS = 54;
    public static final int V116220_COLORED_FLAME = 55;
    public static final int V116220_SPARKLER = 56;
    public static final int V116220_CONDUIT = 57;
    public static final int V116220_BUBBLE_COLUMN_UP = 58;
    public static final int V116220_BUBBLE_COLUMN_DOWN = 59;
    public static final int V116220_SNEEZE = 60;
    public static final int V116220_SHULKER_BULLET = 61;
    public static final int V116220_BLEACH = 62;
    public static final int V116220_LARGE_EXPLOSION = 63;
    public static final int V116220_MYCELIUM_DUST = 64;
    public static final int V116220_FALLING_RED_DUST = 65;
    public static final int V116220_CAMPFIRE_SMOKE = 66;
    public static final int V116220_TALL_CAMPFIRE_SMOKE = 67;
    public static final int V116220_FALLING_DRAGONS_BREATH = 68;
    public static final int V116220_DRAGONS_BREATH = 69;
    public static final int V116220_BLUE_FLAME = 70;
    public static final int V116220_SOUL = 71;
    public static final int V116220_OBSIDIAN_TEAR = 72;
    public static final int V116220_PORTAL_REVERSE = 73;
    public static final int V116220_SNOWFLAKE = 74;
    public static final int V116220_VIBRATION_SIGNAL = 75;
    public static final int V116220_SCULK_SENSOR_REDSTONE = 76;
    public static final int V116220_SPORE_BLOSSOM_SHOWER = 77;
    public static final int V116220_SPORE_BLOSSOM_AMBIENT = 78;
    public static final int V116220_WAX = 79;
    public static final int V116220_ELECTRIC_SPARK = 80;

    public static final int V11710_BUBBLE = 1;
    public static final int V11710_BUBBLE_MANUAL = 2;
    public static final int V11710_CRITICAL = 3;
    public static final int V11710_BLOCK_FORCE_FIELD = 4;
    public static final int V11710_SMOKE = 5;
    public static final int V11710_EXPLODE = 6;
    public static final int V11710_EVAPORATION = 7;
    public static final int V11710_FLAME = 8;
    public static final int V11710_CANDLE_FLAME = 9;
    public static final int V11710_LAVA = 10;
    public static final int V11710_LARGE_SMOKE = 11;
    public static final int V11710_REDSTONE = 12;
    public static final int V11710_RISING_RED_DUST = 13;
    public static final int V11710_ITEM_BREAK = 14;
    public static final int V11710_SNOWBALL_POOF = 15;
    public static final int V11710_HUGE_EXPLODE = 16;
    public static final int V11710_HUGE_EXPLODE_SEED = 17;
    public static final int V11710_MOB_FLAME = 18;
    public static final int V11710_HEART = 19;
    public static final int V11710_TERRAIN = 20;
    public static final int V11710_SUSPENDED_TOWN = 21, V11710_TOWN_AURA = 21;
    public static final int V11710_PORTAL = 22;
    // 23 same as 22
    public static final int V11710_SPLASH = 24, V11710_WATER_SPLASH = 24;
    public static final int V11710_WATER_SPLASH_MANUAL = 25;
    public static final int V11710_WATER_WAKE = 26;
    public static final int V11710_DRIP_WATER = 27;
    public static final int V11710_DRIP_LAVA = 28;
    public static final int V11710_DRIP_HONEY = 29;
    public static final int V11710_STALACTITE_DRIP_WATER = 30;
    public static final int V11710_STALACTITE_DRIP_LAVA = 31;
    public static final int V11710_FALLING_DUST = 32, V11710_DUST = 32;
    public static final int V11710_MOB_SPELL = 33;
    public static final int V11710_MOB_SPELL_AMBIENT = 34;
    public static final int V11710_MOB_SPELL_INSTANTANEOUS = 35;
    public static final int V11710_INK = 36;
    public static final int V11710_SLIME = 37;
    public static final int V11710_RAIN_SPLASH = 38;
    public static final int V11710_VILLAGER_ANGRY = 39;
    public static final int V11710_VILLAGER_HAPPY = 40;
    public static final int V11710_ENCHANTMENT_TABLE = 41;
    public static final int V11710_TRACKING_EMITTER = 42;
    public static final int V11710_NOTE = 43;
    public static final int V11710_WITCH_SPELL = 44;
    public static final int V11710_CARROT = 45;
    public static final int V11710_MOB_APPEARANCE  = 46;
    public static final int V11710_END_ROD = 47;
    public static final int V11710_RISING_DRAGONS_BREATH = 48;
    public static final int V11710_SPIT = 49;
    public static final int V11710_TOTEM = 50;
    public static final int V11710_FOOD = 51;
    public static final int V11710_FIREWORKS_STARTER = 52;
    public static final int V11710_FIREWORKS_SPARK = 53;
    public static final int V11710_FIREWORKS_OVERLAY = 54;
    public static final int V11710_BALLOON_GAS = 55;
    public static final int V11710_COLORED_FLAME = 56;
    public static final int V11710_SPARKLER = 57;
    public static final int V11710_CONDUIT = 58;
    public static final int V11710_BUBBLE_COLUMN_UP = 59;
    public static final int V11710_BUBBLE_COLUMN_DOWN = 60;
    public static final int V11710_SNEEZE = 61;
    public static final int V11710_SHULKER_BULLET = 62;
    public static final int V11710_BLEACH = 63;
    public static final int V11710_LARGE_EXPLOSION = 64;
    public static final int V11710_MYCELIUM_DUST = 65;
    public static final int V11710_FALLING_RED_DUST = 66;
    public static final int V11710_CAMPFIRE_SMOKE = 67;
    public static final int V11710_TALL_CAMPFIRE_SMOKE = 68;
    public static final int V11710_FALLING_DRAGONS_BREATH = 69;
    public static final int V11710_DRAGONS_BREATH = 70;
    public static final int V11710_BLUE_FLAME = 71;
    public static final int V11710_SOUL = 72;
    public static final int V11710_OBSIDIAN_TEAR = 73;
    public static final int V11710_PORTAL_REVERSE = 74;
    public static final int V11710_SNOWFLAKE = 75;
    public static final int V11710_VIBRATION_SIGNAL = 76;
    public static final int V11710_SCULK_SENSOR_REDSTONE = 77;
    public static final int V11710_SPORE_BLOSSOM_SHOWER = 78;
    public static final int V11710_SPORE_BLOSSOM_AMBIENT = 79;
    public static final int V11710_WAX = 80;
    public static final int V11710_ELECTRIC_SPARK = 81;

    static {
        Arrays.fill(v12ToV112Book, -1);
        v12ToV112Book[Particle.TYPE_BUBBLE] = TYPE_BUBBLE;
        v12ToV112Book[Particle.TYPE_CRITICAL] = TYPE_CRITICAL;
        v12ToV112Book[Particle.TYPE_BLOCK_FORCE_FIELD] = TYPE_BLOCK_FORCE_FIELD;
        v12ToV112Book[Particle.TYPE_SMOKE] = TYPE_SMOKE;
        v12ToV112Book[Particle.TYPE_EXPLODE] = TYPE_EXPLODE;
        v12ToV112Book[Particle.TYPE_EVAPORATION] = TYPE_EVAPORATION;
        v12ToV112Book[Particle.TYPE_FLAME] = TYPE_FLAME;
        v12ToV112Book[Particle.TYPE_LAVA] = TYPE_LAVA;
        v12ToV112Book[Particle.TYPE_LARGE_SMOKE] = TYPE_LARGE_SMOKE;
        v12ToV112Book[Particle.TYPE_REDSTONE] = TYPE_REDSTONE;
        v12ToV112Book[Particle.TYPE_RISING_RED_DUST] = TYPE_RISING_RED_DUST;
        v12ToV112Book[Particle.TYPE_ITEM_BREAK] = TYPE_ITEM_BREAK;
        v12ToV112Book[Particle.TYPE_SNOWBALL_POOF] = TYPE_SNOWBALL_POOF;
        v12ToV112Book[Particle.TYPE_HUGE_EXPLODE] = TYPE_HUGE_EXPLODE;
        v12ToV112Book[Particle.TYPE_HUGE_EXPLODE_SEED] = TYPE_HUGE_EXPLODE_SEED;
        v12ToV112Book[Particle.TYPE_MOB_FLAME] = TYPE_MOB_FLAME;
        v12ToV112Book[Particle.TYPE_HEART] = TYPE_HEART;
        v12ToV112Book[Particle.TYPE_TERRAIN] = TYPE_TERRAIN;
        v12ToV112Book[Particle.TYPE_SUSPENDED_TOWN] = TYPE_SUSPENDED_TOWN;
        v12ToV112Book[Particle.TYPE_PORTAL] = TYPE_PORTAL;
        v12ToV112Book[Particle.TYPE_SPLASH] = TYPE_SPLASH;
        v12ToV112Book[Particle.TYPE_WATER_WAKE] = TYPE_WATER_WAKE;
        v12ToV112Book[Particle.TYPE_DRIP_WATER] = TYPE_DRIP_WATER;
        v12ToV112Book[Particle.TYPE_DRIP_LAVA] = TYPE_DRIP_LAVA;
        v12ToV112Book[Particle.TYPE_FALLING_DUST] = TYPE_FALLING_DUST;
        v12ToV112Book[Particle.TYPE_MOB_SPELL] = TYPE_MOB_SPELL;
        v12ToV112Book[Particle.TYPE_MOB_SPELL_AMBIENT] = TYPE_MOB_SPELL_AMBIENT;
        v12ToV112Book[Particle.TYPE_MOB_SPELL_INSTANTANEOUS] = TYPE_MOB_SPELL_INSTANTANEOUS;
        v12ToV112Book[Particle.TYPE_INK] = TYPE_INK;
        v12ToV112Book[Particle.TYPE_SLIME] = TYPE_SLIME;
        v12ToV112Book[Particle.TYPE_RAIN_SPLASH] = TYPE_RAIN_SPLASH;
        v12ToV112Book[Particle.TYPE_VILLAGER_ANGRY] = TYPE_VILLAGER_ANGRY;
        v12ToV112Book[Particle.TYPE_VILLAGER_HAPPY] = TYPE_VILLAGER_HAPPY;
        v12ToV112Book[Particle.TYPE_ENCHANTMENT_TABLE] = TYPE_ENCHANTMENT_TABLE;
        v12ToV112Book[Particle.TYPE_TRACKING_EMITTER] = TYPE_TRACKING_EMITTER;
        v12ToV112Book[Particle.TYPE_NOTE] = TYPE_NOTE;
        v12ToV112Book[Particle.TYPE_WITCH_SPELL] = TYPE_WITCH_SPELL;
        v12ToV112Book[Particle.TYPE_CARROT] = TYPE_CARROT;
        v12ToV112Book[Particle.TYPE_END_ROD] = TYPE_END_ROD;
        v12ToV112Book[Particle.TYPE_DRAGONS_BREATH] = TYPE_DRAGONS_BREATH;
        //v12ToV112Book[Particle.TYPE_SPIT] = TYPE_SPIT;
        //v12ToV112Book[Particle.TYPE_TOTEM] = TYPE_TOTEM;
        //v12ToV112Book[Particle.TYPE_FOOD] = TYPE_FOOD;
        //v12ToV112Book[Particle.TYPE_FIREWORKS_STARTER] = TYPE_FIREWORKS_STARTER;
        v12ToV112Book[Particle.TYPE_FIREWORKS_SPARK] = TYPE_FIREWORKS_SPARK;
        //v12ToV112Book[Particle.TYPE_FIREWORKS_OVERLAY] = TYPE_FIREWORKS_OVERLAY;
        //v12ToV112Book[Particle.TYPE_BALLOON_GAS] = TYPE_BALLOON_GAS;
        //v12ToV112Book[Particle.TYPE_COLORED_FLAME] = TYPE_COLORED_FLAME;
        //v12ToV112Book[Particle.TYPE_SPARKLER] = TYPE_SPARKLER;
        //v12ToV112Book[Particle.TYPE_CONDUIT] = TYPE_CONDUIT;
        //v12ToV112Book[Particle.TYPE_BUBBLE_COLUMN_UP] = TYPE_BUBBLE_COLUMN_UP;
        //v12ToV112Book[Particle.TYPE_BUBBLE_COLUMN_DOWN] = TYPE_BUBBLE_COLUMN_DOWN;
        //v12ToV112Book[Particle.TYPE_SNEEZE] = TYPE_SNEEZE;

        Arrays.fill(v12ToV114Book, -1);
        v12ToV114Book[Particle.TYPE_BUBBLE] = V114_BUBBLE;
        v12ToV114Book[Particle.TYPE_CRITICAL] = V114_CRITICAL;
        v12ToV114Book[Particle.TYPE_BLOCK_FORCE_FIELD] = V114_BLOCK_FORCE_FIELD;
        v12ToV114Book[Particle.TYPE_SMOKE] = V114_SMOKE;
        v12ToV114Book[Particle.TYPE_EXPLODE] = V114_EXPLODE;
        v12ToV114Book[Particle.TYPE_EVAPORATION] = V114_EVAPORATION;
        v12ToV114Book[Particle.TYPE_FLAME] = V114_FLAME;
        v12ToV114Book[Particle.TYPE_LAVA] = V114_LAVA;
        v12ToV114Book[Particle.TYPE_LARGE_SMOKE] = V114_LARGE_SMOKE;
        v12ToV114Book[Particle.TYPE_REDSTONE] = V114_REDSTONE;
        v12ToV114Book[Particle.TYPE_RISING_RED_DUST] = V114_RISING_RED_DUST;
        v12ToV114Book[Particle.TYPE_ITEM_BREAK] = V114_ITEM_BREAK;
        v12ToV114Book[Particle.TYPE_SNOWBALL_POOF] = V114_SNOWBALL_POOF;
        v12ToV114Book[Particle.TYPE_HUGE_EXPLODE] = V114_HUGE_EXPLODE;
        v12ToV114Book[Particle.TYPE_HUGE_EXPLODE_SEED] = V114_HUGE_EXPLODE_SEED;
        v12ToV114Book[Particle.TYPE_MOB_FLAME] = V114_MOB_FLAME;
        v12ToV114Book[Particle.TYPE_HEART] = V114_HEART;
        v12ToV114Book[Particle.TYPE_TERRAIN] = V114_TERRAIN;
        v12ToV114Book[Particle.TYPE_SUSPENDED_TOWN] = V114_SUSPENDED_TOWN;
        v12ToV114Book[Particle.TYPE_PORTAL] = V114_PORTAL;
        v12ToV114Book[Particle.TYPE_SPLASH] = V114_SPLASH;
        v12ToV114Book[Particle.TYPE_WATER_WAKE] = V114_WATER_WAKE;
        v12ToV114Book[Particle.TYPE_DRIP_WATER] = V114_DRIP_WATER;
        v12ToV114Book[Particle.TYPE_DRIP_LAVA] = V114_DRIP_LAVA;
        v12ToV114Book[Particle.TYPE_FALLING_DUST] = V114_FALLING_DUST;
        v12ToV114Book[Particle.TYPE_MOB_SPELL] = V114_MOB_SPELL;
        v12ToV114Book[Particle.TYPE_MOB_SPELL_AMBIENT] = V114_MOB_SPELL_AMBIENT;
        v12ToV114Book[Particle.TYPE_MOB_SPELL_INSTANTANEOUS] = V114_MOB_SPELL_INSTANTANEOUS;
        v12ToV114Book[Particle.TYPE_INK] = V114_INK;
        v12ToV114Book[Particle.TYPE_SLIME] = V114_SLIME;
        v12ToV114Book[Particle.TYPE_RAIN_SPLASH] = V114_RAIN_SPLASH;
        v12ToV114Book[Particle.TYPE_VILLAGER_ANGRY] = V114_VILLAGER_ANGRY;
        v12ToV114Book[Particle.TYPE_VILLAGER_HAPPY] = V114_VILLAGER_HAPPY;
        v12ToV114Book[Particle.TYPE_ENCHANTMENT_TABLE] = V114_ENCHANTMENT_TABLE;
        v12ToV114Book[Particle.TYPE_TRACKING_EMITTER] = V114_TRACKING_EMITTER;
        v12ToV114Book[Particle.TYPE_NOTE] = V114_NOTE;
        v12ToV114Book[Particle.TYPE_WITCH_SPELL] = V114_WITCH_SPELL;
        v12ToV114Book[Particle.TYPE_CARROT] = V114_CARROT;
        v12ToV114Book[Particle.TYPE_END_ROD] = V114_END_ROD;
        v12ToV114Book[Particle.TYPE_DRAGONS_BREATH] = V114_DRAGONS_BREATH;
        //v12ToV114Book[Particle.TYPE_SPIT] = V114_SPIT;
        //v12ToV114Book[Particle.TYPE_TOTEM] = V114_TOTEM;
        //v12ToV114Book[Particle.TYPE_FOOD] = V114_FOOD;
        //v12ToV114Book[Particle.TYPE_FIREWORKS_STARTER] = V114_FIREWORKS_STARTER;
        v12ToV114Book[Particle.TYPE_FIREWORKS_SPARK] = V114_FIREWORKS_SPARK;
        //v12ToV114Book[Particle.TYPE_FIREWORKS_OVERLAY] = V114_FIREWORKS_OVERLAY;
        //v12ToV114Book[Particle.TYPE_BALLOON_GAS] = V114_BALLOON_GAS;
        //v12ToV114Book[Particle.TYPE_COLORED_FLAME] = V114_COLORED_FLAME;
        //v12ToV114Book[Particle.TYPE_SPARKLER] = V114_SPARKLER;
        //v12ToV114Book[Particle.TYPE_CONDUIT] = V114_CONDUIT;
        //v12ToV114Book[Particle.TYPE_BUBBLE_COLUMN_UP] = V114_BUBBLE_COLUMN_UP;
        //v12ToV114Book[Particle.TYPE_BUBBLE_COLUMN_DOWN] = V114_BUBBLE_COLUMN_DOWN;
        //v12ToV114Book[Particle.TYPE_SNEEZE] = V114_SNEEZE;

        Arrays.fill(v12ToV116220Book, -1);
        v12ToV116220Book[Particle.TYPE_BUBBLE] = V116220_BUBBLE;
        v12ToV116220Book[Particle.TYPE_CRITICAL] = V116220_CRITICAL;
        v12ToV116220Book[Particle.TYPE_BLOCK_FORCE_FIELD] = V116220_BLOCK_FORCE_FIELD;
        v12ToV116220Book[Particle.TYPE_SMOKE] = V116220_SMOKE;
        v12ToV116220Book[Particle.TYPE_EXPLODE] = V116220_EXPLODE;
        v12ToV116220Book[Particle.TYPE_EVAPORATION] = V116220_EVAPORATION;
        v12ToV116220Book[Particle.TYPE_FLAME] = V116220_FLAME;
        v12ToV116220Book[Particle.TYPE_LAVA] = V116220_LAVA;
        v12ToV116220Book[Particle.TYPE_LARGE_SMOKE] = V116220_LARGE_SMOKE;
        v12ToV116220Book[Particle.TYPE_REDSTONE] = V116220_REDSTONE;
        v12ToV116220Book[Particle.TYPE_RISING_RED_DUST] = V116220_RISING_RED_DUST;
        v12ToV116220Book[Particle.TYPE_ITEM_BREAK] = V116220_ITEM_BREAK;
        v12ToV116220Book[Particle.TYPE_SNOWBALL_POOF] = V116220_SNOWBALL_POOF;
        v12ToV116220Book[Particle.TYPE_HUGE_EXPLODE] = V116220_HUGE_EXPLODE;
        v12ToV116220Book[Particle.TYPE_HUGE_EXPLODE_SEED] = V116220_HUGE_EXPLODE_SEED;
        v12ToV116220Book[Particle.TYPE_MOB_FLAME] = V116220_MOB_FLAME;
        v12ToV116220Book[Particle.TYPE_HEART] = V116220_HEART;
        v12ToV116220Book[Particle.TYPE_TERRAIN] = V116220_TERRAIN;
        v12ToV116220Book[Particle.TYPE_SUSPENDED_TOWN] = V116220_SUSPENDED_TOWN;
        v12ToV116220Book[Particle.TYPE_PORTAL] = V116220_PORTAL;
        v12ToV116220Book[Particle.TYPE_SPLASH] = V116220_SPLASH;
        v12ToV116220Book[Particle.TYPE_WATER_WAKE] = V116220_WATER_WAKE;
        v12ToV116220Book[Particle.TYPE_DRIP_WATER] = V116220_DRIP_WATER;
        v12ToV116220Book[Particle.TYPE_DRIP_LAVA] = V116220_DRIP_LAVA;
        v12ToV116220Book[Particle.TYPE_FALLING_DUST] = V116220_FALLING_DUST;
        v12ToV116220Book[Particle.TYPE_MOB_SPELL] = V116220_MOB_SPELL;
        v12ToV116220Book[Particle.TYPE_MOB_SPELL_AMBIENT] = V116220_MOB_SPELL_AMBIENT;
        v12ToV116220Book[Particle.TYPE_MOB_SPELL_INSTANTANEOUS] = V116220_MOB_SPELL_INSTANTANEOUS;
        v12ToV116220Book[Particle.TYPE_INK] = V116220_INK;
        v12ToV116220Book[Particle.TYPE_SLIME] = V116220_SLIME;
        v12ToV116220Book[Particle.TYPE_RAIN_SPLASH] = V116220_RAIN_SPLASH;
        v12ToV116220Book[Particle.TYPE_VILLAGER_ANGRY] = V116220_VILLAGER_ANGRY;
        v12ToV116220Book[Particle.TYPE_VILLAGER_HAPPY] = V116220_VILLAGER_HAPPY;
        v12ToV116220Book[Particle.TYPE_ENCHANTMENT_TABLE] = V116220_ENCHANTMENT_TABLE;
        v12ToV116220Book[Particle.TYPE_TRACKING_EMITTER] = V116220_TRACKING_EMITTER;
        v12ToV116220Book[Particle.TYPE_NOTE] = V116220_NOTE;
        v12ToV116220Book[Particle.TYPE_WITCH_SPELL] = V116220_WITCH_SPELL;
        v12ToV116220Book[Particle.TYPE_CARROT] = V116220_CARROT;
        v12ToV116220Book[Particle.TYPE_END_ROD] = V116220_END_ROD;
        v12ToV116220Book[Particle.TYPE_DRAGONS_BREATH] = V116220_DRAGONS_BREATH;
        //v12ToV116220Book[Particle.TYPE_SPIT] = V116220_SPIT;
        //v12ToV116220Book[Particle.TYPE_TOTEM] = V116220_TOTEM;
        //v12ToV116220Book[Particle.TYPE_FOOD] = V116220_FOOD;
        //v12ToV116220Book[Particle.TYPE_FIREWORKS_STARTER] = V116220_FIREWORKS_STARTER;
        v12ToV116220Book[Particle.TYPE_FIREWORKS_SPARK] = V116220_FIREWORKS_SPARK;
        //v12ToV116220Book[Particle.TYPE_FIREWORKS_OVERLAY] = V116220_FIREWORKS_OVERLAY;
        //v12ToV116220Book[Particle.TYPE_BALLOON_GAS] = V116220_BALLOON_GAS;
        //v12ToV116220Book[Particle.TYPE_COLORED_FLAME] = V116220_COLORED_FLAME;
        //v12ToV116220Book[Particle.TYPE_SPARKLER] = V116220_SPARKLER;
        //v12ToV116220Book[Particle.TYPE_CONDUIT] = V116220_CONDUIT;
        //v12ToV116220Book[Particle.TYPE_BUBBLE_COLUMN_UP] = V116220_BUBBLE_COLUMN_UP;
        //v12ToV116220Book[Particle.TYPE_BUBBLE_COLUMN_DOWN] = V116220_BUBBLE_COLUMN_DOWN;
        //v12ToV116220Book[Particle.TYPE_SNEEZE] = V116220_SNEEZE;

        Arrays.fill(v12ToV11710Book, -1);
        v12ToV11710Book[Particle.TYPE_BUBBLE] = V11710_BUBBLE;
        v12ToV11710Book[Particle.TYPE_CRITICAL] = V11710_CRITICAL;
        v12ToV11710Book[Particle.TYPE_BLOCK_FORCE_FIELD] = V11710_BLOCK_FORCE_FIELD;
        v12ToV11710Book[Particle.TYPE_SMOKE] = V11710_SMOKE;
        v12ToV11710Book[Particle.TYPE_EXPLODE] = V11710_EXPLODE;
        v12ToV11710Book[Particle.TYPE_EVAPORATION] = V11710_EVAPORATION;
        v12ToV11710Book[Particle.TYPE_FLAME] = V11710_FLAME;
        v12ToV11710Book[Particle.TYPE_LAVA] = V11710_LAVA;
        v12ToV11710Book[Particle.TYPE_LARGE_SMOKE] = V11710_LARGE_SMOKE;
        v12ToV11710Book[Particle.TYPE_REDSTONE] = V11710_REDSTONE;
        v12ToV11710Book[Particle.TYPE_RISING_RED_DUST] = V11710_RISING_RED_DUST;
        v12ToV11710Book[Particle.TYPE_ITEM_BREAK] = V11710_ITEM_BREAK;
        v12ToV11710Book[Particle.TYPE_SNOWBALL_POOF] = V11710_SNOWBALL_POOF;
        v12ToV11710Book[Particle.TYPE_HUGE_EXPLODE] = V11710_HUGE_EXPLODE;
        v12ToV11710Book[Particle.TYPE_HUGE_EXPLODE_SEED] = V11710_HUGE_EXPLODE_SEED;
        v12ToV11710Book[Particle.TYPE_MOB_FLAME] = V11710_MOB_FLAME;
        v12ToV11710Book[Particle.TYPE_HEART] = V11710_HEART;
        v12ToV11710Book[Particle.TYPE_TERRAIN] = V11710_TERRAIN;
        v12ToV11710Book[Particle.TYPE_SUSPENDED_TOWN] = V11710_SUSPENDED_TOWN;
        v12ToV11710Book[Particle.TYPE_PORTAL] = V11710_PORTAL;
        v12ToV11710Book[Particle.TYPE_SPLASH] = V11710_SPLASH;
        v12ToV11710Book[Particle.TYPE_WATER_WAKE] = V11710_WATER_WAKE;
        v12ToV11710Book[Particle.TYPE_DRIP_WATER] = V11710_DRIP_WATER;
        v12ToV11710Book[Particle.TYPE_DRIP_LAVA] = V11710_DRIP_LAVA;
        v12ToV11710Book[Particle.TYPE_FALLING_DUST] = V11710_FALLING_DUST;
        v12ToV11710Book[Particle.TYPE_MOB_SPELL] = V11710_MOB_SPELL;
        v12ToV11710Book[Particle.TYPE_MOB_SPELL_AMBIENT] = V11710_MOB_SPELL_AMBIENT;
        v12ToV11710Book[Particle.TYPE_MOB_SPELL_INSTANTANEOUS] = V11710_MOB_SPELL_INSTANTANEOUS;
        v12ToV11710Book[Particle.TYPE_INK] = V11710_INK;
        v12ToV11710Book[Particle.TYPE_SLIME] = V11710_SLIME;
        v12ToV11710Book[Particle.TYPE_RAIN_SPLASH] = V11710_RAIN_SPLASH;
        v12ToV11710Book[Particle.TYPE_VILLAGER_ANGRY] = V11710_VILLAGER_ANGRY;
        v12ToV11710Book[Particle.TYPE_VILLAGER_HAPPY] = V11710_VILLAGER_HAPPY;
        v12ToV11710Book[Particle.TYPE_ENCHANTMENT_TABLE] = V11710_ENCHANTMENT_TABLE;
        v12ToV11710Book[Particle.TYPE_TRACKING_EMITTER] = V11710_TRACKING_EMITTER;
        v12ToV11710Book[Particle.TYPE_NOTE] = V11710_NOTE;
        v12ToV11710Book[Particle.TYPE_WITCH_SPELL] = V11710_WITCH_SPELL;
        v12ToV11710Book[Particle.TYPE_CARROT] = V11710_CARROT;
        v12ToV11710Book[Particle.TYPE_END_ROD] = V11710_END_ROD;
        v12ToV11710Book[Particle.TYPE_DRAGONS_BREATH] = V11710_DRAGONS_BREATH;
        //v12ToV11710Book[Particle.TYPE_SPIT] = V11710_SPIT;
        //v12ToV11710Book[Particle.TYPE_TOTEM] = V11710_TOTEM;
        //v12ToV11710Book[Particle.TYPE_FOOD] = V11710_FOOD;
        //v12ToV11710Book[Particle.TYPE_FIREWORKS_STARTER] = V11710_FIREWORKS_STARTER;
        v12ToV11710Book[Particle.TYPE_FIREWORKS_SPARK] = V11710_FIREWORKS_SPARK;
        //v12ToV11710Book[Particle.TYPE_FIREWORKS_OVERLAY] = V11710_FIREWORKS_OVERLAY;
        //v12ToV11710Book[Particle.TYPE_BALLOON_GAS] = V11710_BALLOON_GAS;
        //v12ToV11710Book[Particle.TYPE_COLORED_FLAME] = V11710_COLORED_FLAME;
        //v12ToV11710Book[Particle.TYPE_SPARKLER] = V11710_SPARKLER;
        //v12ToV11710Book[Particle.TYPE_CONDUIT] = V11710_CONDUIT;
        //v12ToV11710Book[Particle.TYPE_BUBBLE_COLUMN_UP] = V11710_BUBBLE_COLUMN_UP;
        //v12ToV11710Book[Particle.TYPE_BUBBLE_COLUMN_DOWN] = V11710_BUBBLE_COLUMN_DOWN;
        //v12ToV11710Book[Particle.TYPE_SNEEZE] = V11710_SNEEZE;
    }

    public static int translateTo112(int particleId) {
        if (particleId < 0 || particleId >= Particle.TYPE_UNDEFINED) {
            return particleId;
        }
        int value = v12ToV112Book[particleId];
        return value != -1 ? value : particleId;
    }

    public static int translateTo114(int particleId) {
        if (particleId < 0 || particleId >= Particle.TYPE_UNDEFINED) {
            return particleId;
        }
        int value = v12ToV114Book[particleId];
        return value != -1 ? value : particleId;
    }

    public static int translateTo116220(int particleId) {
        if (particleId < 0 || particleId >= Particle.TYPE_UNDEFINED) {
            return particleId;
        }
        int value = v12ToV116220Book[particleId];
        return value != -1 ? value : particleId;
    }

    public static int translateTo11710(int particleId) {
        if (particleId < 0 || particleId >= Particle.TYPE_UNDEFINED) {
            return particleId;
        }
        int value = v12ToV11710Book[particleId];
        return value != -1 ? value : particleId;
    }

    public static int translateTo(AbstractProtocol protocol, int particleId) {
        int ver = protocol.getProtocolStart();
        if (ver >= AbstractProtocol.PROTOCOL_117_10.getProtocolStart()) {
            return translateTo11710(particleId);
        } else if (ver >= AbstractProtocol.PROTOCOL_116_220.getProtocolStart()) {
            return translateTo116220(particleId);
        } else if (ver >= AbstractProtocol.PROTOCOL_114.getProtocolStart()) {
            return translateTo114(particleId);
        } else if (ver >= AbstractProtocol.PROTOCOL_112.getProtocolStart()) {
            return translateTo112(particleId);
        }
        return particleId;
    }
}
