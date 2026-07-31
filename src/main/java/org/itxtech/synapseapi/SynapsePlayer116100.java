package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.blockentity.*;
import cn.nukkit.command.Command;
import cn.nukkit.command.data.CommandPermission;
import cn.nukkit.entity.*;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.knockback.KnockbackProfile;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.property.*;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.ItemAttackDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.TakeResultAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.*;
import cn.nukkit.item.armortrim.TrimMaterial;
import cn.nukkit.item.armortrim.TrimMaterials;
import cn.nukkit.item.armortrim.TrimPattern;
import cn.nukkit.item.armortrim.TrimPatterns;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.level.format.generic.ChunkCachedData;
import cn.nukkit.level.format.generic.ChunkPacketCache;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.*;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.AnimatePacket.Action;
import cn.nukkit.network.protocol.types.*;
import cn.nukkit.potion.Effect;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.LoginChainData;
import cn.nukkit.utils.TextFormat;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.itxtech.synapseapi.camera.CameraManager;
import org.itxtech.synapseapi.dialogue.NPCDialoguePlayerHandler;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.filtertext.FilterTextService;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.Experiments;
import org.itxtech.synapseapi.multiprotocol.common.Experiments.Experiment;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraInstruction;
import org.itxtech.synapseapi.multiprotocol.common.camera.ControlScheme;
import org.itxtech.synapseapi.multiprotocol.common.clock.InitializeRegistryAction;
import org.itxtech.synapseapi.multiprotocol.common.clock.TimeMarker;
import org.itxtech.synapseapi.multiprotocol.common.drawer.Shape;
import org.itxtech.synapseapi.multiprotocol.common.level.DimensionDefinition;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheMissResponsePacket112;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.CreativeContentPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.StartGamePacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.FilterTextPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.ResourcePacksInfoPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.StartGamePacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.CameraShakePacket116210;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.StartGamePacket117;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NPCRequestPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NpcDialoguePacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.ResourcePacksInfoPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.StartGamePacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.StartGamePacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.UpdateSubChunkBlocksPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.PlayerStartItemCooldownPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.ChangeMobPropertyPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.SpawnParticleEffectPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.StartGamePacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.NetworkChunkPublisherUpdatePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.StartGamePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.StartGamePacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.ToastRequestPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.DeathInfoPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.StartGamePacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAdventureSettingsPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol11950.protocol.UpdateClientInputLocksPacket11950;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.CommandRequestPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.PlayerSkinPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.StartGamePacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11963.protocol.PlayerSkinPacket11963;
import org.itxtech.synapseapi.multiprotocol.protocol11970.protocol.CameraInstructionPacket11970;
import org.itxtech.synapseapi.multiprotocol.protocol11980.protocol.OpenSignPacket11980;
import org.itxtech.synapseapi.multiprotocol.protocol11980.protocol.RequestChunkRadiusPacket11980;
import org.itxtech.synapseapi.multiprotocol.protocol11980.protocol.StartGamePacket11980;
import org.itxtech.synapseapi.multiprotocol.protocol120.protocol.EmotePacket120;
import org.itxtech.synapseapi.multiprotocol.protocol120.protocol.StartGamePacket120;
import org.itxtech.synapseapi.multiprotocol.protocol120.protocol.TrimDataPacket120;
import org.itxtech.synapseapi.multiprotocol.protocol12030.protocol.CameraInstructionPacket12030;
import org.itxtech.synapseapi.multiprotocol.protocol12030.protocol.ResourcePacksInfoPacket12030;
import org.itxtech.synapseapi.multiprotocol.protocol12030.protocol.StartGamePacket12030;
import org.itxtech.synapseapi.multiprotocol.protocol12040.protocol.BossEventPacket12040;
import org.itxtech.synapseapi.multiprotocol.protocol12040.protocol.DisconnectPacket12040;
import org.itxtech.synapseapi.multiprotocol.protocol12050.protocol.PlayerToggleCrafterSlotRequestPacket12050;
import org.itxtech.synapseapi.multiprotocol.protocol12060.protocol.SetHudPacket12060;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.LecternUpdatePacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.ResourcePacksInfoPacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol12080.protocol.ResourcePackStackPacket12080;
import org.itxtech.synapseapi.multiprotocol.protocol12080.protocol.StartGamePacket12080;
import org.itxtech.synapseapi.multiprotocol.protocol12080.protocol.UpdatePlayerGameTypePacket12080;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.ContainerClosePacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.StartGamePacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.TextPacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121100.protocol.CameraAimAssistPacket120100;
import org.itxtech.synapseapi.multiprotocol.protocol121100.protocol.CameraInstructionPacket121100;
import org.itxtech.synapseapi.multiprotocol.protocol121100.protocol.StartGamePacket121100;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.AnimatePacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.CameraInstructionPacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.DebugDrawerPacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.GraphicsOverrideParameterPacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol1212.protocol.ClientboundCloseFormPacket1212;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol12130.protocol.EmotePacket12130;
import org.itxtech.synapseapi.multiprotocol.protocol12130.protocol.ResourcePacksInfoPacket12130;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.CameraInstructionPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.MovementEffectPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.ResourcePacksInfoPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12150.protocol.CameraAimAssistPacket12050;
import org.itxtech.synapseapi.multiprotocol.protocol12150.protocol.ResourcePacksInfoPacket12150;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.BossEventPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.ClientCameraAimAssistPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CreativeContentPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.StartGamePacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.LevelSoundEventPacketV312170;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.PlayerUpdateEntityOverridesPacket12170;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.SetHudPacket12170;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.ClientboundControlSchemeSetPacket12180;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.PlayerLocationPacket12180;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.CameraInstructionPacket12190;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ResourcePacksInfoPacket12190;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.StartGamePacket12190;
import org.itxtech.synapseapi.multiprotocol.protocol126.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.CameraInstructionPacket12610;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.SyncWorldClocksPacket12610;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.UpdateClientInputLocksPacket12610;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.itxtech.synapseapi.multiprotocol.utils.*;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.utils.BlobTrack;
import org.itxtech.synapseapi.utils.PacketUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.SharedConstants.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

//TODO: 这个类已经用了好几个版本了 有时间可以整理一下 像以前那样分成不同版本
@Log4j2
public class SynapsePlayer116100 extends SynapsePlayer116 {

    public static final int GAME_TYPE_SURVIVAL = 0;
    public static final int GAME_TYPE_CREATIVE = 1;
    public static final int GAME_TYPE_ADVENTURE = 2;
    public static final int GAME_TYPE_SURVIVAL_VIEWER = 3;
    public static final int GAME_TYPE_CREATIVE_VIEWER = 4;
    public static final int GAME_TYPE_DEFAULT = 5;
    /**
     * 原生的旁观模式.
     * @since 1.18.30
     */
    public static final int GAME_TYPE_SPECTATOR = 6;

    protected static final byte[] EMPTY_SUBCHUNK_HEIGHTMAP = new byte[256];
    public static final byte[] EMPTY_SUBCHUNK_DATA = new byte[]{
            8, // sub chunk version
            0, // no layers - client will treat this as all-air
    };

    protected final Long2ObjectMap<IntSet> subChunkRequestQueue = new Long2ObjectOpenHashMap<>();
    protected final Long2ObjectMap<IntSet> subChunkSendQueue = new Long2ObjectOpenHashMap<>();

    public NPCDialoguePlayerHandler npcDialoguePlayerHandler;

    private byte skinHack; // 1.19.62

    protected int clientMaxViewDistance = -1;

    protected boolean allowAimAssist;
    protected String aimAssistPresetId = "";
    protected float aimAssistViewAngleX = 30;
    protected float aimAssistViewAngleY = 45;
    protected float aimAssistDistance = 5.7f;
    protected int aimAssistMode = CameraAimAssistPacket12050.MODE_ANGLE;

    private final Long2ObjectMap<ShapeInstance> shapes = new Long2ObjectOpenHashMap<>();

