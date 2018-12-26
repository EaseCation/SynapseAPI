package org.itxtech.synapseapi.multiprotocol.utils;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.entity.Entity;

public class EntityDataItemIDTranslator {

	public final static Map<Integer, Integer> v12ToV14Book;

	public final static int FLAGS = 0;
	public final static int STRUCTURAL_INTEGRITY = 1;
	public final static int VARIANT = 2;
	public final static int COLOR_INDEX = 3;
	public final static int NAME = 4;
	public final static int OWNER = 5;
	public final static int TARGET = 6;
	public final static int AIR_SUPPLY = 7;
	public final static int EFFECT_COLOR = 8;
	public final static int EFFECT_AMBIENCE = 9;
	public final static int JUMP_DURATION = 10;
	public final static int HURT = 11;
	public final static int HURT_DIR = 12;
	public final static int ROW_TIME_LEFT = 13;
	public final static int ROW_TIME_RIGHT = 14;
	public final static int VALUE = 15;
	public final static int DISPLAY_TILE_RUNTIME_ID = 16;
	public final static int DISPLAY_OFFSET = 17;
	public final static int CUSTOM_DISPLAY = 18;
	public final static int SWELL = 19;
	public final static int OLD_SWELL = 20;
	public final static int SWELL_DIR = 21;
	public final static int CHARGE_AMOUNT = 22;
	public final static int CARRY_BLOCK_RUNTIME_ID = 23;
	public final static int CLIENT_EVENT = 24;
	public final static int USING_ITEM = 25;
	public final static int PLAYER_FLAGS = 26;
	public final static int PLAYER_INDEX = 27;
	public final static int BED_POSITION = 28;
	public final static int X_POWER = 29;
	public final static int Y_POWER = 30;
	public final static int Z_POWER = 31;
	public final static int AUX_POWER = 32;
	public final static int FISHX = 33;
	public final static int FISHZ = 34;
	public final static int FISHANGLE = 35;
	public final static int AUX_VALUE_DATA = 36;
	public final static int LEASH_HOLDER = 37;
	public final static int SCALE = 38;
	public final static int INTERACT_TEXT = 39;
	public final static int SKIN_ID = 40;
	public final static int ACTIONS = 41;
	public final static int AIR_SUPPLY_MAX = 42;
	public final static int MARK_VARIANT = 43;
	public final static int CONTAINER_TYPE = 44;
	public final static int CONTAINER_SIZE = 45;
	public final static int CONTAINER_STRENGTH_MODIFIER = 46;
	public final static int BLOCK_TARGET = 47;
	public final static int INV = 48;
	public final static int TARGET_A = 49;
	public final static int TARGET_B = 50;
	public final static int TARGET_C = 51;
	public final static int AERIAL_ATTACK = 52;
	public final static int WIDTH = 53;
	public final static int HEIGHT = 54;
	public final static int FUSE_TIME = 55;
	public final static int SEAT_OFFSET = 56;
	public final static int SEAT_LOCK_RIDER_ROTATION = 57;
	public final static int SEAT_LOCK_RIDER_ROTATION_DEGREES = 58;
	public final static int SEAT_ROTATION_OFFSET = 59;
	public final static int DATA_RADIUS = 60;
	public final static int DATA_WAITING = 61;
	public final static int DATA_PARTICLE = 62;
	public final static int PEEK_ID = 63;
	public final static int ATTACH_FACE = 64;
	public final static int ATTACHED = 65;
	public final static int ATTACH_POS = 66;
	public final static int TRADE_TARGET = 67;
	public final static int CAREER = 68;
	public final static int HAS_COMMAND_BLOCK = 69;
	public final static int COMMAND_NAME = 70;
	public final static int LAST_COMMAND_OUTPUT = 71;
	public final static int TRACK_COMMAND_OUTPUT = 72;
	public final static int CONTROLLING_SEAT_INDEX = 73;
	public final static int STRENGTH = 74;
	public final static int STRENGTH_MAX = 75;
	public final static int DATA_SPELL_CASTING_COLOR = 76;
	public final static int DATA_LIFETIME_TICKS = 77;
	public final static int POSE_INDEX = 78;
	public final static int DATA_TICK_OFFSET = 79;
	public final static int NAMETAG_ALWAYS_SHOW = 80;
	public final static int COLOR_2_INDEX = 81;
	public final static int NAME_AUTHOR = 82;
	public final static int SCORE = 83;
	public final static int BALLOON_ANCHOR = 84;
	public final static int PUFFED_STATE = 85;
	
