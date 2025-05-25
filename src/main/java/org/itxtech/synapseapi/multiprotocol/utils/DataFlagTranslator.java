package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.Arrays;

public class DataFlagTranslator {
	public final static int[] v12ToV14Book = new int[Entity.DATA_FLAG_UNDEFINED];
	public final static int[] v12ToV17Book = new int[Entity.DATA_FLAG_UNDEFINED];
	public final static int[] v12ToV11950Book = new int[Entity.DATA_FLAG_UNDEFINED];

	public static final int ONFIRE = 0;
	public static final int SNEAKING = 1;
	public static final int RIDING = 2;
	public static final int SPRINTING = 3;
	public static final int USINGITEM = 4;
	public static final int INVISIBLE = 5;
	public static final int TEMPTED = 6;
	public static final int INLOVE = 7;
	public static final int SADDLED = 8;
	public static final int POWERED = 9;
	public static final int IGNITED = 10;
	public static final int BABY = 11;
	public static final int CONVERTING = 12;
	public static final int CRITICAL = 13;
	public static final int CAN_SHOW_NAME = 14;
	public static final int ALWAYS_SHOW_NAME = 15;
	public static final int NOAI = 16;
	public static final int SILENT = 17;
	public static final int WALLCLIMBING = 18;
	public static final int CANCLIMB = 19;
	public static final int CANSWIM = 20;
	public static final int CANFLY = 21;
	public static final int CANWALK = 22;
	public static final int RESTING = 23;
	public static final int SITTING = 24;
	public static final int ANGRY = 25;
	public static final int INTERESTED = 26;
	public static final int CHARGED = 27;
	public static final int TAMED = 28;
	public static final int LEASHED = 29;
	public static final int SHEARED = 30;
	public static final int GLIDING = 31;
	public static final int ELDER = 32;
	public static final int MOVING = 33;
	public static final int BREATHING = 34;
	public static final int CHESTED = 35;
	public static final int STACKABLE = 36;
	public static final int SHOW_BOTTOM = 37;
	public static final int STANDING = 38;
	public static final int SHAKING = 39;
	public static final int IDLING = 40;
	public static final int CASTING = 41;
	public static final int CHARGING = 42;
	public static final int WASD_CONTROLLED = 43;
	public static final int CAN_POWER_JUMP = 44;
	public static final int LINGERING = 45;
	public static final int HAS_COLLISION = 46;
	public static final int HAS_GRAVITY = 47;
	public static final int FIRE_IMMUNE = 48;
	public static final int DANCING = 49;
	public static final int ENCHANTED = 50;
	public static final int RETURNTRIDENT = 51;
	public static final int CONTAINER_IS_PRIVATE = 52;
	public static final int IS_TRANSFORMING = 53;
	public static final int DAMAGENEARBYMOBS = 54;
	public static final int SWIMMING = 55;

	public static final int FLAG_17_ORPHANED = 29;
	public static final int FLAG_17_LEASHED = 30;
	public static final int FLAG_17_SHEARED = 31;
	public static final int FLAG_17_GLIDING = 32;
	public static final int FLAG_17_ELDER = 33;
	public static final int FLAG_17_MOVING = 34;
	public static final int FLAG_17_BREATHING = 35;
	public static final int FLAG_17_CHESTED = 36;
	public static final int FLAG_17_STACKABLE = 37;
	public static final int FLAG_17_SHOWBASE = 38;
	public static final int FLAG_17_REARING = 39;
	public static final int FLAG_17_VIBRATING = 40;
	public static final int FLAG_17_IDLING = 41;
	public static final int FLAG_17_EVOKER_SPELL = 42;
	public static final int FLAG_17_CHARGE_ATTACK = 43;
	public static final int FLAG_17_WASD_CONTROLLED = 44;
	public static final int FLAG_17_CAN_POWER_JUMP = 45;
	public static final int FLAG_17_LINGER = 46;
	public static final int FLAG_17_HAS_COLLISION = 47;
	public static final int FLAG_17_AFFECTED_BY_GRAVITY = 48;
	public static final int FLAG_17_FIRE_IMMUNE = 49;
	public static final int FLAG_17_DANCING = 50;
	public static final int FLAG_17_ENCHANTED = 51;
	public static final int FLAG_17_RETURN_TRIDENT = 52;
	public static final int FLAG_17_CONTAINER_IS_PRIVATE = 53;
	public static final int FLAG_17_TRANSFORMING = 54;
	public static final int FLAG_17_SPIN_ATTACK = 55;
	public static final int FLAG_17_SWIMMING = 56;
	public static final int FLAG_17_BRIBED = 57;
	public static final int FLAG_17_PREGNANT = 58;
	public static final int FLAG_17_LAYING_EGG = 59;
	public static final int FLAG_17_RIDER_CAN_PICK = 60;