    public SynapsePlayer116100(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, synapseEntry, clientID, socketAddress);
    }

    @Override
    public void handleLoginPacket(PlayerLoginPacket packet) {
        super.handleLoginPacket(packet);
        if (!this.isSynapseLogin) {
            return;
        }

        blockVersion = StaticVersion.fromProtocol(protocol, isNetEaseClient());

        if (!isClosed()) {
            level.onPlayerAdd(this);
        }

        if (cachedExtra != null) {
            JsonElement maxViewDistance = cachedExtra.get("viewDistanceMax");
            if (maxViewDistance != null) {
                int viewDistance = maxViewDistance.getAsInt();
                if (viewDistance >= 4 && viewDistance <= 96) {
                    this.clientMaxViewDistance = viewDistance;
                }
            }
        }
    }

    @Override
    protected DataPacket generateStartGamePacket(Position spawnPosition) {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            StartGamePacket12630 startGamePacket = new StartGamePacket12630();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            experiments.add(VanillaExperiments.VOXEL_SHAPES);
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
            StartGamePacket12620 startGamePacket = new StartGamePacket12620();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            experiments.add(VanillaExperiments.VOXEL_SHAPES);
            experiments.add(VanillaExperiments.FURNACE_RECIPE_BOOK);
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            StartGamePacket126 startGamePacket = new StartGamePacket126();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            if (getProtocol() >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
                experiments.add(VanillaExperiments.VOXEL_SHAPES);
                experiments.add(VanillaExperiments.FURNACE_RECIPE_BOOK);
            }
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
            StartGamePacket121130 startGamePacket = new StartGamePacket121130();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_100.getProtocolStart()) {
            StartGamePacket121100 startGamePacket = new StartGamePacket121100();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            if (getProtocol() < AbstractProtocol.PROTOCOL_121_111.getProtocolStart()) {
                experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            }
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            StartGamePacket12190 startGamePacket = new StartGamePacket12190();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
            StartGamePacket12160 startGamePacket = new StartGamePacket12160();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket12160.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket12160.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            if (getProtocol() >= AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
                experiments.add(VanillaExperiments.EXPERIMENTAL_GRAPHICS);
                experiments.add(VanillaExperiments.LOCATOR_BAR);
            }
            if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
                experiments.add(VanillaExperiments.THIRD_PERSON_CAMERAS);
                experiments.add(VanillaExperiments.CAMERA_AIM_ASSIST);
            } else {
                experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
            }
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121.getProtocolStart()) {
            StartGamePacket121 startGamePacket = new StartGamePacket121();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket121.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket121.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            if (getProtocol() < AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                experiments.add(VanillaExperiments.DATA_DRIVEN_ITEMS);
            }
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                experiments.add(VanillaExperiments.THIRD_PERSON_CAMERAS);
                experiments.add(VanillaExperiments.FOCUS_TARGET_CAMERA);
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
                    experiments.add(VanillaExperiments.CAMERA_AIM_ASSIST);
                }
            }
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_120_80.getProtocolStart()) {
            StartGamePacket12080 startGamePacket = new StartGamePacket12080();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket12080.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket12080.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            experiments.add(VanillaExperiments.DATA_DRIVEN_ITEMS);
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_120_30.getProtocolStart()) {
            StartGamePacket12030 startGamePacket = new StartGamePacket12030();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket12030.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket12030.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            List<Experiment> experiments = new ArrayList<>();
            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
            experiments.add(VanillaExperiments.DATA_DRIVEN_ITEMS);
            experiments.add(VanillaExperiments.GAMETEST);
            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
            if (getProtocol() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
                experiments.add(VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES);
            }
            if (isBetaClient()) {
                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
            }
            startGamePacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
            StartGamePacket120 startGamePacket = new StartGamePacket120();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket120.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket120.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.isSoundServerAuthoritative = isServerAuthoritativeSoundEnabled();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES,
                    VanillaExperiments.CAMERAS
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_80.getProtocolStart()) {
            StartGamePacket11980 startGamePacket = new StartGamePacket11980();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11980.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11980.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_60.getProtocolStart()) {
            StartGamePacket11960 startGamePacket = new StartGamePacket11960();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11960.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11960.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
            StartGamePacket11920 startGamePacket = new StartGamePacket11920();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11920.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11920.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_10.getProtocolStart()) {
            StartGamePacket11910 startGamePacket = new StartGamePacket11910();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11910.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11910.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119.getProtocolStart()) {
            StartGamePacket119 startGamePacket = new StartGamePacket119();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket119.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket119.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.playerPropertyData = getCompiledPlayerProperties();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (isNetEaseClient() && this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30_NE.getProtocolStart()) {
            StartGamePacket11830NE startGamePacket = new StartGamePacket11830NE();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11830NE.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11830NE.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            StartGamePacket11830 startGamePacket = new StartGamePacket11830();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11830.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11830.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
            StartGamePacket118 startGamePacket = new StartGamePacket118();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket118.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket118.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_117_30.getProtocolStart()) {
            StartGamePacket11730 startGamePacket = new StartGamePacket11730();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket11730.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket11730.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES,
                    VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_117.getProtocolStart()) {
            StartGamePacket117 startGamePacket = new StartGamePacket117();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket117.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket117.MOVEMENT_CLIENT_AUTHORITATIVE;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS,
                    VanillaExperiments.GAMETEST,
                    VanillaExperiments.UPCOMING_CREATOR_FEATURES
            );
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_116_200.getProtocolStart()) {
            StartGamePacket116200 startGamePacket = new StartGamePacket116200();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.movementType = serverAuthoritativeMovement ? StartGamePacket116200.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket116200.MOVEMENT_CLIENT_AUTHORITATIVE;
            // 启用后破坏方块时的物品栏事务就不会塞在PlayerAuthInputPacket了
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking &&
                    //(!this.isNetEaseClient() && this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart());
                    (this.isNetEaseClient() || this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart());
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            startGamePacket.experiments = new Experiments(
                    VanillaExperiments.DATA_DRIVEN_BIOMES,
                    VanillaExperiments.DATA_DRIVEN_ITEMS
            );
            return startGamePacket;
        } else if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
            StartGamePacket116100NE startGamePacket = new StartGamePacket116100NE();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.level.getDifficulty();
            startGamePacket.spawnX = (int) spawnPosition.x;
            startGamePacket.spawnY = (int) spawnPosition.y;
            startGamePacket.spawnZ = (int) spawnPosition.z;
            startGamePacket.hasAchievementsDisabled = true;
            startGamePacket.dayCycleStopTime = -1;
            startGamePacket.rainLevel = 0;
            startGamePacket.lightningLevel = 0;
            startGamePacket.commandsEnabled = this.isEnableClientCommand();
            startGamePacket.levelId = "";
            startGamePacket.worldName = this.getServer().getNetwork().getName();
            startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
            startGamePacket.gameRules = getSupportedRules();
            startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
            startGamePacket.isMovementServerAuthoritative = this.isNetEaseClient();
            startGamePacket.currentTick = 0;//this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
            return startGamePacket;
        }
        StartGamePacket116100 startGamePacket = new StartGamePacket116100();
        startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
        startGamePacket.netease = this.isNetEaseClient();
        startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
        startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
        startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
        startGamePacket.x = (float) this.x;
        startGamePacket.y = (float) this.y;
        startGamePacket.z = (float) this.z;
        startGamePacket.yaw = (float) this.yaw;
        startGamePacket.pitch = (float) this.pitch;
        startGamePacket.seed = -1;
        startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
        startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
        startGamePacket.difficulty = this.level.getDifficulty();
        startGamePacket.spawnX = (int) spawnPosition.x;
        startGamePacket.spawnY = (int) spawnPosition.y;
        startGamePacket.spawnZ = (int) spawnPosition.z;
        startGamePacket.hasAchievementsDisabled = true;
        startGamePacket.dayCycleStopTime = -1;
        startGamePacket.rainLevel = 0;
        startGamePacket.lightningLevel = 0;
        startGamePacket.commandsEnabled = this.isEnableClientCommand();
        startGamePacket.levelId = "";
        startGamePacket.worldName = this.getServer().getNetwork().getName();
        startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
        startGamePacket.gameRules = getSupportedRules();
        startGamePacket.isInventoryServerAuthoritative = SERVER_AUTHORITATIVE_INVENTORY;
        startGamePacket.movementType = this.isNetEaseClient() ? StartGamePacket116100.MOVEMENT_SERVER_AUTHORITATIVE : StartGamePacket116100.MOVEMENT_CLIENT_AUTHORITATIVE;
        startGamePacket.currentTick = 0;//this.server.getTick();
        startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
        startGamePacket.experiments = new Experiments(
                VanillaExperiments.DATA_DRIVEN_BIOMES,
                VanillaExperiments.DATA_DRIVEN_ITEMS
        );
        return startGamePacket;
    }

    /**
     * Returns a client-friendly gamemode of the specified real gamemode This
     * function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that
     * includes Survival, Creative and Adventure)
     * <p>
     * TODO: remove this when Spectator Mode gets added properly to MCPE
     */
    private static int getClientFriendlyGamemode(int gamemode) {
        gamemode &= 0x03;
        if (gamemode == Player.SPECTATOR) {
//            if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
//                return GAME_TYPE_SPECTATOR; //TODO: test me
//            }
            return Player.CREATIVE;
        }
        return gamemode;
    }

    @Override
    public void handleDataPacket(DataPacket packet) {
        if (!isInitialized() && !PacketUtil.canBeSentBeforeLogin(packet.pid())) {
            return;
        }

        if (!this.isSynapseLogin) {
            super.handleDataPacket(packet);
            return;
        }

        packetswitch:
        switch (packet.pid()) {
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2:
                if (isServerAuthoritativeSoundEnabled()) {
                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "sound12");
                    break;
                }
                // DEPRECATED
                break;
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3:
                if (isServerAuthoritativeSoundEnabled()) {
                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "sound3");
                    break;
                }
                if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
                    if (!callPacketReceiveEvent(packet)) {
                        break;
                    }
                    LevelSoundEventPacketV312630 levelSoundEventPacket = (LevelSoundEventPacketV312630) packet;
                    int sound = LevelSoundEventMap.getId(levelSoundEventPacket.sound);
                    SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
                            sound,
                            new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
                            LevelSoundEventUtil.translateExtraDataFromClient(sound, levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
                            0,
                            levelSoundEventPacket.entityIdentifier,
                            levelSoundEventPacket.isBabyMob,
                            levelSoundEventPacket.isGlobal,
                            levelSoundEventPacket.entityUniqueId);
                    if (this.isSpectator()) {
                        event.setCancelled();
                    }
                    this.getServer().getPluginManager().callEvent(event);
                    // 接入服务端权威音效后不再转发旧版本的C2S包.
                    // 如有音效缺失, 查看客户端代码在相关位置补上即可
                    if (false && !event.isCancelled()) {
                        this.sendLevelSoundEvent(
                                event.getLevelSound(),
                                event.getPos(),
                                event.getExtraData(),
                                event.getPitch(),
                                event.getEntityIdentifier(),
                                event.isBabyMob(),
                                event.isGlobal(),
                                event.getEntityUniqueId()
                        );
                        this.getViewers().values().stream()
                                .filter(p -> p instanceof SynapsePlayer)
                                .forEach(p -> ((SynapsePlayer) p).sendLevelSoundEvent(
                                        event.getLevelSound(),
                                        event.getPos(),
                                        event.getExtraData(),
                                        event.getPitch(),
                                        event.getEntityIdentifier(),
                                        event.isBabyMob(),
                                        event.isGlobal(),
                                        event.getEntityUniqueId()
                                ));
                    }
                    break;
                }
                if (getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
                    if (!callPacketReceiveEvent(packet)) {
                        break;
                    }
                    LevelSoundEventPacketV312620 levelSoundEventPacket = (LevelSoundEventPacketV312620) packet;
                    int sound = levelSoundEventPacket.sound;
                    SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
                            sound,
                            new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
                            LevelSoundEventUtil.translateExtraDataFromClient(sound, levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
                            0,
                            levelSoundEventPacket.entityIdentifier,
                            levelSoundEventPacket.isBabyMob,
                            levelSoundEventPacket.isGlobal,
                            levelSoundEventPacket.entityUniqueId);
                    if (this.isSpectator()) {
                        event.setCancelled();
                    }
                    this.getServer().getPluginManager().callEvent(event);
                    // 接入服务端权威音效后不再转发旧版本的C2S包.
                    // 如有音效缺失, 查看客户端代码在相关位置补上即可
                    if (false && !event.isCancelled()) {
                        this.sendLevelSoundEvent(
                                event.getLevelSound(),
                                event.getPos(),
                                event.getExtraData(),
                                event.getPitch(),
                                event.getEntityIdentifier(),
                                event.isBabyMob(),
                                event.isGlobal(),
                                event.getEntityUniqueId()
                        );
                        this.getViewers().values().stream()
                                .filter(p -> p instanceof SynapsePlayer)
                                .forEach(p -> ((SynapsePlayer) p).sendLevelSoundEvent(
                                        event.getLevelSound(),
                                        event.getPos(),
                                        event.getExtraData(),
                                        event.getPitch(),
                                        event.getEntityIdentifier(),
                                        event.isBabyMob(),
                                        event.isGlobal(),
                                        event.getEntityUniqueId()
                                ));
                    }
                    break;
                }
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
                    if (!callPacketReceiveEvent(packet)) {
                        break;
                    }
                    LevelSoundEventPacketV312170 levelSoundEventPacket = (LevelSoundEventPacketV312170) packet;
                    int sound = levelSoundEventPacket.sound;
                    SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
                            sound,
                            new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
                            LevelSoundEventUtil.translateExtraDataFromClient(sound, levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
                            0,
                            levelSoundEventPacket.entityIdentifier,
                            levelSoundEventPacket.isBabyMob,
                            levelSoundEventPacket.isGlobal,
                            levelSoundEventPacket.entityUniqueId);
                    if (this.isSpectator()) {
                        event.setCancelled();
                    }
                    this.getServer().getPluginManager().callEvent(event);
                    // 接入服务端权威音效后不再转发旧版本的C2S包.
                    // 如有音效缺失, 查看客户端代码在相关位置补上即可
                    if (false && !event.isCancelled()) {
                        this.sendLevelSoundEvent(
                                event.getLevelSound(),
                                event.getPos(),
                                event.getExtraData(),
                                event.getPitch(),
                                event.getEntityIdentifier(),
                                event.isBabyMob(),
                                event.isGlobal(),
                                event.getEntityUniqueId()
                        );
                        this.getViewers().values().stream()
                                .filter(p -> p instanceof SynapsePlayer)
                                .forEach(p -> ((SynapsePlayer) p).sendLevelSoundEvent(
                                        event.getLevelSound(),
                                        event.getPos(),
                                        event.getExtraData(),
                                        event.getPitch(),
                                        event.getEntityIdentifier(),
                                        event.isBabyMob(),
                                        event.isGlobal(),
                                        event.getEntityUniqueId()
                                ));
                    }
                    break;
                }
                super.handleDataPacket(packet);
                break;
            case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                ResourcePackClientResponsePacket16 responsePacket = (ResourcePackClientResponsePacket16) packet;
                switch (responsePacket.responseStatus) {
                    case ResourcePackClientResponsePacket.STATUS_REFUSED:
                        this.close("", "disconnectionScreen.noReason");
                        break;
                    case ResourcePackClientResponsePacket.STATUS_SEND_PACKS:
                        for (ResourcePackClientResponsePacket16.Entry entry : responsePacket.packEntries) {
                            ResourcePack resourcePack = this.resourcePacks.getOrDefault(entry.uuid, this.behaviourPacks.get(entry.uuid));
                            if (resourcePack == null) {
                                this.close("", "disconnectionScreen.resourcePack");
                                break;
                            }

                            ResourcePackDataInfoPacket dataInfoPacket = new ResourcePackDataInfoPacket();
                            dataInfoPacket.packId = resourcePack.getPackId();
                            dataInfoPacket.maxChunkSize = RESOURCE_PACK_CHUNK_SIZE;
                            dataInfoPacket.chunkCount = resourcePack.getChunkCount();
                            dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
                            dataInfoPacket.sha256 = resourcePack.getSha256();
                            if (resourcePack.getPackType().equals("resources")) {
                                dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
                            } else if (resourcePack.getPackType().equals("data")) {
                                dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_BEHAVIOR;
                            }
                            this.dataPacket(dataInfoPacket);
                        }
                        break;
                    case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS:
                        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                            ResourcePackStackPacket121130 stackPacket = new ResourcePackStackPacket121130();
                            stackPacket.mustAccept = this.forceResources;
                            stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
                            if (isNetEaseClient()) {
                                stackPacket.resourcePackStack = ArrayUtils.addAll(stackPacket.resourcePackStack, behaviourPacks.values().toArray(new ResourcePack[0]));
                            }
                            List<Experiment> experiments = new ArrayList<>();
                            experiments.add(VanillaExperiments.GAMETEST);
                            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
                            if (isBetaClient()) {
                                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
                            }
                            experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
                            if (getProtocol() >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
                                experiments.add(VanillaExperiments.VOXEL_SHAPES);
                                if (getProtocol() < AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
                                    experiments.add(VanillaExperiments.FURNACE_RECIPE_BOOK);
                                }
                            }
                            stackPacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
                            this.dataPacket(stackPacket);
                        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_120_80.getProtocolStart()) {
                            ResourcePackStackPacket12080 stackPacket = new ResourcePackStackPacket12080();
                            stackPacket.mustAccept = this.forceResources;
                            stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
                            stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
                            List<Experiment> experiments = new ArrayList<>();
                            if (getProtocol() < AbstractProtocol.PROTOCOL_121_111.getProtocolStart()) {
                                experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
                            }
                            if (getProtocol() < AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                                experiments.add(VanillaExperiments.DATA_DRIVEN_ITEMS);
                            }
                            experiments.add(VanillaExperiments.GAMETEST);
                            experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
                            if (isBetaClient()) {
                                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
                            }
                            if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
                                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                                    experiments.add(VanillaExperiments.THIRD_PERSON_CAMERAS);
                                    if (getProtocol() < AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
                                        experiments.add(VanillaExperiments.FOCUS_TARGET_CAMERA);
                                    }
                                }
                                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
                                    experiments.add(VanillaExperiments.CAMERA_AIM_ASSIST);
                                }
                            } else {
                                experiments.add(VanillaExperiments.EXPERIMENTAL_CREATOR_CAMERAS);
                            }
                            if (getProtocol() == AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
                                experiments.add(VanillaExperiments.EXPERIMENTAL_GRAPHICS);
                                experiments.add(VanillaExperiments.LOCATOR_BAR);
                            }
                            stackPacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
                            this.dataPacket(stackPacket);
                        } else if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                            ResourcePackStackPacket113 stackPacket = new ResourcePackStackPacket113();
                            stackPacket.mustAccept = this.forceResources;
                            stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
                            stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
                            this.dataPacket(stackPacket);
                        } else {
                            ResourcePackStackPacket116100 stackPacket = new ResourcePackStackPacket116100();
                            stackPacket.mustAccept = this.forceResources;
                            stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
                            stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
                            List<Experiment> experiments = new ArrayList<>();
                            experiments.add(VanillaExperiments.DATA_DRIVEN_BIOMES);
                            experiments.add(VanillaExperiments.DATA_DRIVEN_ITEMS);
                            experiments.add(VanillaExperiments.GAMETEST);
                            if (getProtocol() >= AbstractProtocol.PROTOCOL_117.getProtocolStart()) {
                                experiments.add(VanillaExperiments.UPCOMING_CREATOR_FEATURES);
                            }
                            if (getProtocol() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart() && getProtocol() >= AbstractProtocol.PROTOCOL_117_30.getProtocolStart()) {
                                experiments.add(VanillaExperiments.EXPERIMENTAL_MOLANG_FEATURES);
                            }
                            if (getProtocol() < AbstractProtocol.PROTOCOL_120_30.getProtocolStart() && getProtocol() >= AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
                                experiments.add(VanillaExperiments.CAMERAS);
                            }
                            if (isBetaClient() && getProtocol() >= AbstractProtocol.PROTOCOL_120_30.getProtocolStart()) {
                                experiments.add(VanillaExperiments.DEFERRED_TECHNICAL_PREVIEW);
                            }
                            stackPacket.experiments = new Experiments(experiments.toArray(new Experiment[0]));
                            this.dataPacket(stackPacket);
                        }

                        break;
                    case ResourcePackClientResponsePacket.STATUS_COMPLETED:
                        if (this.preLoginEventTask.isFinished()) {
                            this.completeLoginSequence();
                        } else {
                            this.shouldLogin = true;
                        }
                        break;
                }
                break;
            case ProtocolInfo.CONTAINER_CLOSE_PACKET:
                if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    return;
                }

                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121.getProtocolStart()) {
                    ContainerClosePacket121 containerClosePacket = (ContainerClosePacket121) packet;
                    if (!this.spawned || containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen) {
                        break;
                    }

                    Inventory windowInventory = this.windowIndex.get(containerClosePacket.windowId);
                    if (windowInventory != null) {
                        this.server.getPluginManager().callEvent(new InventoryCloseEvent(windowInventory, this));

                        if (containerClosePacket.windowId == ContainerIds.INVENTORY) {
                            this.inventoryOpen = false;
                        }

                        this.closingWindowId = containerClosePacket.windowId;
                        this.removeWindow(this.windowIndex.get(containerClosePacket.windowId), true);
                        this.closingWindowId = Integer.MIN_VALUE;
                    } else if (containerClosePacket.windowId != -1) {
                        this.getServer().getLogger().debug(getName() + " unopened window: " + containerClosePacket.windowId);
                    }

                    if (containerClosePacket.windowId == -1) {
                        if (this.lastOpenedWindowId != -1) {
                            log.debug("{} pushing a new container screen ({}) failed because a container screen that was already open on the client has not yet been closed", getName(), lastOpenedWindowId);

                            if (this.inventoryOpen && this.lastOpenedWindowId == ContainerIds.INVENTORY) {
                                this.inventoryOpen = false;
                            }

                            Inventory lastOpenedInventory = this.windowIndex.get(this.lastOpenedWindowId);
                            if (lastOpenedInventory != null) {
                                this.closingWindowId = Integer.MAX_VALUE;
                                this.removeWindow(lastOpenedInventory, true);
                                this.closingWindowId = Integer.MIN_VALUE;
                            }
                            this.lastOpenedWindowId = -1;
                            return;
                        }

                        this.craftingType = CRAFTING_SMALL;
                        this.resetCraftingGridType();
                        this.addWindow(this.craftingGrid, ContainerIds.NONE);

                        ContainerClosePacket121 pk = new ContainerClosePacket121();
                        pk.wasServerInitiated = false;
                        pk.windowId = -1;
                        this.dataPacket(pk);
                    } else { // Close bugged inventory
                        ContainerClosePacket121 pk = new ContainerClosePacket121();
                        pk.windowId = containerClosePacket.windowId;
                        this.dataPacket(pk);

                        for (Inventory open : new ArrayList<>(this.windows.keySet())) {
                            if (!(open instanceof ContainerInventory) && !(open instanceof PlayerEnderChestInventory)) {
                                continue;
                            }
                            this.server.getPluginManager().callEvent(new InventoryCloseEvent(open, this));

                            this.removeWindow(open, true);
                        }
                    }
                    break;
                }

                ContainerClosePacket116100 containerClosePacket = (ContainerClosePacket116100) packet;
                if (!this.spawned || containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen) {
                    break;
                }
                //this.getServer().getLogger().warning("Got ContainerClosePacket: " + containerClosePacket);

                Inventory windowInventory = this.windowIndex.get(containerClosePacket.windowId);
                if (windowInventory != null) {
                    this.server.getPluginManager().callEvent(new InventoryCloseEvent(windowInventory, this));

                    if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

                    this.closingWindowId = containerClosePacket.windowId;
                    this.removeWindow(this.windowIndex.get(containerClosePacket.windowId), true);
                    this.closingWindowId = Integer.MIN_VALUE;
                } else if (containerClosePacket.windowId != -1) {
                    this.getServer().getLogger().debug(getName() + " unopened window: " + containerClosePacket.windowId);
                }

                if (containerClosePacket.windowId == -1) {
                    this.craftingType = CRAFTING_SMALL;
                    this.resetCraftingGridType();
                    this.addWindow(this.craftingGrid, ContainerIds.NONE);

                    ContainerClosePacket116100 pk = new ContainerClosePacket116100();
                    pk.wasServerInitiated = false;
                    pk.windowId = -1;
                    this.dataPacket(pk);
                }
                break;
            case ProtocolInfo.MOVE_PLAYER_PACKET:
                if (this.serverAuthoritativeMovement) {
                    break;
                }

                if (this.teleportPosition != null) {
                    break;
                }

                MovePlayerPacket116100NE movePlayerPacket = (MovePlayerPacket116100NE) packet;
                if (!validateCoordinate(movePlayerPacket.x) || !validateCoordinate(movePlayerPacket.y) || !validateCoordinate(movePlayerPacket.z)
                        || !validateFloat(movePlayerPacket.pitch) || !validateFloat(movePlayerPacket.yaw) || !validateFloat(movePlayerPacket.headYaw)) {
                    this.getServer().getLogger().warning("Invalid movement received: " + this.getName());
                    this.close("", "Invalid movement");
                    return;
                }

                Vector3 newPos = new Vector3(movePlayerPacket.x, movePlayerPacket.y - this.getBaseOffset(), movePlayerPacket.z);

                if (newPos.distanceSquared(this) == 0 && movePlayerPacket.yaw % 360 == this.yaw && movePlayerPacket.pitch % 360 == this.pitch) {
                    break;
                }

                boolean revert = false;
                if (!this.isAlive() || !this.spawned) {
                    revert = true;
                    this.forceMovement = new Vector3(this.x, this.y, this.z);
                }

                if (riding != null) forceMovement = null;

                if (this.forceMovement != null && (newPos.distanceSquared(this.forceMovement) > 0.1 || revert)) {
                    this.sendPosition(this.forceMovement, this.yaw, this.pitch, MovePlayerPacket116100NE.MODE_TELEPORT);
                } else {
                    movePlayerPacket.yaw %= 360;
                    movePlayerPacket.pitch %= 360;

                    if (movePlayerPacket.yaw < 0) {
                        movePlayerPacket.yaw += 360;
                    }

                    this.setRotation(movePlayerPacket.yaw, movePlayerPacket.pitch);
                    this.newPosition = newPos;
                    this.forceMovement = null;
                }
                break;
            case ProtocolInfo.TEXT_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                if (!this.spawned || !this.isAlive()) {
                    break;
                }
                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
                    TextPacket126 textPacket = (TextPacket126) packet;

                    if (textPacket.type == TextPacket126.TYPE_CHAT) {
                        FilterTextService.filter(textPacket.message, this, true, this::preChat);
                    }
                } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                    TextPacket121130 textPacket = (TextPacket121130) packet;

                    if (textPacket.type == TextPacket121130.TYPE_CHAT) {
                        FilterTextService.filter(textPacket.message, this, true, this::preChat);
                    }
                } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121.getProtocolStart()) {
                    TextPacket121 textPacket = (TextPacket121) packet;

                    if (textPacket.type == TextPacket121.TYPE_CHAT) {
                        FilterTextService.filter(textPacket.message, this, true, this::preChat);
                    }
                } else if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                    TextPacket116100NE textPacket = (TextPacket116100NE) packet;

                    if (textPacket.type == TextPacket116100NE.TYPE_CHAT) {
                        FilterTextService.filter(textPacket.message, this, true, this::preChat);
                    }
                } else {
                    TextPacket116100 textPacket = (TextPacket116100) packet;

                    if (textPacket.type == TextPacket116100.TYPE_CHAT) {
                        FilterTextService.filter(textPacket.message, this, true, this::preChat);
                    }
                }
                break;
            case ProtocolInfo.FILTER_TEXT_PACKET:
                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_116_200.getProtocolStart()) {
                    FilterTextPacket116200 filterTextPacket = (FilterTextPacket116200) packet;
                    if (filterTextPacket.text == null || filterTextPacket.text.length() > 64) {
                        this.getServer().getLogger().debug(this.getName() + ": FilterTextPacket with too long text");
                        return;
                    }
                    FilterTextPacket116200 textResponsePacket = new FilterTextPacket116200();
                    textResponsePacket.text = filterTextPacket.text; //TODO: 铁砧重命名物品需要接入网易敏感词检查
                    textResponsePacket.fromServer = true;
                    this.dataPacket(textResponsePacket);
                }
                break;
            case ProtocolInfo.PLAYER_ACTION_PACKET:
                if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_210.getProtocolStart()
                        && (!this.isNetEaseClient() || this.getProtocol() < AbstractProtocol.PROTOCOL_116_200.getProtocolStart())) {
                    PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;

                    if (playerActionPacket.action == PlayerActionPacket14.ACTION_CREATIVE_PLAYER_DESTROY_BLOCK) {
                        if (true) {
                            break;
                        }
                        if (gamemode != CREATIVE || !spawned || !isAlive()) {
                            break;
                        }

                        BlockVector3 blockPos = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                        BlockFace face = BlockFace.fromIndex(playerActionPacket.data);

                        resetCraftingGridType();

                        if (blockPos.distanceSquared(this) > 10000) {
                            break;
                        }

                        Block block = level.getBlock(blockPos);

                        PlayerInteractEvent event = new PlayerInteractEvent(this, inventory.getItemInHand(), block, face,
                                block.isAir() ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
                        event.call();
                        if (event.isCancelled()) {
                            inventory.sendHeldItem(this);

                            level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                            level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(blockPos)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                            BlockEntity blockEntity = level.getBlockEntityIfLoaded(blockPos);
                            if (blockEntity instanceof BlockEntitySpawnable) {
                                ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                            }

                            break;
                        }

                        breakingBlock = block;
//                        lastBreak = System.currentTimeMillis();

                        Item item = inventory.getItemInHand();
                        Item oldItem = item.clone();

                        if (!canInteract(blockPos.add(0.5, 0.5, 0.5), MAX_REACH_DISTANCE_CREATIVE)
                                || (item = level.useBreakOn(blockPos.asVector3(), face, item, this, true)) == null) {
                            inventory.sendHeldItem(this);

                            level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                            level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(blockPos)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                            BlockEntity blockEntity = level.getBlockEntityIfLoaded(blockPos);
                            if (blockEntity instanceof BlockEntitySpawnable) {
                                ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                            }

                            breakingBlock = null;
                            break;
                        }

                        breakingBlock = null;

                        if (!isSurvival()) {
                            break;
                        }

                        getFoodData().updateFoodExpLevel(0.005f);

                        if (!item.equals(oldItem) || item.getCount() != oldItem.getCount()) {
                            inventory.setItemInHand(item);
                            inventory.sendHeldItem(getViewers().values());
                        }

                        this.setUsingItem(false);
                        break;
                    }

                    super.handleDataPacket(packet);
                    break;
                }

                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119.getProtocolStart()) {
                    PlayerActionPacket119 playerActionPacket = (PlayerActionPacket119) packet;
                    if (!this.callPacketReceiveEvent(playerActionPacket.toDefault())) break;

                    if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket119.ACTION_RESPAWN /*&& playerActionPacket.action != PlayerActionPacket119.ACTION_DIMENSION_CHANGE_REQUEST*/)) {
                        break;
                    }

                    playerActionPacket.entityId = this.id;
                    BlockVector3 pos = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);

                    actionswitch:
                    switch (playerActionPacket.action) {
                        case PlayerActionPacket119.ACTION_RESPAWN:
                            if (!this.spawned || this.isAlive() || !this.isOnline()) {
                                break;
                            }
                            this.respawn();
                            break;
                        case PlayerActionPacket119.ACTION_START_BREAK:
                            if (isServerAuthoritativeBlockBreakingEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action0");
                                return;
                            }

                            if (this.isSpectator()) {
                                break;
                            }

                            long currentBreak = System.currentTimeMillis();
                            BlockVector3 currentBreakPosition = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                            // HACK: Client spams multiple left clicks so we need to skip them.
                            if ((lastBreakPosition.equalsVec(currentBreakPosition) && (currentBreak - this.lastBreak) < 10) || !canInteract(pos.blockCenter(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
                                break;
                            }

                            Block target = this.level.getBlock(pos, false);
                            BlockFace face = BlockFace.fromIndex(playerActionPacket.data);

                            PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == BlockID.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
                            this.getServer().getPluginManager().callEvent(playerInteractEvent);
                            if (playerInteractEvent.isCancelled()) {
                                this.inventory.sendHeldItem(this);
                                break;
                            }

                            if (!isAdventure()) {
                                switch (target.getId()) {
                                    case Block.NOTEBLOCK:
                                        ((BlockNoteblock) target).emitSound();
                                        break actionswitch;
                                    case Block.DRAGON_EGG:
                                        if (!this.isCreative()) {
                                            ((BlockDragonEgg) target).teleport();
                                            break actionswitch;
                                        }
                                        break;
                                    case Block.FRAME:
                                    case Block.GLOW_FRAME:
                                        BlockEntity itemFrame = this.level.getBlockEntityIfLoaded(pos);
                                        if (itemFrame instanceof BlockEntityItemFrame && (((BlockEntityItemFrame) itemFrame).dropItem(this) || isCreative() && getProtocol() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart())) {
                                            break actionswitch;
                                        }
                                        break;
                                    case Block.LECTERN:
                                        if (level.getBlockEntityIfLoaded(pos) instanceof BlockEntityLectern lectern && lectern.dropBook(this)) {
                                            lectern.spawnToAll();
                                            if (isCreative()) {
                                                break actionswitch;
                                            }
                                        }
                                        break;
                                }
                            }

                            Block block = target.getSide(face);
                            if (block.isFire()) {
                                this.level.setBlock(block, Block.get(BlockID.AIR), true);
                                this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
                                break;
                            }

                            Item held = inventory.getItemInHand();
                            if (!this.isCreative()) {
                                //improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
                                //Done by lmlstarqaq
                                double breakTime = Mth.ceil(target.getBreakTime(held, this) * 20);
                                if (breakTime > 0) {
                                    LevelEventPacket pk = new LevelEventPacket();
                                    pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
                                    pk.x = (float) pos.x;
                                    pk.y = (float) pos.y;
                                    pk.z = (float) pos.z;
                                    pk.data = (int) (65535 / breakTime);
                                    this.getLevel().addChunkPacket(pos.getChunkX(), pos.getChunkZ(), pk);
                                }
                            } else if (held.isSword() || held.is(Item.TRIDENT) || held.is(Item.MACE)) {
                                break;
                            }

                            this.breakingBlock = target;
                            this.lastBreak = currentBreak;
                            this.lastBreakPosition = currentBreakPosition;
                            break;
                        case PlayerActionPacket119.ACTION_STOP_BREAK:
                        case PlayerActionPacket119.ACTION_ABORT_BREAK:
                            if (isServerAuthoritativeBlockBreakingEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action2");
                                return;
                            }

                            if (pos.distanceSquared(this) < 100) { // same as with ACTION_START_BREAK
                                LevelEventPacket pk = new LevelEventPacket();
                                pk.evid = LevelEventPacket.EVENT_BLOCK_STOP_BREAK;
                                pk.x = (float) pos.x;
                                pk.y = (float) pos.y;
                                pk.z = (float) pos.z;
                                pk.data = 0;
                                this.getLevel().addChunkPacket(pos.getChunkX(), pos.getChunkZ(), pk);
                            }
                            this.breakingBlock = null;
                            break;
                        case PlayerActionPacket119.ACTION_STOP_SLEEPING:
                            this.stopSleep();
                            break;
                        case PlayerActionPacket119.ACTION_JUMP:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action8");
                                return;
                            }

                            PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent(this);
                            this.server.getPluginManager().callEvent(playerJumpEvent);
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_START_SPRINT:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action9");
                                return;
                            }

                            if (isSprinting()) {
                                break packetswitch;
                            }

                            PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
                            if (hasEffect(Effect.BLINDNESS)) {
                                playerToggleSprintEvent.setCancelled();
                            }
                            this.server.getPluginManager().callEvent(playerToggleSprintEvent);
                            if (playerToggleSprintEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSprinting(true);
                            }
                            this.formWindows.clear();
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_SPRINT:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action10");
                                return;
                            }

                            if (!isSprinting()) {
                                break packetswitch;
                            }

                            playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleSprintEvent);
                            if (playerToggleSprintEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSprinting(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_START_SNEAK:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action11");
                                return;
                            }

                            if (isSneaking()) {
                                break packetswitch;
                            }

                            PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleSneakEvent);
                            if (playerToggleSneakEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSneaking(true);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_SNEAK:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action12");
                                return;
                            }

                            if (!isSneaking()) {
                                break packetswitch;
                            }

                            playerToggleSneakEvent = new PlayerToggleSneakEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleSneakEvent);
                            if (playerToggleSneakEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSneaking(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_DIMENSION_CHANGE_ACK:
                            this.onDimensionChangeSuccess();
                            break;
                        case PlayerActionPacket119.ACTION_START_GLIDE:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action15");
                                return;
                            }

                            if (isGliding()) {
                                break packetswitch;
                            }

                            PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
                            Item chestplate = getArmorInventory().getChestplate();
                            if (chestplate.getId() != Item.ELYTRA || chestplate.getDamage() >= chestplate.getMaxDurability() - 1) {
                                playerToggleGlideEvent.setCancelled();
                            }
                            this.server.getPluginManager().callEvent(playerToggleGlideEvent);
                            if (playerToggleGlideEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setGliding(true);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_GLIDE:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action16");
                                return;
                            }

                            if (!isGliding()) {
                                break packetswitch;
                            }

                            playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleGlideEvent);
                            if (playerToggleGlideEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setGliding(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_BUILD_DENIED:
//                            level.addLevelEvent(pos.blockCenter(), LevelEventPacket.EVENT_PARTICLE_BLOCK_FORCE_FIELD);
                            LevelEventPacket pk = new LevelEventPacket();
                            pk.evid = LevelEventPacket.EVENT_PARTICLE_BLOCK_FORCE_FIELD;
                            pk.x = pos.x + 0.5f;
                            pk.y = pos.y + 0.5f;
                            pk.z = pos.z + 0.5f;
                            dataPacket(pk);
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_CONTINUE_BREAK:
                            if (isServerAuthoritativeBlockBreakingEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action18");
                                return;
                            }

                            if (this.isBreakingBlock(pos)) {
                                block = this.level.getBlock(pos, false);
                                face = BlockFace.fromIndex(playerActionPacket.data);
                                Vector3 blockCenter = pos.blockCenter();
                                this.level.addParticle(new PunchBlockParticle(blockCenter, block, face));
                                level.addLevelEvent(blockCenter, LevelEventPacket.EVENT_PARTICLE_PUNCH_BLOCK_DOWN + face.getIndex(), block.getFullId());

                                int breakTime = Mth.ceil(block.getBreakTime(inventory.getItemInHand(), this) * 20);
                                level.addLevelEvent(pos.asVector3(), LevelEventPacket.EVENT_BLOCK_UPDATE_BREAK, breakTime <= 0 ? 0 : 65535 / breakTime);
                            }
                            break;
                        case PlayerActionPacket119.ACTION_START_SWIMMING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action21");
                                return;
                            }

                            if (isSwimming()) {
                                break;
                            }

                            PlayerToggleSwimEvent playerToggleSwimEvent = new PlayerToggleSwimEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleSwimEvent);
                            if (playerToggleSwimEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSwimming(true);
                            }
                            break;
                        case PlayerActionPacket119.ACTION_STOP_SWIMMING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action22");
                                return;
                            }

                            if (!isSwimming()) {
                                break;
                            }

                            playerToggleSwimEvent = new PlayerToggleSwimEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleSwimEvent);
                            if (playerToggleSwimEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSwimming(false);
                            }
                            break;
                        case PlayerActionPacket.ACTION_START_SPIN_ATTACK:
                            if (isSpectator()) {
                                setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
                                break;
                            }

                            Item trident = getInventory().getItemInHand();
                            if (trident.is(Item.TRIDENT)) {
                                int riptide = trident.getEnchantmentLevel(Enchantment.RIPTIDE);
                                if (riptide > 0) {
                                    //TODO: check water/rain
                                    if (setDataFlag(DATA_FLAG_SPIN_ATTACK, true)) {
                                        damageNearbyMobsTick = 20;
                                        level.addLevelSoundEvent(this.add(0, getHeight() * 0.5f, 0), switch (riptide) {
                                            case 1 -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_1;
                                            case 2 -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_2;
                                            default -> LevelSoundEventPacket.SOUND_ITEM_TRIDENT_RIPTIDE_3;
                                        });
                                    }
                                }
                            }
                            break;
                        case PlayerActionPacket.ACTION_STOP_SPIN_ATTACK:
                            damageNearbyMobsTick = 0;
                            setDataFlag(DATA_FLAG_SPIN_ATTACK, false);
                            break;
                        case PlayerActionPacket119.ACTION_CREATIVE_PLAYER_DESTROY_BLOCK:
                            if (true) {
                                break;
                            }
                            if (gamemode != CREATIVE) {
                                break;
                            }

                            BlockVector3 blockPos = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                            face = BlockFace.fromIndex(playerActionPacket.data);

                            resetCraftingGridType();

                            if (blockPos.distanceSquared(this) > 10000) {
                                break;
                            }

                            block = level.getBlock(blockPos);

                            PlayerInteractEvent event = new PlayerInteractEvent(this, inventory.getItemInHand(), block, face,
                                    block.isAir() ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
                            event.call();
                            if (event.isCancelled()) {
                                inventory.sendHeldItem(this);

                                level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                                level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(blockPos)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                                BlockEntity blockEntity = level.getBlockEntityIfLoaded(blockPos);
                                if (blockEntity instanceof BlockEntitySpawnable) {
                                    ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                                }

                                break;
                            }

                            breakingBlock = block;
//                            lastBreak = System.currentTimeMillis();

                            Item item = inventory.getItemInHand();
                            Item oldItem = item.clone();

                            if (!canInteract(blockPos.add(0.5, 0.5, 0.5), MAX_REACH_DISTANCE_CREATIVE)
                                    || (item = level.useBreakOn(blockPos.asVector3(), face, item, this, true)) == null) {
                                inventory.sendHeldItem(this);

                                level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                                level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(blockPos)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                                BlockEntity blockEntity = level.getBlockEntityIfLoaded(blockPos);
                                if (blockEntity instanceof BlockEntitySpawnable) {
                                    ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                                }

                                breakingBlock = null;
                                break;
                            }

                            breakingBlock = null;

                            if (!isSurvival()) {
                                break;
                            }

                            getFoodData().updateFoodExpLevel(0.005f);

                            if (!item.equals(oldItem) || item.getCount() != oldItem.getCount()) {
                                inventory.setItemInHand(item);
                                inventory.sendHeldItem(getViewers().values());
                            }

                            break;
                        case PlayerActionPacket.ACTION_MISSED_SWING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action31");
                                return;
                            }

                            if (isServerAuthoritativeSoundEnabled()) {
                                level.addLevelSoundEvent(this, inventory.getItemInHand().getAttackMissSound(), EntityFullNames.PLAYER);
                            }
                            break;
                        case PlayerActionPacket.ACTION_START_CRAWLING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action32");
                                return;
                            }

                            if (isCrawling()) {
                                break packetswitch;
                            }

                            PlayerToggleCrawlEvent playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
                            if (playerToggleCrawlEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setCrawling(true);
                            }
                            break packetswitch;
                        case PlayerActionPacket.ACTION_STOP_CRAWLING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action33");
                                return;
                            }

                            if (!isCrawling()) {
                                break packetswitch;
                            }

                            playerToggleCrawlEvent = new PlayerToggleCrawlEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleCrawlEvent);
                            if (playerToggleCrawlEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setCrawling(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket.ACTION_START_FLYING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action34");
                                return;
                            }

                            if (!server.getAllowFlight() && !getAdventureSettings().get(Type.ALLOW_FLIGHT)) {
                                kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server", false);
                                return;
                            }

                            if (getAdventureSettings().get(Type.FLYING)) {
                                break packetswitch;
                            }

                            PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, true);
                            if (isSpectator()) {
                                playerToggleFlightEvent.setCancelled();
                            }
                            this.server.getPluginManager().callEvent(playerToggleFlightEvent);
                            if (playerToggleFlightEvent.isCancelled()) {
                                this.sendAbilities(this, this.getAdventureSettings());
                            } else {
                                this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
                            }
                            break packetswitch;
                        case PlayerActionPacket.ACTION_STOP_FLYING:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action35");
                                return;
                            }

                            if (!getAdventureSettings().get(Type.FLYING)) {
                                break packetswitch;
                            }

                            playerToggleFlightEvent = new PlayerToggleFlightEvent(this, false);
                            if (isSpectator()) {
                                playerToggleFlightEvent.setCancelled();
                            }
                            this.server.getPluginManager().callEvent(playerToggleFlightEvent);
                            if (playerToggleFlightEvent.isCancelled()) {
                                this.sendAbilities(this, this.getAdventureSettings());
                            } else {
                                this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
                            }
                            break packetswitch;
                        case PlayerActionPacket.ACTION_HANDLED_TELEPORT:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action30");
                                return;
                            }
                            break;
                        case PlayerActionPacket.ACTION_ACK_ENTITY_DATA:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action36");
                                return;
                            }
                            break;
                        case PlayerActionPacket.ACTION_START_USING_ITEM:
                            if (isServerAuthoritativeMovementEnabled()) {
                                onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action37");
                                return;
                            }
                            break;
                    }

                    this.setUsingItem(!this.isJavaClient()
                            && playerActionPacket.action == PlayerActionPacket.ACTION_ITEM_USE_ON_START);
                    break;
                }

                PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;
                if (!this.callPacketReceiveEvent(((PlayerActionPacket14) packet).toDefault())) break;

                if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN)) {
                    break;
                }

                playerActionPacket.entityId = this.id;
                Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);

                actionswitch:
                switch (playerActionPacket.action) {
                    case PlayerActionPacket14.ACTION_START_BREAK:
                        if (isServerAuthoritativeBlockBreakingEnabled()) {
                            onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "action0");
                            break;
                        }

                        if (this.isSpectator()) {
                            break;
                        }

                        long currentBreak = System.currentTimeMillis();
                        BlockVector3 currentBreakPosition = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                        // HACK: Client spams multiple left clicks so we need to skip them.
                        if ((lastBreakPosition.equalsVec(currentBreakPosition) && (currentBreak - this.lastBreak) < 10) || !canInteract(pos.blockCenter(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
                            break;
                        }

                        Block target = this.level.getBlock(pos, false);
                        BlockFace face = BlockFace.fromIndex(playerActionPacket.data);

                        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == BlockID.AIR ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
                        this.getServer().getPluginManager().callEvent(playerInteractEvent);
                        if (playerInteractEvent.isCancelled()) {
                            this.inventory.sendHeldItem(this);
                            break;
                        }

                        if (/*!isAdventure()*/true) {
                            switch (target.getId()) {
                                case Block.NOTEBLOCK:
                                    ((BlockNoteblock) target).emitSound();
                                    break actionswitch;
                                case Block.DRAGON_EGG:
                                    if (!this.isCreative()) {
                                        ((BlockDragonEgg) target).teleport();
                                        break actionswitch;
                                    }
                                    break;
                                case Block.FRAME:
                                case Block.GLOW_FRAME:
                                    BlockEntity itemFrame = this.level.getBlockEntityIfLoaded(pos);
                                    if (itemFrame instanceof BlockEntityItemFrame && (((BlockEntityItemFrame) itemFrame).dropItem(this) || isCreative())) {
                                        break actionswitch;
                                    }
                                    break;
                                case Block.LECTERN:
                                    if (level.getBlockEntityIfLoaded(pos) instanceof BlockEntityLectern lectern && lectern.dropBook(this)) {
                                        lectern.spawnToAll();
                                        if (isCreative()) {
                                            break actionswitch;
                                        }
                                    }
                                    break;
                            }
                        }

                        Block block = target.getSide(face);
                        if (block.isFire()) {
                            this.level.setBlock(block, Block.get(BlockID.AIR), true);
                            this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
                            break;
                        }

                        Item held = inventory.getItemInHand();
                        if (!this.isCreative()) {
                            //improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
                            //Done by lmlstarqaq
                            double breakTime = Mth.ceil(target.getBreakTime(held, this) * 20);
                            if (breakTime > 0) {
                                LevelEventPacket pk = new LevelEventPacket();
                                pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
                                pk.x = (float) pos.x;
                                pk.y = (float) pos.y;
                                pk.z = (float) pos.z;
                                pk.data = (int) (65535 / breakTime);
                                this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
                            }
                        } else if (held.isSword() || held.is(Item.TRIDENT) || held.is(Item.MACE)) {
                            break;
                        }

                        this.breakingBlock = target;
                        this.lastBreak = currentBreak;
                        this.lastBreakPosition = currentBreakPosition;

                        this.setUsingItem(false);
                        break;
                    default:
                        super.handleDataPacket(packet);
                        break;
                }
                break;
            case ProtocolInfo.SUB_CHUNK_REQUEST_PACKET:
                if (!this.isSubChunkRequestAvailable()) {
                    break;
                }

                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
                    SubChunkRequestPacket12630 subChunkRequest = (SubChunkRequestPacket12630) packet;
                    int dimension = subChunkRequest.dimension;
                    int subChunkX = subChunkRequest.subChunkX;
                    int subChunkY = subChunkRequest.subChunkY;
                    int subChunkZ = subChunkRequest.subChunkZ;

                    for (BlockVector3 offset : subChunkRequest.positionOffsets) {
                        this.handleSubChunkRequest(dimension, subChunkX + offset.x, subChunkY + offset.y, subChunkZ + offset.z);
                    }
                } else if (this.getProtocol() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    SubChunkRequestPacket118 subChunkRequest = (SubChunkRequestPacket118) packet;
                    this.handleSubChunkRequest(subChunkRequest.dimension, subChunkRequest.subChunkX, subChunkRequest.subChunkY, subChunkRequest.subChunkZ);
                } else {
                    SubChunkRequestPacket11810 subChunkRequest = (SubChunkRequestPacket11810) packet;
                    int dimension = subChunkRequest.dimension;
                    int subChunkX = subChunkRequest.subChunkX;
                    int subChunkY = subChunkRequest.subChunkY;
                    int subChunkZ = subChunkRequest.subChunkZ;

                    for (BlockVector3 offset : subChunkRequest.positionOffsets) {
                        this.handleSubChunkRequest(dimension, subChunkX + offset.x, subChunkY + offset.y, subChunkZ + offset.z);
                    }
                }
                break;
            case ProtocolInfo.MODAL_FORM_RESPONSE_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                if (!callPacketReceiveEvent(packet)) {
                    break;
                }

                if (!this.spawned || !this.isAlive()) {
                    break;
                }

                ModalFormResponsePacket11920 modalFormPacket = (ModalFormResponsePacket11920) packet;

                FormWindow window = formWindows.get(modalFormPacket.formId);
                if (window != null) {
                    window.setResponse(modalFormPacket.data.trim(), getProtocol());

                    PlayerFormRespondedEvent event = new PlayerFormRespondedEvent(this, modalFormPacket.formId, window);
                    getServer().getPluginManager().callEvent(event);

                    formWindows.remove(modalFormPacket.formId);
                    break;
                }

                window = serverSettings.get(modalFormPacket.formId);
                if (window != null) {
                    window.setResponse(modalFormPacket.data.trim(), getProtocol());

                    PlayerSettingsRespondedEvent event = new PlayerSettingsRespondedEvent(this, modalFormPacket.formId, window);
                    getServer().getPluginManager().callEvent(event);

                    //Set back new settings if not been cancelled
                    if (!event.isCancelled() && window instanceof FormWindowCustom) {
                        ((FormWindowCustom) window).setElementsFromResponse();
                    }
                }
                break;
            case ProtocolInfo.MAP_INFO_REQUEST_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                if (!callPacketReceiveEvent(packet)) {
                    break;
                }

                MapInfoRequestPacket11920 mapInfoRequestPacket = (MapInfoRequestPacket11920) packet;
                Item mapItem = null;

                for (Item item : this.offhandInventory.getContents().values()) {
                    if (item instanceof ItemMap && ((ItemMap) item).getMapId() == mapInfoRequestPacket.mapId) {
                        mapItem = item;
                    }
                }

                if (mapItem == null) {
                    for (Item item : this.inventory.getContents().values()) {
                        if (item instanceof ItemMap && ((ItemMap) item).getMapId() == mapInfoRequestPacket.mapId) {
                            mapItem = item;
                        }
                    }
                }

                if (mapItem == null) {
                    for (BlockEntity blockEntity : this.level.getBlockEntities().values()) {
                        if (blockEntity instanceof BlockEntityItemFrame) {
                            BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) blockEntity;
                            Item item = itemFrame.getItem();
                            if (item instanceof ItemMap && ((ItemMap) item).getMapId() == mapInfoRequestPacket.mapId) {
                                ((ItemMap) item).sendImage(this);
                                break;
                            }
                        }
                    }
                }

                if (mapItem != null) {
                    final Player player = this;
                    final Item mapItemFinal = mapItem;
                    PlayerMapInfoRequestEvent event;
                    getServer().getPluginManager().callEvent(event = new PlayerMapInfoRequestEvent(this, mapItem));

                    if (!event.isCancelled()) {
                        this.getServer().getScheduler().scheduleAsyncTask(SynapseAPI.getInstance(), new AsyncTask() {
                            @Override
                            public void onRun() {
                                ((ItemMap) mapItemFinal).sendImage(player);
                            }
                        });
                    }
                }

                break;
            case ProtocolInfo.ADVENTURE_SETTINGS_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_119.getProtocolStart()) {
                    break;
                }
                super.handleDataPacket(packet);
                break;
            case ProtocolInfo.REQUEST_ABILITY_PACKET:
                if (!callPacketReceiveEvent(packet)) {
                    break;
                }
                RequestAbilityPacket119 requestAbilityPacket = (RequestAbilityPacket119) packet;
                if (requestAbilityPacket.ability == PlayerAbility.FLYING && requestAbilityPacket.type == RequestAbilityPacket119.TYPE_BOOL) {
                    if (requestAbilityPacket.boolValue && !server.getAllowFlight() && !getAdventureSettings().get(Type.ALLOW_FLIGHT)) {
                        this.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server", false);
                        break;
                    }

                    if (this.getAdventureSettings().get(Type.FLYING) == requestAbilityPacket.boolValue) {
                        break;
                    }

                    PlayerToggleFlightEvent playerToggleFlightEvent = new PlayerToggleFlightEvent(this, requestAbilityPacket.boolValue);
                    if (this.isSpectator()) {
                        playerToggleFlightEvent.setCancelled();
                    }
                    this.server.getPluginManager().callEvent(playerToggleFlightEvent);
                    if (playerToggleFlightEvent.isCancelled()) {
                        this.sendAbilities(this, this.getAdventureSettings());
                    } else {
                        this.getAdventureSettings().set(Type.FLYING, playerToggleFlightEvent.isFlying());
                    }
                }
                break;
            case ProtocolInfo.NPC_REQUEST_PACKET:
                if (!callPacketReceiveEvent(packet)) {
                    break;
                }
                NPCRequestPacket11710 npcRequestPacket = (NPCRequestPacket11710) packet;
                String sceneName = npcRequestPacket.sceneName;
                Entity entity = this.getLevel().getEntity(npcRequestPacket.entityRuntimeId);
                if (entity == null) {
                    NpcDialoguePacket11710 pk = new NpcDialoguePacket11710();
                    pk.npcEntityUniqueId = npcRequestPacket.entityRuntimeId;
                    pk.actionType = NpcDialoguePacket11710.ACTION_CLOSE;
                    pk.sceneName = sceneName;
                    dataPacket(pk);
                    break;  // 世界中不存在这个实体
                }
                if (npcRequestPacket.type == NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION) {
                    if (!this.getNpcDialoguePlayerHandler().onDialogueResponse(sceneName, npcRequestPacket.actionIndex)) {
                        NpcDialoguePacket11710 pk = new NpcDialoguePacket11710();
                        pk.npcEntityUniqueId = npcRequestPacket.entityRuntimeId;
                        pk.actionType = NpcDialoguePacket11710.ACTION_CLOSE;
                        pk.sceneName = sceneName;
                        dataPacket(pk);
                    }
                } else if (npcRequestPacket.type == NPCRequestPacket11710.TYPE_EXECUTE_OPENING_COMMANDS) {
                    if (!this.getNpcDialoguePlayerHandler().onDialogueOpening(sceneName)) {
                        NpcDialoguePacket11710 pk = new NpcDialoguePacket11710();
                        pk.npcEntityUniqueId = npcRequestPacket.entityRuntimeId;
                        pk.actionType = NpcDialoguePacket11710.ACTION_CLOSE;
                        pk.sceneName = sceneName;
                        dataPacket(pk);
                    }
                } else if (npcRequestPacket.type == NPCRequestPacket11710.TYPE_EXECUTE_CLOSING_COMMANDS) {
                    if (!this.getNpcDialoguePlayerHandler().onDialogueClosing(sceneName)) {
                        NpcDialoguePacket11710 pk = new NpcDialoguePacket11710();
                        pk.npcEntityUniqueId = npcRequestPacket.entityRuntimeId;
                        pk.actionType = NpcDialoguePacket11710.ACTION_CLOSE;
                        pk.sceneName = sceneName;
                        dataPacket(pk);
                    }

                }
                break;
            case ProtocolInfo.COMMAND_REQUEST_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                    CommandRequestPacket121130 commandRequestPacket = (CommandRequestPacket121130) packet;

                    if (!commandRequestPacket.command.startsWith("/")) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "cmd_pf_chat", commandRequestPacket.command);
                        break;
                    }

                    int extra;
                    int length = commandRequestPacket.command.length();
                    if (length > 30) {
                        float score = length / 15f;
                        extra = Mth.clamp((int) Mth.square(score), 1, 15);
                    } else {
                        extra = 0;
                    }
                    this.addViolationLevel(5 + extra, "cmd_req_chat");

                    if (!this.spawned || !this.isAlive()) {
                        break;
                    }

                    this.resetCraftingGridType();
                    this.craftingType = CRAFTING_SMALL;

                    if (this.messageCounter <= 0) {
                        break;
                    }

                    if (commandRequestPacket.command.length() > 512) {
                        break;
                    }

                    this.messageCounter--;

                    String command = commandRequestPacket.command;
                    if (this.removeFormat) {
                        command = TextFormat.clean(command, true);
                    }

                    PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(this, command);
                    playerCommandPreprocessEvent.call();
                    if (playerCommandPreprocessEvent.isCancelled()) {
                        break;
                    }

                    this.server.dispatchCommand(playerCommandPreprocessEvent.getPlayer(), playerCommandPreprocessEvent.getMessage().substring(1));
                    break;
                }

                if (getProtocol() < AbstractProtocol.PROTOCOL_119_60.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                CommandRequestPacket11960 commandRequestPacket = (CommandRequestPacket11960) packet;

                if (!commandRequestPacket.command.startsWith("/")) {
                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "cmd_pf_chat", commandRequestPacket.command);
                    break;
                }

                int extra;
                int length = commandRequestPacket.command.length();
                if (length > 30) {
                    float score = length / 15f;
                    extra = Mth.clamp((int) Mth.square(score), 1, 15);
                } else {
                    extra = 0;
                }
                this.addViolationLevel(5 + extra, "cmd_req_chat");

                if (!this.spawned || !this.isAlive()) {
                    break;
                }

                this.resetCraftingGridType();
                this.craftingType = CRAFTING_SMALL;

                if (this.messageCounter <= 0) {
                    break;
                }

                if (commandRequestPacket.command.length() > 512) {
                    break;
                }

                this.messageCounter--;

                String command = commandRequestPacket.command;
                if (this.removeFormat) {
                    command = TextFormat.clean(command, true);
                }

                PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(this, command);
                playerCommandPreprocessEvent.call();
                if (playerCommandPreprocessEvent.isCancelled()) {
                    break;
                }

                this.server.dispatchCommand(playerCommandPreprocessEvent.getPlayer(), playerCommandPreprocessEvent.getMessage().substring(1));
                break;
            case ProtocolInfo.SET_PLAYER_GAME_TYPE_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_120_80.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                SetPlayerGameTypePacket setPlayerGameTypePacket = (SetPlayerGameTypePacket) packet;
                int gamemode = vanillaGamemodeToNukkitGamemode(setPlayerGameTypePacket.gamemode);
                if (gamemode == this.gamemode) {
                    break;
                }

                if (!this.hasPermission("nukkit.command.gamemode") || gamemode == -1) {
                    UpdatePlayerGameTypePacket12080 updatePlayerGameTypePacket = new UpdatePlayerGameTypePacket12080();
                    updatePlayerGameTypePacket.gamemode = getClientFriendlyGamemode(this.gamemode);
                    updatePlayerGameTypePacket.playerEntityUniqueId = getId();
                    this.dataPacket(updatePlayerGameTypePacket);

                    this.sendAbilities(this, this.getAdventureSettings());
                    break;
                }

                this.setGamemode(gamemode, true);
                Command.broadcastCommandMessage(this, new TranslationContainer("commands.gamemode.success.self", Server.getGamemodeString(this.gamemode)));

                UpdatePlayerGameTypePacket12080 updatePlayerGameTypePacket = new UpdatePlayerGameTypePacket12080();
                updatePlayerGameTypePacket.gamemode = getClientFriendlyGamemode(this.gamemode);
                updatePlayerGameTypePacket.playerEntityUniqueId = getId();
                this.dataPacket(updatePlayerGameTypePacket);
                break;
            case ProtocolInfo.PLAYER_SKIN_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_119_63.getProtocolStart()) {
                    PlayerSkinPacket11963 playerSkinPacket = (PlayerSkinPacket11963) packet;

                    PlayerSkinPacket11963 skinResponse = new PlayerSkinPacket11963();
                    skinResponse.uuid = playerSkinPacket.uuid;
                    skinResponse.skin = playerSkinPacket.skin;
                    skinResponse.newSkinName = playerSkinPacket.newSkinName;
                    skinResponse.oldSkinName = playerSkinPacket.oldSkinName;
                    dataPacket(skinResponse);
                } else if (getProtocol() >= AbstractProtocol.PROTOCOL_119_60.getProtocolStart()) {
                    PlayerSkinPacket11960 playerSkinPacket = (PlayerSkinPacket11960) packet;

                    if (skinHack == 0) {
                        LoginChainData loginData = getLoginChainData();
                        String gameVersion = loginData.getGameVersion();
                        if ("1.19.62".equals(gameVersion)) {
                            skinHack = 1;
                        } else {
                            skinHack = -1;
                        }
                    }

                    PlayerSkinPacket11960 skinResponse = new PlayerSkinPacket11960();
                    skinResponse.uuid = playerSkinPacket.uuid;
                    skinResponse.skin = playerSkinPacket.skin;
                    skinResponse.newSkinName = playerSkinPacket.newSkinName;
                    skinResponse.oldSkinName = playerSkinPacket.oldSkinName;
                    if (skinHack == 1) {
                        skinResponse.extend = true;
                    }
                    dataPacket(skinResponse);
                }

                //TODO: PlayerChangeSkinEvent
                /*
                LoginChainData loginData = getLoginChainData();
                String uid = loginData.getNetEaseUID();
                if (uid == null || uid.isEmpty()) {
                    uid = loginData.getXUID();
                }
                server.updatePlayerListData(getUniqueId(), getId(), getDisplayName(), skin, uid, new Player[]{this});

                super.handleDataPacket(packet);
                */
                break;
            case ProtocolInfo.REQUEST_CHUNK_RADIUS_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_119_80.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }

                RequestChunkRadiusPacket11980 requestChunkRadiusPacket = (RequestChunkRadiusPacket11980) packet;
                this.viewDistance = Mth.clamp(requestChunkRadiusPacket.radius, 4, 96);
                this.clientMaxViewDistance = Math.max(this.viewDistance, Mth.clamp(requestChunkRadiusPacket.maxRadius, 4, 96));
                int requestedRadius = Math.min(this.viewDistance, this.getMaxViewDistance());

                // 玩家已登录后主动改变视距时触发事件
                PlayerChunkRadiusRequestEvent event = new PlayerChunkRadiusRequestEvent(this, this.chunkRadius, requestedRadius);
                this.server.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    // 取消：发送旧视距给客户端
                    ChunkRadiusUpdatedPacket chunkRadiusUpdatePacket = new ChunkRadiusUpdatedPacket();
                    chunkRadiusUpdatePacket.radius = this.chunkRadius;
                    this.dataPacket(chunkRadiusUpdatePacket);
                } else {
                    // 使用插件设置的最终视距
                    this.chunkRadius = event.getRadius();
                    ChunkRadiusUpdatedPacket chunkRadiusUpdatePacket = new ChunkRadiusUpdatedPacket();
                    chunkRadiusUpdatePacket.radius = this.chunkRadius;
                    this.dataPacket(chunkRadiusUpdatePacket);
                }

                break;
            case ProtocolInfo.EMOTE_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_30.getProtocolStart()) {
                    if (!this.spawned) {
                        break;
                    }
                    EmotePacket12130 emotePacket = (EmotePacket12130) packet;
                    if (emotePacket.runtimeId != this.getLocalEntityId()) {
                        server.getLogger().warning(this.username + " sent EmotePacket with invalid entity id: " + emotePacket.runtimeId + " != " + this.getLocalEntityId());
                        break;
                    }
                    if (emotePacket.emoteID.length() != 32 + 4) { // 00000000-0000-0000-0000-000000000000
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "emote");
                        break;
                    }
                    if ((emotePacket.flags & EmotePacket12130.FLAG_SERVER) != 0) {
                        break;
                    }
                    if (!emoteRequest()) {
                        this.addViolationLevel(8, "emote_req");
                        break;
                    }
                    if (isNetEaseClient()) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "ce_emote", emotePacket.emoteID);
                        break;
                    }
                    if (isSpectator()) {
                        break;
                    }
                    if (emoting) {
                        break;
                    }

                    int flags = emotePacket.flags | EmotePacket12130.FLAG_SERVER;
                    if (MUTE_EMOTE_CHAT) {
                        flags |= EmotePacket12130.FLAG_MUTE_ANNOUNCEMENT;
                    }
                    for (Player viewer : this.getViewers().values()) {
                        viewer.playEmote(emotePacket.emoteID, this.getId(), flags, emotePacket.emoteTicks);
                    }
                    break;
                }

                if (getProtocol() < AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }

                if (!callPacketReceiveEvent(packet)) break;
                if (!this.spawned) {
                    break;
                }
                EmotePacket120 emotePacket = (EmotePacket120) packet;
                if (emotePacket.runtimeId != this.getLocalEntityId()) {
                    server.getLogger().warning(this.username + " sent EmotePacket with invalid entity id: " + emotePacket.runtimeId + " != " + this.getLocalEntityId());
                    break;
                }
                if (emotePacket.emoteID.length() != 32 + 4) { // 00000000-0000-0000-0000-000000000000
                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "emote");
                    break;
                }
                if ((emotePacket.flags & EmotePacket120.FLAG_SERVER) != 0) {
                    break;
                }
                if (!emoteRequest()) {
                    this.addViolationLevel(8, "emote_req");
                    break;
                }
                if (isNetEaseClient()) {
                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "ce_emote", emotePacket.emoteID);
                    break;
                }
                if (isSpectator()) {
                    break;
                }
                if (emoting) {
                    break;
                }

                int flags = emotePacket.flags | EmotePacket120.FLAG_SERVER;
                if (MUTE_EMOTE_CHAT) {
                    flags |= EmotePacket120.FLAG_MUTE_ANNOUNCEMENT;
                }
                for (Player viewer : this.getViewers().values()) {
                    viewer.playEmote(emotePacket.emoteID, this.getId(), flags);
                }
                break;
            case ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET:
                if (isServerAuthoritativeMovementEnabled() && getProtocol() >= AbstractProtocol.PROTOCOL_120_60.getProtocolStart()) {
                    if (getProtocol() >= AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "input_boat");
                    }
                    break;
                }
                super.handleDataPacket(packet);
                break;
            case ProtocolInfo.ANIMATE_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                    AnimatePacket121130 animatePk = (AnimatePacket121130) packet;

                    if (!validateFloat(animatePk.data)) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_data_nan");
                        break;
                    }

                    if (isServerAuthoritativeMovementEnabled() && (animatePk.action == Action.ROW_LEFT || animatePk.action == Action.ROW_RIGHT)) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_paddle");
                        break;
                    }

                    if (!callPacketReceiveEvent(animatePk.toDefault())) break;
                    if (!this.spawned || !this.isAlive()) {
                        break;
                    }

                    PlayerAnimationEvent animationEvent = new PlayerAnimationEvent(this, animatePk.action);
                    this.server.getPluginManager().callEvent(animationEvent);
                    if (animationEvent.isCancelled()) {
                        break;
                    }

                    AnimatePacket.Action animation = animationEvent.getAnimationType();
                    switch (animation) {
                        case SWING_ARM:
                            this.swingTime = -1;
                            this.swinging = true;

                            if (false && !isBreakingBlock()) {
                                level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE, EntityFullNames.PLAYER, this);
                            }
                            break;
                        case WAKE_UP:
                            break;
                        default:
                            onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim");
                            return;
                    }

                    AnimatePacket animatePacket = new AnimatePacket();
                    animatePacket.eid = this.getId();
                    animatePacket.action = animationEvent.getAnimationType();
