package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.block.Block;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

@Deprecated
public enum LevelSoundEventEnum {

    SOUND_ITEM_USE_ON(0, 0, 0),
    SOUND_HIT(1, 1, 1),
    SOUND_STEP(2, 2, 2),
    SOUND_FLY(3, 3, 3),
    SOUND_JUMP(4, 4, 4),
    SOUND_BREAK(5, 5, 5),
    SOUND_PLACE(6, 6, 6),
    SOUND_HEAVY_STEP(7, 7, 7),
    SOUND_GALLOP(8, 8, 8),
    SOUND_FALL(9, 9, 9),
    SOUND_AMBIENT(10, 10, 10),
    SOUND_AMBIENT_BABY(11, 11, 11),
    SOUND_AMBIENT_IN_WATER(12, 12, 12),
    SOUND_BREATHE(13, 13, 13),
    SOUND_DEATH(14, 14, 14),
    SOUND_DEATH_IN_WATER(15, 15, 15),
    SOUND_DEATH_TO_ZOMBIE(16, 16, 16),
    SOUND_HURT(17, 17, 17),
    SOUND_HURT_IN_WATER(18, 18, 18),
    SOUND_MAD(19, 19, 19),
    SOUND_BOOST(20, 20, 20),
    SOUND_BOW(21, 21, 21),
    SOUND_SQUISH_BIG(22, 22, 22),
    SOUND_SQUISH_SMALL(23, 23, 23),
    SOUND_FALL_BIG(24, 24, 24),
    SOUND_FALL_SMALL(25, 25, 25),
    SOUND_SPLASH(26, 26, 26),
    SOUND_FIZZ(27, 27, 27),
    SOUND_FLAP(28, 28, 28),
    SOUND_SWIM(29, 29, 29),
    SOUND_DRINK(30, 30, 30),
    SOUND_EAT(31, 31, 31),
    SOUND_TAKEOFF(32, 32, 32),
    SOUND_SHAKE(33, 33, 33),
    SOUND_PLOP(34, 34, 34),
    SOUND_LAND(35, 35, 35),
    SOUND_SADDLE(36, 36, 36),
    SOUND_ARMOR(37, 37, 37),
    SOUND_MOB_ARMOR_STAND_PLACE(-1, 38, 38),
    SOUND_ADD_CHEST(38, 39, 39),
    SOUND_THROW(39, 40, 40),
    SOUND_ATTACK(40, 41, 41),
    SOUND_ATTACK_NODAMAGE(41, 42, 42),
    SOUND_ATTACK_STRONG(42, 43, 43),
    SOUND_WARN(43, 44, 44),
    SOUND_SHEAR(44, 45, 45),
    SOUND_MILK(45, 46, 46),
    SOUND_THUNDER(46, 47, 47),
    SOUND_EXPLODE(47, 48, 48),
    SOUND_FIRE(48, 49, 49),
    SOUND_IGNITE(49, 50, 50),
    SOUND_FUSE(50, 51, 51),
    SOUND_STARE(51, 52, 52),
    SOUND_SPAWN(52, 53, 53),
    SOUND_SHOOT(53, 54, 54),
    SOUND_BREAK_BLOCK(54, 55, 55),
    SOUND_LAUNCH(55, 56, 56),
    SOUND_BLAST(56, 57, 57),
    SOUND_LARGE_BLAST(57, 58, 58),
    SOUND_TWINKLE(58, 59, 59),
    SOUND_REMEDY(59, 60, 60),
    SOUND_UNFECT(60, 61, 61),
    SOUND_LEVELUP(61, 62, 62),
    SOUND_BOW_HIT(62, 63, 63),
    SOUND_BULLET_HIT(63, 64, 64),
    SOUND_EXTINGUISH_FIRE(64, 65, 65),
    SOUND_ITEM_FIZZ(65, 66, 66),
    SOUND_CHEST_OPEN(66, 67, 67),
    SOUND_CHEST_CLOSED(67, 68, 68),
    SOUND_SHULKERBOX_OPEN(68, 69, 69),
    SOUND_SHULKERBOX_CLOSED(69, 70, 70),
    SOUND_ENDERCHEST_OPEN(-1, 71, 71),
    SOUND_ENDERCHEST_CLOSED(-1, 72, 72),
    SOUND_POWER_ON(70, 73, 73),
    SOUND_POWER_OFF(71, 74, 74),
    SOUND_ATTACH(72, 75, 75),
    SOUND_DETACH(73, 76, 76),
    SOUND_DENY(74, 77, 77),
    SOUND_TRIPOD(75, 78, 78),
    SOUND_POP(76, 79, 79),
    SOUND_DROP_SLOT(77, 80, 80),
    SOUND_NOTE(78, 81, 81),
    SOUND_THORNS(79, 82, 82),
    SOUND_PISTON_IN(80, 83, 83),
    SOUND_PISTON_OUT(81, 84, 84),
    SOUND_PORTAL(82, 85, 85),
    SOUND_WATER(83, 86, 86),
    SOUND_LAVA_POP(84, 87, 87),
    SOUND_LAVA(85, 88, 88),
    SOUND_BURP(86, 89, 89),
    SOUND_BUCKET_FILL_WATER(87, 90, 90),
    SOUND_BUCKET_FILL_LAVA(88, 91, 91),
    SOUND_BUCKET_EMPTY_WATER(89, 92, 92),
    SOUND_BUCKET_EMPTY_LAVA(90, 93, 93),
    SOUND_ARMOR_EQUIP_CHAIN(-1, 94, 94),
    SOUND_ARMOR_EQUIP_DIAMOND(-1, 95, 95),
    SOUND_ARMOR_EQUIP_GENERIC(-1, 96, 96),
    SOUND_ARMOR_EQUIP_GOLD(-1, 97, 97),
    SOUND_ARMOR_EQUIP_IRON(-1, 98, 98),
    SOUND_ARMOR_EQUIP_LEATHER(-1, 99, 99),
    SOUND_ARMOR_EQUIP_ELYTRA(-1, 100, 100),
    SOUND_RECORD_13(91, 101, 101),
    SOUND_RECORD_CAT(92, 102, 102),
    SOUND_RECORD_BLOCKS(93, 103, 103),
    SOUND_RECORD_CHIRP(94, 104, 104),
    SOUND_RECORD_FAR(95, 105, 105),
    SOUND_RECORD_MALL(96, 106, 106),
    SOUND_RECORD_MELLOHI(97, 107, 107),
    SOUND_RECORD_STAL(98, 108, 108),
    SOUND_RECORD_STRAD(99, 109, 109),
    SOUND_RECORD_WARD(100, 110, 110),
    SOUND_RECORD_11(101, 111, 111),
    SOUND_RECORD_WAIT(102, 112, 112),
    SOUND_STOP_RECORD(103, 103, 113),
    SOUND_GUARDIAN_FLOP(104, 114, 114),
    SOUND_ELDERGUARDIAN_CURSE(105, 115, 115),
    SOUND_MOB_WARNING(106, 116, 116),
    SOUND_MOB_WARNING_BABY(107, 117, 117),
    SOUND_TELEPORT(108, 118, 118),
    SOUND_SHULKER_OPEN(109, 119, 119),
    SOUND_SHULKER_CLOSE(110, 120, 120),
    SOUND_HAGGLE(111, 121, 121),
    SOUND_HAGGLE_YES(112, 122, 122),
    SOUND_HAGGLE_NO(113, 123, 123),
    SOUND_HAGGLE_IDLE(114, 124, 124),
    SOUND_CHORUSGROW(115, 125, 125),
    SOUND_CHORUSDEATH(116, 126, 126),
    SOUND_GLASS(117, 127, 127),
    SOUND_POTION_BREWED(-1, 128, 128),
    SOUND_CAST_SPELL(118, 129, 129),
    SOUND_PREPARE_ATTACK(119, 130, 130),
    SOUND_PREPARE_SUMMON(120, 131, 131),
    SOUND_PREPARE_WOLOLO(121, 132, 132),
    SOUND_FANG(122, 133, 133),
    SOUND_CHARGE(123, 134, 134),
    SOUND_CAMERA_TAKE_PICTURE(124, 135, 135),
    SOUND_LEASHKNOT_PLACE(125, 136, 136),
    SOUND_LEASHKNOT_BREAK(126, 137, 137),
    SOUND_GROWL(127, 138, 138),
    SOUND_WHINE(128, 139, 139),
    SOUND_PANT(129, 140, 140),
    SOUND_PURR(130, 141, 141),
    SOUND_PURREOW(131, 142, 142),
    SOUND_DEATH_MIN_VOLUME(132, 143, 143),
    SOUND_DEATH_MID_VOLUME(133, 144, 144),
    SOUND_IMITATE_BLAZE(134, 145, 145),
    SOUND_IMITATE_CAVE_SPIDER(135, 146, 146),
    SOUND_IMITATE_CREEPER(136, 147, 147),
    SOUND_IMITATE_ELDER_GUARDIAN(137, 148, 148),
    SOUND_IMITATE_ENDER_DRAGON(138, 149, 149),
    SOUND_IMITATE_ENDERMAN(139, 150, 150),
    SOUND_IMITATE_EVOCATION_ILLAGER(141, 152, 152),
    SOUND_IMITATE_GHAST(142, 153, 153),
    SOUND_IMITATE_HUSK(143, 154, 154),
    SOUND_IMITATE_ILLUSION_ILLAGER(144, 155, 155),
    SOUND_IMITATE_MAGMA_CUBE(145, 156, 156),
    SOUND_IMITATE_POLAR_BEAR(146, 157, 157),
    SOUND_IMITATE_SHULKER(147, 158, 158),
    SOUND_IMITATE_SILVERFISH(148, 159, 159),
    SOUND_IMITATE_SKELETON(149, 160, 160),
    SOUND_IMITATE_SLIME(150, 161, 161),
    SOUND_IMITATE_SPIDER(151, 162, 162),
    SOUND_IMITATE_STRAY(152, 163, 163),
    SOUND_IMITATE_VEX(153, 164, 164),
    SOUND_IMITATE_VINDICATION_ILLAGER(154, 165, 165),
    SOUND_IMITATE_WITCH(155, 166, 166),
    SOUND_IMITATE_WITHER(156, 167, 167),
    SOUND_IMITATE_WITHER_SKELETON(157, 168, 168),
    SOUND_IMITATE_WOLF(158, 169, 169),
    SOUND_IMITATE_ZOMBIE(159, 170, 170),
    SOUND_IMITATE_ZOMBIE_PIGMAN(160, 171, 171),
    SOUND_IMITATE_ZOMBIE_VILLAGER(161, 172, 172),
    SOUND_BLOCK_END_PORTAL_FRAME_FILL(162, 173, 173),
    SOUND_BLOCK_END_PORTAL_SPAWN(163, 174, 174),
    SOUND_RANDOM_ANVIL_USE(164, 175, 175),
    SOUND_BOTTLE_DRAGONBREATH(165, 176, 176),
    SOUND_PORTAL_TRAVEL(-1, 177, 177),
    SOUND_ITEM_TRIDENT_HIT(-1, -1, 178),
    SOUND_ITEM_TRIDENT_RETURN(-1, -1, 179),
    SOUND_ITEM_TRIDENT_RIPTIDE_1(-1, -1, 180),
    SOUND_ITEM_TRIDENT_RIPTIDE_2(-1, -1, 181),
    SOUND_ITEM_TRIDENT_RIPTIDE_3(-1, -1, 182),
    SOUND_ITEM_TRIDENT_THROW(-1, -1, 183),
    SOUND_ITEM_TRIDENT_THUNDER(-1, -1, 184),
    SOUND_ITEM_TRIDENT_HIT_GROUND(-1, -1, 185),
    SOUND_DEFAULT(166, 178, 186),
    SOUND_ELEMCONSTRUCT_OPEN(-1, -1, 188),
    SOUND_ICEBOMB_HIT(-1, -1, 189),
    SOUND_BALLOONPOP(-1, -1, 190),
    SOUND_LT_REACTION_ICEBOMB(-1, -1, 191),
    SOUND_LT_REACTION_BLEACH(-1, -1, 192),
    SOUND_LT_REACTION_EPASTE(-1, -1, 193),
    SOUND_LT_REACTION_EPASTE2(-1, -1, 194),
    SOUND_LT_REACTION_FERTILIZER(-1, -1, 199),
    SOUND_LT_REACTION_FIREBALL(-1, -1, 200),
    SOUND_LT_REACTION_MGSALT(-1, -1, 201),
    SOUND_LT_REACTION_MISCFIRE(-1, -1, 202),
    SOUND_LT_REACTION_FIRE(-1, -1, 203),
    SOUND_LT_REACTION_MISCEXPLOSION(-1, -1, 204),
    SOUND_LT_REACTION_MISCMYSTICAL(-1, -1, 205),
    SOUND_LT_REACTION_MISCMYSTICAL2(-1, -1, 206),
    SOUND_LT_REACTION_PRODUCT(-1, -1, 207),
    SOUND_SPARKLER_USE(-1, -1, 208),
    SOUND_GLOWSTICK_USE(-1, -1, 209),
    SOUND_SPARKLER_ACTIVE(-1, -1, 210),
    SOUND_CONVERT_TO_DROWNED(-1, -1, 211),
    SOUND_BUCKET_FILL_FISH(-1, -1, 212),
    SOUND_BUCKET_EMPTY_FISH(-1, -1, 213),
    SOUND_BUBBLE_UP(-1, -1, 214),
    SOUND_BUBBLE_DOWN(-1, -1, 215),
    SOUND_BUBBLE_POP(-1, -1, 216),
    SOUND_BUBBLE_UPINSIDE(-1, -1, 217),
    SOUND_BUBBLE_DOWNINSIDE(-1, -1, 218),
    SOUND_HURT_BABY(-1, -1, 219),
    SOUND_DEATH_BABY(-1, -1, 220),
    SOUND_STEP_BABY(-1, -1, 221),
    SOUND_BORN(-1, -1, 223),
    SOUND_BLOCK_TURTLE_EGG_BREAK(-1, -1, 224),
    SOUND_BLOCK_TURTLE_EGG_CRACK(-1, -1, 225),
    SOUND_BLOCK_TURTLE_EGG_HATCH(-1, -1, 226),
    SOUND_BLOCK_TURTLE_EGG_ATTACK(-1, -1, 228),
    SOUND_BEACON_ACTIVATE(-1, -1, 229),
    SOUND_BEACON_AMBIENT(-1, -1, 230),
    SOUND_BEACON_DEACTIVATE(-1, -1, 231),
    SOUND_BEACON_POWER(-1, -1, 232),
    SOUND_CONDUIT_ACTIVATE(-1, -1, 233),
    SOUND_CONDUIT_AMBIENT(-1, -1, 234),
    SOUND_CONDUIT_ATTACK(-1, -1, 235),
    SOUND_CONDUIT_DEACTIVATE(-1, -1, 236),
    SOUND_CONDUIT_SHORT(-1, -1, 237),
    SOUND_SWOOP(-1, -1, 238),
    SOUND_BLOCK_BAMBOO_SAPLING_PLACE(-1, -1, 239),
    SOUND_PRESNEEZE(-1, -1, 240),
    SOUND_SNEEZE(-1, -1, 241),
    SOUND_AMBIENT_TAME(-1, -1, 242),
    SOUND_SCARED(-1, -1, 243),
    SOUND_BLOCK_SCAFFOLDING_CLIMB(-1, -1, 244),
    SOUND_CROSSBOW_LOADING_START(-1, -1, 245),
    SOUND_CROSSBOW_LOADING_MIDDLE(-1, -1, 246),
    SOUND_CROSSBOW_LOADING_END(-1, -1, 247),
    SOUND_CROSSBOW_SHOOT(-1, -1, 248),
    SOUND_CROSSBOW_QUICK_CHARGE_START(-1, -1, 249),
    SOUND_CROSSBOW_QUICK_CHARGE_MIDDLE(-1, -1, 250),
    SOUND_CROSSBOW_QUICK_CHARGE_END(-1, -1, 251),
    SOUND_AMBIENT_AGGRESSIVE(-1, -1, 252),
    SOUND_AMBIENT_WORRIED(-1, -1, 253),
    SOUND_CANT_BREED(-1, -1, 254),
    SOUND_UNDEFINED(167, 179, 255);