	public static final int FLAG_1_TRANSITION_SETTING = 61;
	public static final int FLAG_1_EATING = 62;
	public static final int FLAG_1_LAYING_DOWN = 63;
	public static final int FLAG_1_SNEEZING = 64;
	public static final int FLAG_1_TRUSTING = 65;
	public static final int FLAG_1_ROLLING = 66;
	public static final int FLAG_1_SCARED = 67;
	public static final int FLAG_1_IN_SCAFFOLDING = 68;
	public static final int FLAG_1_OVER_SCAFFOLDING = 69;
	public static final int FLAG_1_FALL_THROUGH_SCAFFOLDING = 70;
	public static final int FLAG_1_BLOCKING = 71;
	public static final int FLAG_1_TRANSITION_BLOCKING = 72;
	public static final int FLAG_1_BLOCKED_USING_SHIELD = 73;
	public static final int FLAG_1_BLOCKED_USING_DAMAGED_SHIELD = 74;
	public static final int FLAG_1_SLEEPING = 75;
	public static final int FLAG_1_ENTITY_GROW_UP = 76;
	public static final int FLAG_1_TRADE_INTEREST = 77;
	public static final int FLAG_1_DOOR_BREAKER = 78;
	public static final int FLAG_1_BREAKING_OBSTRUCTION = 79;
	public static final int FLAG_1_DOOR_OPENER = 80;
	public static final int FLAG_1_IS_ILLAGER_CAPTAIN = 81;
	public static final int FLAG_1_STUNNED = 82;
	public static final int FLAG_1_ROARING = 83;
	public static final int FLAG_1_DELAYED_ATTACK = 84;
	public static final int FLAG_1_IS_AVOIDING_MOBS = 85;
	public static final int FLAG_1_IS_AVOIDING_BLOCKS = 86;
	public static final int FLAG_1_FACING_TARGET_TO_RANGE_ATTACK = 87;
	public static final int FLAG_1_HIDDEN_WHEN_INVISIBLE = 88;
	public static final int FLAG_1_IS_IN_UI = 89;
	public static final int FLAG_1_STALKING = 90;
	public static final int FLAG_1_EMOTING = 91;
	public static final int FLAG_1_CELEBRATING = 92;
	public static final int FLAG_1_ADMIRING = 93;
	public static final int FLAG_1_CELEBRATING_SPECIAL = 94;
	public static final int FLAG_1_OUT_OF_CONTROL = 95;
	public static final int FLAG_1_RAM_ATTACK = 96;
	public static final int FLAG_1_PLAYING_DEAD = 97;
	public static final int FLAG_1_IN_ASCENDABLE_BLOCK = 98;
	public static final int FLAG_1_OVER_DESCENDABLE_BLOCK = 99;
	public static final int FLAG_1_CROAKING = 100;
	public static final int FLAG_1_EAT_MOB = 101;
	public static final int FLAG_1_JUMP_GOAL_JUMP = 102;
	public static final int FLAG_1_EMERGING = 103;
	public static final int FLAG_1_SNIFFING = 104;
	public static final int FLAG_1_DIGGING = 105;
	public static final int FLAG_1_SONIC_BOOM = 106;

	public static final int FLAG_11950_CAN_DASH = 46;
	public static final int FLAG_11950_LINGER = 47;
	public static final int FLAG_11950_HAS_COLLISION = 48;
	public static final int FLAG_11950_AFFECTED_BY_GRAVITY = 49;
	public static final int FLAG_11950_FIRE_IMMUNE = 50;
	public static final int FLAG_11950_DANCING = 51;
	public static final int FLAG_11950_ENCHANTED = 52;
	public static final int FLAG_11950_RETURN_TRIDENT = 53;
	public static final int FLAG_11950_CONTAINER_IS_PRIVATE = 54;
	public static final int FLAG_11950_TRANSFORMING = 55;
	public static final int FLAG_11950_SPIN_ATTACK = 56;
	public static final int FLAG_11950_SWIMMING = 57;
	public static final int FLAG_11950_BRIBED = 58;
	public static final int FLAG_11950_PREGNANT = 59;
	public static final int FLAG_11950_LAYING_EGG = 60;
	public static final int FLAG_11950_RIDER_CAN_PICK = 61;
	public static final int FLAG_11950_TRANSITION_SETTING = 62;
	public static final int FLAG_11950_EATING = 63;
	public static final int FLAG_11950_LAYING_DOWN = 64;
	public static final int FLAG_11950_SNEEZING = 65;
	public static final int FLAG_11950_TRUSTING = 66;
	public static final int FLAG_11950_ROLLING = 67;
	public static final int FLAG_11950_SCARED = 68;
	public static final int FLAG_11950_IN_SCAFFOLDING = 69;
	public static final int FLAG_11950_OVER_SCAFFOLDING = 70;
	public static final int FLAG_11950_FALL_THROUGH_SCAFFOLDING = 71;
	public static final int FLAG_11950_BLOCKING = 72;
	public static final int FLAG_11950_TRANSITION_BLOCKING = 73;
	public static final int FLAG_11950_BLOCKED_USING_SHIELD = 74;
	public static final int FLAG_11950_BLOCKED_USING_DAMAGED_SHIELD = 75;
	public static final int FLAG_11950_SLEEPING = 76;
	public static final int FLAG_11950_ENTITY_GROW_UP = 77;
	public static final int FLAG_11950_TRADE_INTEREST = 78;
	public static final int FLAG_11950_DOOR_BREAKER = 79;
	public static final int FLAG_11950_BREAKING_OBSTRUCTION = 80;
	public static final int FLAG_11950_DOOR_OPENER = 81;
	public static final int FLAG_11950_IS_ILLAGER_CAPTAIN = 82;
	public static final int FLAG_11950_STUNNED = 83;
	public static final int FLAG_11950_ROARING = 84;
	public static final int FLAG_11950_DELAYED_ATTACK = 85;
	public static final int FLAG_11950_IS_AVOIDING_MOBS = 86;
	public static final int FLAG_11950_IS_AVOIDING_BLOCKS = 87;
	public static final int FLAG_11950_FACING_TARGET_TO_RANGE_ATTACK = 88;
	public static final int FLAG_11950_HIDDEN_WHEN_INVISIBLE = 89;
	public static final int FLAG_11950_IS_IN_UI = 90;
	public static final int FLAG_11950_STALKING = 91;
	public static final int FLAG_11950_EMOTING = 92;
	public static final int FLAG_11950_CELEBRATING = 93;
	public static final int FLAG_11950_ADMIRING = 94;
	public static final int FLAG_11950_CELEBRATING_SPECIAL = 95;
	public static final int FLAG_11950_OUT_OF_CONTROL = 96;
	public static final int FLAG_11950_RAM_ATTACK = 97;
	public static final int FLAG_11950_PLAYING_DEAD = 98;
	public static final int FLAG_11950_IN_ASCENDABLE_BLOCK = 99;
	public static final int FLAG_11950_OVER_DESCENDABLE_BLOCK = 100;
	public static final int FLAG_11950_CROAKING = 101;
	public static final int FLAG_11950_EAT_MOB = 102;
	public static final int FLAG_11950_JUMP_GOAL_JUMP = 103;
	public static final int FLAG_11950_EMERGING = 104;
	public static final int FLAG_11950_SNIFFING = 105;
	public static final int FLAG_11950_DIGGING = 106;
	public static final int FLAG_11950_SONIC_BOOM = 107;
	public static final int FLAG_11950_HAS_DASH_COOLDOWN = 108;
	public static final int FLAG_11950_PUSH_TOWARDS_CLOSEST_SPACE = 109;