//                    animatePacket.data = animatePk.data;
                    animatePacket.swingSource = animatePk.swingSource;
                    Server.broadcastPacket(this.getViewers().values(), animatePacket);
                    break;
                } else if (getProtocol() >= AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
                    AnimatePacket121120 animatePk = (AnimatePacket121120) packet;

                    if (!validateFloat(animatePk.data)) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_data_nan");
                        break;
                    }

                    if (isServerAuthoritativeMovementEnabled() && (animatePk.action == Action.ROW_LEFT || animatePk.action == Action.ROW_RIGHT)) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_paddle");
                        break;
                    }

                    if (!callPacketReceiveEvent(animatePk.toDefault())) break;
                    if (!this.spawned || !this.isAlive()) {
                        break;
                    }

                    PlayerAnimationEvent animationEvent = new PlayerAnimationEvent(this, animatePk.action);
                    this.server.getPluginManager().callEvent(animationEvent);
                    if (animationEvent.isCancelled()) {
                        break;
                    }

                    AnimatePacket.Action animation = animationEvent.getAnimationType();
                    switch (animation) {
                        case SWING_ARM:
                            this.swingTime = -1;
                            this.swinging = true;

                            if (false && !isBreakingBlock()) {
                                level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ATTACK_NODAMAGE, EntityFullNames.PLAYER, this);
                            }
                            break;
                        case ROW_RIGHT:
                        case ROW_LEFT:
                            if (this.riding instanceof EntityBoat boat) {
                                float paddleTime = animatePk.rowingTime;
                                if (!validateFloat(paddleTime)) {
                                    onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_nan");
                                    return;
                                }

                                boat.onPaddle(animation, paddleTime);
                            }
                            return;
                        case WAKE_UP:
                            break;
                        default:
                            onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim");
                            return;
                    }

                    AnimatePacket animatePacket = new AnimatePacket();
                    animatePacket.eid = this.getId();
                    animatePacket.action = animationEvent.getAnimationType();
