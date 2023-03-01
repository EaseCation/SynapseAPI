package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDragonEgg;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.command.data.CommandPermission;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.level.format.generic.ChunkPacketCache;
import cn.nukkit.level.particle.PunchBlockParticle;
import cn.nukkit.math.*;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.LoginChainData;
import co.aikar.timings.Timings;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.dialogue.NPCDialoguePlayerHandler;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
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
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.ResourcePacksInfoPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.StartGamePacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.StartGamePacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.PlayerStartItemCooldownPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.SpawnParticleEffectPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.StartGamePacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.NetworkChunkPublisherUpdatePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol11830ne.protocol.StartGamePacket11830NE;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.StartGamePacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.ToastRequestPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.StartGamePacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910.AbilityLayer;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAdventureSettingsPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.MapInfoRequestPacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.ModalFormResponsePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.NetworkChunkPublisherUpdatePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.StartGamePacket11920;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.CommandRequestPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.PlayerSkinPacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11960.protocol.StartGamePacket11960;
import org.itxtech.synapseapi.multiprotocol.protocol11963.protocol.PlayerSkinPacket11963;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.itxtech.synapseapi.multiprotocol.utils.ItemComponentDefinitions;
import org.itxtech.synapseapi.utils.BlobTrack;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.SharedConstants.*;
import static cn.nukkit.level.format.generic.ChunkRequestTask.*;
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

    protected final Long2ObjectMap<IntSet> subChunkRequestQueue = new Long2ObjectOpenHashMap<>();
    protected final Long2ObjectMap<IntSet> subChunkSendQueue = new Long2ObjectOpenHashMap<>();

    //TODO: 新版加载屏系统接入跨服
    protected boolean dimensionChanging;
    protected LongSet dimensionWaitingChunks = new LongOpenHashSet();
    protected int dimensionRequestedChunkCount;
    protected int dimensionNeedingChunk;
    protected boolean dimensionBack;
    protected LongSet overworldRequestedChunks = new LongOpenHashSet();
    protected Comparator<LevelChunkPacket> chunkPacketComparator = (packet0, packet1) -> {
        int spawnX = getChunkX();
        int spawnZ = getChunkZ();
        return Integer.compare(distance(spawnX, spawnZ, packet0.chunkX, packet0.chunkZ), distance(spawnX, spawnZ, packet1.chunkX, packet1.chunkZ));
    };
    protected long changeDimensionTimeout = -1;
    protected long changeDimensionAckTimeout = -1;
    protected long changeDimensionBackTimeout = -1;
    protected boolean dimensionNeedBackAck;
    protected boolean changeDimensionImmobile;
    protected Position changeDimensionPosition;

    public NPCDialoguePlayerHandler npcDialoguePlayerHandler;

    private byte skinHack; // 1.19.62

    public SynapsePlayer116100(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, synapseEntry, clientID, socketAddress);
    }

    @Override
    protected DataPacket generateStartGamePacket(Position spawnPosition) {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_119_60.getProtocolStart()) {
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
//            startGamePacket.isMovementServerAuthoritative = true;
//            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
            startGamePacket.isMovementServerAuthoritative = true;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
            startGamePacket.isMovementServerAuthoritative = true;
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking;
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
            startGamePacket.isMovementServerAuthoritative = true;
            // 启用后破坏方块时的物品栏事务就不会塞在PlayerAuthInputPacket了
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking &&
                    //(!this.isNetEaseClient() && this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart());
                    (this.isNetEaseClient() || this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart());
            startGamePacket.currentTick = this.server.getTick();
            startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
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
            startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
            startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.difficulty = this.server.getDifficulty();
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
            startGamePacket.isMovementServerAuthoritative = this.isNetEaseClient;
            startGamePacket.currentTick = this.server.getTick();
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
        startGamePacket.dimension = (byte) (this.level.getDimension().ordinal() & 0xff);
        startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
        startGamePacket.difficulty = this.server.getDifficulty();
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
        startGamePacket.isMovementServerAuthoritative = this.isNetEaseClient;
        startGamePacket.currentTick = this.server.getTick();
        startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
        return startGamePacket;
    }

    /**
     * Returns a client-friendly gamemode of the specified real gamemode This
     * function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that
     * includes Survival, Creative and Adventure)
     * <p>
     * TODO: remove this when Spectator Mode gets added properly to MCPE
     */
    private int getClientFriendlyGamemode(int gamemode) {
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
        if (!this.isSynapseLogin) {
            super.handleDataPacket(packet);
            return;
        }

        packetswitch:
        switch (packet.pid()) {
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
                        if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
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
                ContainerClosePacket116100 containerClosePacket = (ContainerClosePacket116100) packet;
                if (!this.spawned || containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen) {
                    break;
                }
                //this.getServer().getLogger().warning("Got ContainerClosePacket: " + containerClosePacket);

                if (this.windowIndex.containsKey(containerClosePacket.windowId)) {
                    this.server.getPluginManager().callEvent(new InventoryCloseEvent(this.windowIndex.get(containerClosePacket.windowId), this));

                    if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

                    this.closingWindowId = containerClosePacket.windowId;
                    this.removeWindow(this.windowIndex.get(containerClosePacket.windowId), true);
                    this.closingWindowId = Integer.MIN_VALUE;
                } else {
                    this.getServer().getLogger().debug("Unopened window: " + containerClosePacket.windowId);
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
            case ProtocolInfo.PLAYER_INPUT_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                if (!this.isAlive() || !this.spawned) {
                    break;
                }

                PlayerInputPacket ipk = (PlayerInputPacket) packet;
                if (!validateVehicleInput(ipk.motionX) || !validateVehicleInput(ipk.motionY)) {
                    this.getServer().getLogger().warning("Invalid vehicle input received: " + this.getName());
                    this.close("", "Invalid vehicle input");
                    return;
                }

                if (riding instanceof EntityRideable) {
                    ((EntityRideable) riding).onPlayerInput(this, ipk.motionX, ipk.motionY);
                }
                break;
            case ProtocolInfo.MOVE_PLAYER_PACKET:
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

                Vector3 newPos = new Vector3(movePlayerPacket.x, movePlayerPacket.y - this.getEyeHeight(), movePlayerPacket.z);

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

                if (riding != null) {
                    if (riding instanceof EntityRideable && !(this.riding instanceof EntityBoat)) {
                        Vector3f offset = riding.getMountedOffset(this);
                        ((EntityRideable) riding).onPlayerRiding(this.temporalVector.setComponents(movePlayerPacket.x - offset.x, movePlayerPacket.y - offset.y, movePlayerPacket.z - offset.z), (movePlayerPacket.headYaw + 90) % 360, 0);
                    }
                }
                break;
            case ProtocolInfo.TEXT_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                if (!this.spawned || !this.isAlive()) {
                    break;
                }
                if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                    TextPacket116100NE textPacket = (TextPacket116100NE) packet;

                    if (textPacket.type == TextPacket116100NE.TYPE_CHAT) {
                        this.chat(textPacket.message);
                    }
                } else {
                    TextPacket116100 textPacket = (TextPacket116100) packet;

                    if (textPacket.type == TextPacket116100.TYPE_CHAT) {
                        this.chat(textPacket.message);
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
                        && (!this.isNetEaseClient || this.getProtocol() < AbstractProtocol.PROTOCOL_116_200.getProtocolStart())) {
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

                            BlockEntity blockEntity = level.getBlockEntity(blockPos);
                            if (blockEntity instanceof BlockEntitySpawnable) {
                                ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                            }

                            break;
                        }

                        breakingBlock = block;
//                        lastBreak = System.currentTimeMillis();

                        Item item = inventory.getItemInHand();
                        Item oldItem = item.clone();

                        if (!canInteract(blockPos.add(0.5, 0.5, 0.5), 16)
                                || (item = level.useBreakOn(blockPos.asVector3(), face, item, this, true)) == null) {
                            inventory.sendHeldItem(this);

                            level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

                            BlockEntity blockEntity = level.getBlockEntity(blockPos);
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

                        getFoodData().updateFoodExpLevel(0.005);

                        if (!item.equals(oldItem) || item.getCount() != oldItem.getCount()) {
                            inventory.setItemInHand(item);
                            inventory.sendHeldItem(getViewers().values());
                        }

                        this.startAction = -1;
                        this.startActionTimestamp = -1;
                        this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
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
                    Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);

                    actionswitch:
                    switch (playerActionPacket.action) {
                        case PlayerActionPacket119.ACTION_RESPAWN:
                            if (!this.spawned || this.isAlive() || !this.isOnline()) {
                                break;
                            }

                            if (this.server.isHardcore()) {
                                this.setBanned(true);
                                break;
                            }

                            this.craftingType = CRAFTING_SMALL;
                            this.resetCraftingGridType();

                            PlayerRespawnEvent playerRespawnEvent = new PlayerRespawnEvent(this, this.getSpawn());
                            this.server.getPluginManager().callEvent(playerRespawnEvent);

                            Position respawnPos = playerRespawnEvent.getRespawnPosition();

                            this.teleport(respawnPos, null);

                            this.setSprinting(false);
                            this.setSneaking(false);

                            this.setDataProperty(new ShortEntityData(Player.DATA_AIR, 400), false);
                            this.deadTicks = 0;
                            this.noDamageTicks = 60;

                            this.removeAllEffects();

                            SetHealthPacket healthPacket = new SetHealthPacket();
                            healthPacket.health = getMaxHealth();
                            this.dataPacket(healthPacket);

                            this.setHealth(this.getMaxHealth());
                            this.getFoodData().setLevel(20, 20);

                            this.sendData(this);
                            this.sendData(this.getViewers().values().toArray(new Player[0]));

                            this.setMovementSpeed(DEFAULT_SPEED);

                            this.getAdventureSettings().update();
                            this.inventory.sendContents(this);
                            this.inventory.sendArmorContents(this);

                            this.spawnToAll();
                            this.scheduleUpdate();
                            break;
                        case PlayerActionPacket119.ACTION_START_BREAK:
                            if (this.isSpectator()) {
                                break;
                            }
                            long currentBreak = System.currentTimeMillis();
                            BlockVector3 currentBreakPosition = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                            // HACK: Client spams multiple left clicks so we need to skip them.
                            if ((lastBreakPosition.equalsVec(currentBreakPosition) && (currentBreak - this.lastBreak) < 10) || pos.distanceSquared(this) > 100) {
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
                            switch (target.getId()) {
                                case Block.NOTEBLOCK:
                                    ((BlockNoteblock) target).emitSound();
                                    break actionswitch;
                                case Block.DRAGON_EGG:
                                    if (!this.isCreative()) {
                                        ((BlockDragonEgg) target).teleport();
                                        break actionswitch;
                                    }
                                case Block.BLOCK_FRAME:
                                case Block.BLOCK_GLOW_FRAME:
                                    BlockEntity itemFrame = this.level.getBlockEntity(pos);
                                    if (itemFrame instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) itemFrame).dropItem(this)) {
                                        break actionswitch;
                                    }
                            }
                            Block block = target.getSide(face);
                            if (block.isFire()) {
                                this.level.setBlock(block, Block.get(BlockID.AIR), true);
                                this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
                                break;
                            }
                            if (!this.isCreative()) {
                                //improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
                                //Done by lmlstarqaq
                                double breakTime = Mth.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
                                if (breakTime > 0) {
                                    LevelEventPacket pk = new LevelEventPacket();
                                    pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
                                    pk.x = (float) pos.x;
                                    pk.y = (float) pos.y;
                                    pk.z = (float) pos.z;
                                    pk.data = (int) (65535 / breakTime);
                                    this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
                                }
                            }

                            this.breakingBlock = target;
                            this.lastBreak = currentBreak;
                            this.lastBreakPosition = currentBreakPosition;
                            break;
                        case PlayerActionPacket119.ACTION_ABORT_BREAK:
                        case PlayerActionPacket119.ACTION_STOP_BREAK:
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
                        case PlayerActionPacket119.ACTION_GET_UPDATED_BLOCK:
                            break; //TODO
                        case PlayerActionPacket119.ACTION_DROP_ITEM:
                            break; //TODO
                        case PlayerActionPacket119.ACTION_STOP_SLEEPING:
                            this.stopSleep();
                            break;
                        case PlayerActionPacket119.ACTION_JUMP:
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_START_SPRINT:
                            PlayerToggleSprintEvent playerToggleSprintEvent = new PlayerToggleSprintEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleSprintEvent);
                            if (playerToggleSprintEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSprinting(true);
                            }
                            this.formWindows.clear();
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_SPRINT:
                            playerToggleSprintEvent = new PlayerToggleSprintEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleSprintEvent);
                            if (playerToggleSprintEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSprinting(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_START_SNEAK:
                            PlayerToggleSneakEvent playerToggleSneakEvent = new PlayerToggleSneakEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleSneakEvent);
                            if (playerToggleSneakEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setSneaking(true);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_SNEAK:
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
                            break; //TODO
                        case PlayerActionPacket119.ACTION_START_GLIDE:
                            PlayerToggleGlideEvent playerToggleGlideEvent = new PlayerToggleGlideEvent(this, true);
                            this.server.getPluginManager().callEvent(playerToggleGlideEvent);
                            if (playerToggleGlideEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setGliding(true);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_STOP_GLIDE:
                            playerToggleGlideEvent = new PlayerToggleGlideEvent(this, false);
                            this.server.getPluginManager().callEvent(playerToggleGlideEvent);
                            if (playerToggleGlideEvent.isCancelled()) {
                                this.sendData(this);
                            } else {
                                this.setGliding(false);
                            }
                            break packetswitch;
                        case PlayerActionPacket119.ACTION_CONTINUE_BREAK:
                            if (this.isBreakingBlock()) {
                                block = this.level.getBlock(pos, false);
                                face = BlockFace.fromIndex(playerActionPacket.data);
                                this.level.addParticle(new PunchBlockParticle(pos, block, face));
                            }
                            break;
                        case PlayerActionPacket119.ACTION_START_SWIMMING:
                            this.setSwimming(true);
                            break;
                        case PlayerActionPacket119.ACTION_STOP_SWIMMING:
                            this.setSwimming(false);
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

                                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                                if (blockEntity instanceof BlockEntitySpawnable) {
                                    ((BlockEntitySpawnable) blockEntity).spawnTo(this);
                                }

                                break;
                            }

                            breakingBlock = block;
//                            lastBreak = System.currentTimeMillis();

                            Item item = inventory.getItemInHand();
                            Item oldItem = item.clone();

                            if (!canInteract(blockPos.add(0.5, 0.5, 0.5), 16)
                                    || (item = level.useBreakOn(blockPos.asVector3(), face, item, this, true)) == null) {
                                inventory.sendHeldItem(this);

                                level.sendBlocks(new Player[]{this}, new Block[]{block}, UpdateBlockPacket.FLAG_ALL_PRIORITY);

                                BlockEntity blockEntity = level.getBlockEntity(blockPos);
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

                            getFoodData().updateFoodExpLevel(0.005);

                            if (!item.equals(oldItem) || item.getCount() != oldItem.getCount()) {
                                inventory.setItemInHand(item);
                                inventory.sendHeldItem(getViewers().values());
                            }

                            break;
                    }

                    this.startAction = -1;
                    this.startActionTimestamp = -1;
                    this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
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
                        if (this.isSpectator()) {
                            break;
                        }
                        long currentBreak = System.currentTimeMillis();
                        BlockVector3 currentBreakPosition = new BlockVector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                        // HACK: Client spams multiple left clicks so we need to skip them.
                        if ((lastBreakPosition.equalsVec(currentBreakPosition) && (currentBreak - this.lastBreak) < 10) || pos.distanceSquared(this) > 100) {
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
                        switch (target.getId()) {
                            case Block.NOTEBLOCK:
                                ((BlockNoteblock) target).emitSound();
                                break actionswitch;
                            case Block.DRAGON_EGG:
                                if (!this.isCreative()) {
                                    ((BlockDragonEgg) target).teleport();
                                    break actionswitch;
                                }
                            case Block.BLOCK_FRAME:
                            case Block.BLOCK_GLOW_FRAME:
                                BlockEntity itemFrame = this.level.getBlockEntity(pos);
                                if (itemFrame instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) itemFrame).dropItem(this)) {
                                    break actionswitch;
                                }
                        }
                        Block block = target.getSide(face);
                        if (block.isFire()) {
                            this.level.setBlock(block, Block.get(BlockID.AIR), true);
                            this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
                            break;
                        }
                        if (!this.isCreative()) {
                            //improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
                            //Done by lmlstarqaq
                            double breakTime = Mth.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
                            if (breakTime > 0) {
                                LevelEventPacket pk = new LevelEventPacket();
                                pk.evid = LevelEventPacket.EVENT_BLOCK_START_BREAK;
                                pk.x = (float) pos.x;
                                pk.y = (float) pos.y;
                                pk.z = (float) pos.z;
                                pk.data = (int) (65535 / breakTime);
                                this.getLevel().addChunkPacket(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, pk);
                            }
                        }

                        this.breakingBlock = target;
                        this.lastBreak = currentBreak;
                        this.lastBreakPosition = currentBreakPosition;

                        this.startAction = -1;
                        this.startActionTimestamp = -1;
                        this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
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

                if (this.getProtocol() < AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
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

                if (formWindows.containsKey(modalFormPacket.formId)) {
                    FormWindow window = formWindows.get(modalFormPacket.formId);
                    window.setResponse(modalFormPacket.data.trim());

                    PlayerFormRespondedEvent event = new PlayerFormRespondedEvent(this, modalFormPacket.formId, window);
                    getServer().getPluginManager().callEvent(event);

                    formWindows.remove(modalFormPacket.formId);
                } else if (serverSettings.containsKey(modalFormPacket.formId)) {
                    FormWindow window = serverSettings.get(modalFormPacket.formId);
                    window.setResponse(modalFormPacket.data.trim());

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
                        this.kick(PlayerKickEvent.Reason.FLYING_DISABLED, "Flying is not enabled on this server");
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
                Entity entity = this.getLevel().getEntity(npcRequestPacket.entityRuntimeId);
                if (entity == null) {
                    break;  // 世界中不存在这个实体
                }
                String sceneName = npcRequestPacket.sceneName;
                if (npcRequestPacket.type == NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION) {
                    this.getNpcDialoguePlayerHandler().onDialogueResponse(sceneName, npcRequestPacket.actionIndex);
                }
                break;
            case ProtocolInfo.COMMAND_REQUEST_PACKET:
                if (getProtocol() < AbstractProtocol.PROTOCOL_119_60.getProtocolStart()) {
                    super.handleDataPacket(packet);
                    break;
                }

                if (!this.spawned || !this.isAlive()) {
                    break;
                }
                this.craftingType = CRAFTING_SMALL;

                CommandRequestPacket11960 commandRequestPacket = (CommandRequestPacket11960) packet;
                PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(this, commandRequestPacket.command);
                playerCommandPreprocessEvent.call();
                if (playerCommandPreprocessEvent.isCancelled()) {
                    break;
                }

                Timings.playerCommandTimer.startTiming();
                this.server.dispatchCommand(playerCommandPreprocessEvent.getPlayer(), playerCommandPreprocessEvent.getMessage().substring(1));
                Timings.playerCommandTimer.stopTiming();
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
        if (this.protocol >= AbstractProtocol.PROTOCOL_117_10.getProtocolStart()) {
            ResourcePacksInfoPacket11710 resourcePacket = new ResourcePacksInfoPacket11710();
            resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
            resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
            resourcePacket.mustAccept = this.forceResources;
//            resourcePacket.forceServerPacks = this.forceResources;
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

    @Override
    protected void initClientBlobCache() {
        if (!this.isNetEaseClient()
                /*&& this.protocol < AbstractProtocol.PROTOCOL_118.getProtocolStart()*/
        ) {
            super.initClientBlobCache();
        }
    }

    @Override
    public void playAnimation(String animation, long entityRuntimeId) {
        if (this.protocol >= AbstractProtocol.PROTOCOL_117_30.getProtocolStart()) {
            AnimateEntityPacket11730 pk = new AnimateEntityPacket11730();
            pk.entityRuntimeIds = new long[]{entityRuntimeId};
            pk.animation = animation;
            this.dataPacket(pk);
        } else {
            AnimateEntityPacket116100 pk = new AnimateEntityPacket116100();
            pk.entityRuntimeIds = new long[]{entityRuntimeId};
            pk.animation = animation;
            this.dataPacket(pk);
        }
    }

    @Override
    protected boolean isExtendedLevel() {
        return this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart();
    }

    @Override
    public boolean isSubChunkRequestAvailable() {
        return USE_SUB_CHUNK_REQUEST && this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart() && !this.isNetEaseClient();
    }

    @Override
    public void sendChunk(int x, int z, int subChunkCount, ChunkBlobCache blobCache, DataPacket packet) {
        if (!this.isSubChunkRequestAvailable() || blobCache == null) {
            super.sendChunk(x, z, subChunkCount, blobCache, packet);
            return;
        }
        if (!this.connected) {
            return;
        }
        this.noticeChunkPublisherUpdate();
        long chunkHash = Level.chunkHash(x, z);
        this.usedChunks.put(chunkHash, true);
        this.chunkLoadCount++;
        boolean centerChunk = CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;

        if (this.isBlobCacheAvailable() && this.isSubModeLevelChunkBlobCacheEnabled() && !centerChunk) {
            long[] ids;
            Long2ObjectMap<byte[]> extendedClientBlobs;
            if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
                ids = blobCache.getExtendedBlobIdsNew();
                extendedClientBlobs = blobCache.getExtendedClientBlobsNew();
            } else {
                ids = blobCache.getExtendedBlobIds();
                extendedClientBlobs = blobCache.getExtendedClientBlobs();
            }
            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, extendedClientBlobs.get(hash)));

            LevelChunkPacket pk = new LevelChunkPacket();
            pk.chunkX = x;
            pk.chunkZ = z;
            if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            } else {
                pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
            }
            pk.subChunkRequestLimit = subChunkCount /*+ PADDING_SUB_CHUNK_COUNT*/;
            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubModeCachedPayload();
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
                if (this != entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this);
                }
            }
        }
    }

    @Override
    public void sendChunk(int x, int z, int subChunkCount, ChunkBlobCache blobCache, byte[] payload, byte[] subModePayload) {
        if (!this.isSubChunkRequestAvailable() || blobCache == null || this.getChunkX() == x && this.getChunkZ() == z) {
            super.sendChunk(x, z, subChunkCount, blobCache, payload, subModePayload);
            return;
        }
        if (!this.connected) {
            return;
        }
        this.noticeChunkPublisherUpdate();
        long chunkHash = Level.chunkHash(x, z);
        this.usedChunks.put(chunkHash, true);
        this.chunkLoadCount++;
        boolean centerChunk = CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;

        LevelChunkPacket pk = new LevelChunkPacket();
        pk.chunkX = x;
        pk.chunkZ = z;
        if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
        } else {
            pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
        }
        pk.subChunkRequestLimit = subChunkCount /*+ PADDING_SUB_CHUNK_COUNT*/;
        if (this.isBlobCacheAvailable() && this.isSubModeLevelChunkBlobCacheEnabled() && !centerChunk) {
            long[] ids;
            Long2ObjectMap<byte[]> extendedClientBlobs;
            if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
                ids = blobCache.getExtendedBlobIdsNew();
                extendedClientBlobs = blobCache.getExtendedClientBlobsNew();
            } else {
                ids = blobCache.getExtendedBlobIds();
                extendedClientBlobs = blobCache.getExtendedClientBlobs();
            }
            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, extendedClientBlobs.get(hash)));

            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubModeCachedPayload();
        } else {
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
                if (this != entity && !entity.closed && entity.isAlive()) {
                    entity.spawnTo(this);
                }
            }
        }
    }

    @Override
    public void sendSubChunks(int dimension, int x, int z, int subChunkCount, ChunkBlobCache blobCache, Map<StaticVersion, byte[][]> payload, byte[] heightMapType, byte[][] heightMap) {
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

        boolean centerChunk = CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;
        if (subChunkCount != 0) {
            subChunkCount += PADDING_SUB_CHUNK_COUNT;
        }
        byte[][] payloads = payload.get(StaticVersion.fromProtocol(this.protocol, this.isNetEaseClient()));

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = y + PADDING_SUB_CHUNK_COUNT;

            SubChunkPacket pk = this.createSubChunkPacket();
//            pk.dimension = dimension;
            pk.dimension = Level.DIMENSION_OVERWORLD;
            pk.subChunkX = x;
            pk.subChunkY = y;
            pk.subChunkZ = z;

            if (subChunkCount == 0) {
                pk.data = PADDING_SUB_CHUNK_BLOB;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = EMPTY_SUBCHUNK_HEIGHTMAP;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                }
                this.dataPacket(pk);
                iter.remove();
                continue;
            } else if (index >= subChunkCount) {
                pk.data = PADDING_SUB_CHUNK_BLOB;
                pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                }
                this.dataPacket(pk);
                iter.remove();
                continue;
            }

            if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart() && (y < 0 || blobCache != null && (y >= blobCache.getEmptySection().length || blobCache.getEmptySection()[y]))) {
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
            } else if (blobCache != null && this.isBlobCacheAvailable() && this.isSubChunkBlobCacheEnabled() && !centerChunk) {
                long[] ids;
                Long2ObjectMap<byte[]> extendedClientBlobs;
                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
                    ids = blobCache.getExtendedBlobIdsNew();
                    extendedClientBlobs = blobCache.getExtendedClientBlobsNew();
                } else {
                    ids = blobCache.getExtendedBlobIds();
                    extendedClientBlobs = blobCache.getExtendedClientBlobs();
                }
                long hash = ids[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, extendedClientBlobs.get(hash)));

                pk.data = blobCache.getSubChunkCachedPayload()[index];
                pk.blobId = hash;
                pk.cacheEnabled = true;
            } else {
                pk.data = payloads[index];
            }

            pk.heightMapType = heightMapType[index];
            pk.heightMap = heightMap[index];
            this.dataPacket(pk);
            iter.remove();
        }
    }

    @Override
    public void sendSubChunks(int dimension, int x, int z, int subChunkCount, ChunkBlobCache blobCache, ChunkPacketCache packetCache, byte[] heightMapType, byte[][] heightMap) {
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

        boolean centerChunk = CENTER_CHUNK_WITHOUT_CACHE && this.getChunkX() == x && this.getChunkZ() == z;
        if (subChunkCount != 0) {
            subChunkCount += PADDING_SUB_CHUNK_COUNT;
        }
        BatchPacket[] packets = packetCache.getSubPackets(StaticVersion.fromProtocol(this.protocol, this.isNetEaseClient()));

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = y + PADDING_SUB_CHUNK_COUNT;

            if (subChunkCount == 0) {
                SubChunkPacket pk = this.createSubChunkPacket();
//                pk.dimension = dimension;
                pk.dimension = Level.DIMENSION_OVERWORLD;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = PADDING_SUB_CHUNK_BLOB;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = EMPTY_SUBCHUNK_HEIGHTMAP;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                }
                this.dataPacket(pk);
                iter.remove();
                continue;
            } else if (index >= subChunkCount) {
                SubChunkPacket pk = this.createSubChunkPacket();
//                pk.dimension = dimension;
                pk.dimension = Level.DIMENSION_OVERWORLD;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = PADDING_SUB_CHUNK_BLOB;
                pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                }
                this.dataPacket(pk);
                iter.remove();
                continue;
            }

            if (ENABLE_EMPTY_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart() && (y < 0 || blobCache != null && (y >= blobCache.getEmptySection().length || blobCache.getEmptySection()[y]))) {
                SubChunkPacket pk = this.createSubChunkPacket();
//                pk.dimension = dimension;
                pk.dimension = Level.DIMENSION_OVERWORLD;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                pk.heightMapType = heightMapType[index];
                pk.heightMap = heightMap[index];
                this.dataPacket(pk);
            } else if (blobCache != null && this.isBlobCacheAvailable() && this.isSubChunkBlobCacheEnabled() && !centerChunk) {
                long[] ids;
                Long2ObjectMap<byte[]> extendedClientBlobs;
                if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
                    ids = blobCache.getExtendedBlobIdsNew();
                    extendedClientBlobs = blobCache.getExtendedClientBlobsNew();
                } else {
                    ids = blobCache.getExtendedBlobIds();
                    extendedClientBlobs = blobCache.getExtendedClientBlobs();
                }
                long hash = ids[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, extendedClientBlobs.get(hash)));

                SubChunkPacket pk = this.createSubChunkPacket();
