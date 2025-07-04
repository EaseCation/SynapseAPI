package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.Position;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.event.player.netease.SynapsePlayerNetEaseStoreBuySuccEvent;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.StartGamePacket17;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.TextPacket17;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SynapsePlayer17 extends SynapsePlayer16 {

	public SynapsePlayer17(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
	}

	@Override
	public void handleDataPacket(DataPacket packet) {
		if (!this.isSynapseLogin) {
			super.handleDataPacket(packet);
			return;
		}
		switch (packet.pid()) {
			case ProtocolInfo.TEXT_PACKET:
				if (!callPacketReceiveEvent(packet)) break;
				if (!this.spawned || !this.isAlive()) {
					break;
				}

				TextPacket17 textPacket = (TextPacket17) packet;

				if (textPacket.type == TextPacket17.TYPE_CHAT) {
					this.chat(textPacket.message);
				}
				break;
			case ProtocolInfo.PACKET_STORE_BUY_SUCC:
				if (!callPacketReceiveEvent(packet)) break;
				SynapsePlayerNetEaseStoreBuySuccEvent event = new SynapsePlayerNetEaseStoreBuySuccEvent(this);
				Server.getInstance().getPluginManager().callEvent(event);
				break;
			default:
				super.handleDataPacket(packet);
		}

	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket17 startGamePacket = new StartGamePacket17();
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
	private static int getClientFriendlyGamemode(int gamemode) {
		gamemode &= 0x03;
		if (gamemode == Player.SPECTATOR) {
			return Player.CREATIVE;
		}
		return gamemode;
	}

	@Override
	public void sendJukeboxPopup(TranslationContainer message) {
		TextPacket17 pk = new TextPacket17();
		pk.type = TextPacket17.JUKE_BOX_POPUP;
		pk.isLocalized = true;
		pk.message = message.getText();
		pk.parameters = Arrays.stream(message.getParameters()).map(String::valueOf).toArray(String[]::new);
		this.dataPacket(pk);
	}
}
