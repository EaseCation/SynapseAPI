package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import java.util.UUID;

@ToString(exclude = "itemDataPalette")
public class StartGamePacket11920 extends Packet11920 {
    public static final int NETWORK_ID = ProtocolInfo.START_GAME_PACKET;

    public static final int GAME_PUBLISH_SETTING_NO_MULTI_PLAY = 0;
    public static final int GAME_PUBLISH_SETTING_INVITE_ONLY = 1;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_ONLY = 2;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS = 3;
    public static final int GAME_PUBLISH_SETTING_PUBLIC = 4;

    public static final int BIOME_TYPE_DEFAULT = 0;
    public static final int BIOME_TYPE_USER_DEFINED = 1;

    public static final int MOVEMENT_CLIENT_AUTHORITATIVE = 0;
    public static final int MOVEMENT_SERVER_AUTHORITATIVE = 1;
    public static final int MOVEMENT_SERVER_AUTHORITATIVE_WITH_REWIND = 2;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public boolean netease;

    public AbstractProtocol protocol;

    public long entityUniqueId;
    public long entityRuntimeId;
    public int playerGamemode;
    public float x;
    public float y;
    public float z;
    public float yaw;
    public float pitch;

    public int seed;
    public int spawnBiomeType = BIOME_TYPE_DEFAULT;
    public String userDefinedBiomeName = "plains";
    public byte dimension;
    public int generator = 1;
    public int worldGamemode;
    public int difficulty;
    public int spawnX;
    public int spawnY;
    public int spawnZ;
    public boolean hasAchievementsDisabled = true;
    public boolean worldEditor;
    public int dayCycleStopTime = -1; //-1 = not stopped, any positive value = stopped at that time
    public int eduEditionOffer = 0;
    public boolean hasEduFeaturesEnabled = false;
    public String eduProductUUID = "";
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
    public boolean hasPreviouslyUsedExperiments;
    public boolean bonusChest = false;
    public boolean hasStartWithMapEnabled = false;
    public boolean trustingPlayers;
    public int permissionLevel = 1;
    public int serverChunkTickRange = 4;
    public boolean hasLockedBehaviorPack = false;
    public boolean hasLockedResourcePack = false;
    public boolean isFromLockedWorldTemplate = false;
    public boolean isUsingMsaGamertagsOnly = false;
    public boolean isFromWorldTemplate = false;
    public boolean isWorldTemplateOptionLocked = false;
    public boolean isOnlySpawningV1Villagers = false;
    public boolean isDisablingPersonas;
    public boolean isDisablingCustomSkins;
    public String vanillaVersion = "1.19.20";
    public int limitedWorldWidth = 16;
    public int limitedWorldLength = 16;
    public boolean isNewNether;
    public String eduSharedUriResourceButtonName = "";
    public String eduSharedUriResourceLinkUri = "";
    public boolean experimentalGameplayOverride;
    public byte chatRestrictionLevel;
    public boolean disablePlayerInteractions;

    public String levelId = ""; //base64 string, usually the same as world folder name in vanilla
    public String worldName = "";
    public String premiumWorldTemplateId = "00000000-0000-0000-0000-000000000000";
    public boolean isTrial = false;
    public int movementType;
    public int rewindHistorySize = 20;
    public boolean isBlockBreakingServerAuthoritative;
    public long currentTick;
    public int enchantmentSeed;
    public byte[] blockProperties;
    public byte[] itemDataPalette;
    public String multiplayerCorrelationId = "";
    public boolean isInventoryServerAuthoritative;
    public String serverEngine = "1.19.20";
    public byte[] playerPropertyData = CompoundTag.EMPTY;
    /**
     * A XXHash64 of all block states by their compound tag.
     * A value of 0 will not be validated by the client.
     */
    public long blockRegistryChecksum = 0;
    public UUID worldTemplateId = new UUID(0, 0);
    public boolean clientSideGenerationEnabled;

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

        this.putLLong(this.seed);
        this.putLShort(this.spawnBiomeType);
        this.putString(this.userDefinedBiomeName);
        this.putVarInt(this.dimension);
        this.putVarInt(this.generator);
        this.putVarInt(this.worldGamemode);
        this.putVarInt(this.difficulty);
        this.putBlockVector3(this.spawnX, this.spawnY, this.spawnZ);
        this.putBoolean(this.hasAchievementsDisabled);
        this.putBoolean(this.worldEditor);
        this.putVarInt(this.dayCycleStopTime);
        this.putVarInt(this.eduEditionOffer);
        this.putBoolean(this.hasEduFeaturesEnabled);
        this.putString(this.eduProductUUID);
        this.putLFloat(this.rainLevel);
        this.putLFloat(this.lightningLevel);
        this.putBoolean(this.hasConfirmedPlatformLockedContent);
        this.putBoolean(this.multiplayerGame);
        this.putBoolean(this.broadcastToLAN);
        this.putVarInt(this.xblBroadcastIntent);
        this.putVarInt(this.platformBroadcastIntent);
        this.putBoolean(this.commandsEnabled);
        this.putBoolean(this.isTexturePacksRequired);
        this.putGameRules(this.gameRules);
        this.putLInt(0); // Experiment count
        this.putBoolean(this.hasPreviouslyUsedExperiments);
        this.putBoolean(this.bonusChest);
        this.putBoolean(this.hasStartWithMapEnabled);
        this.putVarInt(this.permissionLevel);
        this.putLInt(this.serverChunkTickRange);
        this.putBoolean(this.hasLockedBehaviorPack);
        this.putBoolean(this.hasLockedResourcePack);
        this.putBoolean(this.isFromLockedWorldTemplate);
        this.putBoolean(this.isUsingMsaGamertagsOnly);
        this.putBoolean(this.isFromWorldTemplate);
        this.putBoolean(this.isWorldTemplateOptionLocked);
        this.putBoolean(this.isOnlySpawningV1Villagers);
        this.putBoolean(this.isDisablingPersonas);
        this.putBoolean(this.isDisablingCustomSkins);
        this.putString("*");//this.putString(this.helper.getGameVersion());
        this.putLInt(this.limitedWorldWidth);
        this.putLInt(this.limitedWorldLength);
        this.putBoolean(this.isNewNether);
        this.putString(this.eduSharedUriResourceButtonName);
        this.putString(this.eduSharedUriResourceLinkUri);
        this.putBoolean(this.experimentalGameplayOverride);
        this.putByte(this.chatRestrictionLevel);
        this.putBoolean(this.disablePlayerInteractions);

        this.putString(this.levelId);
        this.putString(this.worldName);
        this.putString(this.premiumWorldTemplateId);
        this.putBoolean(this.isTrial);
        this.putVarInt(this.movementType);
        this.putVarInt(this.rewindHistorySize);
        this.putBoolean(this.isBlockBreakingServerAuthoritative);
        this.putLLong(this.currentTick);
        this.putVarInt(this.enchantmentSeed);
        this.put(this.blockProperties == null ? AdvancedGlobalBlockPalette.getCompiledBlockProperties(this.protocol, netease) : this.blockProperties);
        this.put(this.itemDataPalette == null ? AdvancedRuntimeItemPalette.getCompiledData(this.protocol, netease) : this.itemDataPalette);
        this.putString(this.multiplayerCorrelationId);
        this.putBoolean(this.isInventoryServerAuthoritative);
        this.putString(this.helper.getGameVersion());//this.putString(this.serverEngine);
        this.put(this.playerPropertyData);
        this.putLLong(this.blockRegistryChecksum);
        this.putUUID(this.worldTemplateId);
        this.putBoolean(this.clientSideGenerationEnabled);
    }
}