	static {
		v12ToV14Book = new HashMap<>();
		v12ToV14Book.put(Entity.DATA_FLAGS, FLAGS);
		v12ToV14Book.put(Entity.DATA_HEALTH, STRUCTURAL_INTEGRITY);
		v12ToV14Book.put(Entity.DATA_VARIANT, VARIANT);
		v12ToV14Book.put(Entity.DATA_COLOR, COLOR_INDEX );
		v12ToV14Book.put(Entity.DATA_NAMETAG, NAME );
		v12ToV14Book.put(Entity.DATA_OWNER_EID, OWNER );
		v12ToV14Book.put(Entity.DATA_TARGET_EID, TARGET );
		v12ToV14Book.put(Entity.DATA_AIR, AIR_SUPPLY );
		v12ToV14Book.put(Entity.DATA_POTION_COLOR, EFFECT_COLOR );
		v12ToV14Book.put(Entity.DATA_POTION_AMBIENT, EFFECT_AMBIENCE );
		v12ToV14Book.put(Entity.DATA_JUMP_DURATION, JUMP_DURATION );
		v12ToV14Book.put(Entity.DATA_HURT_TIME, HURT );
		v12ToV14Book.put(Entity.DATA_HURT_DIRECTION, HURT_DIR  );
		v12ToV14Book.put(Entity.DATA_PADDLE_TIME_LEFT, ROW_TIME_LEFT  );
		v12ToV14Book.put(Entity.DATA_PADDLE_TIME_RIGHT, ROW_TIME_RIGHT );
		v12ToV14Book.put(Entity.DATA_EXPERIENCE_VALUE, VALUE  );
		v12ToV14Book.put(Entity.DATA_DISPLAY_ITEM, DISPLAY_TILE_RUNTIME_ID  );
		v12ToV14Book.put(Entity.DATA_DISPLAY_OFFSET, DISPLAY_OFFSET  );
		v12ToV14Book.put(Entity.DATA_HAS_DISPLAY, CUSTOM_DISPLAY  ); //18
		// DATA_ENDERMAN_HELD_ITEM_ID = 23 unknown to translate. 
		// DATA_ENDERMAN_HELD_ITEM_DAMAGE = 24 unknown to translate. 
		// DATA_ENTITY_AGE = 25 unknown to translate. 
		v12ToV14Book.put(Entity.DATA_PLAYER_FLAGS, PLAYER_FLAGS );
		v12ToV14Book.put(Entity.DATA_PLAYER_BED_POSITION, BED_POSITION );
		v12ToV14Book.put(Entity.DATA_FIREBALL_POWER_X, X_POWER );
		v12ToV14Book.put(Entity.DATA_FIREBALL_POWER_Y, Y_POWER );
		v12ToV14Book.put(Entity.DATA_FIREBALL_POWER_Z, Z_POWER );
		v12ToV14Book.put(Entity.DATA_POTION_AUX_VALUE, AUX_VALUE_DATA );
		v12ToV14Book.put(Entity.DATA_LEAD_HOLDER_EID, LEASH_HOLDER );
		v12ToV14Book.put(Entity.DATA_SCALE, SCALE );
		v12ToV14Book.put(Entity.DATA_INTERACTIVE_TAG, INTERACT_TEXT );
		v12ToV14Book.put(Entity.DATA_NPC_SKIN_ID, SKIN_ID );
		// DATA_URL_TAG = 42 unknown to translate. 
		v12ToV14Book.put(Entity.DATA_MAX_AIR, AIR_SUPPLY_MAX );
		v12ToV14Book.put(Entity.DATA_MARK_VARIANT, MARK_VARIANT );
		// DATA_FLAG_FIRE_IMMUNE = 47 unknown to translate. 
		v12ToV14Book.put(Entity.DATA_BLOCK_TARGET, BLOCK_TARGET );
		v12ToV14Book.put(Entity.DATA_WITHER_INVULNERABLE_TICKS, INV );
		v12ToV14Book.put(Entity.DATA_WITHER_TARGET_1, TARGET_A );
		v12ToV14Book.put(Entity.DATA_WITHER_TARGET_2, TARGET_B );
		v12ToV14Book.put(Entity.DATA_WITHER_TARGET_3, TARGET_C );
		v12ToV14Book.put(Entity.DATA_BOUNDING_BOX_WIDTH, WIDTH );
		v12ToV14Book.put(Entity.DATA_BOUNDING_BOX_HEIGHT, HEIGHT );
		v12ToV14Book.put(Entity.DATA_FUSE_LENGTH, FUSE_TIME );
		v12ToV14Book.put(Entity.DATA_RIDER_SEAT_POSITION, SEAT_OFFSET );
		v12ToV14Book.put(Entity.DATA_RIDER_ROTATION_LOCKED, SEAT_LOCK_RIDER_ROTATION );
		// DATA_RIDER_MAX_ROTATION = 59 unknown to translate.
		// DATA_RIDER_MIN_ROTATION = 60 unknown to translate.
		v12ToV14Book.put(Entity.DATA_AREA_EFFECT_CLOUD_RADIUS, DATA_RADIUS );
		v12ToV14Book.put(Entity.DATA_AREA_EFFECT_CLOUD_WAITING, DATA_WAITING );
		v12ToV14Book.put(Entity.DATA_AREA_EFFECT_CLOUD_PARTICLE_ID, DATA_PARTICLE );
		v12ToV14Book.put(64, PEEK_ID );
		v12ToV14Book.put(Entity.DATA_SHULKER_ATTACH_FACE, ATTACH_FACE );
		v12ToV14Book.put(Entity.DATA_SHULKER_ATTACH_POS, ATTACH_POS );
		v12ToV14Book.put(Entity.DATA_TRADING_PLAYER_EID, TRADE_TARGET );
		v12ToV14Book.put(Entity.DATA_COMMAND_BLOCK_COMMAND, COMMAND_NAME );
		v12ToV14Book.put(Entity.DATA_COMMAND_BLOCK_LAST_OUTPUT, LAST_COMMAND_OUTPUT );
		v12ToV14Book.put(Entity.DATA_COMMAND_BLOCK_TRACK_OUTPUT, TRACK_COMMAND_OUTPUT );
		v12ToV14Book.put(Entity.DATA_CONTROLLING_RIDER_SEAT_NUMBER, CONTROLLING_SEAT_INDEX );
		v12ToV14Book.put(Entity.DATA_STRENGTH, STRENGTH );
		v12ToV14Book.put(Entity.DATA_MAX_STRENGTH, STRENGTH_MAX );
		v12ToV14Book.put(Entity.DATA_ALWAYS_SHOW_NAMETAG, NAMETAG_ALWAYS_SHOW );
	}
	
	public static Integer translateTo14Id(int v12Id) {
		return v12ToV14Book.get(v12Id);
	}
}
