package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * Created on 15-10-13.
 */
@ToString
public class StartGamePacket14 extends Packet14 {

	public static final int NETWORK_ID = ProtocolInfo.START_GAME_PACKET;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public long entityUniqueId;
	public long entityRuntimeId;
	public int playerGamemode;
	public float x;
	public float y;
	public float z;
	public float yaw;
	public float pitch;
	public int seed;
	public byte dimension;
	public int generator = 1;
	public int worldGamemode;
	public int difficulty;
	public int spawnX;
	public int spawnY;
	public int spawnZ;
	public boolean hasAchievementsDisabled = true;
	public int dayCycleStopTime = -1; // -1 = not stopped, any positive value = stopped at that time
	public boolean eduMode = false;
	public boolean areEduFeatureEnabled = false; // added by 1.4
	public float rainLevel;
	public float lightningLevel;
	public boolean multiplayerGame = true;
	public boolean broadcastToLAN = true;
	public boolean broadcastToXboxLive = true;
	public boolean commandsEnabled;
	public boolean isTexturePacksRequired = false;
	public GameRules gameRules = null;
	public boolean bonusChest = false;
	public boolean startWithMapEnabled = false; // added by 1.4
	public boolean trustPlayers = false;
	public int permissionLevel = 1;
	public int gamePublish = 4;
	public int serverChunkTickRange = 4; // added by 1.4
	public boolean canPlatformBroadcast = true; // added by 1.4
	public int broadcastMode = 3; // added by 1.4
	public boolean xblBroadcastIntent = true; // added by 1.4

	public String levelId = ""; // base64 string, usually the same as world folder name in vanilla
	public String worldName = "";
	public String premiumWorldTemplateId = "";
	public boolean unknown = false;
	public long currentTick;

	public int enchantmentSeed;

	@Override
	public void decode() {

	}

	@Override
	public void encode() {
		this.reset();
		this.putEntityUniqueId(this.entityUniqueId);
		this.putEntityRuntimeId(this.entityRuntimeId);
		this.putVarInt(this.playerGamemode);
		this.putVector3f(this.x, this.y, this.z);
		this.putLFloat(this.pitch);
		this.putLFloat(this.yaw);
		this.putVarInt(this.seed);
		this.putVarInt(this.dimension);
		this.putVarInt(this.generator);
		this.putVarInt(this.worldGamemode);
		this.putVarInt(this.difficulty);
		this.putBlockVector3(this.spawnX, this.spawnY, this.spawnZ);
		this.putBoolean(this.hasAchievementsDisabled);
		this.putVarInt(this.dayCycleStopTime);
		this.putBoolean(this.eduMode);
		this.putBoolean(this.areEduFeatureEnabled); // added by 1.4
		this.putLFloat(this.rainLevel);
		this.putLFloat(this.lightningLevel);
		this.putBoolean(this.multiplayerGame);
		this.putBoolean(this.broadcastToLAN);
		this.putBoolean(this.broadcastToXboxLive);
		this.putBoolean(this.commandsEnabled);
		this.putBoolean(this.isTexturePacksRequired);
		this.putGameRules(this.gameRules);
		this.putBoolean(this.bonusChest);
		this.putBoolean(this.startWithMapEnabled); // added by 1.4
		this.putBoolean(this.trustPlayers);
		this.putVarInt(this.permissionLevel);
		this.putVarInt(this.gamePublish);
		this.putLInt(this.serverChunkTickRange); // added by 1.4
		this.putBoolean(this.canPlatformBroadcast); // added by 1.4
		this.putVarInt(this.broadcastMode); // added by 1.4
		this.putBoolean(this.xblBroadcastIntent); // added by 1.4

		this.putBoolean(false); // unknown bytes.
		this.putBoolean(false); // unknown bytes.
		this.putBoolean(false); // unknown bytes.

		this.putString(this.levelId);
		this.putString(this.worldName);
		this.putString(this.premiumWorldTemplateId);
		this.putBoolean(this.unknown);
		this.putLLong(this.currentTick);
		this.putVarInt(this.enchantmentSeed);
	}
}
