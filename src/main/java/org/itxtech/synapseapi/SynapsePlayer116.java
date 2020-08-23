package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.StartGamePacket116;

import java.net.InetSocketAddress;

public class SynapsePlayer116 extends SynapsePlayer113 {

	public SynapsePlayer116(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
		super(interfaz, synapseEntry, clientID, socketAddress);
		this.levelChangeLoadScreen = false;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket116 startGamePacket = new StartGamePacket116();
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
		packetswitch:
		switch (packet.pid()) {

			default:
				super.handleDataPacket(packet);
				break;
		}

	}

}
