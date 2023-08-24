package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockLectern;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityLectern;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.resourcepacks.ResourcePack;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerNetworkStackLatencyUpdateEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.LecternUpdatePacket110;
import org.itxtech.synapseapi.multiprotocol.protocol111.protocol.LecternUpdatePacket111;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.LevelSoundEventPacketV319;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.ResourcePacksInfoPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.StartGamePacket19;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

public class SynapsePlayer19 extends SynapsePlayer18 {
	private boolean pingNeedUpdate;

	public SynapsePlayer19(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		// this.levelChangeLoadScreen = true;
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		switch (packet.pid()) {
			case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3:
				if (!callPacketReceiveEvent(packet)) break;
				LevelSoundEventPacketV319 levelSoundEventPacket = (LevelSoundEventPacketV319) packet;
				int sound = levelSoundEventPacket.sound;
				SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
						sound,
						new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
						LevelSoundEventUtil.translateExtraDataFromClient(sound, levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
						0,
						levelSoundEventPacket.entityIdentifier,
						levelSoundEventPacket.isBabyMob,
						levelSoundEventPacket.isGlobal);
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
							event.isGlobal()
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
									event.isGlobal()
							));
				}
				break;
			case ProtocolInfo.NETWORK_STACK_LATENCY_PACKET:
				NetworkStackLatencyPacket19 networkStackLatencyPacket = (NetworkStackLatencyPacket19) packet;
				if (!networkStackLatencyPacket.isFromServer) {
					NetworkStackLatencyPacket19 pong = new NetworkStackLatencyPacket19();
					pong.isFromServer = false;
					pong.timestamp = networkStackLatencyPacket.timestamp;
					dataPacket(pong);
					break;
				}

				if (pingNs == 0) {
					break;
				}

				if (NETWORK_STACK_LATENCY_TELEMETRY) {
					latencyNs = System.nanoTime() - pingNs;
					ping();
				}

				if (pingNeedUpdate) {
					pingNeedUpdate = false;
					new SynapsePlayerNetworkStackLatencyUpdateEvent(this, networkStackLatencyPacket.timestamp, latencyNs).call();
				}
				break;
			case ProtocolInfo.LECTERN_UPDATE_PACKET:
				if (getProtocol() >= AbstractProtocol.PROTOCOL_111.getProtocolStart()) {
					LecternUpdatePacket111 lecternUpdatePacket = (LecternUpdatePacket111) packet;

					if (distanceSquared(lecternUpdatePacket.x, lecternUpdatePacket.y, lecternUpdatePacket.z) > 100) {
						break;
					}

					Block block = level.getBlock(lecternUpdatePacket.x, lecternUpdatePacket.y, lecternUpdatePacket.z);
					if (block.getId() != BlockID.LECTERN) {
						break;
					}
					BlockEntity blockEntity = level.getBlockEntityIfLoaded(block);
					if (!(blockEntity instanceof BlockEntityLectern)) {
						break;
					}
					BlockEntityLectern lectern = (BlockEntityLectern) blockEntity;

					if (lecternUpdatePacket.droppingBook) {
						if (lectern.dropBook(this)) {
							lectern.spawnToAll();
						} else {
							lectern.spawnTo(this);
						}
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
				} else if (getProtocol() == AbstractProtocol.PROTOCOL_110.getProtocolStart()) {
					LecternUpdatePacket110 lecternUpdatePacket = (LecternUpdatePacket110) packet;

					if (distanceSquared(lecternUpdatePacket.x, lecternUpdatePacket.y, lecternUpdatePacket.z) > 100) {
						break;
					}

					Block block = level.getBlock(lecternUpdatePacket.x, lecternUpdatePacket.y, lecternUpdatePacket.z);
					if (block.getId() != BlockID.LECTERN) {
						break;
					}
					BlockEntity blockEntity = level.getBlockEntityIfLoaded(block);
					if (!(blockEntity instanceof BlockEntityLectern)) {
						break;
					}
					BlockEntityLectern lectern = (BlockEntityLectern) blockEntity;

					if (lecternUpdatePacket.droppingBook) {
						if (lectern.dropBook(this)) {
							lectern.spawnToAll();
						} else {
							lectern.spawnTo(this);
						}
						break;
					}

					if (lectern.setPage(lecternUpdatePacket.page)) {
						((BlockLectern) block).onPageTurn();

						lectern.spawnToAll();
					} else {
						lectern.spawnTo(this);
					}
				}
				break;
			default:
				super.handleDataPacket(packet);
				break;
		}
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket19 startGamePacket = new StartGamePacket19();
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
		startGamePacket.eduMode = false;
		startGamePacket.rainLevel = 0;
		startGamePacket.lightningLevel = 0;
		startGamePacket.commandsEnabled = this.isEnableClientCommand();
		startGamePacket.levelId = "";
		startGamePacket.worldName = this.getServer().getNetwork().getName();
		startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
		startGamePacket.gameRules = getSupportedRules();
		startGamePacket.blockPalette = AdvancedGlobalBlockPalette.getCompiledTable(AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient());
		startGamePacket.enchantmentSeed = ThreadLocalRandom.current().nextInt();
		return startGamePacket;
	}

	@Override
	protected DataPacket generateResourcePackInfoPacket() {
		ResourcePacksInfoPacket19 resourcePacket = new ResourcePacksInfoPacket19();
		resourcePacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
		resourcePacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
		resourcePacket.mustAccept = this.forceResources;
		return resourcePacket;
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
	public void sendLevelSoundEvent(int levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
//		if (levelSound == null || levelSound.getV18() == -1) return;
		LevelSoundEventPacketV319 pk = new LevelSoundEventPacketV319();
		pk.sound = levelSound;
		pk.x = (float) pos.x;
		pk.y = (float) pos.y;
		pk.z = (float) pos.z;
		pk.extraData = LevelSoundEventUtil.translateTo18ExtraData(levelSound, extraData, pitch, AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient);
		pk.entityIdentifier = entityIdentifier;
		pk.isBabyMob = isBabyMob;
		pk.isGlobal = isGlobal;
		this.dataPacket(pk);
	}

	@Override
	public void requestPing() {
		pingNeedUpdate = true;
	}

	@Override
	public void ping() {
		long time = System.nanoTime();
		pingNs = time;

		NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
		packet.isFromServer = true;
		packet.timestamp = time;
		dataPacket(packet);
	}
}