//                pk.dimension = dimension;
                pk.dimension = Level.DIMENSION_OVERWORLD;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = blobCache.getSubChunkCachedPayload()[index];
                pk.blobId = hash;
                pk.cacheEnabled = true;
                pk.heightMapType = heightMapType[index];
                pk.heightMap = heightMap[index];
                this.dataPacket(pk);
            } else {
                this.dataPacket(packets[index]);
            }

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
                pk.dimension = Level.DIMENSION_OVERWORLD;
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
        Timings.playerChunkSendTimer.startTiming();
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

            if (this.level.requestSubChunks(chunkX, chunkZ, this)) {
                IntSet requests = this.subChunkSendQueue.get(index);
                if (requests == null) {
                    requests = newRequests;
                    this.subChunkSendQueue.put(index, requests);
                } else {
                    requests.addAll(newRequests);
                }

                ++count;
            } else {
                IntIterator yIter = newRequests.iterator();
                while (yIter.hasNext()) {
                    int chunkY = yIter.nextInt();

                    SubChunkPacket pk = this.createSubChunkPacket();
                    pk.dimension = Level.DIMENSION_OVERWORLD;
                    pk.subChunkX = chunkX;
                    pk.subChunkY = chunkY;
                    pk.subChunkZ = chunkZ;
                    pk.requestResult = SubChunkPacket.REQUEST_RESULT_PLAYER_NOT_FOUND;
                    this.dataPacket(pk);

                    yIter.remove();
                }
            }
            iter.remove();
        }
        Timings.playerChunkSendTimer.stopTiming();
    }

    @Override
    protected boolean canSendQueuedChunk() {
        return this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart() || super.canSendQueuedChunk();
    }

    @Override
    public boolean isBlobCacheDisabled() {
        // client performance is very bad in 1.18.0, Microjang messed up everything :(
        return this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart();
    }

    public boolean isSubChunkBlobCacheEnabled() {
        return true;
    }

    public boolean isSubModeLevelChunkBlobCacheEnabled() {
        return false; //FIXME: crash 1.18.0
    }

    protected boolean handleSubChunkRequest(int dimension, int subChunkX, int subChunkY, int subChunkZ) {
        if (subChunkY < -4 || subChunkY > 19) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_Y_INDEX_OUT_OF_BOUNDS;
            this.dataPacket(pk);
            return false;
        }
        if (dimension == Level.DIMENSION_NETHER || dimension == Level.DIMENSION_THE_END) {
            if (this.dimensionChanging && !this.dimensionBack && this.dimensionWaitingChunks.remove(Level.chunkHash(subChunkX, subChunkZ)) && ++this.dimensionRequestedChunkCount >= this.dimensionNeedingChunk) {
                this.changeDimensionTimeout = -1;
                this.changeDimensionToOverworld();
            }
            if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = subChunkX;
                pk.subChunkY = subChunkY;
                pk.subChunkZ = subChunkZ;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS_ALL_AIR;
                this.dataPacket(pk);
            } else /*if (this.dimensionChanging)*/ {
                SubChunkPacket pk = this.createSubChunkPacket();
                pk.dimension = dimension;
                pk.subChunkX = subChunkX;
                pk.subChunkY = subChunkY;
                pk.subChunkZ = subChunkZ;
                pk.requestResult = SubChunkPacket.REQUEST_RESULT_SUCCESS;
                if (pk.subChunkY == -4) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = EMPTY_SUBCHUNK_HEIGHTMAP;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                boolean centerChunk = this.getChunkX() == subChunkX && this.getChunkZ() == subChunkZ;
                if (false && this.isBlobCacheAvailable() && !centerChunk) {
                    this.clientCacheTrack.put(PADDING_SUB_CHUNK_HASH, new BlobTrack(PADDING_SUB_CHUNK_HASH, PADDING_SUB_CHUNK_BLOB));
                    pk.cacheEnabled = true;
                    pk.blobId = PADDING_SUB_CHUNK_HASH;
                } else {
                    pk.data = PADDING_SUB_CHUNK_BLOB;
                }
                this.dataPacket(pk);
            }
            return false;
        }
        if (dimension != Level.DIMENSION_OVERWORLD) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_WRONG_DIMENSION;
            this.dataPacket(pk);
            return false;
        }
        if (this.dimensionChanging) {
            long index = Level.chunkHash(subChunkX, subChunkZ);
            if (this.overworldRequestedChunks.add(index) && this.overworldRequestedChunks.size() >= this.dimensionNeedingChunk) {
                this.dimensionChanging = false;
            }
        }
        if (subChunkY > 0xf) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.data = PADDING_SUB_CHUNK_BLOB; // higher padding
            pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
            this.dataPacket(pk);
            return false;
        }
        if (this.getLoaderId() <= 0) {
            SubChunkPacket pk = this.createSubChunkPacket();
            pk.dimension = dimension;
            pk.subChunkX = subChunkX;
            pk.subChunkY = subChunkY;
            pk.subChunkZ = subChunkZ;
            pk.requestResult = SubChunkPacket.REQUEST_RESULT_PLAYER_NOT_FOUND;
            this.dataPacket(pk);
            return false;
        }

        long index = Level.chunkHash(subChunkX, subChunkZ);
        if (this.subChunkSendQueue.containsKey(index)) {
            this.subChunkSendQueue.get(index).add(subChunkY);
            return true;
        }

        IntSet requests = this.subChunkRequestQueue.get(index);
        if (requests == null) {
            requests = new IntOpenHashSet();
            this.subChunkRequestQueue.put(index, requests);
        }
        requests.add(subChunkY);
        return true;
    }

    protected SubChunkPacket createSubChunkPacket() {
        return this.protocol < AbstractProtocol.PROTOCOL_118_10.getProtocolStart() ? new SubChunkPacket() : new SubChunkPacket11810();
    }

    public boolean isDimensionChangingSystemEnabled() {
        return USE_CHANGE_DIMENSION_PACKET && this.isSubChunkRequestAvailable();
    }

    @Override
    protected boolean onLevelSwitch() {
        if (this.isDimensionChangingSystemEnabled() && !this.dimensionChanging) {
            this.dimensionChanging = true;
//            this.dimensionBack = false;
            this.dimensionRequestedChunkCount = 0;
            this.dimensionNeedingChunk = Math.min(chunkRadius * chunkRadius, 20);

            ChangeDimensionPacket pk0 = new ChangeDimensionPacket();
            pk0.dimension = Level.DIMENSION_NETHER;
            pk0.x = (float) this.x;
            pk0.y = (float) this.y + this.getEyeHeight();
            pk0.z = (float) this.z;
//            pk0.respawn = true;
            this.dataPacket(pk0);

            int centerChunkX = this.getChunkX();
            int centerChunkZ = this.getChunkZ();
            Vector2 chunkPos2d = new Vector2(centerChunkX, centerChunkZ);
            List<LevelChunkPacket> packets = new ObjectArrayList<>();
            int count = 0;
            for (int x = -chunkRadius; x < chunkRadius; x++) {
                for (int z = -chunkRadius; z < chunkRadius; z++) {
                    int chunkX = centerChunkX + x;
                    int chunkZ = centerChunkZ + z;
                    if (chunkPos2d.distance(chunkX, chunkZ) > chunkRadius) {
                        continue; // round
                    }
                    ++count;
                    boolean centerChunk = centerChunkX == chunkX && centerChunkZ == chunkZ;

                    LevelChunkPacket packet = new LevelChunkPacket();
                    packet.chunkX = chunkX;
                    packet.chunkZ = chunkZ;
//                    packet.subChunkCount = 0;
                    if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                        packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
                    } else {
                        packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
                    }
                    packet.subChunkRequestLimit = 0;
                    if (false && this.isBlobCacheAvailable() && !centerChunk) {
                        this.clientCacheTrack.put(MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(MINIMIZE_BIOME_PALETTES_HASH, MINIMIZE_BIOME_PALETTES));
                        packet.cacheEnabled = true;
                        packet.blobIds = new long[]{MINIMIZE_BIOME_PALETTES_HASH};
                        packet.data = new byte[1]; // borderBlocks
                    } else {
                        packet.data = MINIMIZE_CHUNK_DATA_NO_CACHE;
                    }
                    packets.add(packet);

                    this.dimensionWaitingChunks.add(Level.chunkHash(chunkX, chunkZ));
                }
            }
            this.dimensionNeedingChunk = Math.min(count, this.dimensionNeedingChunk);
            packets.sort(this.chunkPacketComparator);
            packets.forEach(packet -> {
                this.noticeChunkPublisherUpdate();
                this.dataPacket(packet);
            });

            /*RespawnPacket pkk0 = new RespawnPacket();
            pkk0.x = pos.x;
            pkk0.y = pos.y;
            pkk0.z = pos.z;
            pkk0.runtimeEntityId = this.getId();
            pkk0.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN;
            this.dataPacket(pkk0);

            RespawnPacket pkk1 = new RespawnPacket();
            pkk1.x = pos.x;
            pkk1.y = pos.y;
            pkk1.z = pos.z;
            pkk1.runtimeEntityId = this.getId();
            pkk1.respawnState = RespawnPacket.STATE_CLIENT_READY_TO_SPAWN;
            this.dataPacket(pkk1);

            SetHealthPacket pk = new SetHealthPacket();
            pk.health = 20;
            this.dataPacket(pk);*/

            this.sendPosition(this, this.yaw, this.pitch, MovePlayerPacket.MODE_RESET);

            if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                ackPacket.entityId = getId();
                ackPacket.x = getFloorX();
                ackPacket.y = getFloorY();
                ackPacket.z = getFloorZ();
                ackPacket.resultX = ackPacket.x;
                ackPacket.resultY = ackPacket.y;
                ackPacket.resultZ = ackPacket.z;
                dataPacket(ackPacket);
            }