    private final int v12;
    private final int v14;
    private final int v18;

    LevelSoundEventEnum(int v12, int v14, int v18) {
        this.v12 = v12;
        this.v14 = v14;
        this.v18 = v18;
    }

    public static final LevelSoundEventEnum[] v12ToEnum = new LevelSoundEventEnum[256];
    public static final LevelSoundEventEnum[] v14ToEnum = new LevelSoundEventEnum[256];
    public static final LevelSoundEventEnum[] v18ToEnum = new LevelSoundEventEnum[256];

    static {
        for (LevelSoundEventEnum value : values()) {
            if (value.v12 != -1) {
                v12ToEnum[value.v12] = value;
            }
            if (value.v14 != -1) {
                v14ToEnum[value.v14] = value;
            }
            if (value.v18 != -1) {
                v18ToEnum[value.v18] = value;
            }
        }
    }

    public int getV12() {
        return v12;
    }

    public int getV14() {
        return v14;
    }

    public int getV18() {
        return v18;
    }

    public static LevelSoundEventEnum fromV12(int v12) {
        if (v12 < 0 || v12 >= v12ToEnum.length) {
            return LevelSoundEventEnum.SOUND_UNDEFINED;
        }
        return v12ToEnum[v12];
        // return Arrays.stream(values0()).filter(s -> s.v12 == v12).findFirst().orElse(null);
    }

