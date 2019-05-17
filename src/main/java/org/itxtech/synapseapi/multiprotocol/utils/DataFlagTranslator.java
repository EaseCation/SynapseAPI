package org.itxtech.synapseapi.multiprotocol.utils;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.entity.Entity;

public class DataFlagTranslator {
	public final static Map<Integer, Integer> v12ToV14Book = new HashMap<>();
	public final static Map<Integer, Integer> v12ToV17Book = new HashMap<>();

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
	public static final int FLAG_17_SHOW_TRIDENT_ROPE = 52; // tridents show an animated rope when enchanted with loyalty after they are thrown and return to their owner. To be combined with DATA_OWNER_EID
	public static final int FLAG_17_CONTAINER_PRIVATE = 53; //inventory is private, doesn't drop contents when killed if true
	//54 TransformationComponent
	public static final int FLAG_17_SPIN_ATTACK = 55;
	public static final int FLAG_17_SWIMMING = 56;
	public static final int FLAG_17_BRIBED = 57; //dolphins have this set when they go to find treasure for the player
	public static final int FLAG_17_PREGNANT = 58;
	public static final int FLAG_17_LAYING_EGG = 59;

	static {
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
		v12ToV14Book.put(Entity.DATA_FLAG_GRAVITY, HAS_GRAVITY);
		v12ToV14Book.put(Entity.DATA_FLAG_SWIMMING, SWIMMING);

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
		v12ToV17Book.put(Entity.DATA_FLAG_RESTING, RESTING);
		v12ToV17Book.put(Entity.DATA_FLAG_SITTING, SITTING);
		v12ToV17Book.put(Entity.DATA_FLAG_ANGRY, ANGRY);
		v12ToV17Book.put(Entity.DATA_FLAG_INTERESTED, INTERESTED);
		v12ToV17Book.put(Entity.DATA_FLAG_CHARGED, CHARGED);
		v12ToV17Book.put(Entity.DATA_FLAG_TAMED, TAMED);

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
		v12ToV17Book.put(Entity.DATA_FLAG_GRAVITY, FLAG_17_AFFECTED_BY_GRAVITY);
		v12ToV17Book.put(Entity.DATA_FLAG_SWIMMING, FLAG_17_SWIMMING);
	}
	
	public static Integer translateTo14Id(int v12Id) {
		return v12ToV14Book.get(v12Id);
	}

	public static long translate14(long data) {
		long flags = 0;
		for (int i = 0; i < 64; i++) {
			if ((data & (1L << i)) > 0) {
				Integer bit = DataFlagTranslator.translateTo14Id(i);
				if(bit == null) { // unknown version 14 translation.
					continue;
				}
				flags |= (1L << bit);
			}
		}
		return flags;
	}

	public static Integer translateTo17Id(int v12Id) {
		return v12ToV17Book.get(v12Id);
	}

	public static long translate17(long data) {
		long flags = 0;
		for (int i = 0; i < 64; i++) {
			if ((data & (1L << i)) > 0) {
				Integer bit = DataFlagTranslator.translateTo17Id(i);
				if(bit == null) { // unknown version 14 translation.
					continue;
				}
				flags |= (1L << bit);
			}
		}
		return flags;
	}
}