	public static final int FLAG_11970_SCENTING = 110;
	public static final int FLAG_11970_RISING = 111;
	public static final int FLAG_11970_HAPPY = 112;
	public static final int FLAG_11970_SEARCHING = 113;

	public static final int FLAG_12010_CRAWLING = 114;

	public static final int FLAG_12040_TIMER_FLAG_1 = 115;
	public static final int FLAG_12040_TIMER_FLAG_2 = 116;
	public static final int FLAG_12040_TIMER_FLAG_3 = 117;

	public static final int FLAG_12080_BODY_ROTATION_BLOCKED = 118;

	public static final int FLAG_12160_RENDERS_WHEN_INVISIBLE = 119;

	public static final int FLAG_12170_BODY_ROTATION_AXIS_ALIGNED = 120;
	public static final int FLAG_12170_COLLIDABLE = 121;
	public static final int FLAG_12170_WASD_AIR_CONTROLLED = 122;

	public static final int FLAG_12180_DOES_SERVER_AUTH_ONLY_DISMOUNT = 123;

	public static final int FLAG_12190_BODY_ROTATION_ALWAYS_FOLLOWS_HEAD = 124;

	public static final int[] COUNT = Utils.make(() -> {
		int[] versions = new int[AbstractProtocol.getValues().length];
		Arrays.fill(versions, 120);
		versions[AbstractProtocol.PROTOCOL_121_60.ordinal()] = 120;
		versions[AbstractProtocol.PROTOCOL_121_70.ordinal()] = 123;
		versions[AbstractProtocol.PROTOCOL_121_80.ordinal()] = 124;
		versions[AbstractProtocol.PROTOCOL_121_90.ordinal()] = 125;
		return versions;
	});

