package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

/**
 * Created on 15-10-13.
 */
@ToString(exclude = {"blockPalette", "itemDataPalette"})
public class StartGamePacket112 extends Packet112 {

	public static final int NETWORK_ID = ProtocolInfo.START_GAME_PACKET;

	public static final int GAME_PUBLISH_SETTING_NO_MULTI_PLAY = 0;
	public static final int GAME_PUBLISH_SETTING_INVITE_ONLY = 1;
	public static final int GAME_PUBLISH_SETTING_FRIENDS_ONLY = 2;
	public static final int GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS = 3;
	public static final int GAME_PUBLISH_SETTING_PUBLIC = 4;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public boolean netease;

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
	public int dayCycleStopTime = -1; //-1 = not stopped, any positive value = stopped at that time
	public boolean eduMode = false;
	public boolean hasEduFeaturesEnabled = false;
	public float rainLevel;
	public float lightningLevel;
	public boolean hasConfirmedPlatformLockedContent = false;
	public boolean multiplayerGame = true;
	public boolean broadcastToLAN = true;
	public int xblBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC;
	public int platformBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC;
	public boolean commandsEnabled;
	public boolean isTexturePacksRequired = false;
	public GameRules gameRules;
	public boolean bonusChest = false;
	public boolean hasStartWithMapEnabled = false;
	public int permissionLevel = 1;
	public int serverChunkTickRange = 4;
	public boolean hasLockedBehaviorPack = false;
	public boolean hasLockedResourcePack = false;
	public boolean isFromLockedWorldTemplate = false;
	public boolean useMsaGamertagsOnly = false;
	public boolean isFromWorldTemplate = false;
	public boolean isWorldTemplateOptionLocked = false;
	public boolean isOnlySpawningV1Villagers = false;
	public String levelId = ""; //base64 string, usually the same as world folder name in vanilla
	public String worldName = "";
	public String premiumWorldTemplateId = "";
	public boolean isTrial = false;
	public long currentTick; //only used if isTrial is true
	public int enchantmentSeed;
	public String multiplayerCorrelationId = ""; //TODO: this should be filled with a UUID of some sort

	public byte[] blockPalette = null;
	public byte[] itemDataPalette = null;

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
		this.putBoolean(this.hasEduFeaturesEnabled);
		this.putLFloat(this.rainLevel);
		this.putLFloat(this.lightningLevel);
		this.putBoolean(this.hasConfirmedPlatformLockedContent);
		this.putBoolean(this.multiplayerGame);
		this.putBoolean(this.broadcastToLAN);
		this.putVarInt(this.xblBroadcastIntent);
		this.putVarInt(this.platformBroadcastIntent);
		this.putBoolean(this.commandsEnabled);
		this.putBoolean(this.isTexturePacksRequired);
		this.putGameRules(gameRules);
		this.putBoolean(this.bonusChest);
		this.putBoolean(this.hasStartWithMapEnabled);
		this.putVarInt(this.permissionLevel);
		this.putLInt(this.serverChunkTickRange);
		this.putBoolean(this.hasLockedBehaviorPack);
		this.putBoolean(this.hasLockedResourcePack);
		this.putBoolean(this.isFromLockedWorldTemplate);
		this.putBoolean(this.useMsaGamertagsOnly);
		this.putBoolean(this.isFromWorldTemplate);
		this.putBoolean(this.isWorldTemplateOptionLocked);
		this.putBoolean(this.isOnlySpawningV1Villagers);
		this.putString(this.levelId);
		this.putString(this.worldName);
		this.putString(this.premiumWorldTemplateId);
		this.putBoolean(this.isTrial);
		this.putLLong(this.currentTick);
		this.putVarInt(this.enchantmentSeed);
		this.put(blockPalette == null ? AdvancedGlobalBlockPalette.getCompiledTable(AbstractProtocol.PROTOCOL_112, netease) : blockPalette);
		this.put(itemDataPalette == null ? AdvancedGlobalBlockPalette.getCompiledItemDataPalette(AbstractProtocol.PROTOCOL_112, netease): itemDataPalette);
		this.putString(this.multiplayerCorrelationId);
	}
}