//                    animatePacket.data = animatePk.data;
                    animatePacket.rowingTime = animatePk.rowingTime;
                    Server.broadcastPacket(this.getViewers().values(), animatePacket);
                    break;
                }

                if (isServerAuthoritativeMovementEnabled() && getProtocol() >= AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
                    AnimatePacket animatePacket = (AnimatePacket) packet;
                    if (animatePacket.action == Action.ROW_LEFT || animatePacket.action == Action.ROW_RIGHT) {
                        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "anim_paddle");
                        break;
                    }
                }
                super.handleDataPacket(packet);
                break;
            case ProtocolInfo.LECTERN_UPDATE_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                LecternUpdatePacket12070 lecternUpdatePacket = (LecternUpdatePacket12070) packet;

                if (!canInteract(temporalVector.setComponents(lecternUpdatePacket.x + 0.5, lecternUpdatePacket.y + 0.5, lecternUpdatePacket.z + 0.5), isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
                    break;
                }

                Block block = level.getBlock(lecternUpdatePacket.x, lecternUpdatePacket.y, lecternUpdatePacket.z, false);
                if (block.getId() != BlockID.LECTERN) {
                    break;
                }
                BlockEntity blockEntity = level.getBlockEntityIfLoaded(block);
                if (!(blockEntity instanceof BlockEntityLectern lectern)) {
                    break;
                }

                if (lectern.getTotalPages() != lecternUpdatePacket.totalPages) {
                    lectern.spawnTo(this);
                    break;
                }

                if (lectern.setPage(lecternUpdatePacket.page)) {
                    ((BlockLectern) block).onPageTurn();

                    lectern.spawnToAll();
                } else {
                    lectern.spawnTo(this);
                }
                break;
            case ProtocolInfo.ACTOR_EVENT_PACKET:
                if (getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
                    if (!callPacketReceiveEvent(packet)) {
                        break;
                    }
                    if (!this.spawned || !this.isAlive()) {
                        break;
                    }
                    EntityEventPacket12620 entityEventPacket = (EntityEventPacket12620) packet;
                    if (entityEventPacket.event != EntityEventPacket.ENCHANT) {
                        this.craftingType = CRAFTING_SMALL;
                    }
                    //this.resetCraftingGridType();

                    switch (entityEventPacket.event) {
                        case EntityEventPacket.EATING_ITEM:
                            if (entityEventPacket.data == 0 || entityEventPacket.eid != this.getId()) {
                                break;
                            }
                            Item held = inventory.getItemInHand();
                            if (!(held instanceof ItemEdible) && !held.is(Item.POTION) && !held.is(Item.MILK_BUCKET) && (!(held instanceof ItemChemicalTickable tickable) || tickable.isActivated())) {
                                break;
                            }

                            EntityEventPacket pk = new EntityEventPacket();
                            pk.eid = getId();
                            pk.event = EntityEventPacket.EATING_ITEM;
                            pk.data = (held.getId() << 16) | held.getDamage();
                            this.dataPacket(pk);
                            Server.broadcastPacket(this.getViewers().values(), pk);
                            break;
                        case EntityEventPacket.ENCHANT:
                            if (entityEventPacket.eid != this.getId()) {
                                break;
                            }
                            if (this.getWindowById(ENCHANT_WINDOW_ID) != null) {
                                break; //附魔现在在 EnchantTransaction 中扣减经验等级
                            }

                            Inventory inventory = this.getWindowById(ANVIL_WINDOW_ID);
                            if (inventory instanceof AnvilInventory anvilInventory) {
                                anvilInventory.setCost(-entityEventPacket.data);
                            } else if (this.getWindowById(ENCHANT_WINDOW_ID) != null) {
                                int levels = entityEventPacket.data; // Sent as negative number of levels lost
                                if (levels < 0) {
                                    this.setExperience(this.getExperience(), this.getExperienceLevel() + levels);
                                }
                                break;
                            }
                            break;
                    }
                    break;
                }

                if (!callPacketReceiveEvent(packet)) {
                    break;
                }
                if (!this.spawned || !this.isAlive()) {
                    break;
                }
                EntityEventPacket116100 entityEventPacket = (EntityEventPacket116100) packet;
                if (entityEventPacket.event != EntityEventPacket.ENCHANT) {
                    this.craftingType = CRAFTING_SMALL;
                }
                //this.resetCraftingGridType();

                switch (entityEventPacket.event) {
                    case EntityEventPacket.EATING_ITEM:
                        if (entityEventPacket.data == 0 || entityEventPacket.eid != this.getId()) {
                            break;
                        }
                        Item held = inventory.getItemInHand();
                        if (!(held instanceof ItemEdible) && !held.is(Item.POTION) && !held.is(Item.MILK_BUCKET) && (!(held instanceof ItemChemicalTickable tickable) || tickable.isActivated())) {
                            break;
                        }

                        EntityEventPacket pk = new EntityEventPacket();
                        pk.eid = getId();
                        pk.event = EntityEventPacket.EATING_ITEM;
                        pk.data = (held.getId() << 16) | held.getDamage();
                        this.dataPacket(pk);
                        Server.broadcastPacket(this.getViewers().values(), pk);
                        break;
                    case EntityEventPacket.ENCHANT:
                        if (entityEventPacket.eid != this.getId()) {
                            break;
                        }
                        if (this.getWindowById(ENCHANT_WINDOW_ID) != null) {
                            break; //附魔现在在 EnchantTransaction 中扣减经验等级
                        }

                        Inventory inventory = this.getWindowById(ANVIL_WINDOW_ID);
                        if (inventory instanceof AnvilInventory anvilInventory) {
                            anvilInventory.setCost(-entityEventPacket.data);
                        } else if (this.getWindowById(ENCHANT_WINDOW_ID) != null) {
                            int levels = entityEventPacket.data; // Sent as negative number of levels lost
                            if (levels < 0) {
                                this.setExperience(this.getExperience(), this.getExperienceLevel() + levels);
                            }
                            break;
                        }
                        break;
                }
                break;
            case ProtocolInfo.CLIENT_CAMERA_AIM_ASSIST_PACKET:
                ClientCameraAimAssistPacket12160 cameraAimAssistInstructionPacket = (ClientCameraAimAssistPacket12160) packet;
                allowAimAssist = cameraAimAssistInstructionPacket.allowAimAssist;
                switch (cameraAimAssistInstructionPacket.action) {
                    case ClientCameraAimAssistPacket12160.ACTION_SET_FROM_CAMERA_PRESET -> {
                        String cameraPresetId = cameraAimAssistInstructionPacket.cameraPresetId;
                        //TODO: setAimAssist(getAimAssistFromCameraPreset(cameraPresetId));
                    }
                    case ClientCameraAimAssistPacket12160.ACTION_CLEAR -> {
                        if (allowAimAssist && !aimAssistPresetId.isEmpty()) {
                            sendAimAssist(CameraAimAssistPacket12050.ACTION_SET, aimAssistMode, aimAssistViewAngleX, aimAssistViewAngleY, aimAssistDistance, aimAssistPresetId);
                        } else {
                            sendAimAssist(CameraAimAssistPacket12050.ACTION_CLEAR, CameraAimAssistPacket12050.MODE_ANGLE, 30, 45, 5.7f, "");
                        }
                    }
                }
                break;
            case ProtocolInfo.BOSS_EVENT_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_120_40.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
                    BossEventPacket12630 bossEventPacket = (BossEventPacket12630) packet;
                    switch (bossEventPacket.type) {
                        case BossEventPacket.TYPE_QUERY:
                            DummyBossBar bossBar = dummyBossBars.get(bossEventPacket.bossEid);
                            if (bossBar != null) {
                                bossBar.reshow();
                            }
                            break;
                    }
                    break;
                }
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
                    BossEventPacket12160 bossEventPacket = (BossEventPacket12160) packet;
                    switch (bossEventPacket.type) {
                        case BossEventPacket.TYPE_QUERY:
                            DummyBossBar bossBar = dummyBossBars.get(bossEventPacket.bossEid);
                            if (bossBar != null) {
                                bossBar.reshow();
                            }
                            break;
                    }
                    break;
                }
                BossEventPacket12040 bossEventPacket = (BossEventPacket12040) packet;
                switch (bossEventPacket.type) {
                    case BossEventPacket.TYPE_QUERY:
                        DummyBossBar bossBar = dummyBossBars.get(bossEventPacket.bossEid);
                        if (bossBar != null) {
                            bossBar.reshow();
                        }
                        break;
                }
                break;
            case ProtocolInfo.INTERACT_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                InteractPacket121130 interactPacket = (InteractPacket121130) packet;
                if (!callPacketReceiveEvent(interactPacket.toDefault())) {
                    break;
                }

                if (interactPacket.action == InteractPacket121130.ACTION_OPEN_INVENTORY
                        && (interactPacket.target == getLocalEntityId() || isRiding() && interactPacket.target == riding.getId() && riding.getNetworkId() != EntityID.CHEST_BOAT && (!riding.getDataFlag(DATA_FLAG_TAMED) || riding.getNetworkId() == EntityID.SKELETON_HORSE))
                        && !this.inventoryOpen && !isSpectator()) {
//					this.openInventory();
                    this.inventory.open(this);
                    this.inventoryOpen = true;
                    break;
                }

                if (!this.spawned || !this.isAlive()) {
                    break;
                }

