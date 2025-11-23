package org.itxtech.synapseapi.multiprotocol.protocol121.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AwardAchievementPacket121 extends Packet121 {
    public static final int NETWORK_ID = ProtocolInfo.AWARD_ACHIEVEMENT_PACKET;

    public static final int ACHIEVEMENT_CHEST_FULL_OF_COBBLESTONE = 7;
    public static final int ACHIEVEMENT_DIAMOND_FOR_YOU = 10;
    public static final int ACHIEVEMENT_IRON_BELLY = 20;
    public static final int ACHIEVEMENT_IRON_MAN = 21;
    public static final int ACHIEVEMENT_ON_A_RAIL = 29;
    public static final int ACHIEVEMENT_OVERKILL = 30;
    public static final int ACHIEVEMENT_RETURN_TO_SENDER = 37;
    public static final int ACHIEVEMENT_SNIPER_DUEL = 38;
    public static final int ACHIEVEMENT_STAYIN_FROSTY = 39;
    public static final int ACHIEVEMENT_TAKE_INVENTORY = 40;
    public static final int ACHIEVEMENT_MAP_ROOM = 50;
    public static final int ACHIEVEMENT_FREIGHT_STATION = 52;
    public static final int ACHIEVEMENT_SMELT_EVERYTHING = 53;
    public static final int ACHIEVEMENT_TASTE_OF_YOUR_OWN_MEDICINE = 54;
    public static final int ACHIEVEMENT_WHEN_PIGS_FLY = 56;
    public static final int ACHIEVEMENT_INCEPTION = 58;
    public static final int ACHIEVEMENT_ARTIFICIAL_SELECTION = 60;
    public static final int ACHIEVEMENT_FREE_DIVER = 61;
    public static final int ACHIEVEMENT_SPAWN_THE_WITHER = 62;
    public static final int ACHIEVEMENT_BEACONATOR = 63;
    public static final int ACHIEVEMENT_GREAT_VIEW = 64;
    public static final int ACHIEVEMENT_SUPER_SONIC = 65;
    public static final int ACHIEVEMENT_THE_END_AGAIN = 66;
    public static final int ACHIEVEMENT_TREASURE_HUNTER = 67;
    public static final int ACHIEVEMENT_SHOOTING_STAR = 68;
    public static final int ACHIEVEMENT_FASHION_SHOW = 69;
    public static final int ACHIEVEMENT_SELF_PUBLISHED_AUTHOR = 71;
    public static final int ACHIEVEMENT_ALTERNATIVE_FUEL = 72;
    public static final int ACHIEVEMENT_SLEEP_WITH_THE_FISHES = 73;
    public static final int ACHIEVEMENT_CASTAWAY = 74;
    public static final int ACHIEVEMENT_IM_A_MARINE_BIOLOGIST = 75;
    public static final int ACHIEVEMENT_SAIL_THE_7_SEAS = 76;
    public static final int ACHIEVEMENT_ME_GOLD = 77;
    public static final int ACHIEVEMENT_AHOY = 78;
    public static final int ACHIEVEMENT_ATLANTIS = 79;
    public static final int ACHIEVEMENT_ONE_PICKLE_TWO_PICKLE_SEA_PICKLE_FOUR = 80;
    public static final int ACHIEVEMENT_DOA_BARREL_ROLL = 81;
    public static final int ACHIEVEMENT_MOSKSTRAUMEN = 82;
    public static final int ACHIEVEMENT_ECHOLOCATION = 83;
    public static final int ACHIEVEMENT_WHERE_HAVE_YOU_BEEN = 84;
    public static final int ACHIEVEMENT_TOP_OF_THE_WORLD = 85;
    public static final int ACHIEVEMENT_FRUIT_ON_THE_LOOM = 86;
    public static final int ACHIEVEMENT_SOUND_THE_ALARM = 87;
    public static final int ACHIEVEMENT_BUY_LOW_SELL_HIGH = 88;
    public static final int ACHIEVEMENT_DISENCHANTED = 89;
    public static final int ACHIEVEMENT_TIME_FOR_STEW = 90;
    public static final int ACHIEVEMENT_BEE_OUR_GUEST = 91;
    public static final int ACHIEVEMENT_TOTAL_BEE_LOCATION = 92;
    public static final int ACHIEVEMENT_STICKY_SITUATION = 93;
    public static final int ACHIEVEMENT_COVER_ME_IN_DEBRIS = 94;
    public static final int ACHIEVEMENT_FLOAT_YOUR_GOAT = 95;
    public static final int ACHIEVEMENT_FRIEND = 96;
    public static final int ACHIEVEMENT_WAX_ON_WAX_OFF = 97;
    public static final int ACHIEVEMENT_STRIDER_RIDDEN_IN_LAVA_IN_OVERWORLD = 98;
    public static final int ACHIEVEMENT_GOAT_HORN_ACQUIRED = 99;
    public static final int ACHIEVEMENT_JUKEBOX_USED_IN_MEADOWS = 100;
    public static final int ACHIEVEMENT_TRADED_AT_WORLD_HEIGHT = 101;
    public static final int ACHIEVEMENT_SURVIVED_FALLFROM_WORLD_HEIGHT = 102;
    public static final int ACHIEVEMENT_SNEAK_CLOSE_TO_SCULK_SENSOR = 103;
    public static final int ACHIEVEMENT_IT_SPREADS = 104;
    public static final int ACHIEVEMENT_BIRTHDAY_SONG = 105;
    public static final int ACHIEVEMENT_WITH_OUR_POWERS_COMBINED = 106;
    public static final int ACHIEVEMENT_PLANTING_THE_PAST = 107;
    public static final int ACHIEVEMENT_CAREFUL_RESTORATION = 108;
    public static final int ACHIEVEMENT_REVAULTING = 109;
    public static final int ACHIEVEMENT_CRAFTERS_CRAFTING_CRAFTERS = 110;
    public static final int ACHIEVEMENT_WHO_NEEDS_ROCKETS = 111;
    public static final int ACHIEVEMENT_OVER_OVERKILL = 112;
    public static final int ACHIEVEMENT_HEART_TRANSPLANTER = 113;
    public static final int ACHIEVEMENT_STAY_HYDRATED = 114;
    public static final int ACHIEVEMENT_MOB_KABOB = 115;

    public int achievement;

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
        putLInt(achievement);
    }
}
