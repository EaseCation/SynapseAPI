package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.entity.Entity;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class DataFlagTranslator {
	public final static Int2IntMap v12ToV14Book = new Int2IntOpenHashMap();
	public final static Int2IntMap v12ToV17Book = new Int2IntOpenHashMap();

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
	public static final int COUNT = 56;

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
	public static final int FLAG_1_UNDEFINED = 107;

	static {
		v12ToV14Book.defaultReturnValue(-1);
		v12ToV14Book.put(Entity.DATA_FLAG_ONFIRE, ONFIRE);
		v12ToV14Book.put(Entity.DATA_FLAG_SNEAKING, SNEAKING);
		v12ToV14Book.put(Entity.DATA_FLAG_RIDING, RIDING);
		v12ToV14Book.put(Entity.DATA_FLAG_SPRINTING, SPRINTING);
		v12ToV14Book.put(Entity.DATA_FLAG_ACTION, USINGITEM);
		v12ToV14Book.put(Entity.DATA_FLAG_INVISIBLE, INVISIBLE);
		v12ToV14Book.put(Entity.DATA_FLAG_TEMPTED, TEMPTED);
		v12ToV14Book.put(Entity.DATA_FLAG_INLOVE, INLOVE);
		v12ToV14Book.put(Entity.DATA_FLAG_SADDLED, SADDLED);
		v12ToV14Book.put(Entity.DATA_FLAG_POWERED, POWERED);
		v12ToV14Book.put(Entity.DATA_FLAG_IGNITED, IGNITED);
		v12ToV14Book.put(Entity.DATA_FLAG_BABY, BABY);
		v12ToV14Book.put(Entity.DATA_FLAG_CONVERTING, CONVERTING);
		v12ToV14Book.put(Entity.DATA_FLAG_CRITICAL, CRITICAL);
		v12ToV14Book.put(Entity.DATA_FLAG_CAN_SHOW_NAMETAG, CAN_SHOW_NAME);
		v12ToV14Book.put(Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG, ALWAYS_SHOW_NAME );
		v12ToV14Book.put(Entity.DATA_FLAG_NO_AI, NOAI);
		v12ToV14Book.put(Entity.DATA_FLAG_SILENT, SILENT);
		v12ToV14Book.put(Entity.DATA_FLAG_WALLCLIMBING, WALLCLIMBING);
		v12ToV14Book.put(Entity.DATA_FLAG_CAN_CLIMB, CANCLIMB);
		v12ToV14Book.put(Entity.DATA_FLAG_SWIMMER, CANSWIM);
		v12ToV14Book.put(Entity.DATA_FLAG_CAN_FLY, CANFLY);
		v12ToV14Book.put(Entity.DATA_FLAG_CAN_WALK, CANWALK);
		v12ToV14Book.put(Entity.DATA_FLAG_RESTING, RESTING);
		v12ToV14Book.put(Entity.DATA_FLAG_SITTING, SITTING);
		v12ToV14Book.put(Entity.DATA_FLAG_ANGRY, ANGRY);
		v12ToV14Book.put(Entity.DATA_FLAG_INTERESTED, INTERESTED);
		v12ToV14Book.put(Entity.DATA_FLAG_CHARGED, CHARGED);
		v12ToV14Book.put(Entity.DATA_FLAG_TAMED, TAMED);

		v12ToV14Book.put(Entity.DATA_FLAG_LEASHED, LEASHED);
		v12ToV14Book.put(Entity.DATA_FLAG_SHEARED, SHEARED);
		v12ToV14Book.put(Entity.DATA_FLAG_GLIDING, GLIDING);
		v12ToV14Book.put(Entity.DATA_FLAG_ELDER, ELDER);
		v12ToV14Book.put(Entity.DATA_FLAG_MOVING, MOVING);
		v12ToV14Book.put(Entity.DATA_FLAG_BREATHING, BREATHING);
		v12ToV14Book.put(Entity.DATA_FLAG_CHESTED, CHESTED);
		v12ToV14Book.put(Entity.DATA_FLAG_STACKABLE, STACKABLE);
		v12ToV14Book.put(Entity.DATA_FLAG_SHOWBASE, SHOW_BOTTOM); // maybe
		v12ToV14Book.put(Entity.DATA_FLAG_REARING, STANDING); // maybe
		v12ToV14Book.put(Entity.DATA_FLAG_VIBRATING, SHAKING );
		v12ToV14Book.put(Entity.DATA_FLAG_IDLING, IDLING);
		v12ToV14Book.put(Entity.DATA_FLAG_EVOKER_SPELL, CASTING); // maybe
		v12ToV14Book.put(Entity.DATA_FLAG_CHARGE_ATTACK, CHARGING);
		v12ToV14Book.put(Entity.DATA_FLAG_WASD_CONTROLLED, WASD_CONTROLLED);
		v12ToV14Book.put(Entity.DATA_FLAG_CAN_POWER_JUMP, CAN_POWER_JUMP);
		v12ToV14Book.put(Entity.DATA_FLAG_LINGER, LINGERING);
		v12ToV14Book.put(Entity.DATA_FLAG_HAS_COLLISION, HAS_COLLISION);
		v12ToV14Book.put(Entity.DATA_FLAG_GRAVITY, HAS_GRAVITY);
		v12ToV14Book.put(Entity.DATA_FLAG_FIRE_IMMUNE, FIRE_IMMUNE);
		v12ToV14Book.put(Entity.DATA_FLAG_DANCING, DANCING);
		v12ToV14Book.put(Entity.DATA_FLAG_ENCHANTED, ENCHANTED);
		v12ToV14Book.put(Entity.DATA_FLAG_RETURN_TRIDENT, RETURNTRIDENT);
		v12ToV14Book.put(Entity.DATA_FLAG_CONTAINER_IS_PRIVATE, CONTAINER_IS_PRIVATE);
		v12ToV14Book.put(Entity.DATA_FLAG_TRANSFORMING, IS_TRANSFORMING);
		v12ToV14Book.put(Entity.DATA_FLAG_SPIN_ATTACK, DAMAGENEARBYMOBS);
		v12ToV14Book.put(Entity.DATA_FLAG_SWIMMING, SWIMMING);

		v12ToV17Book.defaultReturnValue(-1);
		v12ToV17Book.put(Entity.DATA_FLAG_ONFIRE, ONFIRE);
		v12ToV17Book.put(Entity.DATA_FLAG_SNEAKING, SNEAKING);
		v12ToV17Book.put(Entity.DATA_FLAG_RIDING, RIDING);
		v12ToV17Book.put(Entity.DATA_FLAG_SPRINTING, SPRINTING);
		v12ToV17Book.put(Entity.DATA_FLAG_ACTION, USINGITEM);
		v12ToV17Book.put(Entity.DATA_FLAG_INVISIBLE, INVISIBLE);
		v12ToV17Book.put(Entity.DATA_FLAG_TEMPTED, TEMPTED);
		v12ToV17Book.put(Entity.DATA_FLAG_INLOVE, INLOVE);
		v12ToV17Book.put(Entity.DATA_FLAG_SADDLED, SADDLED);
		v12ToV17Book.put(Entity.DATA_FLAG_POWERED, POWERED);
		v12ToV17Book.put(Entity.DATA_FLAG_IGNITED, IGNITED);
		v12ToV17Book.put(Entity.DATA_FLAG_BABY, BABY);
		v12ToV17Book.put(Entity.DATA_FLAG_CONVERTING, CONVERTING);
		v12ToV17Book.put(Entity.DATA_FLAG_CRITICAL, CRITICAL);
		v12ToV17Book.put(Entity.DATA_FLAG_CAN_SHOW_NAMETAG, CAN_SHOW_NAME);
		v12ToV17Book.put(Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG, ALWAYS_SHOW_NAME );
		v12ToV17Book.put(Entity.DATA_FLAG_NO_AI, NOAI);
		v12ToV17Book.put(Entity.DATA_FLAG_SILENT, SILENT);
		v12ToV17Book.put(Entity.DATA_FLAG_WALLCLIMBING, WALLCLIMBING);
		v12ToV17Book.put(Entity.DATA_FLAG_CAN_CLIMB, CANCLIMB);
		v12ToV17Book.put(Entity.DATA_FLAG_SWIMMER, CANSWIM);
		v12ToV17Book.put(Entity.DATA_FLAG_CAN_FLY, CANFLY);
		v12ToV17Book.put(Entity.DATA_FLAG_CAN_WALK, CANWALK);
		v12ToV17Book.put(Entity.DATA_FLAG_RESTING, RESTING);
		v12ToV17Book.put(Entity.DATA_FLAG_SITTING, SITTING);
		v12ToV17Book.put(Entity.DATA_FLAG_ANGRY, ANGRY);
		v12ToV17Book.put(Entity.DATA_FLAG_INTERESTED, INTERESTED);
		v12ToV17Book.put(Entity.DATA_FLAG_CHARGED, CHARGED);
		v12ToV17Book.put(Entity.DATA_FLAG_TAMED, TAMED);
		v12ToV17Book.put(Entity.DATA_FLAG_ORPHANED, FLAG_17_ORPHANED);
		v12ToV17Book.put(Entity.DATA_FLAG_LEASHED, FLAG_17_LEASHED);
		v12ToV17Book.put(Entity.DATA_FLAG_SHEARED, FLAG_17_SHEARED);
		v12ToV17Book.put(Entity.DATA_FLAG_GLIDING, FLAG_17_GLIDING);
		v12ToV17Book.put(Entity.DATA_FLAG_ELDER, FLAG_17_ELDER);
		v12ToV17Book.put(Entity.DATA_FLAG_MOVING, FLAG_17_MOVING);
		v12ToV17Book.put(Entity.DATA_FLAG_BREATHING, FLAG_17_BREATHING);
		v12ToV17Book.put(Entity.DATA_FLAG_CHESTED, FLAG_17_CHESTED);
		v12ToV17Book.put(Entity.DATA_FLAG_STACKABLE, FLAG_17_STACKABLE);
		v12ToV17Book.put(Entity.DATA_FLAG_SHOWBASE, FLAG_17_SHOWBASE); // maybe
		v12ToV17Book.put(Entity.DATA_FLAG_REARING, FLAG_17_REARING); // maybe
		v12ToV17Book.put(Entity.DATA_FLAG_VIBRATING, FLAG_17_VIBRATING );
		v12ToV17Book.put(Entity.DATA_FLAG_IDLING, FLAG_17_IDLING);
		v12ToV17Book.put(Entity.DATA_FLAG_EVOKER_SPELL, FLAG_17_EVOKER_SPELL); // maybe
		v12ToV17Book.put(Entity.DATA_FLAG_CHARGE_ATTACK, FLAG_17_CHARGE_ATTACK);
		v12ToV17Book.put(Entity.DATA_FLAG_WASD_CONTROLLED, FLAG_17_WASD_CONTROLLED);
		v12ToV17Book.put(Entity.DATA_FLAG_CAN_POWER_JUMP, FLAG_17_CAN_POWER_JUMP);
		v12ToV17Book.put(Entity.DATA_FLAG_LINGER, FLAG_17_LINGER);
		v12ToV17Book.put(Entity.DATA_FLAG_HAS_COLLISION, FLAG_17_HAS_COLLISION);
		v12ToV17Book.put(Entity.DATA_FLAG_GRAVITY, FLAG_17_AFFECTED_BY_GRAVITY);
		v12ToV17Book.put(Entity.DATA_FLAG_FIRE_IMMUNE, FLAG_17_FIRE_IMMUNE);
		v12ToV17Book.put(Entity.DATA_FLAG_DANCING, FLAG_17_DANCING);
		v12ToV17Book.put(Entity.DATA_FLAG_ENCHANTED, FLAG_17_ENCHANTED);
		v12ToV17Book.put(Entity.DATA_FLAG_RETURN_TRIDENT, FLAG_17_RETURN_TRIDENT);
		v12ToV17Book.put(Entity.DATA_FLAG_CONTAINER_IS_PRIVATE, FLAG_17_CONTAINER_IS_PRIVATE);
		v12ToV17Book.put(Entity.DATA_FLAG_TRANSFORMING, FLAG_17_TRANSFORMING);
		v12ToV17Book.put(Entity.DATA_FLAG_SPIN_ATTACK, FLAG_17_SPIN_ATTACK);
		v12ToV17Book.put(Entity.DATA_FLAG_SWIMMING, FLAG_17_SWIMMING);
		v12ToV17Book.put(Entity.DATA_FLAG_BRIBED, FLAG_17_BRIBED);
		v12ToV17Book.put(Entity.DATA_FLAG_PREGNANT, FLAG_17_PREGNANT);
		v12ToV17Book.put(Entity.DATA_FLAG_LAYING_EGG, FLAG_17_LAYING_EGG);
		v12ToV17Book.put(Entity.DATA_FLAG_RIDER_CAN_PICK, FLAG_17_RIDER_CAN_PICK);

		v12ToV17Book.put(Entity.DATA_FLAG_TRANSITION_SETTING, FLAG_1_TRANSITION_SETTING);
		v12ToV17Book.put(Entity.DATA_FLAG_EATING, FLAG_1_EATING);
		v12ToV17Book.put(Entity.DATA_FLAG_LAYING_DOWN, FLAG_1_LAYING_DOWN);
		v12ToV17Book.put(Entity.DATA_FLAG_SNEEZING, FLAG_1_SNEEZING);
		v12ToV17Book.put(Entity.DATA_FLAG_TRUSTING, FLAG_1_TRUSTING);
		v12ToV17Book.put(Entity.DATA_FLAG_ROLLING, FLAG_1_ROLLING);
		v12ToV17Book.put(Entity.DATA_FLAG_SCARED, FLAG_1_SCARED);
		v12ToV17Book.put(Entity.DATA_FLAG_IN_SCAFFOLDING, FLAG_1_IN_SCAFFOLDING);
		v12ToV17Book.put(Entity.DATA_FLAG_OVER_SCAFFOLDING, FLAG_1_OVER_SCAFFOLDING);
		v12ToV17Book.put(Entity.DATA_FLAG_FALL_THROUGH_SCAFFOLDING, FLAG_1_FALL_THROUGH_SCAFFOLDING);
		v12ToV17Book.put(Entity.DATA_FLAG_BLOCKING, FLAG_1_BLOCKING);
		v12ToV17Book.put(Entity.DATA_FLAG_TRANSITION_BLOCKING, FLAG_1_TRANSITION_BLOCKING);
		v12ToV17Book.put(Entity.DATA_FLAG_BLOCKED_USING_SHIELD, FLAG_1_BLOCKED_USING_SHIELD);
		v12ToV17Book.put(Entity.DATA_FLAG_BLOCKED_USING_DAMAGED_SHIELD, FLAG_1_BLOCKED_USING_DAMAGED_SHIELD);
		v12ToV17Book.put(Entity.DATA_FLAG_SLEEPING, FLAG_1_SLEEPING);
		v12ToV17Book.put(Entity.DATA_FLAG_ENTITY_GROW_UP, FLAG_1_ENTITY_GROW_UP);
		v12ToV17Book.put(Entity.DATA_FLAG_TRADE_INTEREST, FLAG_1_TRADE_INTEREST);
		v12ToV17Book.put(Entity.DATA_FLAG_DOOR_BREAKER, FLAG_1_DOOR_BREAKER);
		v12ToV17Book.put(Entity.DATA_FLAG_BREAKING_OBSTRUCTION, FLAG_1_BREAKING_OBSTRUCTION);
		v12ToV17Book.put(Entity.DATA_FLAG_DOOR_OPENER, FLAG_1_DOOR_OPENER);
		v12ToV17Book.put(Entity.DATA_FLAG_IS_ILLAGER_CAPTAIN, FLAG_1_IS_ILLAGER_CAPTAIN);
		v12ToV17Book.put(Entity.DATA_FLAG_STUNNED, FLAG_1_STUNNED);
		v12ToV17Book.put(Entity.DATA_FLAG_ROARING, FLAG_1_ROARING);
		v12ToV17Book.put(Entity.DATA_FLAG_DELAYED_ATTACK, FLAG_1_DELAYED_ATTACK);
		v12ToV17Book.put(Entity.DATA_FLAG_IS_AVOIDING_MOBS, FLAG_1_IS_AVOIDING_MOBS);
		v12ToV17Book.put(Entity.DATA_FLAG_IS_AVOIDING_BLOCKS, FLAG_1_IS_AVOIDING_BLOCKS);
		v12ToV17Book.put(Entity.DATA_FLAG_FACING_TARGET_TO_RANGE_ATTACK, FLAG_1_FACING_TARGET_TO_RANGE_ATTACK);
		v12ToV17Book.put(Entity.DATA_FLAG_HIDDEN_WHEN_INVISIBLE, FLAG_1_HIDDEN_WHEN_INVISIBLE);
		v12ToV17Book.put(Entity.DATA_FLAG_IS_IN_UI, FLAG_1_IS_IN_UI);
		v12ToV17Book.put(Entity.DATA_FLAG_STALKING, FLAG_1_STALKING);
		v12ToV17Book.put(Entity.DATA_FLAG_EMOTING, FLAG_1_EMOTING);
		v12ToV17Book.put(Entity.DATA_FLAG_CELEBRATING, FLAG_1_CELEBRATING);
		v12ToV17Book.put(Entity.DATA_FLAG_ADMIRING, FLAG_1_ADMIRING);
		v12ToV17Book.put(Entity.DATA_FLAG_CELEBRATING_SPECIAL, FLAG_1_CELEBRATING_SPECIAL);
		v12ToV17Book.put(Entity.DATA_FLAG_RAM_ATTACK, FLAG_1_RAM_ATTACK);
		v12ToV17Book.put(Entity.DATA_FLAG_PLAYING_DEAD, FLAG_1_PLAYING_DEAD);
		v12ToV17Book.put(Entity.DATA_FLAG_IN_ASCENDABLE_BLOCK, FLAG_1_IN_ASCENDABLE_BLOCK);
		v12ToV17Book.put(Entity.DATA_FLAG_OVER_DESCENDABLE_BLOCK, FLAG_1_OVER_DESCENDABLE_BLOCK);
		v12ToV17Book.put(Entity.DATA_FLAG_CROAKING, FLAG_1_CROAKING);
		v12ToV17Book.put(Entity.DATA_FLAG_EAT_MOB, FLAG_1_EAT_MOB);
		v12ToV17Book.put(Entity.DATA_FLAG_JUMP_GOAL_JUMP, FLAG_1_JUMP_GOAL_JUMP);
		v12ToV17Book.put(Entity.DATA_FLAG_EMERGING, FLAG_1_EMERGING);
		v12ToV17Book.put(Entity.DATA_FLAG_SNIFFING, FLAG_1_SNIFFING);
		v12ToV17Book.put(Entity.DATA_FLAG_DIGGING, FLAG_1_DIGGING);
		v12ToV17Book.put(Entity.DATA_FLAG_SONIC_BOOM, FLAG_1_SONIC_BOOM);
		v12ToV17Book.put(Entity.DATA_FLAG_UNDEFINED, FLAG_1_UNDEFINED);
	}

	public static int translateTo14Id(int v12Id) {
		return v12ToV14Book.get(v12Id);
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
		return v12ToV17Book.get(v12Id);
	}

	public static long translate17(long data) {
		long flags = 0;
		for (int i = 0; i < 64; i++) {
			if ((data & (1L << i)) > 0) {
				int bit = DataFlagTranslator.translateTo17Id(i);
				if (bit == -1) { // unknown version 17 translation.
					continue;
				}
				flags |= (1L << bit);
			}
		}
		return flags;
	}
}