//                this.craftingType = CRAFTING_SMALL;
                //this.resetCraftingGridType();

                if (interactPacket.target == 0 && interactPacket.action == InteractPacket121130.ACTION_MOUSEOVER) {
                    this.lookAtEntity = null;
                    this.setButtonText(inventory.getItemInHand().getInteractText(this));
                    setDataProperty(new ByteEntityData(DATA_CAN_RIDE_TARGET, false));
                    break;
                }

                Entity targetEntity = interactPacket.target == this.getId() ? this : this.level.getEntity(interactPacket.target);

                if (targetEntity == null || !this.isAlive() || !targetEntity.isAlive()) {
                    break;
                }

                if (targetEntity instanceof EntityItem || targetEntity instanceof EntityArrow || targetEntity instanceof EntityXPOrb) {
                    this.kick(PlayerKickEvent.Reason.INVALID_PVE, "Attempting to interact with an invalid entity", false);
                    log.warn(this.getServer().getLanguage().translate("nukkit.player.invalidEntity", this.getName()));
                    break;
                }

                switch (interactPacket.action) {
                    case InteractPacket121130.ACTION_MOUSEOVER:
                        this.lookAtEntity = targetEntity;

                        if (targetEntity instanceof EntityInteractable interactable) {
                            String text = interactable.getInteractButtonText(this);
                            this.setButtonText(text);
                            setDataProperty(new ByteEntityData(DATA_CAN_RIDE_TARGET, targetEntity != riding && targetEntity instanceof EntityRideable && ("action.interact.mount".equals(text) || text.startsWith("action.interact.ride."))));
                        }

                        this.getServer().getPluginManager().callEvent(new PlayerMouseOverEntityEvent(this, targetEntity));
                        break;
                    case InteractPacket121130.ACTION_VEHICLE_EXIT:
                        if (!(targetEntity instanceof EntityRideable) || this.riding != targetEntity) {
                            break;
                        }

                        ((EntityRideable) riding).dismountEntity(this);
                        break;
                    case InteractPacket121130.ACTION_OPEN_INVENTORY:
                        if (!(targetEntity instanceof InventoryHolder)) {
                            break;
                        }

                        ((InventoryHolder) targetEntity).openInventory(this);
                        break;
                }
                break;
            case ProtocolInfo.BOOK_EDIT_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                BookEditPacket126 bookEditPacket = (BookEditPacket126) packet;
                if (!callPacketReceiveEvent(bookEditPacket.toDefault())) {
                    break;
                }

                if (isSpectator()) {
                    return;
                }

                if (isCreative()) {
                    // handled in InventoryTransactionPacket
                    return;
                }

                Item oldBook = this.inventory.getItem(bookEditPacket.inventorySlot);
                if (oldBook.getId() != Item.WRITABLE_BOOK) {
                    return;
                }

                if (bookEditPacket.text != null && bookEditPacket.text.length() > ItemBookAndQuill.MAX_PAGE_LENGTH) {
                    this.getServer().getLogger().debug(this.getName() + ": BookEditPacket with too long text");
                    return;
                }

                if (bookEditPacket.pageNumber < 0 || bookEditPacket.pageNumber >= ItemBookAndQuill.MAX_PAGES) {
                    this.getServer().getLogger().debug(this.getName() + ": Invalid BookEditPacket page " + bookEditPacket.pageNumber);
                    return;
                }

                Item newBook = oldBook.clone();
                boolean success;
                switch (bookEditPacket.action) {
                    case REPLACE_PAGE:
                        success = ((ItemBookAndQuill) newBook).setPageText(bookEditPacket.pageNumber, bookEditPacket.text);
                        break;
                    case ADD_PAGE:
                        success = ((ItemBookAndQuill) newBook).insertPage(bookEditPacket.pageNumber, bookEditPacket.text);
                        break;
                    case DELETE_PAGE:
                        success = ((ItemBookAndQuill) newBook).deletePage(bookEditPacket.pageNumber);
                        break;
                    case SWAP_PAGES:
                        success = ((ItemBookAndQuill) newBook).swapPages(bookEditPacket.pageNumber, bookEditPacket.secondaryPageNumber);
                        break;
                    case SIGN_BOOK:
                        if (bookEditPacket.title == null || bookEditPacket.author == null || bookEditPacket.xuid == null
                                || bookEditPacket.title.isEmpty() || bookEditPacket.author.isEmpty()
                                || bookEditPacket.title.length() > ItemBookWritten.MAX_TITLE_LENGTHE
                                || bookEditPacket.author.length() > ItemBookWritten.MAX_AUTHOR_LENGTHE
                                || bookEditPacket.xuid.length() > 64) {
                            this.getServer().getLogger().debug(this.getName() + ": Invalid BookEditPacket action SIGN_BOOK: title/author/xuid is too long");
                            return;
                        }
                        newBook = Item.get(Item.WRITTEN_BOOK, 0, 1, oldBook.getCompoundTag());
                        success = ((ItemBookWritten) newBook).signBook(bookEditPacket.title, bookEditPacket.author, bookEditPacket.xuid, ItemBookWritten.GENERATION_ORIGINAL);
                        break;
                    default:
                        return;
                }

                if (success) {
                    PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(this, oldBook, newBook, bookEditPacket.action);
                    this.server.getPluginManager().callEvent(editBookEvent);
                    if (!editBookEvent.isCancelled()) {
                        this.inventory.setItem(bookEditPacket.inventorySlot, editBookEvent.getNewBook(), bookEditPacket.action != BookEditPacket.Action.SWAP_PAGES);
                    }
                }
                break;
            case ProtocolInfo.CLIENT_CACHE_BLOB_STATUS_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }
                if (!callPacketReceiveEvent(packet)) {
                    break;
                }

                if (!isBlobCacheAvailable()) {
                    break;
                }
                ClientCacheBlobStatusPacket12630 pk = (ClientCacheBlobStatusPacket12630) packet;

                ClientCacheMissResponsePacket112 responsePk = new ClientCacheMissResponsePacket112();
                for (long id : pk.missSet) {
                    BlobTrack track = clientCacheTrack.get(id);
                    if (track != null) {
                        responsePk.blobs.put(id, track.getBlob());
                        clientChunkLoadCount += track.refCnt();
                        clientCacheTrack.remove(id);
                    }

                    if (id == teleportChunkBlobHash) {
                        teleportChunkLoaded = true;
                    }
                }
                dataPacket(responsePk);

                for (long id : pk.hitSet) {
                    BlobTrack track = clientCacheTrack.remove(id);
                    if (track != null) {
                        clientChunkLoadCount += track.refCnt();
                    }

                    if (id == teleportChunkBlobHash) {
                        teleportChunkLoaded = true;
                    }
                }

                sendQueuedChunk = true;
                break;
            case ProtocolInfo.PLAYER_TOGGLE_CRAFTER_SLOT_REQUEST_PACKET:
                PlayerToggleCrafterSlotRequestPacket12050 playerToggleCrafterSlotRequestPacket = (PlayerToggleCrafterSlotRequestPacket12050) packet;

                if (!canInteract(temporalVector.setComponents(playerToggleCrafterSlotRequestPacket.x + 0.5, playerToggleCrafterSlotRequestPacket.y + 0.5, playerToggleCrafterSlotRequestPacket.z + 0.5), isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
                    break;
                }

                block = level.getBlock(playerToggleCrafterSlotRequestPacket.x, playerToggleCrafterSlotRequestPacket.y, playerToggleCrafterSlotRequestPacket.z, false);
                if (block.getId() != BlockID.CRAFTER) {
                    break;
                }
                blockEntity = level.getBlockEntityIfLoaded(block);
                if (!(blockEntity instanceof BlockEntityCrafter crafter)) {
                    break;
                }

                int slot = playerToggleCrafterSlotRequestPacket.slot;
                boolean disabled = playerToggleCrafterSlotRequestPacket.disabled;

                if (slot < 0 || slot >= crafter.getSize()
                        || crafter.isSlotDisabled(slot) == disabled
                        || !crafter.setSlotDisabled(slot, disabled)) {
                    crafter.spawnTo(this);
                    break;
                }
                crafter.spawnToAll();
                break;
            case ProtocolInfo.INVENTORY_TRANSACTION_PACKET:
                if (JavaItemUseRouting.usesLegacyInventoryHandler(getProtocol())) {
                    super.handleDataPacket(packet);
                    break;
                }
                InventoryTransactionPacket12630 transactionPacket = (InventoryTransactionPacket12630) packet;
                boolean javaClient = this.isJavaClient();
                boolean explicitItemUseHandAllowed = javaClient && this.supportsExplicitItemUseHand();
                JavaItemUseRouting.Route transactionRoute = JavaItemUseRouting.resolveTransaction(
                        javaClient, explicitItemUseHandAllowed,
                        transactionPacket.transactionType, transactionPacket.transactionData);
                if (transactionRoute == JavaItemUseRouting.Route.REJECT) {
                    if (transactionPacket.transactionType == InventoryTransactionPacket.TYPE_RELEASE_ITEM
                            && JavaItemUseRouting.shouldCancelRejectedRelease(
                            transactionPacket.transactionType, transactionRoute,
                            this.isUsingItem(), this.getUsingItemHand())) {
                        this.setUsingItem(false);
                    }
                    break;
                }
                if (!callPacketReceiveEvent(packet)) break;

                Item item;

                boolean skipped = false;
                List<InventoryAction> actions = new ArrayList<>(transactionPacket.actions.length);
                for (NetworkInventoryAction networkInventoryAction : transactionPacket.actions) {
                    InventoryAction a = networkInventoryAction.createInventoryAction(this);

                    if (a == null) {
//						this.getServer().getLogger().debug("Unmatched inventory action from " + this.getName() + ": " + networkInventoryAction);
//						this.sendAllInventories();
//						break packetswitch;
                        skipped = true;
                        continue;
                    }

                    if (skipped && networkInventoryAction.windowId == ContainerIds.UI) {
                        return;
                    }

                    actions.add(a);
                }

                if (transactionPacket.isCraftingPart) {
                    try {
                        if (this.craftingTransaction == null) {
                            this.craftingTransaction = new CraftingTransaction(this, actions);
                        } else {
                            for (InventoryAction action : actions) {
                                this.craftingTransaction.addAction(action);
                            }
                        }

                        if (this.craftingTransaction.getPrimaryOutput() != null && this.craftingTransaction.canExecute()) {
                            //we get the actions for this in several packets, so we can't execute it until we get the result

                            this.craftingTransaction.execute();
                            this.craftingTransaction = null;
                            break;
                        }

                        if ((craftingType >> 3) == 0 || craftingType == CRAFTING_STONECUTTER) {
                            break;
                        }
                    } catch (Exception e) {
                        this.getServer().getLogger().logException(e);
                        this.craftingTransaction = null;
                        this.getUIInventory().sendContents(this);
                    }
                } else if (transactionPacket.isEnchantingPart) {
                    if (this.enchantTransaction == null) {
                        this.enchantTransaction = new EnchantTransaction(this, actions);
                    } else {
                        for (InventoryAction action : actions) {
                            this.enchantTransaction.addAction(action);
                        }
                    }
                    if (this.enchantTransaction.canExecute()) {
                        this.enchantTransaction.execute();
                        this.enchantTransaction = null;
                    }
                    return;
                } else if (transactionPacket.isRepairItemPart) {
                    if (this.repairItemTransaction == null) {
                        this.repairItemTransaction = new RepairItemTransaction(this, actions);
                    } else {
                        for (InventoryAction action : actions) {
                            this.repairItemTransaction.addAction(action);
                        }
                    }

                    if (this.repairItemTransaction.canExecute()) {
                        this.repairItemTransaction.execute();
                        this.repairItemTransaction = null;
                        break;
                    }

                    if ((this.craftingType >> 3) != 2) {
                        break;
                    }
                }

                if (this.craftingTransaction != null) {
                    if (CraftingTransaction.checkForCraftingPart(actions)) {
                        for (InventoryAction action : actions) {
                            craftingTransaction.addAction(action);
                        }
                        return;
                    }
//					else {
//						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete crafting transaction from " + this.getName() + ", refusing to execute crafting");
//						this.removeAllWindows(false);
//						this.sendAllInventories();
//						this.craftingTransaction = null;
//					}
                } else if (this.enchantTransaction != null) {
                    if (enchantTransaction.checkForEnchantPart(actions)) {
                        for (InventoryAction action : actions) {
                            enchantTransaction.addAction(action);
                        }
                        return;
                    } else {
                        this.server.getLogger().debug("Got unexpected normal inventory action with incomplete enchanting transaction from " + this.getName() + ", refusing to execute enchant " + transactionPacket);
                        this.removeAllWindows(false);
                        this.sendAllInventories();
                        this.enchantTransaction = null;
                    }
                } else if (this.repairItemTransaction != null) {
                    if (RepairItemTransaction.checkForRepairItemPart(actions)) {
                        for (InventoryAction action : actions) {
                            this.repairItemTransaction.addAction(action);
                        }
                        return;
                    }/* else {
						this.server.getLogger().debug("Got unexpected normal inventory action with incomplete repair item transaction from " + this.getName() + ", refusing to execute repair item " + transactionPacket.toString());
						this.removeAllWindows(false);
						this.sendAllInventories();
						this.repairItemTransaction = null;
					}*/
                    // smithing
                    for (int i = 0; i < actions.size(); i++) {
                        InventoryAction action = actions.get(i);
                        if (!(action instanceof TakeResultAction)) {
                            continue;
                        }
                        List<InventoryAction> sequence = new ArrayList<>(actions.size());
                        sequence.add(action);
                        actions.remove(i);
                        sequence.addAll(actions);
                        actions = sequence;
                        break;
                    }
                }

                switch (transactionPacket.transactionType) {
                    case InventoryTransactionPacket.TYPE_NORMAL:
                        InventoryTransaction transaction = new InventoryTransaction(this, actions);

                        if (!transaction.execute()) {
                            this.server.getLogger().debug("Failed to execute inventory transaction from " + this.getName() + " with actions: " + Arrays.toString(transactionPacket.actions));
                            break packetswitch; //oops!
                        }

                        break packetswitch;
                    case InventoryTransactionPacket.TYPE_MISMATCH:
                        if (transactionPacket.actions.length > 0) {
                            this.server.getLogger().debug("Expected 0 actions for mismatch, got " + transactionPacket.actions.length + ", " + Arrays.toString(transactionPacket.actions));
                        }
                        this.sendAllInventories();

                        break packetswitch;
                    case InventoryTransactionPacket.TYPE_USE_ITEM: {
                        UseItemData useItemData = (UseItemData) transactionPacket.transactionData;

                        Vector3f clickPos = useItemData.clickPos;
                        BlockVector3 blockVector = useItemData.blockPos;
                        BlockFace face = useItemData.face;
                        int type = useItemData.actionType;
                        ItemUseHand interactionHand = transactionRoute.hand();
                        ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

                        try {
                            if (interactionHand == ItemUseHand.OFF_HAND && !JavaItemUseRouting.supportsUseItemAction(type)) {
                                this.inventory.sendHeldItem(this);
                                break packetswitch;
                            }
                            if (face == null && type != InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR) {
                                break packetswitch;
                            }

                            switch (type) {
                                case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK:
                                    // Remove if client bug is ever fixed
                                    boolean spamBug = lastRightClickData != null && System.currentTimeMillis() - lastRightClickTime < 100 &&
                                            lastRightClickData.face == face &&
                                            lastRightClickData.playerPos.distanceSquared(useItemData.playerPos) < Mth.EPSILON &&
                                            lastRightClickData.blockPos.equalsVec(blockVector) &&
                                            lastRightClickData.clickPos.distanceSquared(clickPos) < Mth.EPSILON && // signature spam bug has 0 distance, but allow some error
                                            Block.equals(lastRightClickData.block, useItemData.block) &&
                                            lastRightClickData.clientInteractPrediction == useItemData.clientInteractPrediction &&
                                            lastRightClickData.clientCooldownState == useItemData.clientCooldownState &&
    //                                        lastRightClickData.itemInHand.equals(useItemData.itemInHand) &&
                                            lastRightClickData.hotbarSlot == useItemData.hotbarSlot;
                                    if (spamBug /*&& !(useItemData.itemInHand instanceof ItemBlock)*/) {
                                        return;
                                    }
                                    lastRightClickData = useItemData;
                                    lastRightClickTime = System.currentTimeMillis();

                                    Item i = inventory.getItemInHand();
                                    if (!i.is(Item.BRUSH)) {
                                        this.setDataFlag(DATA_FLAG_ACTION, false);
                                    }

                                    // 从useItemData中设置玩家坐标，用于最精准的碰撞箱判断
                                    this.newPosition = useItemData.playerPos.subtract(0, this.getBaseOffset(), 0);
                                    boolean clientPredictedFailure = false;

                                    if (this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)) {
                                        if (this.isCreative()) {
                                            Vector3 blockPos;
                                            Block clientBlock = useItemData.block;
                                            if (clientBlock != null) {
                                                clientBlock.position(blockVector, level);
                                                blockPos = clientBlock;
                                            } else {
                                                blockPos = blockVector.asVector3();
                                            }
                                            Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
                                            clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
                                            if (this.level.useItemOn(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, this, clientPrediction) != null) {
                                                break packetswitch;
                                            }
                                        } else if (i.equals(useItemData.itemInHand)) {
                                            Item oldItem = i.clone();
                                            Vector3 blockPos;
                                            Block clientBlock = useItemData.block;
                                            if (clientBlock != null) {
                                                clientBlock.position(blockVector, level);
                                                blockPos = clientBlock;
                                            } else {
                                                blockPos = blockVector.asVector3();
                                            }
                                            Boolean clientPrediction = AbstractProtocol.PROTOCOL_121_20.isOlderThanOrEqual(protocol) ? useItemData.clientInteractPrediction : null;
                                            clientPrediction = normalizeUseItemClientPrediction(clientPrediction);
                                            clientPredictedFailure = clientPrediction != null && !clientPrediction;
                                            if ((i = this.level.useItemOn(blockPos, i, face, clickPos.x, clickPos.y, clickPos.z, this, clientPrediction)) != null) {
                                                if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
                                                    inventory.setItemInHand(i);
                                                    inventory.sendHeldItem(this.getViewers().values());
                                                }
                                                break packetswitch;
                                            }
                                        }
                                    }

                                    // 解决卡物品栏问题（只发送物品正确的物品栏）
                                    if (inventory.getItemInHand().getId() == useItemData.itemInHand.getId() && inventory.getItemInHand().getCount() != useItemData.itemInHand.getCount()) {
                                        inventory.sendHeldItem(this);
                                    }

                                    if (clientPredictedFailure) {
                                        break packetswitch;
                                    }

                                    if (blockVector.distanceSquared(this) > 10000) {
                                        break packetswitch;
                                    }

                                    Block target = level.getBlock(blockVector.asVector3());
                                    Block block0 = target.getSide(face);

                                    this.level.sendBlocks(new Player[]{this}, new Block[]{target, block0}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                                    this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target), level.getExtraBlock(block0)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                                    if (target instanceof BlockDoor) {
                                        BlockDoor door = (BlockDoor) target;
                                        Block part;

                                        if (door.isTop()) {
                                            part = target.down();

                                            if (part.getId() == target.getId()) {
                                                target = part;

                                                this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                                            }
                                        }
                                    }
                                    break packetswitch;
                                case InventoryTransactionPacket.USE_ITEM_ACTION_BREAK_BLOCK:
                                    if (!this.spawned || !this.isAlive()) {
                                        break packetswitch;
                                    }

                                    this.resetCraftingGridType();

                                    i = this.getInventory().getItemInHand();

                                    Item oldItem = i.clone();

                                    if (isBreakingBlock(blockVector)
                                            && this.canInteract(blockVector.add(0.5, 0.5, 0.5), this.isCreative() ? MAX_REACH_DISTANCE_CREATIVE : MAX_REACH_DISTANCE_SURVIVAL)
                                            && (i = this.level.useBreakOn(blockVector.asVector3(), face, i, this, true)) != null) {
                                        this.breakingBlock = null;
                                        this.breakingBlockFace = null;
                                        if (this.isSurvival()) {
                                            this.getFoodData().updateFoodExpLevel(0.005f);
                                            if (!i.equals(oldItem) || i.getCount() != oldItem.getCount()) {
                                                inventory.setItemInHand(i);
                                                inventory.sendHeldItem(this.getViewers().values());
                                            }
                                        }
                                        break packetswitch;
                                    }

                                    inventory.sendContents(this);
                                    inventory.sendHeldItem(this);

                                    if (blockVector.distanceSquared(this) < 10000) {
                                        target = this.level.getBlock(blockVector.asVector3());
                                        this.level.sendBlocks(new Player[]{this}, new Block[]{target}, UpdateBlockPacket.FLAG_ALL_PRIORITY);
                                        this.level.sendBlocks(new Player[]{this}, new Block[]{level.getExtraBlock(target)}, UpdateBlockPacket.FLAG_ALL_PRIORITY, 1);

                                        blockEntity = this.level.getBlockEntityIfLoaded(blockVector);
                                        if (blockEntity instanceof BlockEntitySpawnable) {
                                            ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                                        }
                                    }

                                    break packetswitch;
                                case InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_AIR:
                                    Vector3 directionVector = this.getDirectionVector();

                                    if (this.isCreative()) {
                                        item = this.inventory.getItemInHand();
                                    } else if (!this.inventory.getItemInHand().equals(useItemData.itemInHand)) {
    //									server.getLogger().debug(getName() + " held desync\nC: " + useItemData.itemInHand + "\nS: " + inventory.getItemInHand());
    //									this.inventory.sendHeldItem(this); // 这里不再强制同步, 尝试解决使用物品后快速切换物品的回弹问题
                                        break packetswitch;
                                    } else {
                                        item = this.inventory.getItemInHand();
                                    }

                                    this.rebindStartedJavaItemUseHand(explicitItemUseHandAllowed, interactionHand);
                                    PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, directionVector, face, PlayerInteractEvent.Action.RIGHT_CLICK_AIR);
                                    if (isSpectator()) {
                                        interactEvent.setCancelled();
                                    }
                                    this.server.getPluginManager().callEvent(interactEvent);
                                    if (interactEvent.isCancelled()) {
                                        this.inventory.sendHeldItem(this);
                                        break packetswitch;
                                    }

                                    boolean explicitShieldUse = JavaItemUseRouting.supportsExplicitShieldUse(explicitItemUseHandAllowed, item.getId());
                                    boolean itemAccepted = item.onClickAir(this, directionVector) || explicitShieldUse;
                                    if (itemAccepted) {
                                        if (this.isSurvivalLike()) {
                                            this.inventory.setItemInHand(item);
                                        }

                                        if (!this.isUsingItem()) {
                                            this.setUsingItem(item.canRelease() || explicitShieldUse);
                                            break packetswitch;
                                        }

                                        // Used item
                                        //int ticksUsed = this.server.getTick() - this.startAction;
                                        int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

                                        this.setUsingItem(false);

                                        if (!item.onUse(this, ticksUsed)) {
                                            this.inventory.sendContents(this);
                                        }

                                        if ((item.canRelease() || explicitShieldUse) && !item.isNull()) {
                                            this.setUsingItem(true);
                                        }
                                    }

                                    break packetswitch;
                                    default:
                                        //unknown
                                        break;
                            }
                        } finally {
                            this.setItemInteractionHand(previousHand);
                        }
                        break;
                    }
                    case InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY: {
                        UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) transactionPacket.transactionData;
                        int type = useItemOnEntityData.actionType;
                        ItemUseHand interactionHand = transactionRoute.hand();
                        ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

                        try {
                            if (interactionHand == ItemUseHand.OFF_HAND && !JavaItemUseRouting.supportsEntityAction(type)) {
                                this.inventory.sendHeldItem(this);
                                break packetswitch;
                            }
                            Entity target = this.level.getEntity(useItemOnEntityData.entityRuntimeId);
                            if (target == null) {
                                item = this.inventory.getItemInHand();
                                PlayerInteractEvent interactEvent = new PlayerInteractEvent(this, item, this.getDirectionVector(), BlockFace.UP, PlayerInteractEvent.Action.CLICK_UNKNOWN_ENTITY).setUnkownEntityId(useItemOnEntityData.entityRuntimeId);
                                if (isSpectator()) {
                                    interactEvent.setCancelled();
                                }
                                this.server.getPluginManager().callEvent(interactEvent);
                                return;
                            }

                            if (!useItemOnEntityData.itemInHand.equalsExact(this.inventory.getItemInHand())) {
                                this.inventory.sendHeldItem(this);
                            }

                            item = this.inventory.getItemInHand();

                            switch (type) {
                                case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT:
                                    if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : MAX_REACH_DISTANCE_SURVIVAL_ENTITY_INTERACTION)) {
                                        break;
                                    }

                                    PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(this, target, item, useItemOnEntityData.clickPos);
                                    if (this.isSpectator()) playerInteractEntityEvent.setCancelled();
                                    getServer().getPluginManager().callEvent(playerInteractEntityEvent);

                                    if (playerInteractEntityEvent.isCancelled()) {
                                        break;
                                    }
                                    if (target.onInteract(this, item, useItemOnEntityData.clickPos) && this.isSurvival()) {
                                        if (item.isTool()) {
                                            if (item.useOn(target) && item.getDamage() > item.getMaxDurability()) {
                                                item = Items.air();
                                                level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
                                            }
                                        } else {
                                            if (item.count > 1) {
                                                item.count--;
                                            } else {
                                                item = Items.air();
                                            }
                                        }

                                        this.inventory.setItemInHand(item);
                                    }
                                    break;
                                case InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK:
    //								if (++currentTickAttackPacketCount >= 10) {
    //									violation += 25; // 近期TPS不稳定容易误判, 先禁用
    //									return;
    //								}
                                    if (++currentTickAttackPacketCount >= 2) {
                                        return;
                                    }

                                    // 命中边缘时有极小概率没有看着实体 (InteractPacket::InteractUpdate-4)
    /*
								if (this.lookAtEntity != target) {
									this.violation += 6;
									break;
								}
    */
                                    if (!this.canInteract(target, target.getBoundingBox(), isCreative() ? MAX_REACH_DISTANCE_CREATIVE_ENTITY_INTERACTION : MAX_REACH_DISTANCE_SURVIVAL_ENTITY_INTERACTION)) {
                                        break;
                                    }

                                    ItemAttackDamageEvent ev = new ItemAttackDamageEvent(item);
                                    this.server.getPluginManager().callEvent(ev);
                                    float itemDamage = ev.getAttackDamage();

                                    Enchantment[] enchantments = item.getId() != Item.ENCHANTED_BOOK ? item.getEnchantments() : Enchantment.EMPTY;

                                    float damageBonus = 0;
                                    for (Enchantment enchantment : enchantments) {
                                        damageBonus += enchantment.getDamageBonus(this, target);
                                    }
                                    itemDamage += Mth.floor(damageBonus);

                                    Map<EntityDamageEvent.DamageModifier, Float> damage = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
                                    damage.put(EntityDamageEvent.DamageModifier.BASE, itemDamage);

                                    KnockbackProfile targetProfile = this.getKnockbackProfile();
                                    float knockBackH = targetProfile.getBaseH();
                                    float knockBackV = targetProfile.getBaseV();
                                    int knockBackEnchantment = !item.is(Item.ENCHANTED_BOOK) ? item.getEnchantmentLevel(Enchantment.KNOCKBACK) : 0;

                                    if (target instanceof Player) {
                                        if ((((Player) target).getGamemode() & 0x01) > 0) {
                                            break;
                                        } else if (!this.server.getConfiguration().isPvp() || this.level.getDifficulty() == 0 || !level.getGameRules().getBoolean(GameRule.PVP)) {
                                            break;
                                        }
                                    }

                                    EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(this, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, knockBackH, knockBackV, enchantments);
                                    entityDamageByEntityEvent.getKnockbackProfile().setEnchantLevel(knockBackEnchantment);
                                    if (this.isSpectator()) entityDamageByEntityEvent.setCancelled();
                                    if (!target.attack(entityDamageByEntityEvent)) {
                                        if (item.isTool() && this.isSurvival()) {
                                            this.inventory.sendContents(this);
                                        }
                                        break;
                                    }

                                    for (Enchantment enchantment : enchantments) {
                                        enchantment.doPostAttack(item, this, target, null);
                                    }

                                    if (item.isTool() && this.isSurvival()) {
                                        if (item.useOn(target)) {
                                            if (item.getDamage() > item.getMaxDurability()) {
                                                this.inventory.setItemInHand(Items.air());
                                                level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_BREAK);
                                            } else {
                                                this.inventory.setItemInHand(item);
                                            }
                                        } else {
                                            this.inventory.setItemInHand(item);
                                        }
                                    }
                                    return;
                                    default:
                                        break; //unknown
                            }
                        } finally {
                            this.setItemInteractionHand(previousHand);
                        }

                        break;
                    }
                    case InventoryTransactionPacket.TYPE_RELEASE_ITEM: {
                        if (!callPacketReceiveEvent(packet)) break;
                        if (this.isSpectator()) {
                            this.sendAllInventories();
                            break packetswitch;
                        }
                        ReleaseItemData releaseItemData = (ReleaseItemData) transactionPacket.transactionData;
                        ItemUseHand interactionHand = transactionRoute.hand();
                        if (explicitItemUseHandAllowed
                                && this.isUsingItem()
                                && interactionHand != this.getUsingItemHand()) {
                            ItemUseHand previousHand = this.setItemInteractionHand(this.getUsingItemHand());
                            try {
                                this.inventory.sendHeldItem(this);
                            } finally {
                                this.setItemInteractionHand(previousHand);
                                this.setUsingItem(false);
                            }
                            break packetswitch;
                        }
                        ItemUseHand previousHand = this.setItemInteractionHand(interactionHand);

                        try {
                            try {
                                int type = releaseItemData.actionType;
                                switch (type) {
                                    case InventoryTransactionPacket.RELEASE_ITEM_ACTION_RELEASE:
                                        if (this.isUsingItem()) {
                                            item = this.inventory.getItemInHand();
                                            if (this.isUsingOffhandItem() && !this.isUsingSameItem(item)) {
                                                this.inventory.sendHeldItem(this);
                                                return;
                                            }

                                            //int ticksUsed = this.server.getTick() - this.startAction;
                                            int ticksUsed = (int) (System.currentTimeMillis() - this.startActionTimestamp) / 50;

                                            if (!item.onRelease(this, ticksUsed, releaseItemData.headRot)) {
                                                this.inventory.sendContents(this);
                                            }
                                        } else {
                                            this.inventory.sendContents(this);
                                        }
                                        return;
                                    case InventoryTransactionPacket.RELEASE_ITEM_ACTION_CONSUME:
                                        this.getServer().getLogger().debug("Unexpected release item action consume from " + this.getName());
                                        return;
                                    default:
                                        break;
                                }
                            } finally {
                                this.setUsingItem(false);
                            }
                        } finally {
                            this.setItemInteractionHand(previousHand);
                        }
                        break;
                    }
                    default:
                        this.inventory.sendContents(this);
                        break;
                }
                break;
            default:
                super.handleDataPacket(packet);
                break;
        }
    }

    public NPCDialoguePlayerHandler getNpcDialoguePlayerHandler() {
        if (npcDialoguePlayerHandler == null) {
            npcDialoguePlayerHandler = new NPCDialoguePlayerHandler(this);
        }
        return npcDialoguePlayerHandler;
    }

	@Override
	public int addWindow(Inventory inventory, Integer forceId, boolean isPermanent, boolean alwaysOpen) {
		Integer index = this.windows.get(inventory);
		if (index != null) {
			return index;
		}
		int cnt;
		if (forceId == null) {
			this.windowCnt = cnt = Math.max(FIRST_AVAILABLE_WINDOW_ID, ++this.windowCnt % 99);
		} else {
			cnt = forceId;
		}
		this.windows.forcePut(inventory, cnt);

		if (isPermanent) {
			this.permanentWindows.add(cnt);
		}

        if (inventoryOpen) {
            log.debug("{} opened player inventory screen, ignore new container screen request: {}", getName(), cnt);
        }

		if (this.spawned && !inventoryOpen && inventory.open(this)) {
			return cnt;
		} else if (!alwaysOpen) {
            if (!this.permanentWindows.contains(this.getWindowId(inventory)))
                this.windows.remove(inventory);

			return -1;
		} else {
			inventory.getViewers().add(this);
		}

		return cnt;
	}

    @Override
    public void removeWindow(Inventory inventory) {
        this.removeWindow(inventory, false);
    }

    protected void removeWindow(Inventory inventory, boolean isResponse) {
        inventory.close(this);
        if (isResponse && !this.permanentWindows.contains(this.getWindowId(inventory)))
            this.windows.remove(inventory);
    }

    @Override
    protected DataPacket generateResourcePackInfoPacket() {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            ResourcePacksInfoPacket12190 resourcePacket = new ResourcePacksInfoPacket12190();
            resourcePacket.resourcePackEntries = resourcePacks.values().toArray(new ResourcePack[0]);
            if (isNetEaseClient()) {
                resourcePacket.resourcePackEntries = ArrayUtils.addAll(resourcePacket.resourcePackEntries, behaviourPacks.values().toArray(new ResourcePack[0]));
            }
            resourcePacket.mustAccept = forceResources;
            return resourcePacket;
        }
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            ResourcePacksInfoPacket12150 resourcePacket = new ResourcePacksInfoPacket12150();
            resourcePacket.resourcePackEntries = resourcePacks.values().toArray(new ResourcePack[0]);
            if (isNetEaseClient()) {
                resourcePacket.resourcePackEntries = ArrayUtils.addAll(resourcePacket.resourcePackEntries, behaviourPacks.values().toArray(new ResourcePack[0]));
            }
            resourcePacket.mustAccept = forceResources;
            return resourcePacket;
        }
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            ResourcePacksInfoPacket12140 resourcePacket = new ResourcePacksInfoPacket12140();
            resourcePacket.resourcePackEntries = resourcePacks.values().toArray(new ResourcePack[0]);
            if (isNetEaseClient()) {
                resourcePacket.resourcePackEntries = ArrayUtils.addAll(resourcePacket.resourcePackEntries, behaviourPacks.values().toArray(new ResourcePack[0]));
            }
            resourcePacket.mustAccept = forceResources;
            return resourcePacket;
        }
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_30.getProtocolStart()) {
            ResourcePacksInfoPacket12130 resourcePacket = new ResourcePacksInfoPacket12130();
            resourcePacket.resourcePackEntries = resourcePacks.values().toArray(new ResourcePack[0]);
            if (isNetEaseClient()) {
                resourcePacket.resourcePackEntries = ArrayUtils.addAll(resourcePacket.resourcePackEntries, behaviourPacks.values().toArray(new ResourcePack[0]));
            }
            List<ResourcePacksInfoPacket12130.CDNEntry> cdn = new ArrayList<>();
            for (ResourcePack pack : resourcePacket.resourcePackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12130.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            resourcePacket.cdnEntries = cdn.toArray(new ResourcePacksInfoPacket12130.CDNEntry[0]);
            resourcePacket.mustAccept = forceResources;
            return resourcePacket;
        }
        if (this.protocol >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            ResourcePacksInfoPacket12120 resourcePacket = new ResourcePacksInfoPacket12120();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
            List<ResourcePacksInfoPacket12120.CDNEntry> cdn = new ArrayList<>();
            for (ResourcePack pack : resourcePacket.resourcePackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12120.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            for (ResourcePack pack : resourcePacket.behaviourPackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12120.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            resourcePacket.cdnEntries = cdn.toArray(new ResourcePacksInfoPacket12120.CDNEntry[0]);
            return resourcePacket;
        } else if (this.protocol >= AbstractProtocol.PROTOCOL_120_70.getProtocolStart()) {
            ResourcePacksInfoPacket12070 resourcePacket = new ResourcePacksInfoPacket12070();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
            List<ResourcePacksInfoPacket12070.CDNEntry> cdn = new ArrayList<>();
            for (ResourcePack pack : resourcePacket.resourcePackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12070.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            for (ResourcePack pack : resourcePacket.behaviourPackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12070.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            resourcePacket.cdnEntries = cdn.toArray(new ResourcePacksInfoPacket12070.CDNEntry[0]);
            return resourcePacket;
        } else if (this.protocol >= AbstractProtocol.PROTOCOL_120_30.getProtocolStart()) {
            ResourcePacksInfoPacket12030 resourcePacket = new ResourcePacksInfoPacket12030();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
            List<ResourcePacksInfoPacket12030.CDNEntry> cdn = new ArrayList<>();
            for (ResourcePack pack : resourcePacket.resourcePackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12030.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            for (ResourcePack pack : resourcePacket.behaviourPackEntries) {
                String url = pack.getCdnUrl();
                if (!url.isEmpty()) {
                    cdn.add(new ResourcePacksInfoPacket12030.CDNEntry(pack.getPackId() + "_" + pack.getPackVersion(), url));
                }
            }
            resourcePacket.cdnEntries = cdn.toArray(new ResourcePacksInfoPacket12030.CDNEntry[0]);
            return resourcePacket;
        } else if (this.protocol >= AbstractProtocol.PROTOCOL_117_10.getProtocolStart()) {
            ResourcePacksInfoPacket11710 resourcePacket = new ResourcePacksInfoPacket11710();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
            return resourcePacket;
        } else if (this.protocol >= AbstractProtocol.PROTOCOL_116_200.getProtocolStart()) {
            ResourcePacksInfoPacket116200 resourcePacket = new ResourcePacksInfoPacket116200();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
            return resourcePacket;
        }
        return super.generateResourcePackInfoPacket();
    }

//    @Override
//    protected void initClientBlobCache() {
//        if (!this.isNetEaseClient()
//                /*&& this.protocol < AbstractProtocol.PROTOCOL_118.getProtocolStart()*/
//        ) {
//            super.initClientBlobCache();
//        }
//    }

    @Override
    public void playAnimation(String animation, Entity entity) {
        this.playAnimation(animation, "default", "", "__runtime_controller", 0, entity.getId());
    }

    @Override
    public void playAnimation(String animation, long entityRuntimeId) {
        this.playAnimation(animation, "default", "", "__runtime_controller", 0, entityRuntimeId);
    }

    @Override
    public void playAnimation(String animation, String nextState, String stopExpression, String controller, float blendOutTime, Entity... entities) {
        long[] entityRuntimeIds = new long[entities.length];
        for (int i = 0; i < entities.length; i++) {
            entityRuntimeIds[i] = entities[i].getId();
        }
        this.playAnimation(animation, nextState, stopExpression, controller, blendOutTime, entityRuntimeIds);
    }

    @Override
    public void playAnimation(String animation, String nextState, String stopExpression, String controller, float blendOutTime, long... entityRuntimeIds) {
        if (this.protocol >= AbstractProtocol.PROTOCOL_117_30.getProtocolStart()) {
            AnimateEntityPacket11730 pk = new AnimateEntityPacket11730();
            pk.animation = animation;
            pk.nextState = nextState;
            pk.stopExpression = stopExpression;
            pk.controller = controller;
            pk.blendOutTime = blendOutTime;
            pk.entityRuntimeIds = entityRuntimeIds.clone();
            this.dataPacket(pk);
        } else {
            AnimateEntityPacket116100 pk = new AnimateEntityPacket116100();
            pk.animation = animation;
            pk.nextState = nextState;
            pk.stopExpression = stopExpression;
            pk.controller = controller;
            pk.blendOutTime = blendOutTime;
            pk.entityRuntimeIds = entityRuntimeIds.clone();
            this.dataPacket(pk);
        }
    }

    @Override
    public void updateSubChunkBlocks(int subChunkBlockX, int subChunkBlockY, int subChunkBlockZ, BlockChangeEntry[] layer0, BlockChangeEntry[] layer1) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
            return;
        }
        AbstractProtocol protocol = getAbstractProtocol();
        boolean netease = isNetEaseClient();

        UpdateSubChunkBlocksPacket118 packet = new UpdateSubChunkBlocksPacket118();
        packet.subChunkBlockX = subChunkBlockX;
        packet.subChunkBlockY = subChunkBlockY;
        packet.subChunkBlockZ = subChunkBlockZ;
        packet.layer0 = UpdateSubChunkBlocksPacket118.convertBlocks(layer0, protocol, netease);
        packet.layer1 = UpdateSubChunkBlocksPacket118.convertBlocks(layer1, protocol, netease);
        dataPacket(packet);
    }

    @Override
    public boolean isSubChunkRequestAvailable() {
        return USE_SUB_CHUNK_REQUEST && this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart() /*&& (this.isBlobCacheAvailable() || !this.isNetEaseClient())*/;
    }

    @Override
    public void sendChunk(int dimension, int x, int z, int subChunkCount, ChunkCachedData cachedData, DataPacket packet) {
        if (dimension == transferDimension) {
            return;
        }
        if (protocol < AbstractProtocol.PROTOCOL_120_60.getProtocolStart() && !this.isSubChunkRequestAvailable()) {
            super.sendChunk(dimension, x, z, subChunkCount, cachedData, packet);
            return;
        }
        if (!this.connected) {
            return;
        }
        this.noticeChunkPublisherUpdate();
        long chunkHash = Level.chunkHash(x, z);
        this.usedChunks.put(chunkHash, true);
        this.chunkLoadCount++;
        boolean centerChunk = !this.isNeedLevelChangeLoadScreen() && CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;

        if (!this.isSubChunkRequestAvailable()) {
            if (this.isBlobCacheAvailable() && !this.isBlobCacheDisabled()) {
                ChunkBlobCache blobCache = cachedData.getBlobCache();
                long[] blobIds;
                Long2ObjectMap<byte[]> blobs;
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                    blobIds = blobCache.getBlobIds();
                    blobs = blobCache.getBlobs();
                } else {
                    blobIds = blobCache.getBlobIdsLegacy();
                    blobs = blobCache.getBlobsLegacy();
                }

                ObjectIterator<Long2ObjectMap.Entry<byte[]>> iter = Long2ObjectMaps.fastIterator(blobs);
                while (iter.hasNext()) {
                    Long2ObjectMap.Entry<? extends byte[]> entry = iter.next();
                    long hash = entry.getLongKey();
                    clientCacheTrack.put(hash, new BlobTrack(hash, entry.getValue()));
                }

                LevelChunkPacket pk = createLevelChunkPacket();
                pk.chunkX = x;
                pk.chunkZ = z;
                pk.dimension = dimension;
                pk.subChunkCount = subChunkCount;
                pk.cacheEnabled = true;
                pk.blobIds = blobIds;
                pk.data = blobCache.getFullChunkPayload();
                this.dataPacket(pk);

                this.sendQueuedChunk = false;

                if (chunkHash == this.teleportChunkIndex) {
                    if (blobIds.length > 1) {
                        this.teleportChunkBlobHash = blobIds[0];
                        //this.teleportChunkLoaded = false;
                    } else {
                        this.teleportChunkLoaded = true;
                    }
                }
            } else {
                LevelChunkPacket pk = createLevelChunkPacket();
                pk.chunkX = x;
                pk.chunkZ = z;
                pk.dimension = dimension;
                pk.subChunkCount = subChunkCount;
                pk.data = ((LevelChunkPacket12060) packet).data;
                this.dataPacket(pk);
            }
        } else if (this.isBlobCacheAvailable() && this.isSubModeLevelChunkBlobCacheEnabled() && !centerChunk) {
            ChunkBlobCache blobCache = cachedData.getBlobCache();
            long[] ids;
            Long2ObjectMap<byte[]> blobs;
            if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                ids = blobCache.getBlobIds();
                blobs = blobCache.getBlobs();
            } else {
                ids = blobCache.getBlobIdsLegacy();
                blobs = blobCache.getBlobsLegacy();
            }

            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, blobs.get(hash)));

            LevelChunkPacket pk = createLevelChunkPacket();
            pk.chunkX = x;
            pk.chunkZ = z;
            pk.dimension = dimension;
            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            pk.subChunkRequestLimit = subChunkCount;
            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubRequestModeFullChunkPayload();
            this.dataPacket(pk);
        } else if (protocol >= AbstractProtocol.PROTOCOL_120_60.getProtocolStart()) {
            LevelChunkPacket pk = createLevelChunkPacket();
            pk.chunkX = x;
            pk.chunkZ = z;
            pk.dimension = dimension;
            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            pk.subChunkRequestLimit = subChunkCount;
            pk.data = ((LevelChunkPacket12060) packet).data;
            this.dataPacket(pk);
        } else {
            this.dataPacket(packet);
        }

        for (BlockEntity blockEntity : this.level.getChunkBlockEntities(x, z).values()) {
            if (!(blockEntity instanceof BlockEntitySpawnable)) {
                continue;
            }
            ((BlockEntitySpawnable) blockEntity).spawnTo(this);
        }

        //TODO: move to sub chunk response?
        if (this.spawned) {
            for (Entity entity : this.level.getChunkEntities(x, z).values()) {
                if (this != entity && !entity.closed && entity.isAlive() && entity.isWithinEntityViewDistance(this)) {
                    entity.spawnTo(this);
                }
            }
        }
    }

    @Override
    public void sendChunk(int dimension, int x, int z, int subChunkCount, ChunkCachedData cachedData, byte[] payload, byte[] subModePayload) {
        if (dimension == transferDimension) {
            return;
        }
        if (protocol < AbstractProtocol.PROTOCOL_120_60.getProtocolStart() && (!this.isSubChunkRequestAvailable() || this.getChunkX() == x && this.getChunkZ() == z)) {
            super.sendChunk(dimension, x, z, subChunkCount, cachedData, payload, subModePayload);
            return;
        }
        if (!this.connected) {
            return;
        }
        this.noticeChunkPublisherUpdate();
        long chunkHash = Level.chunkHash(x, z);
        this.usedChunks.put(chunkHash, true);
        this.chunkLoadCount++;
        boolean centerChunk;

        LevelChunkPacket pk = createLevelChunkPacket();
        pk.chunkX = x;
        pk.chunkZ = z;
        pk.dimension = dimension;
        if (!this.isSubChunkRequestAvailable()) {
            centerChunk = this.getChunkX() == x && this.getChunkZ() == z;

            if (this.isBlobCacheAvailable() && (!centerChunk || !CENTER_CHUNK_WITHOUT_CACHE) && !this.isBlobCacheDisabled()) {
                ChunkBlobCache blobCache = cachedData.getBlobCache();
                long[] blobIds;
                Long2ObjectMap<byte[]> blobs;
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                    blobIds = blobCache.getBlobIds();
                    blobs = blobCache.getBlobs();
                } else {
                    blobIds = blobCache.getBlobIdsLegacy();
                    blobs = blobCache.getBlobsLegacy();
                }

                ObjectIterator<Long2ObjectMap.Entry<byte[]>> iter = Long2ObjectMaps.fastIterator(blobs);
                while (iter.hasNext()) {
                    Long2ObjectMap.Entry<? extends byte[]> entry = iter.next();
                    long hash = entry.getLongKey();
                    clientCacheTrack.put(hash, new BlobTrack(hash, entry.getValue()));
                }

                pk.subChunkCount = subChunkCount;
                pk.cacheEnabled = true;
                pk.blobIds = blobIds;
                pk.data = blobCache.getFullChunkPayload();

                this.sendQueuedChunk = false;
                if (chunkHash == this.teleportChunkIndex) {
                    if (blobIds.length > 1) {
                        this.teleportChunkBlobHash = blobIds[0];
                        //this.teleportChunkLoaded = false;
                    } else {
                        this.teleportChunkLoaded = true;
                    }
                }
            } else {
                pk.subChunkCount = subChunkCount;
                pk.data = payload;
            }

            if (centerChunk) {
                this.teleportChunkLoaded = true;
            }
        } else if (this.isBlobCacheAvailable() && this.isSubModeLevelChunkBlobCacheEnabled() && !(centerChunk = !this.isNeedLevelChangeLoadScreen() && CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z)) {
            ChunkBlobCache blobCache = cachedData.getBlobCache();
            long[] ids;
            Long2ObjectMap<byte[]> blobs;
            if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                ids = blobCache.getBlobIds();
                blobs = blobCache.getBlobs();
            } else {
                ids = blobCache.getBlobIdsLegacy();
                blobs = blobCache.getBlobsLegacy();
            }

            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, blobs.get(hash)));

            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            pk.subChunkRequestLimit = subChunkCount;
            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubRequestModeFullChunkPayload();
        } else {
            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            pk.subChunkRequestLimit = subChunkCount;
            pk.data = subModePayload;
        }
        this.dataPacket(pk);

        for (BlockEntity blockEntity : this.level.getChunkBlockEntities(x, z).values()) {
            if (!(blockEntity instanceof BlockEntitySpawnable)) {
                continue;
            }
            ((BlockEntitySpawnable) blockEntity).spawnTo(this);
        }

        //TODO: move to sub chunk response?
        if (this.spawned) {
            for (Entity entity : this.level.getChunkEntities(x, z).values()) {
                if (this != entity && !entity.closed && entity.isAlive() && entity.isWithinEntityViewDistance(this)) {
                    entity.spawnTo(this);
                }
            }
        }
    }

    @Override
    public void sendSubChunks(int dimension, int x, int z, int subChunkCount, ChunkCachedData cachedData, Map<StaticVersion, byte[][]> payload, byte[] heightMapType, byte[][] heightMap) {
        if (!this.connected) {
            return;
        }

        long chunkHash = Level.chunkHash(x, z);
        IntSet requests = this.subChunkSendQueue.remove(chunkHash);
        if (requests == null) {
            requests = this.subChunkRequestQueue.remove(chunkHash);
            if (requests == null) {
                return;
            }
        } else {
            IntSet newRequests = this.subChunkRequestQueue.remove(chunkHash);
            if (newRequests != null) {
                requests.addAll(newRequests);
            }
        }

        ChunkBlobCache blobCache = cachedData.getBlobCache();
        int chunkYIndexOffset = level.getHeightRange().getChunkYIndexOffset();
        boolean centerChunk = !this.isNeedLevelChangeLoadScreen() && CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;
        byte[][] payloads = payload.get(blockVersion);

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = Level.yToIndex(y, chunkYIndexOffset);

            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = x;
            pk.subChunkY = y;
            pk.subChunkZ = z;

            if (subChunkCount == 0) {
                pk.data = EMPTY_SUBCHUNK_DATA;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = EMPTY_SUBCHUNK_HEIGHTMAP;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                this.dataPacket(pk);
                iter.remove();
                continue;
            } else if (index >= subChunkCount) {
                pk.data = EMPTY_SUBCHUNK_DATA;
                pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                this.dataPacket(pk);
                iter.remove();
                continue;
            }

            if (index < 0 || index >= cachedData.getEmptySection().length || cachedData.getEmptySection()[index]) {
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
            } else if (this.isBlobCacheAvailable() && this.isSubChunkBlobCacheEnabled() && !centerChunk) {
                long[] ids;
                Long2ObjectMap<byte[]> blobs;
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                    ids = blobCache.getBlobIds();
                    blobs = blobCache.getBlobs();
                } else {
                    ids = blobCache.getBlobIdsLegacy();
                    blobs = blobCache.getBlobsLegacy();
                }

                long hash = ids[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, blobs.get(hash)));

                pk.data = blobCache.getSubChunkPayloads()[index];
                pk.blobId = hash;
                pk.cacheEnabled = true;
            } else {
                pk.data = payloads[index];
            }

            if (index < 0) {
                pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_HIGH;
            } else {
                pk.heightMapType = heightMapType[index];
                pk.heightMap = heightMap[index];
            }
            this.dataPacket(pk);
            iter.remove();
        }
    }

    @Override
    public void sendSubChunks(int dimension, int x, int z, int subChunkCount, ChunkCachedData cachedData, byte[] heightMapType, byte[][] heightMap) {
        if (!this.connected) {
            return;
        }

        long chunkHash = Level.chunkHash(x, z);
        IntSet requests = this.subChunkSendQueue.remove(chunkHash);
        if (requests == null) {
            requests = this.subChunkRequestQueue.remove(chunkHash);
            if (requests == null) {
                return;
            }
        } else {
            IntSet newRequests = this.subChunkRequestQueue.remove(chunkHash);
            if (newRequests != null) {
                requests.addAll(newRequests);
            }
        }

        ChunkPacketCache packetCache = cachedData.getPacketCache();
        assert packetCache != null;
        ChunkBlobCache blobCache = cachedData.getBlobCache();
        int chunkYIndexOffset = level.getHeightRange().getChunkYIndexOffset();
        boolean centerChunk = !this.isNeedLevelChangeLoadScreen() && CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;
        BatchPacket[] packets = packetCache.getSubChunkPackets(blockVersion);
        SubChunkPacket[] packetsUncompressed = packetCache.getSubChunkPacketsUncompressed(blockVersion);

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = Level.yToIndex(y, chunkYIndexOffset);

            if (subChunkCount == 0) {
                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = EMPTY_SUBCHUNK_DATA;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = EMPTY_SUBCHUNK_HEIGHTMAP;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                this.dataPacket(pk);
                iter.remove();
                continue;
            } else if (index >= subChunkCount) {
                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = EMPTY_SUBCHUNK_DATA;
                pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                this.dataPacket(pk);
                iter.remove();
                continue;
            }

            if (index < 0 || index >= cachedData.getEmptySection().length || cachedData.getEmptySection()[index]) {
                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                if (index < 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_HIGH;
                } else {
                    pk.heightMapType = heightMapType[index];
                    pk.heightMap = heightMap[index];
                }
                this.dataPacket(pk);
            } else if (this.isBlobCacheAvailable() && this.isSubChunkBlobCacheEnabled() && !centerChunk) {
                long[] ids;
                Long2ObjectMap<byte[]> blobs;
                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
                    ids = blobCache.getBlobIds();
                    blobs = blobCache.getBlobs();
                } else {
                    ids = blobCache.getBlobIdsLegacy();
                    blobs = blobCache.getBlobsLegacy();
                }

                long hash = ids[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, blobs.get(hash)));

                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = blobCache.getSubChunkPayloads()[index];
                pk.blobId = hash;
                pk.cacheEnabled = true;
                pk.heightMapType = heightMapType[index];
                pk.heightMap = heightMap[index];
                this.dataPacket(pk);
            } else if (isNeedLevelChangeLoadScreen()) {
                SubChunkPacket uncompressed = packetsUncompressed[index];

                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = uncompressed.subChunkX;
                pk.subChunkY = uncompressed.subChunkY;
                pk.subChunkZ = uncompressed.subChunkZ;
                pk.data = uncompressed.data;
                pk.requestResult = uncompressed.requestResult;
                pk.heightMapType = uncompressed.heightMapType;
                pk.heightMap = uncompressed.heightMap;
                this.dataPacket(pk);
            } else {
                this.dataPacket(packets[index]);
            }

            iter.remove();
        }
    }

    @Override
    public void onSubChunkRequestFail(int dimension, int x, int z) {
        if (!this.connected) {
            return;
        }

        long chunkHash = Level.chunkHash(x, z);
        IntSet requests = this.subChunkSendQueue.remove(chunkHash);
        if (requests == null) {
            requests = this.subChunkRequestQueue.remove(chunkHash);
            if (requests == null) {
                return;
            }
        } else {
            IntSet newRequests = this.subChunkRequestQueue.remove(chunkHash);
            if (newRequests != null) {
                requests.addAll(newRequests);
            }
        }

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();

            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = x;
            pk.subChunkY = y;
            pk.subChunkZ = z;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_NO_SUCH_CHUNK;
            this.dataPacket(pk);

            iter.remove();
        }
    }

    @Override
    protected void clearSubChunkQueues() {
        this.clearSubChunkQueue(this.subChunkSendQueue);
        this.clearSubChunkQueue(this.subChunkRequestQueue);
    }

    protected void clearSubChunkQueue(Long2ObjectMap<IntSet> subChunkQueue) {
        ObjectIterator<Long2ObjectMap.Entry<IntSet>> iter = subChunkQueue.long2ObjectEntrySet().iterator();
        while (iter.hasNext()) {
            Long2ObjectMap.Entry<IntSet> entry = iter.next();
            long index = entry.getLongKey();
            int chunkX = Level.getHashX(index);
            int chunkZ = Level.getHashZ(index);
            IntSet requests = entry.getValue();

            IntIterator yIter = requests.iterator();
            while (yIter.hasNext()) {
                int chunkY = yIter.nextInt();

                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = this.getDummyDimension();  // Level.DIMENSION_OVERWORLD;
                pk.subChunkX = chunkX;
                pk.subChunkY = chunkY;
                pk.subChunkZ = chunkZ;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_NO_SUCH_CHUNK;
                this.dataPacket(pk);

                yIter.remove();
            }
            iter.remove();
        }
    }

    @Override
    protected boolean hasSubChunkRequest() {
        return !this.subChunkRequestQueue.isEmpty();
    }

    @Override
    protected void processSubChunkRequest() {
        if (!this.connected) {
            return;
        }
        int count = 0;

        ObjectIterator<Long2ObjectMap.Entry<IntSet>> iter = this.subChunkRequestQueue.long2ObjectEntrySet().iterator();
        while (iter.hasNext()) {
            if (count >= this.chunksPerTick) {
                break;
            }

            Long2ObjectMap.Entry<IntSet> entry = iter.next();
            long index = entry.getLongKey();
            int chunkX = Level.getHashX(index);
            int chunkZ = Level.getHashZ(index);
            IntSet newRequests = entry.getValue();

            if (this.level.requestSubChunks(chunkX, chunkZ, this, getDummyDimension())) {
                IntSet requests = this.subChunkSendQueue.get(index);
                if (requests == null) {
                    requests = newRequests;
                    this.subChunkSendQueue.put(index, requests);
                } else {
                    requests.addAll(newRequests);
                }

                ++count;
            } else {
                boolean playerNotFound = this.getLoaderId() <= 0;

                IntIterator yIter = newRequests.iterator();
                while (yIter.hasNext()) {
                    int chunkY = yIter.nextInt();

                    SubChunkPacket pk = this.createSubChunkPacket();
                    pk.dimension = this.getDummyDimension();  // Level.DIMENSION_OVERWORLD;
                    pk.subChunkX = chunkX;
                    pk.subChunkY = chunkY;
                    pk.subChunkZ = chunkZ;
                    if (playerNotFound) {
                        pk.requestResult = SubChunkPacket.REQUEST_RESULT_PLAYER_NOT_FOUND;
                    } else {
                        pk.requestResult = SubChunkPacket.REQUEST_RESULT_NO_SUCH_CHUNK;
                    }
                    this.dataPacket(pk);

                    yIter.remove();
                }
            }
            iter.remove();
        }
    }

    @Override
    protected boolean canSendQueuedChunk() {
        return this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart() || super.canSendQueuedChunk();
    }

