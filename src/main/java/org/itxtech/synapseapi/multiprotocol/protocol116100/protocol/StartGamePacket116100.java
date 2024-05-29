package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.Experiments;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

@ToString(exclude = "itemDataPalette")
public class StartGamePacket116100 extends Packet116100 {

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
    public Experiments experiments = Experiments.NONE;
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
    public String vanillaVersion = "1.16.100";
    public int limitedWorldWidth = 16;
    public int limitedWorldLength = 16;
    public boolean isNewNether;
    public Boolean experimentalGameplayOverride;
    public String levelId = ""; //base64 string, usually the same as world folder name in vanilla
    public String worldName = "";
    public String premiumWorldTemplateId = "00000000-0000-0000-0000-000000000000";
    public boolean isTrial = false;
    public int movementType;
    public long currentTick;
    public boolean isInventoryServerAuthoritative;

    public int enchantmentSeed;

    public byte[] blockProperties;
    public byte[] itemDataPalette = null;

    public String multiplayerCorrelationId = "";

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
        this.putLShort(this.spawnBiomeType);
        this.putString(this.userDefinedBiomeName);
        this.putVarInt(this.dimension);
        this.putVarInt(this.generator);
        this.putVarInt(this.worldGamemode);
        this.putVarInt(this.difficulty);
        this.putBlockVector3(this.spawnX, this.spawnY, this.spawnZ);
        this.putBoolean(this.hasAchievementsDisabled);
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
        this.putLInt(experiments.experiments.length);
        for (Experiments.Experiment experiment : experiments.experiments) {
            this.putString(experiment.name());
            this.putBoolean(experiment.enable());
        }
        this.putBoolean(experiments.hasPreviouslyUsedExperiments);
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
        this.putString("*");//this.putString(this.helper.getGameVersion());
        this.putLInt(this.limitedWorldWidth);
        this.putLInt(this.limitedWorldLength);
        this.putBoolean(this.isNewNether);
        this.putOptional(this.experimentalGameplayOverride, BinaryStream::putBoolean);

        this.putString(this.levelId);
        this.putString(this.worldName);
        this.putString(this.premiumWorldTemplateId);
        this.putBoolean(this.isTrial);
        this.putUnsignedVarInt(this.movementType);
        this.putLLong(this.currentTick);
        this.putVarInt(this.enchantmentSeed);

        this.put(this.blockProperties == null ? AdvancedGlobalBlockPalette.getCompiledBlockProperties(this.protocol, netease) : this.blockProperties);
        this.put(this.itemDataPalette == null ? AdvancedRuntimeItemPalette.getCompiledData(this.protocol, netease) : this.itemDataPalette);
        this.putString(this.multiplayerCorrelationId);
        this.putBoolean(this.isInventoryServerAuthoritative);

    }
}
