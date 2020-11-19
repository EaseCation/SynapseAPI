package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackDataInfoPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ContainerClosePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.MovePlayerPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ResourcePackStackPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.StartGamePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.net.InetSocketAddress;

public class SynapsePlayer116100 extends SynapsePlayer116 {

    public SynapsePlayer116100(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, synapseEntry, clientID, socketAddress);
    }

    @Override
    protected DataPacket generateStartGamePacket(Position spawnPosition) {
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
                            this.dataPacket(dataInfoPacket);
                        }
                        break;
                    case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS:
                        ResourcePackStackPacket116100 stackPacket = new ResourcePackStackPacket116100();
                        stackPacket.mustAccept = this.forceResources;
                        stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
                        stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
                        this.dataPacket(stackPacket);
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
                ContainerClosePacket116100 containerClosePacket = (ContainerClosePacket116100) packet;
                if (!this.spawned || containerClosePacket.windowId == ContainerIds.INVENTORY && !inventoryOpen) {
                    break;
                }

                if (this.windowIndex.containsKey(containerClosePacket.windowId)) {
                    this.server.getPluginManager().callEvent(new InventoryCloseEvent(this.windowIndex.get(containerClosePacket.windowId), this));

                    if (containerClosePacket.windowId == ContainerIds.INVENTORY) this.inventoryOpen = false;

                    this.removeWindow(this.windowIndex.get(containerClosePacket.windowId));
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

                MovePlayerPacket116100 movePlayerPacket = (MovePlayerPacket116100) packet;
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
                    this.sendPosition(this.forceMovement, this.yaw, this.pitch, MovePlayerPacket116100.MODE_TELEPORT);
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
            default:
                super.handleDataPacket(packet);
                break;
        }
    }
}
