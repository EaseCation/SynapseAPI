package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDragonEgg;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.level.GlobalBlockPaletteInterface.StaticVersion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.generic.ChunkBlobCache;
import cn.nukkit.level.format.generic.ChunkPacketCache;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.ChangeDimensionPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackDataInfoPacket;
import cn.nukkit.network.protocol.SubChunkPacket;
import cn.nukkit.network.protocol.SubChunkPacket11810;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.resourcepacks.ResourcePack;
import co.aikar.timings.Timings;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.AnimateEntityPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ContainerClosePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ResourcePackStackPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.StartGamePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.TextPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.StartGamePacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.FilterTextPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.ResourcePacksInfoPacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.StartGamePacket116200;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.StartGamePacket117;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.ResourcePacksInfoPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.StartGamePacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.StartGamePacket118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.SubChunkRequestPacket118;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.itxtech.synapseapi.utils.BlobTrack;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.nukkit.NukkitSharedConstants.*;
import static org.itxtech.synapseapi.SynapseSharedConstants.*;

//TODO: 这个类已经用了好几个版本了 有时间可以整理一下 像以前那样分成不同版本
@Log4j2
public class SynapsePlayer116100 extends SynapsePlayer116 {

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

    public SynapsePlayer116100(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, synapseEntry, clientID, socketAddress);
    }

    @Override
    protected DataPacket generateStartGamePacket(Position spawnPosition) {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
            StartGamePacket118 startGamePacket = new StartGamePacket118();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = Long.MAX_VALUE;
            startGamePacket.entityRuntimeId = Long.MAX_VALUE;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking = true;
            startGamePacket.currentTick = this.server.getTick();
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_117_30.getProtocolStart()) {
            StartGamePacket11730 startGamePacket = new StartGamePacket11730();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = Long.MAX_VALUE;
            startGamePacket.entityRuntimeId = Long.MAX_VALUE;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking = true;
            startGamePacket.currentTick = this.server.getTick();
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_117.getProtocolStart()) {
            StartGamePacket117 startGamePacket = new StartGamePacket117();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = Long.MAX_VALUE;
            startGamePacket.entityRuntimeId = Long.MAX_VALUE;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking = true;
            startGamePacket.currentTick = this.server.getTick();
            return startGamePacket;
        } else if (this.getProtocol() >= AbstractProtocol.PROTOCOL_116_200.getProtocolStart()) {
            StartGamePacket116200 startGamePacket = new StartGamePacket116200();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = Long.MAX_VALUE;
            startGamePacket.entityRuntimeId = Long.MAX_VALUE;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
            startGamePacket.isBlockBreakingServerAuthoritative = this.serverAuthoritativeBlockBreaking =
                    //!this.isNetEaseClient() && this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart();
                    this.isNetEaseClient() || this.protocol > AbstractProtocol.PROTOCOL_116_200.getProtocolStart();
            startGamePacket.currentTick = this.server.getTick();
            return startGamePacket;
        } else if (this.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
            StartGamePacket116100NE startGamePacket = new StartGamePacket116100NE();
            startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
            startGamePacket.netease = this.isNetEaseClient();
            startGamePacket.entityUniqueId = Long.MAX_VALUE;
            startGamePacket.entityRuntimeId = Long.MAX_VALUE;
            startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
            startGamePacket.x = (float) this.x;
            startGamePacket.y = (float) this.y;
            startGamePacket.z = (float) this.z;
            startGamePacket.yaw = (float) this.yaw;
            startGamePacket.pitch = (float) this.pitch;
            startGamePacket.seed = -1;
            startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
            return startGamePacket;
        }
        StartGamePacket116100 startGamePacket = new StartGamePacket116100();
        startGamePacket.protocol = AbstractProtocol.fromRealProtocol(this.protocol);
        startGamePacket.netease = this.isNetEaseClient();
        startGamePacket.entityUniqueId = Long.MAX_VALUE;
        startGamePacket.entityRuntimeId = Long.MAX_VALUE;
        startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
        startGamePacket.x = (float) this.x;
        startGamePacket.y = (float) this.y;
        startGamePacket.z = (float) this.z;
        startGamePacket.yaw = (float) this.yaw;
        startGamePacket.pitch = (float) this.pitch;
        startGamePacket.seed = -1;
        startGamePacket.dimension = (byte) (this.level.getDimension() & 0xff);
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
                            dataInfoPacket.chunkCount = MathHelper.ceil(resourcePack.getPackSize() / (float) RESOURCE_PACK_CHUNK_SIZE);
                            dataInfoPacket.compressedPackSize = resourcePack.getPackSize();
                            dataInfoPacket.sha256 = resourcePack.getSha256();
                            if (resourcePack.getPackType().equals("resources")) {
                                dataInfoPacket.type = ResourcePackDataInfoPacket.TYPE_RESOURCE;
                            }
                            else if (resourcePack.getPackType().equals("data")) {
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
                if (riding instanceof EntityRideable) {
                    ((EntityRideable) riding).onPlayerInput(this, ipk.motionX, ipk.motionY);
                }
                break;
            case ProtocolInfo.MOVE_PLAYER_PACKET:
                if (this.teleportPosition != null) {
                    break;
                }

                MovePlayerPacket116100NE movePlayerPacket = (MovePlayerPacket116100NE) packet;
                Vector3 newPos = new Vector3(movePlayerPacket.x, movePlayerPacket.y - this.getEyeHeight(), movePlayerPacket.z);

                if (newPos.distanceSquared(this) == 0 && movePlayerPacket.yaw % 360 == this.yaw && movePlayerPacket.pitch % 360 == this.pitch) {
                    break;
                }

                boolean revert = false;
                if (!this.isAlive() || !this.spawned) {
                    revert = true;
                    this.forceMovement = new Vector3(this.x, this.y, this.z);
                }

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
                    super.handleDataPacket(packet);
                    break;
                }
                PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;
                if (!this.callPacketReceiveEvent(((PlayerActionPacket14) packet).toDefault())) break;

                if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN /*&& playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST*/)) {
                    break;
                }

                playerActionPacket.entityId = this.id;
                Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
                BlockFace face = BlockFace.fromIndex(playerActionPacket.face);

                actionswitch:
                switch (playerActionPacket.action) {
                    case PlayerActionPacket14.ACTION_START_BREAK:
                        if (!this.spawned || !this.isAlive() || this.isSpectator() || this.lastBreak != Long.MAX_VALUE || pos.distanceSquared(this) > 100) {
                            break;
                        }
                        Block target = this.level.getBlock(pos);
                        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(this, this.inventory.getItemInHand(), target, face, target.getId() == 0 ? PlayerInteractEvent.Action.LEFT_CLICK_AIR : PlayerInteractEvent.Action.LEFT_CLICK_BLOCK);
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
                            case Block.ITEM_FRAME_BLOCK:
                                BlockEntity itemFrame = this.level.getBlockEntity(pos);
                                if (itemFrame instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) itemFrame).dropItem(this)) {
                                    break actionswitch;
                                }
                        }
                        Block block = target.getSide(face);
                        if (block.getId() == Block.FIRE) {
                            this.level.setBlock(block, Block.get(BlockID.AIR), true);
                            this.level.addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_EXTINGUISH_FIRE);
                            break;
                        }
                        if (!this.isCreative()) {
                            //improved this to take stuff like swimming, ladders, enchanted tools into account, fix wrong tool break time calculations for bad tools (pmmp/PocketMine-MP#211)
                            //Done by lmlstarqaq
                            double breakTime = Math.ceil(target.getBreakTime(this.inventory.getItemInHand(), this) * 20);
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
                        this.lastBreak = System.currentTimeMillis();

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
            default:
                super.handleDataPacket(packet);
                break;
        }
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
        return USE_SUB_CHUNK_REQUEST && this.protocol >= AbstractProtocol.PROTOCOL_118.getProtocolStart();
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
            long[] ids = blobCache.getExtendedBlobIds();
            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, blobCache.getExtendedClientBlobs().get(hash)));

            LevelChunkPacket pk = new LevelChunkPacket();
            pk.chunkX = x;
            pk.chunkZ = z;
            if (ENABLE_SUB_CHUNK_NETWORK_OPTIMIZATION && this.protocol >= AbstractProtocol.PROTOCOL_118_10.getProtocolStart()) {
                pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_TRUNCATED_COLUMN_FAKE_COUNT;
            } else {
                pk.subChunkCount = LevelChunkPacket.CLIENT_REQUEST_FULL_COLUMN_FAKE_COUNT;
            }
            pk.subChunkRequestLimit = subChunkCount + Anvil.PADDING_SUB_CHUNK_COUNT;
            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubModeCachedPayload();
            this.dataPacket(pk);
        } else {
            this.dataPacket(packet);
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
        pk.subChunkRequestLimit = subChunkCount + Anvil.PADDING_SUB_CHUNK_COUNT;
        if (this.isBlobCacheAvailable() && this.isSubModeLevelChunkBlobCacheEnabled() && !centerChunk) {
            long[] ids = blobCache.getExtendedBlobIds();
            long hash = ids[ids.length - 1]; // biome
            this.clientCacheTrack.put(hash, new BlobTrack(hash, blobCache.getExtendedClientBlobs().get(hash)));

            pk.blobIds = new long[]{hash};
            pk.cacheEnabled = true;
            pk.data = blobCache.getSubModeCachedPayload();
        } else {
            pk.data = subModePayload;
        }
        this.dataPacket(pk);

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
            subChunkCount += Anvil.PADDING_SUB_CHUNK_COUNT;
        }
        byte[][] payloads = payload.get(StaticVersion.fromProtocol(this.protocol, this.isNetEaseClient()));

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = y + Anvil.PADDING_SUB_CHUNK_COUNT;

            SubChunkPacket pk = this.createSubChunkPacket();
