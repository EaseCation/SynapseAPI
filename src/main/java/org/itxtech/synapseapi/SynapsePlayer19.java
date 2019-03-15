package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.utils.Binary;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.ResourcePacksInfoPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.StartGamePacket19;

public class SynapsePlayer19 extends SynapsePlayer18 {

	public SynapsePlayer19(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, String ip, int port) {
		super(interfaz, synapseEntry, clientID, ip, port);
		this.levelChangeLoadScreen = true;
	}

	@Override
	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket19 startGamePacket = new StartGamePacket19();
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
		startGamePacket.eduMode = false;
		startGamePacket.rainLevel = 0;
		startGamePacket.lightningLevel = 0;
		startGamePacket.commandsEnabled = this.isEnableClientCommand();
		startGamePacket.levelId = "";
		startGamePacket.worldName = this.getServer().getNetwork().getName();
		startGamePacket.generator = 1; // 0 old, 1 infinite, 2 flat
		startGamePacket.gameRules = getSupportedRules();
		startGamePacket.encode();
		System.out.println(Binary.bytesToHexString(startGamePacket.getBuffer(), true));
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

}