/*
    @Override
    public boolean isBlobCacheDisabled() {
        // client performance is very bad in 1.18.0, Microjang messed up everything :(
        return this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart() && !this.isNetEaseClient();
    }
*/

    public boolean isSubChunkBlobCacheEnabled() {
        return true;
    }

    public boolean isSubModeLevelChunkBlobCacheEnabled() {
        return true;
    }

    protected boolean handleSubChunkRequest(int dimension, int subChunkX, int subChunkY, int subChunkZ) {
        if (this.getLoaderId() <= 0 || dimension == this.transferDimension) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_PLAYER_NOT_FOUND;
            this.dataPacket(pk);
            return false;
        }

        if (!level.getHeightRange().isValidSubChunkY(subChunkY)) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_Y_INDEX_OUT_OF_BOUNDS;
            this.dataPacket(pk);
            return false;
        }

        // dummyDimension HACK!
        if (dimension != Level.DIMENSION_OVERWORLD && dimension != this.getDummyDimension()) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_WRONG_DIMENSION;
            this.dataPacket(pk);
            return false;
        }

        int chunkX = getChunkX();
        int chunkZ = getChunkZ();
        int chunkRadius = getViewDistance();
        final int threshold = 2;
        if (subChunkX > chunkX + chunkRadius + threshold || subChunkX < chunkX - chunkRadius - threshold || subChunkZ > chunkZ + chunkRadius + threshold || subChunkZ < chunkZ - chunkRadius - threshold) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_NO_SUCH_CHUNK;
            this.dataPacket(pk);
            return false;
        }

        long index = Level.chunkHash(subChunkX, subChunkZ);
        IntSet queue = this.subChunkSendQueue.get(index);
        if (queue != null) {
            queue.add(subChunkY);
            return true;
        }

        this.subChunkRequestQueue.computeIfAbsent(index, key -> new IntOpenHashSet())
                .add(subChunkY);
        return true;
    }

    protected SubChunkPacket createSubChunkPacket() {
        return /*this.protocol < AbstractProtocol.PROTOCOL_118_10.getProtocolStart() ? new SubChunkPacket() :*/
                this.protocol >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart() ? new SubChunkPacket12190() : new SubChunkPacket11810();
    }

    @Override
    protected LevelChunkPacket createLevelChunkPacket() {
        return this.protocol < AbstractProtocol.PROTOCOL_120_60.getProtocolStart() ? new LevelChunkPacket() : new LevelChunkPacket12060();
    }

    @Override
    protected void onDimensionChangeSuccess() {
//        log.info("dimension change success: {}", this);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        int tickDiff = currentTick - this.lastUpdate;
        if (tickDiff > 0) {
            if (getProtocol() >= AbstractProtocol.PROTOCOL_120_10.getProtocolStart()) {
                if (onGround && isGliding()) {
                    setGliding(false);
                }
            }

            removeExpiredShapes();
        }
        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnParticleEffect(Vector3f position, String identifier, long entityUniqueId, @Nullable String molangVariables) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            super.spawnParticleEffect(position, identifier, entityUniqueId, null);
            return;
        }

        SpawnParticleEffectPacket11830 packet = new SpawnParticleEffectPacket11830();
        packet.position = position;
        packet.identifier = identifier;
        packet.uniqueEntityId = entityUniqueId;
        packet.dimension = getDummyDimension()/*Level.DIMENSION_OVERWORLD*/;
        packet.molangVariables = molangVariables;
        dataPacket(packet);
    }

    @Override
    public void addCameraShake(float intensity, float duration, int type) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_210.getProtocolStart()) {
            CameraShakePacket116100 pk = new CameraShakePacket116100();
            pk.intensity = intensity;
            pk.duration = duration;
            pk.type = type;
            this.dataPacket(pk);
            return;
        }
        CameraShakePacket116210 pk = new CameraShakePacket116210();
        pk.intensity = intensity;
        pk.duration = duration;
        pk.type = type;
        pk.action = CameraShakePacket116210.ACTION_ADD;
        this.dataPacket(pk);
    }

    @Override
    public void stopCameraShake() {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_210.getProtocolStart()) {
            CameraShakePacket116100 pk = new CameraShakePacket116100();
            pk.intensity = 0;
            pk.duration = 0;
            this.dataPacket(pk);
            return;
        }
        CameraShakePacket116210 pk = new CameraShakePacket116210();
        pk.intensity = 0;
        pk.duration = 0;
        pk.action = CameraShakePacket116210.ACTION_STOP;
        this.dataPacket(pk);
    }

    @Override
    protected void sendItemComponents() {
        DataPacket pk = ItemComponentDefinitions.getPacket(AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
        if (pk == null) {
            if (getProtocol() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
                throw new AssertionError("Missing ItemRegistryPacket: " + getAbstractProtocol());
            }
            pk = new ItemComponentPacket116100();
        }
        this.dataPacket(pk);
    }

    @Override
    public void startItemCooldown(String itemCategory, int duration) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
            return;
        }
        PlayerStartItemCooldownPacket11810 pk = new PlayerStartItemCooldownPacket11810();
        pk.itemCategory = itemCategory;
        pk.cooldownTicks = duration;
        this.dataPacket(pk);
    }

    @Override
    public void sendToast(String title, String content) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_119.getProtocolStart()) {
            return;
        }
        ToastRequestPacket119 pk = new ToastRequestPacket119();
        pk.title = title;
        pk.content = content;
        this.dataPacket(pk);
    }

    @Override
    public void sendJukeboxPopup(TranslationContainer message) {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            TextPacket126 pk = new TextPacket126();
            pk.type = TextPacket126.TYPE_JUKEBOX_POPUP;
            pk.isLocalized = true;
            pk.message = message.getText();
            pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
            this.dataPacket(pk);
            return;
        }

        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
            TextPacket121130 pk = new TextPacket121130();
            pk.type = TextPacket121130.TYPE_JUKEBOX_POPUP;
            pk.isLocalized = true;
            pk.message = message.getText();
            pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
            this.dataPacket(pk);
            return;
        }

        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_121.getProtocolStart()) {
            TextPacket121 pk = new TextPacket121();
            pk.type = TextPacket121.TYPE_JUKEBOX_POPUP;
            pk.isLocalized = true;
            pk.message = message.getText();
            pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
            this.dataPacket(pk);
            return;
        }

        if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
            TextPacket116100NE pk = new TextPacket116100NE();
            pk.type = TextPacket116100NE.TYPE_JUKEBOX_POPUP;
            pk.isLocalized = true;
            pk.message = message.getText();
            pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
            this.dataPacket(pk);
            return;
        }

        TextPacket116100 pk = new TextPacket116100();
        pk.type = TextPacket116100.TYPE_JUKEBOX_POPUP;
        pk.isLocalized = true;
        pk.message = message.getText();
        pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
        this.dataPacket(pk);
    }

    @Override
    protected void noticeChunkPublisherUpdate() {
        if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118_30_NE.getProtocolStart()) {
            NetworkChunkPublisherUpdatePacket11830NE packet = new NetworkChunkPublisherUpdatePacket11830NE();
            packet.x = getFloorX();
            packet.y = getFloorY();
            packet.z = getFloorZ();
            packet.radius = getViewDistance() << 4;
            packet.neteaseMode = isNetEaseClient();
            dataPacket(packet);
            return;
        }

        if (this.getProtocol() < AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
            super.noticeChunkPublisherUpdate();
            return;
        }

        NetworkChunkPublisherUpdatePacket11920 packet = new NetworkChunkPublisherUpdatePacket11920();
        packet.x = getFloorX();
        packet.y = getFloorY();
        packet.z = getFloorZ();
        packet.radius = getViewDistance() << 4;
        dataPacket(packet);
    }

    @Override
    public void sendAbilities(Player player, AdventureSettings settings) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_119_10.getProtocolStart()) {
            super.sendAbilities(player, settings);
            return;
        }

        Set<PlayerAbility> baseAbilities = EnumSet.allOf(PlayerAbility.class);
        Set<PlayerAbility> baseAbilityValues = EnumSet.noneOf(PlayerAbility.class);
        if (settings.get(Type.BUILD)) baseAbilityValues.add(PlayerAbility.BUILD);
        if (settings.get(Type.MINE)) baseAbilityValues.add(PlayerAbility.MINE);
        if (settings.get(Type.DOORS_AND_SWITCHED)) baseAbilityValues.add(PlayerAbility.DOORS_AND_SWITCHES);
        if (settings.get(Type.OPEN_CONTAINERS)) baseAbilityValues.add(PlayerAbility.OPEN_CONTAINERS);
        if (settings.get(Type.ATTACK_PLAYERS)) baseAbilityValues.add(PlayerAbility.ATTACK_PLAYERS);
        if (settings.get(Type.ATTACK_MOBS)) baseAbilityValues.add(PlayerAbility.ATTACK_MOBS);
        if (settings.get(Type.OPERATOR)) baseAbilityValues.add(PlayerAbility.OPERATOR_COMMANDS);
        if (settings.get(Type.TELEPORT)) baseAbilityValues.add(PlayerAbility.TELEPORT);
        if (settings.get(Type.INVULNERABLE)) baseAbilityValues.add(PlayerAbility.INVULNERABLE);
        if (settings.get(Type.FLYING)) baseAbilityValues.add(PlayerAbility.FLYING);
        if (settings.get(Type.ALLOW_FLIGHT)) baseAbilityValues.add(PlayerAbility.MAY_FLY);
        if (settings.get(Type.INSTABUILD)) baseAbilityValues.add(PlayerAbility.INSTABUILD);
        if (settings.get(Type.LIGHTNING)) baseAbilityValues.add(PlayerAbility.LIGHTNING);
        if (settings.get(Type.MUTED)) baseAbilityValues.add(PlayerAbility.MUTED);
        if (settings.get(Type.WORLD_BUILDER)) baseAbilityValues.add(PlayerAbility.WORLD_BUILDER);
        boolean noClip = settings.get(Type.NO_CLIP);
        if (noClip) baseAbilityValues.add(PlayerAbility.NO_CLIP);
        if (settings.get(Type.PRIVILEGED_BUILDER)) baseAbilityValues.add(PlayerAbility.PRIVILEGED_BUILDER);

        AbilityLayer[] layers;
        if (!noClip) {
            layers = new AbilityLayer[]{
                    new AbilityLayer(AbilityLayer.TYPE_BASE, baseAbilities, baseAbilityValues, flySpeed, verticalFlySpeed, walkSpeed),
            };
        } else {
            Set<PlayerAbility> spectatorAbilities = EnumSet.allOf(PlayerAbility.class);
            Set<PlayerAbility> spectatorAbilityValues = EnumSet.copyOf(baseAbilityValues);
            spectatorAbilities.remove(PlayerAbility.FLY_SPEED);
            spectatorAbilities.remove(PlayerAbility.VERTICAL_FLY_SPEED);
            spectatorAbilities.remove(PlayerAbility.WALK_SPEED);
            spectatorAbilityValues.add(PlayerAbility.FLYING);
            spectatorAbilityValues.add(PlayerAbility.NO_CLIP);

            layers = new AbilityLayer[]{
                    new AbilityLayer(AbilityLayer.TYPE_BASE, baseAbilities, baseAbilityValues, flySpeed, verticalFlySpeed, walkSpeed),
                    new AbilityLayer(AbilityLayer.TYPE_SPECTATOR, spectatorAbilities, spectatorAbilityValues, flySpeed, verticalFlySpeed, walkSpeed),
            };
        }

        UpdateAbilitiesPacket11910 packet = new UpdateAbilitiesPacket11910();
        packet.entityUniqueId = player.getId();
        boolean isOp = player.isOp();
        packet.playerPermission = isOp && !player.isSpectator() ? Player.PERMISSION_OPERATOR : Player.PERMISSION_MEMBER;
        packet.commandPermission = isOp ? CommandPermission.GAME_DIRECTORS : CommandPermission.ALL;
        packet.abilityLayers = layers;
        dataPacket(packet);
    }

    @Override
    public void sendAdventureSettings(AdventureSettings settings) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_119_10.getProtocolStart()) {
            super.sendAdventureSettings(settings);
            return;
        }

        UpdateAdventureSettingsPacket11910 packet = new UpdateAdventureSettingsPacket11910();
        packet.noPvM = settings.get(Type.NO_PVM);
        packet.noMvP = settings.get(Type.NO_MVP);
        packet.worldImmutable = settings.get(Type.WORLD_IMMUTABLE);
        packet.showNameTags = settings.get(Type.SHOW_NAME_TAGS);
        packet.autoJump = settings.get(Type.AUTO_JUMP);
        dataPacket(packet);
    }

    @Override
    public void sendNetworkSettings() {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_30.getProtocolStart()) {
            // handle by nemisys
            return;
        }

        super.sendNetworkSettings();
    }

    @Override
    public void openSignEditor(int x, int y, int z, boolean front) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_119_80.getProtocolStart()) {
            return;
        }

        OpenSignPacket11980 packet = new OpenSignPacket11980();
        packet.x = x;
        packet.y = y;
        packet.z = z;
        packet.front = front;
        dataPacket(packet);
    }

    @Override
    public boolean isNeedLevelChangeLoadScreen() {
        return (this.isNetEaseClient() || getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()/* || this.isJavaClient()*/) && this.isSubChunkRequestAvailable() /*&& this.isBlobCacheAvailable()*/;
    }

    @Override
    public void sendDimensionData() {
        if (isNetEaseClient()) {
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            return;
        }
        DimensionDataPacket12620 packet = new DimensionDataPacket12620();
        packet.definitions = DimensionDefinition.BUILTIN_DIMENSIONS;
        dataPacket(packet);
    }

    @Override
    public void playEmote(String emoteId, long entityRuntimeId, int flags, int emoteTicks) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_30.getProtocolStart()) {
            EmotePacket12130 packet = new EmotePacket12130();
            packet.emoteID = emoteId;
            //packet.emoteTicks = emoteTicks; // seems to be irrelevant for the client, we cannot risk rebroadcasting random values received
            packet.runtimeId = entityRuntimeId;
            packet.flags = flags;
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
            super.playEmote(emoteId, entityRuntimeId, flags, emoteTicks);
            return;
        }

        EmotePacket120 packet = new EmotePacket120();
        packet.emoteID = emoteId;
        packet.runtimeId = entityRuntimeId;
        packet.flags = flags;
        dataPacket(packet);
    }

    @Override
    public void sendFogStack(String... fogStack) {
        PlayerFogPacket116100 fog = new PlayerFogPacket116100();
        fog.fogStack = fogStack;
        this.dataPacket(fog);
    }

    @Override
    public void syncEntityProperties() {
        DataPacket packet = EntityPropertiesCache.getPacket(AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
        if (packet == null) {
            return;
        }
        dataPacket(packet);
    }

    protected byte[] getCompiledPlayerProperties() {
        return EntityPropertiesCache.getCompiledPlayerProperties(AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
    }

    protected OptionalInt lookupEntityPropertyIndex(Entity entity, String propertyName) {
        EntityProperty property = entity.getProperties().getProperty(propertyName);
        if (property == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(property.getIndex());
    }

    @Override
    public void sendEntityPropertyInt(Entity entity, String propertyName, int value) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
            int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
            if (index < 0) {
                this.getServer().getLogger().warning("Unknown entity property: " + propertyName);
                return;
            }

            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.eid = entity.getId();
            packet.metadata = new EntityMetadata();
            packet.intProperties.put(index, value);
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            return;
        }

        ChangeMobPropertyPacket11830 packet = new ChangeMobPropertyPacket11830();
        packet.uniqueEntityId = entity.getId();
        packet.property = propertyName;
        packet.intValue = value;
        dataPacket(packet);
    }

    @Override
    public void sendEntityPropertyFloat(Entity entity, String propertyName, float value) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
            int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
            if (index < 0) {
                this.getServer().getLogger().warning("Unknown entity property: " + propertyName);
                return;
            }

            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.eid = entity.getId();
            packet.metadata = new EntityMetadata();
            packet.floatProperties.put(index, value);
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            return;
        }

        ChangeMobPropertyPacket11830 packet = new ChangeMobPropertyPacket11830();
        packet.uniqueEntityId = entity.getId();
        packet.property = propertyName;
        packet.floatValue = value;
        dataPacket(packet);
    }

    @Override
    public void sendEntityPropertyBool(Entity entity, String propertyName, boolean value) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
            int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
            if (index < 0) {
                this.getServer().getLogger().warning("Unknown entity property: " + propertyName);
                return;
            }

            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.eid = entity.getId();
            packet.metadata = new EntityMetadata();
            packet.intProperties.put(index, value ? 1 : 0);
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            return;
        }

        ChangeMobPropertyPacket11830 packet = new ChangeMobPropertyPacket11830();
        packet.uniqueEntityId = entity.getId();
        packet.property = propertyName;
        packet.boolValue = value;
        dataPacket(packet);
    }

    @Override
    public void sendEntityPropertyEnum(Entity entity, String propertyName, String value) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
            EntityProperty property = entity.getProperties().getProperty(propertyName);
            if (property == null) {
                this.getServer().getLogger().warning("Unknown entity property: " + propertyName);
                return;
            }
            if (!(property instanceof EnumEntityProperty enumProperty)) {
                this.getServer().getLogger().warning("Entity property type mismatch: " + propertyName);
                return;
            }
            int valueIndex = enumProperty.getValues().indexOf(value);
            if (valueIndex == -1) {
                this.getServer().getLogger().warning("Unknown entity property value: " + propertyName);
                return;
            }
            int index = property.getIndex();

            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.eid = entity.getId();
            packet.metadata = new EntityMetadata();
            packet.intProperties.put(index, valueIndex);
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            return;
        }

        ChangeMobPropertyPacket11830 packet = new ChangeMobPropertyPacket11830();
        packet.uniqueEntityId = entity.getId();
        packet.property = propertyName;
        packet.stringValue = value;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, String propertyName, int value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
        if (index < 0) {
            getServer().getLogger().warning("unknown entity property: " + propertyName);
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = index;
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = value;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, IntEntityProperty property, int value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = property.getIndex();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = value;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, String propertyName, float value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
        if (index < 0) {
            getServer().getLogger().warning("unknown entity property: " + propertyName);
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = index;
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_FLOAT_OVERRIDE;
        packet.floatValue = value;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, FloatEntityProperty property, float value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = property.getIndex();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_FLOAT_OVERRIDE;
        packet.floatValue = value;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, String propertyName, boolean value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
        if (index < 0) {
            getServer().getLogger().warning("unknown entity property: " + propertyName);
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = index;
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = value ? 1 : 0;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, BooleanEntityProperty property, boolean value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = property.getIndex();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = value ? 1 : 0;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, String propertyName, String value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        EntityProperty property = entity.getProperties().getProperty(propertyName);
        if (property == null) {
            getServer().getLogger().warning("unknown entity property: " + propertyName);
            return;
        }
        if (!(property instanceof EnumEntityProperty enumProperty)) {
            getServer().getLogger().warning("entity property type mismatch: " + propertyName);
            return;
        }
        int valueIndex = enumProperty.getValues().indexOf(value);
        if (valueIndex == -1) {
            getServer().getLogger().warning("unknown entity property value: " + propertyName);
            return;
        }
        int index = property.getIndex();

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = index;
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = valueIndex;
        dataPacket(packet);
    }

    @Override
    public void setEntityPropertyOverride(Entity entity, EnumEntityProperty property, String value) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        int valueIndex = property.getValues().indexOf(value);
        if (valueIndex == -1) {
            getServer().getLogger().warning("unknown entity property value: " + property);
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = property.getIndex();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_SET_INT_OVERRIDE;
        packet.intValue = valueIndex;
        dataPacket(packet);
    }

    @Override
    public void removeEntityPropertyOverride(Entity entity, String propertyName) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        int index = lookupEntityPropertyIndex(entity, propertyName).orElse(-1);
        if (index < 0) {
            getServer().getLogger().warning("unknown entity property: " + propertyName);
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = index;
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_REMOVE_OVERRIDE;
        dataPacket(packet);
    }

    @Override
    public void removeEntityPropertyOverride(Entity entity, EntityProperty property) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.propertyIndex = property.getIndex();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_REMOVE_OVERRIDE;
        dataPacket(packet);
    }

    @Override
    public void removeEntityPropertyOverrides(Entity entity) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            return;
        }

        PlayerUpdateEntityOverridesPacket12170 packet = new PlayerUpdateEntityOverridesPacket12170();
        packet.entityUniqueId = entity.getId();
        packet.type = PlayerUpdateEntityOverridesPacket12170.TYPE_CLEAR_OVERRIDES;
        dataPacket(packet);
    }

    @Override
    protected void sendTrimRecipes() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
            return;
        }

        TrimDataPacket120 packet = new TrimDataPacket120();
        packet.trimPatterns = TrimPatterns.getRegistry().values().toArray(new TrimPattern[0]);
        packet.trimMaterials = TrimMaterials.getRegistry().values().toArray(new TrimMaterial[0]);
        this.dataPacket(packet);
    }

    @Override
    public boolean isServerAuthoritativeSoundEnabled() {
        return getProtocol() >= AbstractProtocol.PROTOCOL_120.getProtocolStart();
    }

    @Override
    protected void sendDeathInfo(TextContainer message) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_119_10.getProtocolStart()) {
            return;
        }

        DeathInfoPacket11910 packet = new DeathInfoPacket11910();
        String text = message.getText();
        if (message instanceof TranslationContainer translation) {
            Object[] parameters = translation.getParameters();
            if (server.isLanguageForced()) {
                packet.messageTranslationKey = server.getLanguage().translate(text, parameters);
            } else {
                String translated = server.getLanguage().translateOnly("nukkit.", text, parameters);
                packet.messageTranslationKey = translated != null ? translated : text;
                String[] params = new String[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    String param = String.valueOf(parameters[i]);
                    String translatedParam = this.server.getLanguage().translateOnly("nukkit.", param, parameters);
                    params[i] = translatedParam != null ? translatedParam : param;
                }
                packet.messageParameters = params;
            }
        } else {
            packet.messageTranslationKey = text;
        }
        dataPacket(packet);
    }

    @Override
    public void sendMotionPredictionHints(long entityRuntimeId, float motionX, float motionY, float motionZ, boolean onGround) {
        MotionPredictionHintsPacket116100 packet = new MotionPredictionHintsPacket116100();
        packet.entityRuntimeId = entityRuntimeId;
        packet.x = motionX;
        packet.y = motionY;
        packet.z = motionZ;
        packet.onGround = onGround;
        dataPacket(packet);
    }

    @Override
    protected void syncFeatureRegistry() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_119_20.getProtocolStart()) {
            return;
        }

        FeatureRegistryPacket11920 packet = new FeatureRegistryPacket11920();
        this.dataPacket(packet);
    }

    @Override
    protected void sendCameraPresets() {
        DataPacket batch = CameraManager.getInstance().getPresetPackets(getAbstractProtocol());
        if (batch == null) {
            return;
        }
        dataPacket(batch);
    }

    @Override
    public void startCameraInstruction(CameraInstruction instruction) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
            CameraInstructionPacket12610 pk = new CameraInstructionPacket12610();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            CameraInstructionPacket126 pk = new CameraInstructionPacket126();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            CameraInstructionPacket121120 pk = new CameraInstructionPacket121120();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_100.getProtocolStart()) {
            CameraInstructionPacket121100 pk = new CameraInstructionPacket121100();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            CameraInstructionPacket12190 pk = new CameraInstructionPacket12190();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            CameraInstructionPacket12140 pk = new CameraInstructionPacket12140();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            CameraInstructionPacket12120 pk = new CameraInstructionPacket12120();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_120_30.getProtocolStart()) {
            CameraInstructionPacket12030 pk = new CameraInstructionPacket12030();
            pk.instruction = instruction;
            this.dataPacket(pk);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
            return;
        }
        CameraInstructionPacket11970 pk = new CameraInstructionPacket11970();
        pk.instruction = instruction;
        this.dataPacket(pk);
    }

    @Override
    public void clearCameraInstruction() {
        startCameraInstruction(CameraInstruction.clear());
    }

    @Override
    public void sendDisconnectScreen(int reason, @Nullable String message) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
            DisconnectPacket12620 packet = new DisconnectPacket12620();
            packet.reason = reason;
            if (message != null) {
                packet.message = !isNetEaseClient() ? TextFormat.clean(message) : message;
            } else {
                packet.hideDisconnectionScreen = true;
            }
            dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            DisconnectPacket12120 packet = new DisconnectPacket12120();
            packet.reason = reason;
            if (message != null) {
                packet.message = !isNetEaseClient() && AbstractProtocol.PROTOCOL_121_100.isOlderThanOrEqual(getProtocol()) ? TextFormat.clean(message) : message;
            } else {
                packet.hideDisconnectionScreen = true;
            }
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_120_40.getProtocolStart()) {
            super.sendDisconnectScreen(reason, message);
            return;
        }

        DisconnectPacket12040 packet = new DisconnectPacket12040();
        packet.reason = reason;
        if (message != null) {
            packet.message = message;
        } else {
            packet.hideDisconnectionScreen = true;
        }
        dataPacket(packet);
    }

    @Override
    public void lockInput(boolean movement, boolean rotation) {
        int flags = InputLock.NONE;
        if (movement) {
            flags |= InputLock.MOVEMENT;
        }
        if (rotation) {
            flags |= InputLock.CAMERA;
        }
        lockInput(flags);
    }

    @Override
    public void lockInput(int flags) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_10.getProtocolStart()) {
            UpdateClientInputLocksPacket12610 packet = new UpdateClientInputLocksPacket12610();
            packet.flags = flags;
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            return;
        }
        UpdateClientInputLocksPacket11950 packet = new UpdateClientInputLocksPacket11950();
        packet.flags = flags;
        packet.x = (float) getX();
        packet.y = (float) getY() + getEyeHeight();
        packet.z = (float) getZ();
        dataPacket(packet);
    }

    @Override
    public void openBlockEditor(int x, int y, int z, int type) {
        if (type == ContainerType.LECTERN && getProtocol() < AbstractProtocol.PROTOCOL_120_60.getProtocolStart()) {
            return;
        }

        super.openBlockEditor(x, y, z, type);
    }

    @Override
    public void hideHud() {
        hideHudElements(HudElement.ALL);
    }

    @Override
    public void hideHudElements(int... elements) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            SetHudPacket12170 packet = new SetHudPacket12170();
            packet.elements = elements;
            packet.visibility = SetHudPacket12170.VISIBILITY_HIDE;
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_120_60.getProtocolStart()) {
            return;
        }
        SetHudPacket12060 packet = new SetHudPacket12060();
        packet.elements = elements;
        packet.visibility = SetHudPacket12060.VISIBILITY_HIDE;
        dataPacket(packet);
    }

    @Override
    public void showHud() {
        showHudElements(HudElement.ALL);
    }

    @Override
    public void showHudElements(int... elements) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            SetHudPacket12170 packet = new SetHudPacket12170();
            packet.elements = elements;
            packet.visibility = SetHudPacket12170.VISIBILITY_RESET;
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_120_60.getProtocolStart()) {
            return;
        }
        SetHudPacket12060 packet = new SetHudPacket12060();
        packet.elements = elements;
        packet.visibility = SetHudPacket12060.VISIBILITY_RESET;
        dataPacket(packet);
    }

    @Override
    public void requestCloseFormWindow() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_2.getProtocolStart()) {
            return;
        }

        dataPacket(new ClientboundCloseFormPacket1212());
    }

    @Override
    protected void firstRespawn(Position pos) {
        super.firstRespawn(pos);

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            return;
        }

        CurrentStructureFeaturePacket12120 packet = new CurrentStructureFeaturePacket12120();
        dataPacket(packet);
    }

    @Override
    protected void firstSyncLocalPlayerEntityData() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_119_40.getProtocolStart()) {
            super.firstSyncLocalPlayerEntityData();
            return;
        }

        SetEntityDataPacket pk = new SetEntityDataPacket();
        pk.eid = getId();
        pk.metadata = dataProperties;

        Pair<Int2IntMap, Int2FloatMap> propertyValues = getProperties().getValues();
        if (propertyValues != null) {
            pk.intProperties = propertyValues.left();
            pk.floatProperties = propertyValues.right();
        }

        dataPacket(pk);
    }

    @Override
    public boolean setViewDistance(int distance) {
        if (clientMaxViewDistance != -1) {
            if (distance > clientMaxViewDistance) {
                distance = clientMaxViewDistance;
            }
        } else if (distance > viewDistance) {
            distance = viewDistance;
        }
        int viewDistance = Math.max(4, distance);
        if (this.chunkRadius == viewDistance) {
            return false;
        }
        this.chunkRadius = viewDistance;

        ChunkRadiusUpdatedPacket pk = new ChunkRadiusUpdatedPacket();
        pk.radius = viewDistance;
        this.dataPacket(pk);
        return true;
    }

    @Override
    protected void tryDisruptIllegalClientBeforeStartGame() {
        if (this.isJavaClient()) {
            return;
        }

        if (SERVER_AUTHORITATIVE_INVENTORY) {
            return;
        }

        InventorySlotPacket packet = new InventorySlotPacket();
        packet.inventoryId = ContainerIds.ARMOR;
        packet.slot = ArmorInventory.SLOT_FEET;
        packet.item = Item.get(Item.NETHERITE_BOOTS);
        dataPacket(packet);
    }

    @Override
    public void sendMovementEffect(long entityRuntimeId, int type, int duration) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            return;
        }

        MovementEffectPacket12140 packet = new MovementEffectPacket12140();
        packet.entityRuntimeId = entityRuntimeId;
        packet.effectType = type;
        packet.effectDuration = duration;
        packet.tick = clientTickRaw;
        dataPacket(packet);
    }

    @Override
    public void sendCreativeContents() {
        DataPacket packet = CreativeItemsPalette.getCachedCreativeContentPacket(getAbstractProtocol(), isNetEaseClient());
        if (packet != null) {
            this.dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
            dataPacket(new CreativeContentPacket12160());
            return;
        }

        CreativeContentPacket116 pk = new CreativeContentPacket116();
        pk.entries = this.getCreativeItems().toArray(new Item[0]);
        this.dataPacket(pk);
    }

    @Override
    public void sendLevelSoundEvent(int levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal, long entityUniqueId) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            LevelSoundEventPacketV312630 pk = new LevelSoundEventPacketV312630();
            pk.sound = LevelSoundEventMap.getName(levelSound);
            pk.x = (float) pos.x;
            pk.y = (float) pos.y;
            pk.z = (float) pos.z;
            pk.extraData = LevelSoundEventUtil.translateTo18ExtraData(levelSound, extraData, pitch, AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
            pk.entityIdentifier = entityIdentifier;
            pk.isBabyMob = isBabyMob;
            pk.isGlobal = isGlobal;
            pk.entityUniqueId = entityUniqueId;
            dataPacket(pk);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
            LevelSoundEventPacketV312620 pk = new LevelSoundEventPacketV312620();
            pk.sound = levelSound;
            pk.x = (float) pos.x;
            pk.y = (float) pos.y;
            pk.z = (float) pos.z;
            pk.extraData = LevelSoundEventUtil.translateTo18ExtraData(levelSound, extraData, pitch, AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
            pk.entityIdentifier = entityIdentifier;
            pk.isBabyMob = isBabyMob;
            pk.isGlobal = isGlobal;
            pk.entityUniqueId = entityUniqueId;
            dataPacket(pk);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_70.getProtocolStart()) {
            super.sendLevelSoundEvent(levelSound, pos, extraData, pitch, entityIdentifier, isBabyMob, isGlobal, entityUniqueId);
            return;
        }

        LevelSoundEventPacketV312170 pk = new LevelSoundEventPacketV312170();
        pk.sound = levelSound;
        pk.x = (float) pos.x;
        pk.y = (float) pos.y;
        pk.z = (float) pos.z;
        pk.extraData = LevelSoundEventUtil.translateTo18ExtraData(levelSound, extraData, pitch, AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient());
        pk.entityIdentifier = entityIdentifier;
        pk.isBabyMob = isBabyMob;
        pk.isGlobal = isGlobal;
        pk.entityUniqueId = entityUniqueId;
        dataPacket(pk);
    }

    @Override
    public void setAimAssist(float viewAngleX, float viewAngleZ) {
        setAimAssist(CameraAimAssistPacket12050.MODE_ANGLE, viewAngleX, viewAngleZ, 8.5f, "minecraft:aim_assist_default");
    }

    @Override
    public void setAimAssist(float viewAngleX, float viewAngleZ, String presetId) {
        setAimAssist(CameraAimAssistPacket12050.MODE_ANGLE, viewAngleX, viewAngleZ, 8.5f, presetId);
    }

    @Override
    public void setAimAssist(float distance) {
        setAimAssist(CameraAimAssistPacket12050.MODE_DISTANCE, 50, 50, distance, "minecraft:aim_assist_default");
    }

    @Override
    public void setAimAssist(float distance, String presetId) {
        setAimAssist(CameraAimAssistPacket12050.MODE_DISTANCE, 50, 50, distance, presetId);
    }

    @Override
    public void setAimAssist(int mode, float viewAngleX, float viewAngleZ, float distance) {
        setAimAssist(mode, viewAngleX, viewAngleZ, distance, "minecraft:aim_assist_default");
    }

    @Override
    public void setAimAssist(int mode, float viewAngleX, float viewAngleZ, float distance, String presetId) {
        aimAssistPresetId = presetId;
        aimAssistViewAngleX = viewAngleX;
        aimAssistViewAngleY = viewAngleZ;
        aimAssistDistance = distance;
        aimAssistMode = mode;
        if (allowAimAssist) {
            sendAimAssist(CameraAimAssistPacket12050.ACTION_SET, aimAssistMode, aimAssistViewAngleX, aimAssistViewAngleY, aimAssistDistance, aimAssistPresetId);
        } else {
            sendAimAssist(CameraAimAssistPacket12050.ACTION_CLEAR, CameraAimAssistPacket12050.MODE_ANGLE, 30, 45, 5.7f, "");
        }
    }

    @Override
    public void setAimAssist(String presetId) {
        if (presetId.isEmpty()) {
            setAimAssist(CameraAimAssistPacket12050.MODE_ANGLE, 30, 45, 5.7f, presetId);
        } else if ("minecraft:aim_assist_default".equals(presetId)) {
            setAimAssist(CameraAimAssistPacket12050.MODE_ANGLE, 50, 50, 8.5f, presetId);
        } else {
            //TODO: custom preset
        }
    }

    @Override
    public void clearAimAssist() {
        aimAssistPresetId = "";
        aimAssistViewAngleX = 30;
        aimAssistViewAngleY = 45;
        aimAssistDistance = 5.7f;
        aimAssistMode = CameraAimAssistPacket12050.MODE_ANGLE;
        sendAimAssist(CameraAimAssistPacket12050.ACTION_CLEAR, aimAssistMode, aimAssistViewAngleX, aimAssistViewAngleY, aimAssistDistance, aimAssistPresetId);
    }

    protected void sendAimAssist(int action, int mode, float viewAngleX, float viewAngleZ, float distance, String presetId) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_100.getProtocolStart()) {
            CameraAimAssistPacket120100 cameraAimAssistPacket = new CameraAimAssistPacket120100();
            cameraAimAssistPacket.presetId = presetId;
            cameraAimAssistPacket.pitch = viewAngleX;
            cameraAimAssistPacket.yaw = viewAngleZ;
            cameraAimAssistPacket.distance = distance;
            cameraAimAssistPacket.mode = mode;
            cameraAimAssistPacket.action = action;
            dataPacket(cameraAimAssistPacket);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            return;
        }
        CameraAimAssistPacket12050 cameraAimAssistPacket = new CameraAimAssistPacket12050();
        cameraAimAssistPacket.presetId = presetId;
        cameraAimAssistPacket.pitch = viewAngleX;
        cameraAimAssistPacket.yaw = viewAngleZ;
        cameraAimAssistPacket.distance = distance;
        cameraAimAssistPacket.mode = mode;
        cameraAimAssistPacket.action = action;
        dataPacket(cameraAimAssistPacket);
    }

    @Override
    protected void sendJigsawStructureData() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            return;
        }

        JigsawStructureDataPacket12120 packet = new JigsawStructureDataPacket12120();
        dataPacket(packet);
    }

    @Override
    public boolean isLocatorBarEnabled() {
        return getProtocol() >= AbstractProtocol.PROTOCOL_121_80.getProtocolStart();
    }

    @Override
    public void updateLocator(long entityUniqueId, float x, float y, float z) {
        if (!isLocatorBarEnabled()) {
            return;
        }

        PlayerLocationPacket12180 packet = new PlayerLocationPacket12180();
        packet.hide = false;
        packet.entityUniqueId = entityUniqueId;
        packet.x = x;
        packet.y = y;
        packet.z = z;
        dataPacket(packet);
    }

    @Override
    public void hideLocator(long entityUniqueId) {
        if (!isLocatorBarEnabled()) {
            return;
        }

        PlayerLocationPacket12180 packet = new PlayerLocationPacket12180();
        packet.hide = true;
        packet.entityUniqueId = entityUniqueId;
        dataPacket(packet);
    }

    @Override
    public void addShape(Shape shape) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            return;
        }

        shapes.put(shape.id, new ShapeInstance(shape, shape.totalTimeLeft > 0 ? (int) (shape.totalTimeLeft * 20) + getServer().getTick() : Integer.MAX_VALUE));

        sendPrimitiveShapePacket(shape.createPacketEntry(getDummyDimension(), getAbstractProtocol()));
    }

    @Override
    public void removeShape(Shape shape) {
        if (shapes.remove(shape.id) == null) {
            return;
        }

        sendPrimitiveShapePacket(new Entry(shape.id, getDummyDimension()));
    }

    @Override
    public void removeAllShapes() {
        if (shapes.isEmpty()) {
            return;
        }

        sendPrimitiveShapePacket(shapes.keySet().longStream().mapToObj(id -> new Entry(id, getDummyDimension())).toArray(Entry[]::new));

        shapes.clear();
    }

    private void removeExpiredShapes() {
        int now = getServer().getTick();
        List<Entry> entries = new ArrayList<>();
        Iterator<ShapeInstance> iterator = shapes.values().iterator();
        while (iterator.hasNext()) {
            ShapeInstance instance = iterator.next();
            if (instance.expirationTick > now) {
                continue;
            }

            entries.add(new Entry(instance.shape.id, getDummyDimension()));
            iterator.remove();
        }
        if (entries.isEmpty()) {
            return;
        }

        sendPrimitiveShapePacket(entries.toArray(new Entry[0]));
    }

    private void sendPrimitiveShapePacket(Entry... entries) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            PrimitiveShapesPacket12630 packet = new PrimitiveShapesPacket12630();
            packet.entries = entries;
            dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_20.getProtocolStart()) {
            PrimitiveShapesPacket12620 packet = new PrimitiveShapesPacket12620();
            packet.entries = entries;
            dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            DebugDrawerPacket126 packet = new DebugDrawerPacket126();
            packet.entries = entries;
            dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
            DebugDrawerPacket121130 packet = new DebugDrawerPacket121130();
            packet.entries = entries;
            dataPacket(packet);
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            DebugDrawerPacket121120 packet = new DebugDrawerPacket121120();
            packet.entries = entries;
            dataPacket(packet);
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            return;
        }
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_100.getProtocolStart() && !isNetEaseClient() && !locallyInitialized) {
            // prevent microsoft client crash (netease has already fixed)
            return;
        }
        ServerScriptDebugDrawerPacket12190 packet = new ServerScriptDebugDrawerPacket12190();
        packet.entries = entries;
        dataPacket(packet);
    }

    @Override
    int getClientMaxViewDistance() {
        return clientMaxViewDistance;
    }

    @Override
    public void setControlScheme(int scheme) {
        if (getProtocol() < AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
            return;
        }
        ClientboundControlSchemeSetPacket12180 packet = new ClientboundControlSchemeSetPacket12180();
        packet.scheme = scheme;
        dataPacket(packet);
    }

    @Override
    public void resetControlScheme() {
        setControlScheme(ControlScheme.LOCKED_PLAYER_RELATIVE_STRAFE);
    }

    @Override
    public void setSkyZenithColor(Color color, String... biomeIdentifiers) {
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SKY_ZENITH_COLOR, color, biomeIdentifiers);
    }

    @Override
    public void resetSkyZenithColor(String... biomeIdentifiers) {
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SKY_ZENITH_COLOR, biomeIdentifiers);
    }

    @Override
    public void setSkyHorizonColor(Color color, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SKY_HORIZON_COLOR, color, biomeIdentifiers);
    }

    @Override
    public void resetSkyHorizonColor(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SKY_HORIZON_COLOR, biomeIdentifiers);
    }

    @Override
    public void setHorizonBlendMin(float blendMin, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MIN, blendMin, biomeIdentifiers);
    }

    @Override
    public void resetHorizonBlendMin(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MIN, biomeIdentifiers);
    }

    @Override
    public void setHorizonBlendMax(float blendMax, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MAX, blendMax, biomeIdentifiers);
    }

    @Override
    public void resetHorizonBlendMax(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MAX, biomeIdentifiers);
    }

    @Override
    public void setHorizonBlendStart(float blendStart, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_START, blendStart, biomeIdentifiers);
    }

    @Override
    public void resetHorizonBlendStart(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_START, biomeIdentifiers);
    }

    @Override
    public void setHorizonBlendMieStart(float blendMieStart, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MIE_START, blendMieStart, biomeIdentifiers);
    }

    @Override
    public void resetHorizonBlendMieStart(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_HORIZON_BLEND_MIE_START, biomeIdentifiers);
    }

    @Override
    public void setRayleighStrength(float rayleighStrength, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_RAYLEIGH_STRENGTH, rayleighStrength, biomeIdentifiers);
    }

    @Override
    public void resetRayleighStrength(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_RAYLEIGH_STRENGTH, biomeIdentifiers);
    }

    @Override
    public void setSunMieStrength(float sunMieStrength, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SUN_MIE_STRENGTH, sunMieStrength, biomeIdentifiers);
    }

    @Override
    public void resetSunMieStrength(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SUN_MIE_STRENGTH, biomeIdentifiers);
    }

    @Override
    public void setMoonMieStrength(float moonMieStrength, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_MOON_MIE_STRENGTH, moonMieStrength, biomeIdentifiers);
    }

    @Override
    public void resetMoonMieStrength(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_MOON_MIE_STRENGTH, biomeIdentifiers);
    }

    @Override
    public void setSunGlareShape(float sunGlareShape, String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        setGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SUN_GLARE_SHAPE, sunGlareShape, biomeIdentifiers);
    }

    @Override
    public void resetSunGlareShape(String... biomeIdentifiers) {
        if (AbstractProtocol.PROTOCOL_121_130.isNewerThan(getProtocol())) {
            return;
        }
        resetGraphicsOverride(GraphicsOverrideParameterPacket121120.TYPE_SUN_GLARE_SHAPE, biomeIdentifiers);
    }

    private void setGraphicsOverride(int type, float value, String... biomeIdentifiers) {
        setGraphicsOverride(type, value, true, biomeIdentifiers);
    }

    private void setGraphicsOverride(int type, float value, boolean timeBased, String... biomeIdentifiers) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            if (timeBased) {
                FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, new Vector3f(value, 0, 0))};
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket12630 packet = new GraphicsOverrideParameterPacket12630();
                    packet.keyframes = keyframes;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            } else {
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket12630 packet = new GraphicsOverrideParameterPacket12630();
                    packet.floatValue = value;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            }
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            if (timeBased) {
                FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, new Vector3f(value, 0, 0))};
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket126 packet = new GraphicsOverrideParameterPacket126();
                    packet.keyframes = keyframes;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            } else {
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket126 packet = new GraphicsOverrideParameterPacket126();
                    packet.floatValue = value;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            }
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            return;
        }
        FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, new Vector3f(value, 0, 0))};
        for (String biomeIdentifier : biomeIdentifiers) {
            GraphicsOverrideParameterPacket121120 packet = new GraphicsOverrideParameterPacket121120();
            packet.keyframes = keyframes;
            packet.biome = biomeIdentifier;
            packet.type = type;
            dataPacket(packet);
        }
    }

    private void setGraphicsOverride(int type, Color value, String... biomeIdentifiers) {
        setGraphicsOverride(type, value, true, biomeIdentifiers);
    }

    private void setGraphicsOverride(int type, Color value, boolean timeBased, String... biomeIdentifiers) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            Vector3f vec = new Vector3f(value.getRed() / 255f, value.getGreen() / 255f, value.getBlue() / 255f);
            if (timeBased) {
                FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, vec)};
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket12630 packet = new GraphicsOverrideParameterPacket12630();
                    packet.keyframes = keyframes;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            } else {
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket12630 packet = new GraphicsOverrideParameterPacket12630();
                    packet.vecValue = vec;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            }
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            Vector3f vec = new Vector3f(value.getRed() / 255f, value.getGreen() / 255f, value.getBlue() / 255f);
            if (timeBased) {
                FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, vec)};
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket126 packet = new GraphicsOverrideParameterPacket126();
                    packet.keyframes = keyframes;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            } else {
                for (String biomeIdentifier : biomeIdentifiers) {
                    GraphicsOverrideParameterPacket126 packet = new GraphicsOverrideParameterPacket126();
                    packet.vecValue = vec;
                    packet.biome = biomeIdentifier;
                    packet.type = type;
                    dataPacket(packet);
                }
            }
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            return;
        }
        FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[]{FloatObjectPair.of(0, new Vector3f(value.getRed() / 255f, value.getGreen() / 255f, value.getBlue() / 255f))};
        for (String biomeIdentifier : biomeIdentifiers) {
            GraphicsOverrideParameterPacket121120 packet = new GraphicsOverrideParameterPacket121120();
            packet.keyframes = keyframes;
            packet.biome = biomeIdentifier;
            packet.type = type;
            dataPacket(packet);
        }
    }

    private void resetGraphicsOverride(int type, String... biomeIdentifiers) {
        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            for (String biomeIdentifier : biomeIdentifiers) {
                GraphicsOverrideParameterPacket12630 packet = new GraphicsOverrideParameterPacket12630();
                packet.biome = biomeIdentifier;
                packet.type = type;
                packet.reset = true;
                dataPacket(packet);
            }
            return;
        }

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126.getProtocolStart()) {
            for (String biomeIdentifier : biomeIdentifiers) {
                GraphicsOverrideParameterPacket126 packet = new GraphicsOverrideParameterPacket126();
                packet.biome = biomeIdentifier;
                packet.type = type;
                packet.reset = true;
                dataPacket(packet);
            }
            return;
        }

        if (getProtocol() < AbstractProtocol.PROTOCOL_121_120.getProtocolStart()) {
            return;
        }
        for (String biomeIdentifier : biomeIdentifiers) {
            GraphicsOverrideParameterPacket121120 packet = new GraphicsOverrideParameterPacket121120();
            packet.biome = biomeIdentifier;
            packet.type = type;
            packet.reset = true;
            dataPacket(packet);
        }
    }

    @Override
    protected void sendVoxelShapes() {
        DataPacket packet = VoxelShapeManager.getPacket(getAbstractProtocol(), isNetEaseClient());
        if (packet == null) {
            return;
        }
        dataPacket(packet);
    }

    @Override
    protected void initializeWorldClocks() {
        if (getProtocol() < AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            return;
        }
        SyncWorldClocksPacket12610 packet = new SyncWorldClocksPacket12610();
        packet.action = new InitializeRegistryAction(new InitializeRegistryAction.Entry[]{
                new InitializeRegistryAction.Entry(7194480507151251734L, "minecraft:overworld", level.getTime(), false, TimeMarker.OVERWORLD),
        });
        dataPacket(packet);
    }

    @Override
    public void sendAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(Attribute.getAttribute(Attribute.HEALTH).setMaxValue(this.getMaxHealth()).setValue(health >= 1 ? (health < getMaxHealth() ? health : getMaxHealth()) : 0));
        attributes.add(Attribute.getAttribute(Attribute.ABSORPTION).setValue(absorption));
        attributes.add(Attribute.getAttribute(Attribute.PLAYER_HUNGER).setValue(this.getFoodData().getLevel()));
        attributes.add(Attribute.getAttribute(Attribute.PLAYER_SATURATION).setValue(this.getFoodData().getFoodSaturationLevel()));
        attributes.add(Attribute.getAttribute(Attribute.PLAYER_EXHAUSTION).setValue(this.getFoodData().getExhaustionLevel()));
        attributes.add(movementSpeedAttribute.copy().setValue(movementSpeedAttribute.getModifiedValue()));
        attributes.add(Attribute.getAttribute(Attribute.UNDERWATER_MOVEMENT));
        attributes.add(Attribute.getAttribute(Attribute.LAVA_MOVEMENT));
        attributes.add(Attribute.getAttribute(Attribute.PLAYER_LEVEL).setValue(this.getExperienceLevel()));
        attributes.add(Attribute.getAttribute(Attribute.PLAYER_EXPERIENCE).setValue(Mth.clamp(((float) this.getExperience()) / calculateRequireExperience(this.getExperienceLevel()), 0, 1)));

        if (getProtocol() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            attributes.add(Attribute.getAttribute(Attribute.FRICTION_MODIFIER));
            attributes.add(Attribute.getAttribute(Attribute.BOUNCINESS));
            attributes.add(Attribute.getAttribute(Attribute.AIR_DRAG_MODIFIER));
        }

        this.setAttribute(attributes.toArray(new Attribute[0]));
    }

    private record ShapeInstance(Shape shape, int expirationTick) {
    }
}
