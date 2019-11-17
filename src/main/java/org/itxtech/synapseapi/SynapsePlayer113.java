package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.ResourcePackStackPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.StartGamePacket113;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.net.InetSocketAddress;

public class SynapsePlayer113 extends SynapsePlayer112 {

	public SynapsePlayer113(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = false;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket113 startGamePacket = new StartGamePacket113();
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
		packetswitch:
		switch (packet.pid()) {
			case ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET:
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
						ResourcePackStackPacket113 stackPacket = new ResourcePackStackPacket113();
						stackPacket.mustAccept = this.forceResources;
						stackPacket.resourcePackStack = this.resourcePacks.values().toArray(new ResourcePack[0]);
						stackPacket.behaviourPackStack = this.behaviourPacks.values().toArray(new ResourcePack[0]);
						this.dataPacket(stackPacket);
						break;
					case ResourcePackClientResponsePacket.STATUS_COMPLETED:
						this.completeLoginSequence();
						break;
				}
				break;
			case ProtocolInfo.RESPAWN_PACKET:
				if (this.isAlive()) {
					break;
				}
				RespawnPacket respawnPacket = (RespawnPacket) packet;
				if (respawnPacket.respawnState == RespawnPacket.STATE_CLIENT_READY_TO_SPAWN) {
					RespawnPacket respawn1 = new RespawnPacket();
					respawn1.x = (float) this.getX();
					respawn1.y = (float) this.getY();
					respawn1.z = (float) this.getZ();
					respawn1.respawnState = RespawnPacket.STATE_READY_TO_SPAWN;
					this.dataPacket(respawn1);
				}
				break;
			case ProtocolInfo.PLAYER_ACTION_PACKET:
				PlayerActionPacket14 playerActionPacket = (PlayerActionPacket14) packet;
				if (!this.spawned || (!this.isAlive() && playerActionPacket.action != PlayerActionPacket14.ACTION_RESPAWN && playerActionPacket.action != PlayerActionPacket14.ACTION_DIMENSION_CHANGE_REQUEST)) {
					break;
				}

				playerActionPacket.entityId = this.id;
				Vector3 pos = new Vector3(playerActionPacket.x, playerActionPacket.y, playerActionPacket.z);
				BlockFace face = BlockFace.fromIndex(playerActionPacket.face);

				switch (playerActionPacket.action) {
					case PlayerActionPacket14.ACTION_RESPAWN:
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
						this.setHealth(this.getMaxHealth());
						this.getFoodData().setLevel(20, 20);

						this.sendData(this);

						this.setMovementSpeed(DEFAULT_SPEED);

						this.getAdventureSettings().update();
						this.inventory.sendContents(this);
						this.inventory.sendArmorContents(this);

						this.spawnToAll();
						this.scheduleUpdate();

						this.startAction = -1;
						this.setDataFlag(Player.DATA_FLAGS, Player.DATA_FLAG_ACTION, false);
						break;
					default:
						super.handleDataPacket(packet);
						break;
				}
			default:
				super.handleDataPacket(packet);
		}

	}

}