	static {
		Arrays.fill(v12ToV14Book, -1);
		v12ToV14Book[Entity.DATA_FLAG_ONFIRE] = ONFIRE;
		v12ToV14Book[Entity.DATA_FLAG_SNEAKING] = SNEAKING;
		v12ToV14Book[Entity.DATA_FLAG_RIDING] = RIDING;
		v12ToV14Book[Entity.DATA_FLAG_SPRINTING] = SPRINTING;
		v12ToV14Book[Entity.DATA_FLAG_ACTION] = USINGITEM;
		v12ToV14Book[Entity.DATA_FLAG_INVISIBLE] = INVISIBLE;
		v12ToV14Book[Entity.DATA_FLAG_TEMPTED] = TEMPTED;
		v12ToV14Book[Entity.DATA_FLAG_INLOVE] = INLOVE;
		v12ToV14Book[Entity.DATA_FLAG_SADDLED] = SADDLED;
		v12ToV14Book[Entity.DATA_FLAG_POWERED] = POWERED;
		v12ToV14Book[Entity.DATA_FLAG_IGNITED] = IGNITED;
		v12ToV14Book[Entity.DATA_FLAG_BABY] = BABY;
		v12ToV14Book[Entity.DATA_FLAG_CONVERTING] = CONVERTING;
		v12ToV14Book[Entity.DATA_FLAG_CRITICAL] = CRITICAL;
		v12ToV14Book[Entity.DATA_FLAG_CAN_SHOW_NAMETAG] = CAN_SHOW_NAME;
		v12ToV14Book[Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG] = ALWAYS_SHOW_NAME ;
		v12ToV14Book[Entity.DATA_FLAG_NO_AI] = NOAI;
		v12ToV14Book[Entity.DATA_FLAG_SILENT] = SILENT;
		v12ToV14Book[Entity.DATA_FLAG_WALLCLIMBING] = WALLCLIMBING;
		v12ToV14Book[Entity.DATA_FLAG_CAN_CLIMB] = CANCLIMB;
		v12ToV14Book[Entity.DATA_FLAG_SWIMMER] = CANSWIM;
		v12ToV14Book[Entity.DATA_FLAG_CAN_FLY] = CANFLY;
		v12ToV14Book[Entity.DATA_FLAG_CAN_WALK] = CANWALK;
		v12ToV14Book[Entity.DATA_FLAG_RESTING] = RESTING;
		v12ToV14Book[Entity.DATA_FLAG_SITTING] = SITTING;
		v12ToV14Book[Entity.DATA_FLAG_ANGRY] = ANGRY;
		v12ToV14Book[Entity.DATA_FLAG_INTERESTED] = INTERESTED;
		v12ToV14Book[Entity.DATA_FLAG_CHARGED] = CHARGED;
		v12ToV14Book[Entity.DATA_FLAG_TAMED] = TAMED;

		v12ToV14Book[Entity.DATA_FLAG_LEASHED] = LEASHED;
		v12ToV14Book[Entity.DATA_FLAG_SHEARED] = SHEARED;
		v12ToV14Book[Entity.DATA_FLAG_GLIDING] = GLIDING;
		v12ToV14Book[Entity.DATA_FLAG_ELDER] = ELDER;
		v12ToV14Book[Entity.DATA_FLAG_MOVING] = MOVING;
		v12ToV14Book[Entity.DATA_FLAG_BREATHING] = BREATHING;
		v12ToV14Book[Entity.DATA_FLAG_CHESTED] = CHESTED;
		v12ToV14Book[Entity.DATA_FLAG_STACKABLE] = STACKABLE;
		v12ToV14Book[Entity.DATA_FLAG_SHOWBASE] = SHOW_BOTTOM; // maybe
		v12ToV14Book[Entity.DATA_FLAG_REARING] = STANDING; // maybe
		v12ToV14Book[Entity.DATA_FLAG_VIBRATING] = SHAKING ;
		v12ToV14Book[Entity.DATA_FLAG_IDLING] = IDLING;
		v12ToV14Book[Entity.DATA_FLAG_EVOKER_SPELL] = CASTING; // maybe
		v12ToV14Book[Entity.DATA_FLAG_CHARGE_ATTACK] = CHARGING;
		v12ToV14Book[Entity.DATA_FLAG_WASD_CONTROLLED] = WASD_CONTROLLED;
		v12ToV14Book[Entity.DATA_FLAG_CAN_POWER_JUMP] = CAN_POWER_JUMP;
		v12ToV14Book[Entity.DATA_FLAG_LINGER] = LINGERING;
		v12ToV14Book[Entity.DATA_FLAG_HAS_COLLISION] = HAS_COLLISION;
		v12ToV14Book[Entity.DATA_FLAG_GRAVITY] = HAS_GRAVITY;
		v12ToV14Book[Entity.DATA_FLAG_FIRE_IMMUNE] = FIRE_IMMUNE;
		v12ToV14Book[Entity.DATA_FLAG_DANCING] = DANCING;
		v12ToV14Book[Entity.DATA_FLAG_ENCHANTED] = ENCHANTED;
		v12ToV14Book[Entity.DATA_FLAG_RETURN_TRIDENT] = RETURNTRIDENT;
		v12ToV14Book[Entity.DATA_FLAG_CONTAINER_IS_PRIVATE] = CONTAINER_IS_PRIVATE;
		v12ToV14Book[Entity.DATA_FLAG_TRANSFORMING] = IS_TRANSFORMING;
		v12ToV14Book[Entity.DATA_FLAG_SPIN_ATTACK] = DAMAGENEARBYMOBS;
		v12ToV14Book[Entity.DATA_FLAG_SWIMMING] = SWIMMING;

		Arrays.fill(v12ToV17Book, -1);
		v12ToV17Book[Entity.DATA_FLAG_ONFIRE] = ONFIRE;
		v12ToV17Book[Entity.DATA_FLAG_SNEAKING] = SNEAKING;
		v12ToV17Book[Entity.DATA_FLAG_RIDING] = RIDING;
		v12ToV17Book[Entity.DATA_FLAG_SPRINTING] = SPRINTING;
		v12ToV17Book[Entity.DATA_FLAG_ACTION] = USINGITEM;
		v12ToV17Book[Entity.DATA_FLAG_INVISIBLE] = INVISIBLE;
		v12ToV17Book[Entity.DATA_FLAG_TEMPTED] = TEMPTED;
		v12ToV17Book[Entity.DATA_FLAG_INLOVE] = INLOVE;
		v12ToV17Book[Entity.DATA_FLAG_SADDLED] = SADDLED;
		v12ToV17Book[Entity.DATA_FLAG_POWERED] = POWERED;
		v12ToV17Book[Entity.DATA_FLAG_IGNITED] = IGNITED;
		v12ToV17Book[Entity.DATA_FLAG_BABY] = BABY;
		v12ToV17Book[Entity.DATA_FLAG_CONVERTING] = CONVERTING;
		v12ToV17Book[Entity.DATA_FLAG_CRITICAL] = CRITICAL;
		v12ToV17Book[Entity.DATA_FLAG_CAN_SHOW_NAMETAG] = CAN_SHOW_NAME;
		v12ToV17Book[Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG] = ALWAYS_SHOW_NAME ;
		v12ToV17Book[Entity.DATA_FLAG_NO_AI] = NOAI;
		v12ToV17Book[Entity.DATA_FLAG_SILENT] = SILENT;
		v12ToV17Book[Entity.DATA_FLAG_WALLCLIMBING] = WALLCLIMBING;
		v12ToV17Book[Entity.DATA_FLAG_CAN_CLIMB] = CANCLIMB;
		v12ToV17Book[Entity.DATA_FLAG_SWIMMER] = CANSWIM;
		v12ToV17Book[Entity.DATA_FLAG_CAN_FLY] = CANFLY;
		v12ToV17Book[Entity.DATA_FLAG_CAN_WALK] = CANWALK;
		v12ToV17Book[Entity.DATA_FLAG_RESTING] = RESTING;
		v12ToV17Book[Entity.DATA_FLAG_SITTING] = SITTING;
		v12ToV17Book[Entity.DATA_FLAG_ANGRY] = ANGRY;
		v12ToV17Book[Entity.DATA_FLAG_INTERESTED] = INTERESTED;
		v12ToV17Book[Entity.DATA_FLAG_CHARGED] = CHARGED;
		v12ToV17Book[Entity.DATA_FLAG_TAMED] = TAMED;
		v12ToV17Book[Entity.DATA_FLAG_ORPHANED] = FLAG_17_ORPHANED;
		v12ToV17Book[Entity.DATA_FLAG_LEASHED] = FLAG_17_LEASHED;
		v12ToV17Book[Entity.DATA_FLAG_SHEARED] = FLAG_17_SHEARED;
		v12ToV17Book[Entity.DATA_FLAG_GLIDING] = FLAG_17_GLIDING;
		v12ToV17Book[Entity.DATA_FLAG_ELDER] = FLAG_17_ELDER;
		v12ToV17Book[Entity.DATA_FLAG_MOVING] = FLAG_17_MOVING;
		v12ToV17Book[Entity.DATA_FLAG_BREATHING] = FLAG_17_BREATHING;
		v12ToV17Book[Entity.DATA_FLAG_CHESTED] = FLAG_17_CHESTED;
		v12ToV17Book[Entity.DATA_FLAG_STACKABLE] = FLAG_17_STACKABLE;
		v12ToV17Book[Entity.DATA_FLAG_SHOWBASE] = FLAG_17_SHOWBASE; // maybe
		v12ToV17Book[Entity.DATA_FLAG_REARING] = FLAG_17_REARING; // maybe
		v12ToV17Book[Entity.DATA_FLAG_VIBRATING] = FLAG_17_VIBRATING ;
		v12ToV17Book[Entity.DATA_FLAG_IDLING] = FLAG_17_IDLING;
		v12ToV17Book[Entity.DATA_FLAG_EVOKER_SPELL] = FLAG_17_EVOKER_SPELL; // maybe
		v12ToV17Book[Entity.DATA_FLAG_CHARGE_ATTACK] = FLAG_17_CHARGE_ATTACK;
		v12ToV17Book[Entity.DATA_FLAG_WASD_CONTROLLED] = FLAG_17_WASD_CONTROLLED;
		v12ToV17Book[Entity.DATA_FLAG_CAN_POWER_JUMP] = FLAG_17_CAN_POWER_JUMP;
		v12ToV17Book[Entity.DATA_FLAG_LINGER] = FLAG_17_LINGER;
		v12ToV17Book[Entity.DATA_FLAG_HAS_COLLISION] = FLAG_17_HAS_COLLISION;
		v12ToV17Book[Entity.DATA_FLAG_GRAVITY] = FLAG_17_AFFECTED_BY_GRAVITY;
		v12ToV17Book[Entity.DATA_FLAG_FIRE_IMMUNE] = FLAG_17_FIRE_IMMUNE;
		v12ToV17Book[Entity.DATA_FLAG_DANCING] = FLAG_17_DANCING;
		v12ToV17Book[Entity.DATA_FLAG_ENCHANTED] = FLAG_17_ENCHANTED;
		v12ToV17Book[Entity.DATA_FLAG_RETURN_TRIDENT] = FLAG_17_RETURN_TRIDENT;
		v12ToV17Book[Entity.DATA_FLAG_CONTAINER_IS_PRIVATE] = FLAG_17_CONTAINER_IS_PRIVATE;
		v12ToV17Book[Entity.DATA_FLAG_TRANSFORMING] = FLAG_17_TRANSFORMING;
		v12ToV17Book[Entity.DATA_FLAG_SPIN_ATTACK] = FLAG_17_SPIN_ATTACK;
		v12ToV17Book[Entity.DATA_FLAG_SWIMMING] = FLAG_17_SWIMMING;
		v12ToV17Book[Entity.DATA_FLAG_BRIBED] = FLAG_17_BRIBED;
		v12ToV17Book[Entity.DATA_FLAG_PREGNANT] = FLAG_17_PREGNANT;
		v12ToV17Book[Entity.DATA_FLAG_LAYING_EGG] = FLAG_17_LAYING_EGG;
		v12ToV17Book[Entity.DATA_FLAG_RIDER_CAN_PICK] = FLAG_17_RIDER_CAN_PICK;

		v12ToV17Book[Entity.DATA_FLAG_TRANSITION_SETTING] = FLAG_1_TRANSITION_SETTING;
		v12ToV17Book[Entity.DATA_FLAG_EATING] = FLAG_1_EATING;
		v12ToV17Book[Entity.DATA_FLAG_LAYING_DOWN] = FLAG_1_LAYING_DOWN;
		v12ToV17Book[Entity.DATA_FLAG_SNEEZING] = FLAG_1_SNEEZING;
		v12ToV17Book[Entity.DATA_FLAG_TRUSTING] = FLAG_1_TRUSTING;
		v12ToV17Book[Entity.DATA_FLAG_ROLLING] = FLAG_1_ROLLING;
		v12ToV17Book[Entity.DATA_FLAG_SCARED] = FLAG_1_SCARED;
		v12ToV17Book[Entity.DATA_FLAG_IN_SCAFFOLDING] = FLAG_1_IN_SCAFFOLDING;
		v12ToV17Book[Entity.DATA_FLAG_OVER_SCAFFOLDING] = FLAG_1_OVER_SCAFFOLDING;
		v12ToV17Book[Entity.DATA_FLAG_FALL_THROUGH_SCAFFOLDING] = FLAG_1_FALL_THROUGH_SCAFFOLDING;
		v12ToV17Book[Entity.DATA_FLAG_BLOCKING] = FLAG_1_BLOCKING;
		v12ToV17Book[Entity.DATA_FLAG_TRANSITION_BLOCKING] = FLAG_1_TRANSITION_BLOCKING;
		v12ToV17Book[Entity.DATA_FLAG_BLOCKED_USING_SHIELD] = FLAG_1_BLOCKED_USING_SHIELD;
		v12ToV17Book[Entity.DATA_FLAG_BLOCKED_USING_DAMAGED_SHIELD] = FLAG_1_BLOCKED_USING_DAMAGED_SHIELD;
		v12ToV17Book[Entity.DATA_FLAG_SLEEPING] = FLAG_1_SLEEPING;
		v12ToV17Book[Entity.DATA_FLAG_ENTITY_GROW_UP] = FLAG_1_ENTITY_GROW_UP;
		v12ToV17Book[Entity.DATA_FLAG_TRADE_INTEREST] = FLAG_1_TRADE_INTEREST;
		v12ToV17Book[Entity.DATA_FLAG_DOOR_BREAKER] = FLAG_1_DOOR_BREAKER;
		v12ToV17Book[Entity.DATA_FLAG_BREAKING_OBSTRUCTION] = FLAG_1_BREAKING_OBSTRUCTION;
		v12ToV17Book[Entity.DATA_FLAG_DOOR_OPENER] = FLAG_1_DOOR_OPENER;
		v12ToV17Book[Entity.DATA_FLAG_IS_ILLAGER_CAPTAIN] = FLAG_1_IS_ILLAGER_CAPTAIN;
		v12ToV17Book[Entity.DATA_FLAG_STUNNED] = FLAG_1_STUNNED;
		v12ToV17Book[Entity.DATA_FLAG_ROARING] = FLAG_1_ROARING;
		v12ToV17Book[Entity.DATA_FLAG_DELAYED_ATTACK] = FLAG_1_DELAYED_ATTACK;
		v12ToV17Book[Entity.DATA_FLAG_IS_AVOIDING_MOBS] = FLAG_1_IS_AVOIDING_MOBS;
		v12ToV17Book[Entity.DATA_FLAG_IS_AVOIDING_BLOCKS] = FLAG_1_IS_AVOIDING_BLOCKS;
		v12ToV17Book[Entity.DATA_FLAG_FACING_TARGET_TO_RANGE_ATTACK] = FLAG_1_FACING_TARGET_TO_RANGE_ATTACK;
		v12ToV17Book[Entity.DATA_FLAG_HIDDEN_WHEN_INVISIBLE] = FLAG_1_HIDDEN_WHEN_INVISIBLE;
		v12ToV17Book[Entity.DATA_FLAG_IS_IN_UI] = FLAG_1_IS_IN_UI;
		v12ToV17Book[Entity.DATA_FLAG_STALKING] = FLAG_1_STALKING;
		v12ToV17Book[Entity.DATA_FLAG_EMOTING] = FLAG_1_EMOTING;
		v12ToV17Book[Entity.DATA_FLAG_CELEBRATING] = FLAG_1_CELEBRATING;
		v12ToV17Book[Entity.DATA_FLAG_ADMIRING] = FLAG_1_ADMIRING;
		v12ToV17Book[Entity.DATA_FLAG_CELEBRATING_SPECIAL] = FLAG_1_CELEBRATING_SPECIAL;
		v12ToV17Book[Entity.DATA_FLAG_OUT_OF_CONTROL] = FLAG_1_OUT_OF_CONTROL;
		v12ToV17Book[Entity.DATA_FLAG_RAM_ATTACK] = FLAG_1_RAM_ATTACK;
		v12ToV17Book[Entity.DATA_FLAG_PLAYING_DEAD] = FLAG_1_PLAYING_DEAD;
		v12ToV17Book[Entity.DATA_FLAG_IN_ASCENDABLE_BLOCK] = FLAG_1_IN_ASCENDABLE_BLOCK;
		v12ToV17Book[Entity.DATA_FLAG_OVER_DESCENDABLE_BLOCK] = FLAG_1_OVER_DESCENDABLE_BLOCK;
		v12ToV17Book[Entity.DATA_FLAG_CROAKING] = FLAG_1_CROAKING;
		v12ToV17Book[Entity.DATA_FLAG_EAT_MOB] = FLAG_1_EAT_MOB;
		v12ToV17Book[Entity.DATA_FLAG_JUMP_GOAL_JUMP] = FLAG_1_JUMP_GOAL_JUMP;
		v12ToV17Book[Entity.DATA_FLAG_EMERGING] = FLAG_1_EMERGING;
		v12ToV17Book[Entity.DATA_FLAG_SNIFFING] = FLAG_1_SNIFFING;
		v12ToV17Book[Entity.DATA_FLAG_DIGGING] = FLAG_1_DIGGING;
		v12ToV17Book[Entity.DATA_FLAG_SONIC_BOOM] = FLAG_1_SONIC_BOOM;

		System.arraycopy(v12ToV17Book, 0, v12ToV11950Book, 0, Entity.DATA_FLAG_UNDEFINED);
		v12ToV11950Book[Entity.DATA_FLAG_CAN_DASH] = FLAG_11950_CAN_DASH;
		v12ToV11950Book[Entity.DATA_FLAG_LINGER] = FLAG_11950_LINGER;
		v12ToV11950Book[Entity.DATA_FLAG_HAS_COLLISION] = FLAG_11950_HAS_COLLISION;
		v12ToV11950Book[Entity.DATA_FLAG_GRAVITY] = FLAG_11950_AFFECTED_BY_GRAVITY;
		v12ToV11950Book[Entity.DATA_FLAG_FIRE_IMMUNE] = FLAG_11950_FIRE_IMMUNE;
		v12ToV11950Book[Entity.DATA_FLAG_DANCING] = FLAG_11950_DANCING;
		v12ToV11950Book[Entity.DATA_FLAG_ENCHANTED] = FLAG_11950_ENCHANTED;
		v12ToV11950Book[Entity.DATA_FLAG_RETURN_TRIDENT] = FLAG_11950_RETURN_TRIDENT;
		v12ToV11950Book[Entity.DATA_FLAG_CONTAINER_IS_PRIVATE] = FLAG_11950_CONTAINER_IS_PRIVATE;
		v12ToV11950Book[Entity.DATA_FLAG_TRANSFORMING] = FLAG_11950_TRANSFORMING;
		v12ToV11950Book[Entity.DATA_FLAG_SPIN_ATTACK] = FLAG_11950_SPIN_ATTACK;
		v12ToV11950Book[Entity.DATA_FLAG_SWIMMING] = FLAG_11950_SWIMMING;
		v12ToV11950Book[Entity.DATA_FLAG_BRIBED] = FLAG_11950_BRIBED;
		v12ToV11950Book[Entity.DATA_FLAG_PREGNANT] = FLAG_11950_PREGNANT;
		v12ToV11950Book[Entity.DATA_FLAG_LAYING_EGG] = FLAG_11950_LAYING_EGG;
		v12ToV11950Book[Entity.DATA_FLAG_RIDER_CAN_PICK] = FLAG_11950_RIDER_CAN_PICK;
		v12ToV11950Book[Entity.DATA_FLAG_TRANSITION_SETTING] = FLAG_11950_TRANSITION_SETTING;
		v12ToV11950Book[Entity.DATA_FLAG_EATING] = FLAG_11950_EATING;
		v12ToV11950Book[Entity.DATA_FLAG_LAYING_DOWN] = FLAG_11950_LAYING_DOWN;
		v12ToV11950Book[Entity.DATA_FLAG_SNEEZING] = FLAG_11950_SNEEZING;
		v12ToV11950Book[Entity.DATA_FLAG_TRUSTING] = FLAG_11950_TRUSTING;
		v12ToV11950Book[Entity.DATA_FLAG_ROLLING] = FLAG_11950_ROLLING;
		v12ToV11950Book[Entity.DATA_FLAG_SCARED] = FLAG_11950_SCARED;
		v12ToV11950Book[Entity.DATA_FLAG_IN_SCAFFOLDING] = FLAG_11950_IN_SCAFFOLDING;
		v12ToV11950Book[Entity.DATA_FLAG_OVER_SCAFFOLDING] = FLAG_11950_OVER_SCAFFOLDING;
		v12ToV11950Book[Entity.DATA_FLAG_FALL_THROUGH_SCAFFOLDING] = FLAG_11950_FALL_THROUGH_SCAFFOLDING;
		v12ToV11950Book[Entity.DATA_FLAG_BLOCKING] = FLAG_11950_BLOCKING;
		v12ToV11950Book[Entity.DATA_FLAG_TRANSITION_BLOCKING] = FLAG_11950_TRANSITION_BLOCKING;
		v12ToV11950Book[Entity.DATA_FLAG_BLOCKED_USING_SHIELD] = FLAG_11950_BLOCKED_USING_SHIELD;
		v12ToV11950Book[Entity.DATA_FLAG_BLOCKED_USING_DAMAGED_SHIELD] = FLAG_11950_BLOCKED_USING_DAMAGED_SHIELD;
		v12ToV11950Book[Entity.DATA_FLAG_SLEEPING] = FLAG_11950_SLEEPING;
		v12ToV11950Book[Entity.DATA_FLAG_ENTITY_GROW_UP] = FLAG_11950_ENTITY_GROW_UP;
		v12ToV11950Book[Entity.DATA_FLAG_TRADE_INTEREST] = FLAG_11950_TRADE_INTEREST;
		v12ToV11950Book[Entity.DATA_FLAG_DOOR_BREAKER] = FLAG_11950_DOOR_BREAKER;
		v12ToV11950Book[Entity.DATA_FLAG_BREAKING_OBSTRUCTION] = FLAG_11950_BREAKING_OBSTRUCTION;
		v12ToV11950Book[Entity.DATA_FLAG_DOOR_OPENER] = FLAG_11950_DOOR_OPENER;
		v12ToV11950Book[Entity.DATA_FLAG_IS_ILLAGER_CAPTAIN] = FLAG_11950_IS_ILLAGER_CAPTAIN;
		v12ToV11950Book[Entity.DATA_FLAG_STUNNED] = FLAG_11950_STUNNED;
		v12ToV11950Book[Entity.DATA_FLAG_ROARING] = FLAG_11950_ROARING;
		v12ToV11950Book[Entity.DATA_FLAG_DELAYED_ATTACK] = FLAG_11950_DELAYED_ATTACK;
		v12ToV11950Book[Entity.DATA_FLAG_IS_AVOIDING_MOBS] = FLAG_11950_IS_AVOIDING_MOBS;
		v12ToV11950Book[Entity.DATA_FLAG_IS_AVOIDING_BLOCKS] = FLAG_11950_IS_AVOIDING_BLOCKS;
		v12ToV11950Book[Entity.DATA_FLAG_FACING_TARGET_TO_RANGE_ATTACK] = FLAG_11950_FACING_TARGET_TO_RANGE_ATTACK;
		v12ToV11950Book[Entity.DATA_FLAG_HIDDEN_WHEN_INVISIBLE] = FLAG_11950_HIDDEN_WHEN_INVISIBLE;
		v12ToV11950Book[Entity.DATA_FLAG_IS_IN_UI] = FLAG_11950_IS_IN_UI;
		v12ToV11950Book[Entity.DATA_FLAG_STALKING] = FLAG_11950_STALKING;
		v12ToV11950Book[Entity.DATA_FLAG_EMOTING] = FLAG_11950_EMOTING;
		v12ToV11950Book[Entity.DATA_FLAG_CELEBRATING] = FLAG_11950_CELEBRATING;
		v12ToV11950Book[Entity.DATA_FLAG_ADMIRING] = FLAG_11950_ADMIRING;
		v12ToV11950Book[Entity.DATA_FLAG_CELEBRATING_SPECIAL] = FLAG_11950_CELEBRATING_SPECIAL;
		v12ToV11950Book[Entity.DATA_FLAG_OUT_OF_CONTROL] = FLAG_11950_OUT_OF_CONTROL;
		v12ToV11950Book[Entity.DATA_FLAG_RAM_ATTACK] = FLAG_11950_RAM_ATTACK;
		v12ToV11950Book[Entity.DATA_FLAG_PLAYING_DEAD] = FLAG_11950_PLAYING_DEAD;
		v12ToV11950Book[Entity.DATA_FLAG_IN_ASCENDABLE_BLOCK] = FLAG_11950_IN_ASCENDABLE_BLOCK;
		v12ToV11950Book[Entity.DATA_FLAG_OVER_DESCENDABLE_BLOCK] = FLAG_11950_OVER_DESCENDABLE_BLOCK;
		v12ToV11950Book[Entity.DATA_FLAG_CROAKING] = FLAG_11950_CROAKING;
		v12ToV11950Book[Entity.DATA_FLAG_EAT_MOB] = FLAG_11950_EAT_MOB;
		v12ToV11950Book[Entity.DATA_FLAG_JUMP_GOAL_JUMP] = FLAG_11950_JUMP_GOAL_JUMP;
		v12ToV11950Book[Entity.DATA_FLAG_EMERGING] = FLAG_11950_EMERGING;
		v12ToV11950Book[Entity.DATA_FLAG_SNIFFING] = FLAG_11950_SNIFFING;
		v12ToV11950Book[Entity.DATA_FLAG_DIGGING] = FLAG_11950_DIGGING;
		v12ToV11950Book[Entity.DATA_FLAG_SONIC_BOOM] = FLAG_11950_SONIC_BOOM;
		v12ToV11950Book[Entity.DATA_FLAG_HAS_DASH_COOLDOWN] = FLAG_11950_HAS_DASH_COOLDOWN;
		v12ToV11950Book[Entity.DATA_FLAG_PUSH_TOWARDS_CLOSEST_SPACE] = FLAG_11950_PUSH_TOWARDS_CLOSEST_SPACE;

		v12ToV11950Book[Entity.DATA_FLAG_SCENTING] = FLAG_11970_SCENTING;
		v12ToV11950Book[Entity.DATA_FLAG_RISING] = FLAG_11970_RISING;
		v12ToV11950Book[Entity.DATA_FLAG_HAPPY] = FLAG_11970_HAPPY;
		v12ToV11950Book[Entity.DATA_FLAG_SEARCHING] = FLAG_11970_SEARCHING;

		v12ToV11950Book[Entity.DATA_FLAG_CRAWLING] = FLAG_12010_CRAWLING;

		v12ToV11950Book[Entity.DATA_FLAG_TIMER_FLAG_1] = FLAG_12040_TIMER_FLAG_1;
		v12ToV11950Book[Entity.DATA_FLAG_TIMER_FLAG_2] = FLAG_12040_TIMER_FLAG_2;
		v12ToV11950Book[Entity.DATA_FLAG_TIMER_FLAG_3] = FLAG_12040_TIMER_FLAG_3;

		v12ToV11950Book[Entity.DATA_FLAG_BODY_ROTATION_BLOCKED] = FLAG_12080_BODY_ROTATION_BLOCKED;

		v12ToV11950Book[Entity.DATA_FLAG_RENDERS_WHEN_INVISIBLE] = FLAG_12160_RENDERS_WHEN_INVISIBLE;

		v12ToV11950Book[Entity.DATA_FLAG_BODY_ROTATION_AXIS_ALIGNED] = FLAG_12170_BODY_ROTATION_AXIS_ALIGNED;
		v12ToV11950Book[Entity.DATA_FLAG_COLLIDABLE] = FLAG_12170_COLLIDABLE;
		v12ToV11950Book[Entity.DATA_FLAG_WASD_AIR_CONTROLLED] = FLAG_12170_WASD_AIR_CONTROLLED;

		v12ToV11950Book[Entity.DATA_FLAG_DOES_SERVER_AUTH_ONLY_DISMOUNT] = FLAG_12180_DOES_SERVER_AUTH_ONLY_DISMOUNT;

		v12ToV11950Book[Entity.DATA_FLAG_BODY_ROTATION_ALWAYS_FOLLOWS_HEAD] = FLAG_12190_BODY_ROTATION_ALWAYS_FOLLOWS_HEAD;
	}