//            this.setImmobile(true);
            this.changeDimensionImmobile = true;
            this.changeDimensionPosition = this.getPosition();

            long timeout = System.currentTimeMillis() + 7 * 1000;
            this.changeDimensionTimeout = timeout;
            this.changeDimensionAckTimeout = timeout;
            return true;
        }
        return false;
    }

    protected void changeDimensionToOverworld() {
        this.dimensionWaitingChunks.clear();
        this.dimensionRequestedChunkCount = 0;
        this.dimensionBack = true;
        this.dimensionNeedBackAck = true;

        ChangeDimensionPacket pk1 = new ChangeDimensionPacket();
        pk1.dimension = Level.DIMENSION_OVERWORLD;
        pk1.x = (float) this.x;
        pk1.y = (float) this.y + this.getEyeHeight();
        pk1.z = (float) this.z;
//        pk1.respawn = true;
        this.dataPacket(pk1);

        int centerChunkX = this.getChunkX();
        int centerChunkZ = this.getChunkZ();
        Vector2 chunkPos2d = new Vector2(centerChunkX, centerChunkZ);
        List<LevelChunkPacket> packets = new ObjectArrayList<>();
        for (int x = -chunkRadius; x < chunkRadius; x++) {
            for (int z = -chunkRadius; z < chunkRadius; z++) {
                int chunkX = centerChunkX + x;
                int chunkZ = centerChunkZ + z;
                if (chunkPos2d.distance(chunkX, chunkZ) > chunkRadius) {
                    continue; // round
                }
                boolean centerChunk = centerChunkX == chunkX && centerChunkZ == chunkZ;
                if (centerChunk) {
                    continue;
                }

                LevelChunkPacket packet = new LevelChunkPacket();
                packet.chunkX = chunkX;
                packet.chunkZ = chunkZ;
//                packet.subChunkCount = 0;
                if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                    packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
                } else {
                    packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
                }
                packet.subChunkRequestLimit = 0;
                if (false && this.isBlobCacheAvailable() && !centerChunk) {
                    this.clientCacheTrack.put(MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(MINIMIZE_BIOME_PALETTES_HASH, MINIMIZE_BIOME_PALETTES));
                    packet.cacheEnabled = true;
                    packet.blobIds = new long[]{MINIMIZE_BIOME_PALETTES_HASH};
                    packet.data = new byte[1]; // borderBlocks
                } else {
                    packet.data = MINIMIZE_CHUNK_DATA_NO_CACHE;
                }
                packets.add(packet);

//                this.dimensionWaitingChunks.add(Level.chunkHash(chunkX, chunkZ));
            }
        }
        packets.sort(this.chunkPacketComparator);
        packets.forEach(packet -> {
            this.noticeChunkPublisherUpdate();
            this.dataPacket(packet);
        });

        /*RespawnPacket pkk0 = new RespawnPacket();
        pkk0.x = pos.x;
        pkk0.y = pos.y;
        pkk0.z = pos.z;
        pkk0.runtimeEntityId = this.getId();
        pkk0.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN;
        this.dataPacket(pkk0);

        RespawnPacket pkk1 = new RespawnPacket();
        pkk1.x = pos.x;
        pkk1.y = pos.y;
        pkk1.z = pos.z;
        pkk1.runtimeEntityId = this.getId();
        pkk1.respawnState = RespawnPacket.STATE_CLIENT_READY_TO_SPAWN;
        this.dataPacket(pkk1);

        SetHealthPacket pk = new SetHealthPacket();
        pk.health = 20;
        this.dataPacket(pk);*/

        this.sendPosition(this, this.yaw, this.pitch, MovePlayerPacket.MODE_RESET);

        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
            ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
            ackPacket.entityId = getId();
            ackPacket.x = getFloorX();
            ackPacket.y = getFloorY();
            ackPacket.z = getFloorZ();
            ackPacket.resultX = ackPacket.x;
            ackPacket.resultY = ackPacket.y;
            ackPacket.resultZ = ackPacket.z;
            dataPacket(ackPacket);
        }

        this.removeAllChunks();
        this.usedChunks = new Long2BooleanOpenHashMap();

        this.changeDimensionPosition = null;
        this.changeDimensionBackTimeout = System.currentTimeMillis() + 15 * 1000;
    }

    @Override
    protected void onDimensionChangeSuccess() {
        this.changeDimensionAckTimeout = -1;
        if (this.dimensionBack) {
            this.dimensionBack = false;
            this.changeDimensionBackTimeout = -1;

            if (this.changeDimensionImmobile) {
                this.changeDimensionImmobile = false;
//                this.setImmobile(false);
            }
        }
//        log.info("dimension change success: {}", this);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        int tickDiff = currentTick - this.lastUpdate;
        if (tickDiff > 0) {
            this.updateSynapsePlayerTiming.startTiming();
            if (this.changeDimensionPosition != null && currentTick % 10 == 0) {
//                this.sendPosition(this.changeDimensionPosition, this.yaw, this.pitch, MovePlayerPacket.MODE_TELEPORT);
            }

            long time = System.currentTimeMillis();

            if (this.changeDimensionBackTimeout != -1 && time > this.changeDimensionBackTimeout) {
//                this.changeDimensionTimeout = -1;
//                this.changeDimensionToOverworld();
            }

            if (this.changeDimensionTimeout != -1 && time > this.changeDimensionTimeout) {
                this.changeDimensionTimeout = -1;
                this.changeDimensionToOverworld();
            }

            if (this.changeDimensionAckTimeout != -1 && time > this.changeDimensionAckTimeout) {
                this.changeDimensionAckTimeout = time + 7 * 1000;

                int centerChunkX = this.getChunkX();
                int centerChunkZ = this.getChunkZ();
                Vector2 chunkPos2d = new Vector2(centerChunkX, centerChunkZ);
                List<LevelChunkPacket> packets = new ObjectArrayList<>();
                for (int x = -chunkRadius; x < chunkRadius; x++) {
                    for (int z = -chunkRadius; z < chunkRadius; z++) {
                        int chunkX = centerChunkX + x;
                        int chunkZ = centerChunkZ + z;
                        if (chunkPos2d.distance(chunkX, chunkZ) > chunkRadius) {
                            continue; // round
                        }
                        boolean centerChunk = centerChunkX == chunkX && centerChunkZ == chunkZ;
                        if (centerChunk) {
                            continue;
                        }

                        LevelChunkPacket packet = new LevelChunkPacket();
                        packet.chunkX = chunkX;
                        packet.chunkZ = chunkZ;
//                        packet.subChunkCount = 0;
                        if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                            packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
                        } else {
                            packet.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
                        }
                        packet.subChunkRequestLimit = 0;
                        if (false && this.isBlobCacheAvailable() && !centerChunk) {
                            this.clientCacheTrack.put(MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(MINIMIZE_BIOME_PALETTES_HASH, MINIMIZE_BIOME_PALETTES));
                            packet.cacheEnabled = true;
                            packet.blobIds = new long[]{MINIMIZE_BIOME_PALETTES_HASH};
                            packet.data = new byte[1]; // borderBlocks
                        } else {
                            packet.data = MINIMIZE_CHUNK_DATA_NO_CACHE;
                        }
                        packets.add(packet);

//                        this.dimensionWaitingChunks.add(Level.chunkHash(chunkX, chunkZ));
                    }
                }
                packets.sort(this.chunkPacketComparator);
                packets.forEach(packet -> {
                    this.noticeChunkPublisherUpdate();
                    this.dataPacket(packet);
                });

                this.sendPosition(this, this.yaw, this.pitch, MovePlayerPacket.MODE_RESET);

                this.removeAllChunks();
                this.usedChunks = new Long2BooleanOpenHashMap();
            }
            this.updateSynapsePlayerTiming.stopTiming();
        }
        return super.onUpdate(currentTick);
    }

    @Override
    public void spawnParticleEffect(Vector3f position, String identifier, long entityUniqueId, int dimension, String molangVariables) {
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_118_30.getProtocolStart()) {
            super.spawnParticleEffect(position, identifier, entityUniqueId, dimension, null);
            return;
        }

        SpawnParticleEffectPacket11830 packet = new SpawnParticleEffectPacket11830();
        packet.position = position;
        packet.identifier = identifier;
        packet.uniqueEntityId = entityUniqueId;
        packet.dimension = dimension;
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
    public void sendItemComponents() {
        ItemComponentPacket116100 pk = new ItemComponentPacket116100();
        pk.entries = ItemComponentDefinitions.get(AbstractProtocol.fromRealProtocol(protocol), isNetEaseClient()).entrySet().stream()
                .map(entry -> new ItemComponentPacket116100.Entry(entry.getKey(), entry.getValue()))
                .toArray(ItemComponentPacket116100.Entry[]::new);
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
        if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
            TextPacket116100NE pk = new TextPacket116100NE();
            pk.type = TextPacket116100NE.TYPE_JUKEBOX_POPUP;
            pk.isLocalized = true;
            pk.message = message.getText();
            pk.parameters = message.getParameters();
            this.dataPacket(pk);
            return;
        }

        TextPacket116100 pk = new TextPacket116100();
        pk.type = TextPacket116100.TYPE_JUKEBOX_POPUP;
        pk.isLocalized = true;
        pk.message = message.getText();
        pk.parameters = message.getParameters();
        this.dataPacket(pk);
    }

    private static int distance(int centerX, int centerZ, int x, int z) {
        int dx = centerX - x;
        int dz = centerZ - z;
        return dx * dx + dz * dz;
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

        Map<PlayerAbility, Boolean> baseAbilities = new EnumMap<>(PlayerAbility.class);
        baseAbilities.put(PlayerAbility.BUILD, settings.get(Type.BUILD));
        baseAbilities.put(PlayerAbility.MINE, settings.get(Type.MINE));
        baseAbilities.put(PlayerAbility.DOORS_AND_SWITCHES, settings.get(Type.DOORS_AND_SWITCHED));
        baseAbilities.put(PlayerAbility.OPEN_CONTAINERS, settings.get(Type.OPEN_CONTAINERS));
        baseAbilities.put(PlayerAbility.ATTACK_PLAYERS, settings.get(Type.ATTACK_PLAYERS));
        baseAbilities.put(PlayerAbility.ATTACK_MOBS, settings.get(Type.ATTACK_MOBS));
        baseAbilities.put(PlayerAbility.OPERATOR_COMMANDS, settings.get(Type.OPERATOR));
        baseAbilities.put(PlayerAbility.TELEPORT, settings.get(Type.TELEPORT));
        baseAbilities.put(PlayerAbility.INVULNERABLE, settings.get(Type.INVULNERABLE));
        baseAbilities.put(PlayerAbility.FLYING, settings.get(Type.FLYING));
        baseAbilities.put(PlayerAbility.MAY_FLY, settings.get(Type.ALLOW_FLIGHT));
        baseAbilities.put(PlayerAbility.INSTABUILD, settings.get(Type.INSTABUILD));
        baseAbilities.put(PlayerAbility.LIGHTNING, settings.get(Type.LIGHTNING));
        baseAbilities.put(PlayerAbility.MUTED, settings.get(Type.MUTED));
        baseAbilities.put(PlayerAbility.WORLD_BUILDER, settings.get(Type.WORLD_BUILDER));
        baseAbilities.put(PlayerAbility.NO_CLIP, settings.get(Type.NO_CLIP));

        baseAbilities.put(PlayerAbility.FLY_SPEED, false);
        baseAbilities.put(PlayerAbility.WALK_SPEED, false);

        UpdateAbilitiesPacket11910 packet = new UpdateAbilitiesPacket11910();
        packet.entityUniqueId = player.getId();
        boolean isOp = player.isOp();
        packet.playerPermission = isOp && !player.isSpectator() ? Player.PERMISSION_OPERATOR : Player.PERMISSION_MEMBER;
        packet.commandPermission = isOp ? CommandPermission.GAME_DIRECTORS : CommandPermission.ALL;
        packet.abilityLayers = new AbilityLayer[]{
                new AbilityLayer(UpdateAbilitiesPacket11910.LAYER_BASE, baseAbilities, 0.05f, 0.1f),
        };
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
    public void setDimension(int dimension) {
        super.setDimension(dimension);

        if (getProtocol() < AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
            return;
        }

        PlayerActionPacket119 packet = new PlayerActionPacket119();
        packet.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
        packet.entityId = getId();
        packet.x = getFloorX();
        packet.y = getFloorY();
        packet.z = getFloorZ();
        packet.resultX = packet.x;
        packet.resultY = packet.y;
        packet.resultZ = packet.z;
        dataPacket(packet);
    }

    //FIXME: 以下断言错误需要处理
    // 跨服时触发
    // Assertion failed: Biome already has initialized Entity!
    //  Condition is false: !mEntity.hasValue()
    //  Function: Biome::initEntity in .\src\common\world\level\biome\Biome.cpp @ 107
}