    public static LevelSoundEventEnum fromV14(int v14) {
        if (v14 < 0 || v14 >= v14ToEnum.length) {
            return LevelSoundEventEnum.SOUND_UNDEFINED;
        }
        return v14ToEnum[v14];
        // return Arrays.stream(values0()).filter(s -> s.v14 == v14).findFirst().orElse(null);
    }

    public static LevelSoundEventEnum fromV18(int v18) {
        if (v18 < 0 || v18 >= v18ToEnum.length) {
            return LevelSoundEventEnum.SOUND_UNDEFINED;
        }
        return v18ToEnum[v18];
        // return Arrays.stream(values0()).filter(s -> s.v18 == v18).findFirst().orElse(null);
    }

    public int translateTo14ExtraData(int extraData) {
        switch (this) {
            case SOUND_PLACE:
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
                return GlobalBlockPalette.getOrCreateRuntimeId(extraData >> Block.BLOCK_META_BITS, extraData & Block.BLOCK_META_MASK);
            default:
                return extraData;
        }
    }

    public int translateTo16ExtraData(int extraData, AbstractProtocol protocol, boolean netease) {
        switch (this) {
            case SOUND_PLACE:
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, extraData >> Block.BLOCK_META_BITS, extraData & Block.BLOCK_META_MASK);
            default:
                return extraData;
        }
    }

    public int translateTo18ExtraData(int extraData, int pitch, AbstractProtocol protocol, boolean netease) {
        switch (this) {
            case SOUND_PLACE:
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
                return AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, extraData >> Block.BLOCK_META_BITS, extraData & Block.BLOCK_META_MASK);
            case SOUND_NOTE:
                return (extraData << 8) | (pitch & 0b11111111);
            default:
                return extraData;
        }
    }

    public int translateExtraDataFromClient(int extraData, AbstractProtocol protocol, boolean netease) {
        switch (this) {
            case SOUND_ITEM_USE_ON:
            case SOUND_HIT:
            case SOUND_PLACE:
            case SOUND_LAND:
            case SOUND_POWER_ON:
            case SOUND_POWER_OFF:
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

    private static final LevelSoundEventEnum[] $VALUES0 = values();

    public static LevelSoundEventEnum[] values0() {
        return $VALUES0;
    }
}