//            pk.dimension = dimension;
            pk.dimension = Level.DIMENSION_OVERWORLD;
            pk.subChunkX = x;
            pk.subChunkY = y;
            pk.subChunkZ = z;

            if (subChunkCount == 0) {
                pk.data = Anvil.PADDING_SUB_CHUNK_BLOB;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = Anvil.PAD_256;
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
                pk.data = Anvil.PADDING_SUB_CHUNK_BLOB;
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
                long hash = blobCache.getExtendedBlobIds()[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, blobCache.getExtendedClientBlobs().get(hash)));

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
            subChunkCount += Anvil.PADDING_SUB_CHUNK_COUNT;
        }
        BatchPacket[] packets = packetCache.getSubPackets(StaticVersion.fromProtocol(this.protocol, this.isNetEaseClient()));

        IntIterator iter = requests.iterator();
        while (iter.hasNext()) {
            int y = iter.nextInt();
            int index = y + Anvil.PADDING_SUB_CHUNK_COUNT;

            if (subChunkCount == 0) {
                SubChunkPacket pk = this.createSubChunkPacket();
//                pk.dimension = dimension;
                pk.dimension = Level.DIMENSION_OVERWORLD;
                pk.subChunkX = x;
                pk.subChunkY = y;
                pk.subChunkZ = z;
                pk.data = Anvil.PADDING_SUB_CHUNK_BLOB;
                if (index == 0) {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_HAS_DATA;
                    pk.heightMap = Anvil.PAD_256;
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
                pk.data = Anvil.PADDING_SUB_CHUNK_BLOB;
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
                long hash = blobCache.getExtendedBlobIds()[index];
                this.clientCacheTrack.put(hash, new BlobTrack(hash, blobCache.getExtendedClientBlobs().get(hash)));

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
                    pk.heightMap = Anvil.PAD_256;
                } else {
                    pk.heightMapType = SubChunkPacket.HEIGHT_MAP_TYPE_ALL_TOO_LOW;
                }
                boolean centerChunk = this.getChunkX() == subChunkX && this.getChunkZ() == subChunkZ;
                if (false && this.isBlobCacheAvailable() && !centerChunk) {
                    this.clientCacheTrack.put(Anvil.PADDING_SUB_CHUNK_HASH, new BlobTrack(Anvil.PADDING_SUB_CHUNK_HASH, Anvil.PADDING_SUB_CHUNK_BLOB));
                    pk.cacheEnabled = true;
                    pk.blobId = Anvil.PADDING_SUB_CHUNK_HASH;
                } else {
                    pk.data = Anvil.PADDING_SUB_CHUNK_BLOB;
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
            pk.data = Anvil.PADDING_SUB_CHUNK_BLOB; // higher padding
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
                        this.clientCacheTrack.put(Anvil.MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(Anvil.MINIMIZE_BIOME_PALETTES_HASH, Anvil.MINIMIZE_BIOME_PALETTES));
                        packet.cacheEnabled = true;
                        packet.blobIds = new long[]{Anvil.MINIMIZE_BIOME_PALETTES_HASH};
                        packet.data = new byte[1]; // borderBlocks
                    } else {
                        packet.data = Anvil.MINIMIZE_CHUNK_DATA_NO_CACHE;
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
                    this.clientCacheTrack.put(Anvil.MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(Anvil.MINIMIZE_BIOME_PALETTES_HASH, Anvil.MINIMIZE_BIOME_PALETTES));
                    packet.cacheEnabled = true;
                    packet.blobIds = new long[]{Anvil.MINIMIZE_BIOME_PALETTES_HASH};
                    packet.data = new byte[1]; // borderBlocks
                } else {
                    packet.data = Anvil.MINIMIZE_CHUNK_DATA_NO_CACHE;
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
                            this.clientCacheTrack.put(Anvil.MINIMIZE_BIOME_PALETTES_HASH, new BlobTrack(Anvil.MINIMIZE_BIOME_PALETTES_HASH, Anvil.MINIMIZE_BIOME_PALETTES));
                            packet.cacheEnabled = true;
                            packet.blobIds = new long[]{Anvil.MINIMIZE_BIOME_PALETTES_HASH};
                            packet.data = new byte[1]; // borderBlocks
                        } else {
                            packet.data = Anvil.MINIMIZE_CHUNK_DATA_NO_CACHE;
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

    private static int distance(int centerX, int centerZ, int x, int z) {
        int dx = centerX - x;
        int dz = centerZ - z;
        return dx * dx + dz * dz;
    }
}
