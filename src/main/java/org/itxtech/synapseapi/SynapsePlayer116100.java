package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDragonEgg;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockNoteblock;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackDataInfoPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
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
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.net.InetSocketAddress;

@Log4j2
public class SynapsePlayer116100 extends SynapsePlayer116 {

    public SynapsePlayer116100(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, synapseEntry, clientID, socketAddress);
    }

    @Override
    protected DataPacket generateStartGamePacket(Position spawnPosition) {
        if (this.getProtocol() >= AbstractProtocol.PROTOCOL_117.getProtocolStart()) {
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
            startGamePacket.isMovementServerAuthoritative = false;
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
            startGamePacket.isMovementServerAuthoritative = false; //FIXME 需要再检查一遍数据包结构
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
                            dataInfoPacket.maxChunkSize = 1048576; //megabyte
                            dataInfoPacket.chunkCount = resourcePack.getPackSize() / dataInfoPacket.maxChunkSize;
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
            case ProtocolInfo.MOVE_PLAYER_PACKET:
                if (this.teleportPosition != null) {
                    break;
                }

                MovePlayerPacket116100NE movePlayerPacket = (MovePlayerPacket116100NE) packet;
                Vector3 newPos = new Vector3(movePlayerPacket.x, movePlayerPacket.y - this.getEyeHeight(), movePlayerPacket.z);

                if (newPos.distanceSquared(this) < 0.01 && movePlayerPacket.yaw % 360 == this.yaw && movePlayerPacket.pitch % 360 == this.pitch) {
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
                    if (riding instanceof EntityBoat) {
                        riding.setPositionAndRotation(this.temporalVector.setComponents(movePlayerPacket.x, movePlayerPacket.y - 1, movePlayerPacket.z), (movePlayerPacket.headYaw + 90) % 360, 0);
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

                    FilterTextPacket116200 textResponsePacket = new FilterTextPacket116200();
                    textResponsePacket.text = filterTextPacket.text; //TODO: 铁砧重命名物品需要接入网易敏感词检查
                    textResponsePacket.fromServer = true;
                    this.dataPacket(textResponsePacket);
                    break;
                }

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
                        if (this.lastBreak != Long.MAX_VALUE || pos.distanceSquared(this) > 100) {
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
                        this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
                        break;
                    default:
                        super.handleDataPacket(packet);
                        break;
                }
                break;
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
        if (!this.isNetEaseClient() /*&& this.protocol < AbstractProtocol.PROTOCOL_116_210.getProtocolStart()*/) super.initClientBlobCache();
    }
}