	public static int translateTo14Id(int v12Id) {
		if (v12Id < 0 || v12Id >= Entity.DATA_FLAG_UNDEFINED) {
			return -1;
		}
		return v12ToV14Book[v12Id];
	}

	public static long translate14(long data) {
		long flags = 0;
		for (int i = 0; i < 64; i++) {
			if ((data & (1L << i)) > 0) {
				int bit = DataFlagTranslator.translateTo14Id(i);
				if (bit == -1) { // unknown version 14 translation.
					continue;
				}
				flags |= (1L << bit);
			}
		}
		return flags;
	}

	public static int translateTo17Id(int v12Id) {
		if (v12Id < 0 || v12Id >= Entity.DATA_FLAG_UNDEFINED) {
			return -1;
		}
		return v12ToV17Book[v12Id];
	}

	public static long[] translate17(long data, long data2) {
		return translate2Flags(data, data2, DataFlagTranslator::translateTo17Id);
	}

	private static long[] translate2Flags(long data, long data2, Int2IntFunction idTranslator) {
		long flags = 0;
		long flags2 = 0;
		for (int i = 0; i < 64; i++) {
			if ((data & (1L << i)) != 0) {
				int newId = idTranslator.applyAsInt(i);
				if (newId >= 64) {
					flags2 |= (1L << newId);
				} else if (newId != -1) {
					flags |= (1L << newId);
				}
			}
			if ((data2 & (1L << i)) != 0) {
				int newId = idTranslator.applyAsInt((1 << 6) | i);
				if (newId >= 64) {
					flags2 |= (1L << newId);
				} else if (newId != -1) {
					flags |= (1L << newId);
				}
			}
		}
		return new long[]{flags, flags2};
	}

	public static int translateTo11950Id(int v12Id) {
		if (v12Id < 0 || v12Id >= Entity.DATA_FLAG_UNDEFINED) {
			return -1;
		}
		return v12ToV11950Book[v12Id];
	}

	public static long[] translate11950(long data, long data2) {
		return translate2Flags(data, data2, DataFlagTranslator::translateTo11950Id);
	}
}
