package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.command.Command;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.*;
import co.aikar.timings.Timing;
import co.aikar.timings.TimingsManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.itxtech.synapseapi.event.player.SynapsePlayerBroadcastLevelSoundEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerConnectEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTransferEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.TextPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12Urgency;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.TextPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.TextPacket17;
import org.itxtech.synapseapi.multiprotocol.utils.CraftingPacketManager;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;
import org.itxtech.synapseapi.network.protocol.spp.FastPlayerListPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.utils.BlobTrack;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.ClientData.Entry;
import org.itxtech.synapseapi.utils.DataPacketEidReplacer;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by boybook on 16/6/24.
 */
public class SynapsePlayer extends Player {

    private static final Map<Integer, Timing> handlePlayerDataPacketTimings = new HashMap<>();
    public boolean isSynapseLogin = false;
    protected SynapseEntry synapseEntry;
    protected boolean isFirstTimeLogin = false;
    protected long synapseSlowLoginUntil = 0;
    protected boolean levelChangeLoadScreen = true;
    private boolean cleanTextColor = false;

    protected String originName;
    protected LoginChainData loginChainData;
    protected boolean isNetEaseClient = false;
    protected JsonObject cachedExtra = new JsonObject();
    protected final JsonObject transferExtra = new JsonObject();

    public SynapsePlayer(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, clientID, socketAddress);
        this.synapseEntry = synapseEntry;
        this.isSynapseLogin = this.synapseEntry != null;
    }

    public boolean isSynapseLogin() {
        return isSynapseLogin;
    }

    public boolean isFirstTimeLogin() {
        return isFirstTimeLogin;
    }

    /**
     * Returns a client-friendly gamemode of the specified real gamemode
     * This function takes care of handling gamemodes known to MCPE (as of 1.1.0.3, that includes Survival, Creative and Adventure)
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

    public LoginChainData getLoginChainData() {
        return this.isSynapseLogin ? this.loginChainData : super.getLoginChainData();
    }

    public void handleLoginPacket(PlayerLoginPacket packet) {
        if (!this.isSynapseLogin) {
            super.handleDataPacket(SynapseAPI.getInstance().getPacket(packet.cachedLoginPacket));
            return;
        }
        this.isFirstTimeLogin = packet.isFirstTime;
        this.cachedExtra = packet.extra;
        SynapsePlayerConnectEvent ev;
        this.server.getPluginManager().callEvent(ev = new SynapsePlayerConnectEvent(this, this.isFirstTimeLogin));
        if (!ev.isCancelled()) {
            this.protocol = packet.protocol;
            try {
                DataPacket pk = PacketRegister.getFullPacket(packet.cachedLoginPacket, packet.protocol);
                if (pk instanceof org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) {
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).isFirstTimeLogin = packet.isFirstTime;
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).username = packet.extra.get("username").getAsString();
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).clientUUID = packet.uuid;
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).xuid = packet.extra.get("xuid").getAsString();
                    this.isNetEaseClient = Optional.ofNullable(packet.extra.get("netease")).orElseGet(() -> new JsonPrimitive(false)).getAsBoolean();
                }
                this.handleDataPacket(pk);
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
                this.close();
            }
        }
    }

    public SynapseEntry getSynapseEntry() {
        return synapseEntry;
    }

    @Override
    protected void processLogin() {
        if (!this.isSynapseLogin) {
            super.processLogin();
            return;
        }
        if (!this.server.isWhitelisted((this.getName()).toLowerCase())) {
            this.kick(PlayerKickEvent.Reason.NOT_WHITELISTED, "Server is white-listed");

            return;
        } else if (this.isBanned()) {
            this.kick(PlayerKickEvent.Reason.NAME_BANNED, "You are banned");
            return;
        } else if (this.server.getIPBans().isBanned(this.getAddress())) {
            this.kick(PlayerKickEvent.Reason.IP_BANNED, "You are banned");
            return;
        }

        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this);
        }
        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this);
        }

        for (Player p : new ArrayList<>(this.server.getOnlinePlayers().values())) {
            if (p != this && p.getName() != null && p.getName().equalsIgnoreCase(this.getName())) {
                if (!p.kick(PlayerKickEvent.Reason.NEW_CONNECTION, "logged in from another location")) {
                    this.close(this.getLeaveMessage(), "Already connected");
                    return;
                }
            } else if (p.loggedIn && this.getUniqueId().equals(p.getUniqueId())) {
                if (!p.kick(PlayerKickEvent.Reason.NEW_CONNECTION, "logged in from another location")) {
                    this.close(this.getLeaveMessage(), "Already connected");
                    return;
                }
            }
        }

        CompoundTag nbt = this.server.getOfflinePlayerData(this.username);
        if (nbt == null) {
            this.close(this.getLeaveMessage(), "Invalid data");

            return;
        }

        this.playedBefore = (nbt.getLong("lastPlayed") - nbt.getLong("firstPlayed")) > 1;

        boolean alive = true;

        nbt.putString("NameTag", this.username);

        if (0 >= nbt.getShort("Health")) {
            alive = false;
        }

        int exp = nbt.getInt("EXP");
        int expLevel = nbt.getInt("expLevel");
        this.setExperience(exp, expLevel);

        this.gamemode = nbt.getInt("playerGameType") & 0x03;
        if (this.server.getForceGamemode()) {
            this.gamemode = this.server.getGamemode();
            nbt.putInt("playerGameType", this.gamemode);
        }

        this.adventureSettings = new AdventureSettings(this)
                .set(Type.WORLD_IMMUTABLE, !isAdventure())
                .set(Type.AUTO_JUMP, true)
                .set(Type.ALLOW_FLIGHT, isCreative())
                .set(Type.NO_CLIP, isSpectator());

        Level level;
        if ((level = this.server.getLevelByName(nbt.getString("Level"))) == null || !alive) {
            this.setLevel(this.server.getDefaultLevel());
            nbt.putString("Level", this.level.getName());
            nbt.getList("Pos", DoubleTag.class)
                    .add(new DoubleTag("0", this.level.getSpawnLocation().x))
                    .add(new DoubleTag("1", this.level.getSpawnLocation().y))
                    .add(new DoubleTag("2", this.level.getSpawnLocation().z));
        } else {
            this.setLevel(level);
        }

        for (Tag achievement : nbt.getCompound("Achievements").getAllTags()) {
            if (!(achievement instanceof ByteTag)) {
                continue;
            }

            if (((ByteTag) achievement).getData() > 0) {
                this.achievements.add(achievement.getName());
            }
        }

        nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000);

        if (this.server.getAutoSave()) {
            this.server.saveOfflinePlayerData(this.username, nbt, true);
        }

        this.sendPlayStatus(PlayStatusPacket.LOGIN_SUCCESS);
        this.server.onPlayerLogin(this);
        ListTag<DoubleTag> posList = nbt.getList("Pos", DoubleTag.class);

        super.init(this.level.getChunk((int) posList.get(0).data >> 4, (int) posList.get(2).data >> 4, true), nbt);

        if (!this.namedTag.contains("foodLevel")) {
            this.namedTag.putInt("foodLevel", 20);
        }
        int foodLevel = this.namedTag.getInt("foodLevel");
        if (!this.namedTag.contains("FoodSaturationLevel")) {
            this.namedTag.putFloat("FoodSaturationLevel", 20);
        }
        float foodSaturationLevel = this.namedTag.getFloat("foodSaturationLevel");
        this.foodData = new PlayerFood(this, foodLevel, foodSaturationLevel);

        if (this.isSpectator()) this.keepMovement = true;

        this.forceMovement = this.teleportPosition = this.getPosition();

        if (this.isFirstTimeLogin) {
            PlayerRequestResourcePackEvent resourcePackEvent = new PlayerRequestResourcePackEvent(this, this.server.getResourcePackManager().getResourcePacksMap(), this.server.getResourcePackManager().getBehaviorPacksMap(), this.server.getForceResources());
            this.server.getPluginManager().callEvent(resourcePackEvent);
            this.resourcePacks = resourcePackEvent.getResourcePacks();
            this.behaviourPacks = resourcePackEvent.getBehaviourPacks();
            this.forceResources = resourcePackEvent.isMustAccept();

            DataPacket infoPacket = generateResourcePackInfoPacket();
            this.dataPacket(infoPacket);

            this.sendNetworkSettings();
        } else {
            //跨服时，传输到目标服务器当前玩家安装上的材质
            if (this.cachedExtra.has("res_packs")) {
                for (JsonElement data : this.cachedExtra.getAsJsonArray("res_packs")) {
                    this.resourcePacks.put(data.getAsString(), this.server.getResourcePackManager().getPackById(data.getAsString()));
                }
            }
            if (this.cachedExtra.has("beh_packs")) {
                for (JsonElement data : this.cachedExtra.getAsJsonArray("beh_packs")) {
                    this.resourcePacks.put(data.getAsString(), this.server.getResourcePackManager().getPackById(data.getAsString()));
                }
            }

            this.completeLoginSequence();
        }
    }

	protected DataPacket generateResourcePackInfoPacket() {
		ResourcePacksInfoPacket infoPacket = new ResourcePacksInfoPacket();
		infoPacket.resourcePackEntries = this.resourcePacks.values().toArray(new ResourcePack[0]);
		infoPacket.behaviourPackEntries = this.behaviourPacks.values().toArray(new ResourcePack[0]);
		infoPacket.mustAccept = this.forceResources;
		return infoPacket;
	}

    @Override
    @SuppressWarnings("deprecation")
    protected void completeLoginSequence() {
        if (!this.isSynapseLogin) {
            super.completeLoginSequence();
            return;
        }
        PlayerLoginEvent ev = new PlayerLoginEvent(this, "Plugin reason");
        try {
            this.server.getPluginManager().callEvent(ev);
        } catch (EventException e) {
            Server.getInstance().getLogger().logException(e);
            ev.setCancelled();
            StringBuilder sb = new StringBuilder();
            sb.append(e.getClass().getName());
            sb.append(": ");
            sb.append(e.getMessage());
            if (e.getCause() != null) {
                sb.append("\n");
                sb.append(e.getCause().getClass().getName());
                sb.append(": ");
                sb.append(e.getCause().getMessage());
            }
            ev.setKickMessage(sb.toString());
            this.sendMessage(sb.toString());
        }

        if (ev.isCancelled()) {
            this.close(this.getLeaveMessage(), ev.getKickMessage());
            return;
        }

        if (this.isCreative()) {
            this.inventory.setHeldItemSlot(0);
        } else {
            this.inventory.setHeldItemSlot(this.inventory.getHotbarSlotIndex(0));
        }

        if (this.isSpectator()) this.keepMovement = true;

        Level level;
        if (this.spawnPosition == null && this.namedTag.contains("SpawnLevel") && (level = this.server.getLevelByName(this.namedTag.getString("SpawnLevel"))) != null) {
            this.spawnPosition = new Position(this.namedTag.getInt("SpawnX"), this.namedTag.getInt("SpawnY"), this.namedTag.getInt("SpawnZ"), level);
        }

        Position spawnPosition = this.getSpawn();
        if (this.isFirstTimeLogin) {
            DataPacket startGamePacket = generateStartGamePacket(spawnPosition);
            this.dataPacket(startGamePacket);
        } else {
            AdventureSettings newSettings = this.getAdventureSettings().clone(this);
            newSettings.set(AdventureSettings.Type.WORLD_IMMUTABLE, gamemode != 3);
            newSettings.set(AdventureSettings.Type.ALLOW_FLIGHT, (gamemode & 0x01) > 0);
            newSettings.set(AdventureSettings.Type.NO_CLIP, gamemode == 0x03);
            newSettings.set(AdventureSettings.Type.FLYING, gamemode == 0x03);
            if (this.isSpectator()) {
                this.keepMovement = true;
            } else {
                this.keepMovement = false;
            }
            SetPlayerGameTypePacket pk = new SetPlayerGameTypePacket();
            pk.gamemode = getClientFriendlyGamemode(gamemode);
            this.dataPacket(pk);

            if (this.levelChangeLoadScreen) {
                ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                changeDimensionPacket.dimension = 0;
                changeDimensionPacket.x = (float) this.getX();
                changeDimensionPacket.y = (float) this.getY() + this.getEyeHeight();
                changeDimensionPacket.z = (float) this.getZ();
                dataPacket(changeDimensionPacket);

                StopSoundPacket stopSoundPacket = new StopSoundPacket();
                stopSoundPacket.name = "portal.travel";
                stopSoundPacket.stopAll = false;
                dataPacket(stopSoundPacket);

                PlaySoundPacket playSoundPacket = new PlaySoundPacket();
                playSoundPacket.name = "camera.take_picture";
                playSoundPacket.x = this.getFloorX();
                playSoundPacket.y = this.getFloorY();
                playSoundPacket.z = this.getFloorZ();
                playSoundPacket.pitch = 1;
                playSoundPacket.volume = 1;
                dataPacket(playSoundPacket);
            } else {
                long flags = (long) (1 << Entity.DATA_FLAG_IMMOBILE);
                this.sendData(this, new EntityMetadata().putLong(Entity.DATA_FLAGS, flags));
            }
        }

        this.loggedIn = true;

        if (this.spawnPosition != null) {
            SetSpawnPositionPacket pk = new SetSpawnPositionPacket();
            pk.spawnType = SetSpawnPositionPacket.TYPE_PLAYER_SPAWN;
            pk.x = (int) this.spawnPosition.x;
            pk.y = (int) this.spawnPosition.y;
            pk.z = (int) this.spawnPosition.z;
            this.dataPacket(pk);
        }

        spawnPosition.level.sendTime(this);

        this.setEnableClientCommand(true);
        this.getAdventureSettings().update();

        this.setMovementSpeed(DEFAULT_SPEED, false);
        this.sendAttributes();
        //this.setNameTagVisible(true);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_SHOW_NAMETAG, true, false);
        //this.setNameTagAlwaysVisible(true);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_ALWAYS_SHOW_NAMETAG, true, false);
        //this.setCanClimb(true);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_CAN_CLIMB, true, false);

        this.server.getLogger().info(this.getServer().getLanguage().translateString("nukkit.player.logIn",
                TextFormat.AQUA + this.username + TextFormat.WHITE,
                this.getAddress(),
                String.valueOf(this.getPort()),
                String.valueOf(this.id),
                this.level.getName(),
                String.valueOf(NukkitMath.round(this.x, 4)),
                String.valueOf(NukkitMath.round(this.y, 4)),
                String.valueOf(NukkitMath.round(this.z, 4))));

        if (this.isOp()) {
            this.setRemoveFormat(false);
        }

        this.forceMovement = this.teleportPosition = this.getPosition();

        this.server.addOnlinePlayer(this);
        this.server.onPlayerCompleteLoginSequence(this);
    }

	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket startGamePacket = new StartGamePacket();
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
		startGamePacket.generator = 1; //0 old, 1 infinite, 2 flat
		startGamePacket.gameRules = this.level.gameRules;
		return startGamePacket;
	}

    @Override
    protected void sendRecipeList() {
        this.dataPacket(CraftingPacketManager.getCachedCraftingPacket(AbstractProtocol.fromRealProtocol(this.protocol)));
    }

    @Override
    protected void doFirstSpawn() {
        super.doFirstSpawn();
        if (this.isNetEaseClient()) {
            ChunkRadiusUpdatedPacket chunkRadiusUpdatePacket = new ChunkRadiusUpdatedPacket();
            chunkRadiusUpdatePacket.radius = this.chunkRadius;
            this.dataPacket(chunkRadiusUpdatePacket);
        }
    }

    public boolean transferByDescription(String serverDescription) {
        return this.transfer(this.getSynapseEntry().getClientData().getHashByDescription(serverDescription));
    }

    public boolean transfer(String hash) {
        return this.transfer(hash, null);
    }

    public boolean transfer(String hash, JsonObject extra) {
        ClientData clients = this.getSynapseEntry().getClientData();
        Entry clientData = clients.clientList.get(hash);

        if (clientData != null) {
            if (extra != null) {
                for (Map.Entry<String, JsonElement> entry : extra.entrySet()) {
                    this.transferExtra.add(entry.getKey(), entry.getValue());
                }
            }
            SynapsePlayerTransferEvent event = new SynapsePlayerTransferEvent(this, clientData);
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return false;
            }

            this.removeAllChunks();

            this.getDummyBossBars().values().forEach(DummyBossBar::destroy);
            this.getDummyBossBars().clear();
            this.teleportPosition = null;
            this.isLevelChange = true;

            if (levelChangeLoadScreen) {
                Task task = new Task() {
                    private SynapsePlayer player;
                    Task putPlayer(SynapsePlayer player) {
                        this.player = player;
                        return this;
                    }
                    @Override
                    public void onRun(int currentTick) {
                        ChangeDimensionPacket changeDimensionPacket1 = new ChangeDimensionPacket();
                        changeDimensionPacket1.dimension = 2;
                        changeDimensionPacket1.x = (float) getX();
                        changeDimensionPacket1.y = (float) getY() + getEyeHeight();
                        changeDimensionPacket1.z = (float) getZ();
                        dataPacket(changeDimensionPacket1);

                        StopSoundPacket stopSoundPacket = new StopSoundPacket();
                        stopSoundPacket.name = "portal.travel";
                        stopSoundPacket.stopAll = false;
                        dataPacket(stopSoundPacket);

                        PlaySoundPacket playSoundPacket0 = new PlaySoundPacket();
                        playSoundPacket0.name = "random.screenshot";
                        playSoundPacket0.x = getFloorX();
                        playSoundPacket0.y = getFloorY();
                        playSoundPacket0.z = getFloorZ();
                        playSoundPacket0.pitch = 1;
                        playSoundPacket0.volume = 1;
                        dataPacket(playSoundPacket0);

                        forceSendEmptyChunks(3);
                        SynapseAPI.getInstance().getTransferDimensionTaskThread().queue(player, hash, transferExtra);
                    }
                }.putPlayer(this);

                if (!this.spawned && !this.isFirstTimeLogin) {  //如果需要跨服，并且还未出生（这时玩家处于ChangeDimension为主世界的加载界面状态）
                    PlayStatusPacket statusPacket0 = new PlayStatusPacket();
                    statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
                    dataPacket(statusPacket0);
                    this.spawned = true;

                    Server.getInstance().getScheduler().scheduleDelayedTask(task, 5);
                } else {
                    task.onRun(0);
                }
            } else {
                Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                    @Override
                    public void onRun(int currentTick) {
                        org.itxtech.synapseapi.network.protocol.spp.TransferPacket pk = new org.itxtech.synapseapi.network.protocol.spp.TransferPacket();
                        pk.uuid = getUniqueId();
                        pk.clientHash = hash;
                        pk.extra = transferExtra;
                        pk.extra.addProperty("username", originName);
                        pk.extra.addProperty("xuid", getLoginChainData().getXUID());
                        pk.extra.addProperty("netease", isNetEaseClient);
                        pk.extra.addProperty("blob_cache", getClientCacheTrack() != null);
                        //跨服时，传输到目标服务器当前玩家安装上的材质
                        JsonArray resPacks = new JsonArray();
                        getResourcePacks().keySet().forEach(resPacks::add);
                        pk.extra.add("res_packs", resPacks);
                        JsonArray behPacks = new JsonArray();
                        getResourcePacks().keySet().forEach(behPacks::add);
                        pk.extra.add("beh_packs", behPacks);
                        getSynapseEntry().sendDataPacket(pk);
                    }
                }, 1);
            }

            return true;
        }
        return false;
    }

    public void transfer(InetSocketAddress address) {
        String hostName = address.getAddress().getHostAddress();
        int port = address.getPort();
        TransferPacket pk = new TransferPacket();
        pk.address = hostName;
        pk.port = port;
        this.dataPacket(pk);
        //String message = "Transferred to " + hostName + ":" + port;
        //this.close(message, message, false);
    }

    public boolean isNetEaseClient() {
        return isNetEaseClient;
    }

    public void setIsNetEaseClient(boolean netEaseClient) {
        isNetEaseClient = netEaseClient;
    }

    @Override
    protected boolean checkTeleportPosition() {
        if (this.teleportPosition != null) {

            int chunkX = (int) this.teleportPosition.x >> 4;
            int chunkZ = (int) this.teleportPosition.z >> 4;

            long centerChunkIndex = Level.chunkHash(chunkX, chunkZ);
            if (!this.usedChunks.containsKey(centerChunkIndex) || !this.usedChunks.get(centerChunkIndex)) {
                if (this.teleportChunkLoaded) {
                    this.lastImmobile = this.isImmobile();
                }

                this.teleportChunkIndex = centerChunkIndex;
                this.teleportChunkLoaded = false;
            }

            for (int X = -1; X <= 1; ++X) {
                for (int Z = -1; Z <= 1; ++Z) {
                    long index = Level.chunkHash(chunkX + X, chunkZ + Z);
                    if (!this.usedChunks.containsKey(index) || !this.usedChunks.get(index)) {
                        return false;
                    }
                }
            }

            //Weather
            this.getLevel().sendWeather(this);
            //Update time
            this.getLevel().sendTime(this);

            this.getServer().getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), () -> this.getLevel().sendTime(this), 10);

            if (this.isLevelChange) { //TODO: remove this
                PlayStatusPacket statusPacket0 = new PlayStatusPacket();
                statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
                this.dataPacket(statusPacket0);

                //DummyBossBar
                this.getDummyBossBars().values().forEach(DummyBossBar::reshow);
                //防止切换世界后的玩家实体因为被客户端卸载而消失问题
                this.getLevel().getPlayers().values().stream().filter(p -> p instanceof SynapsePlayer && p.getViewers().containsKey(this.getLoaderId())).forEach(p -> ((SynapsePlayer) p).forceSpawnTo(this));
            }

            this.spawnToAll();
            this.isLevelChange = false;
            this.forceMovement = this.teleportPosition;
            this.teleportPosition = null;
            return true;
        }

        return false;
    }

    protected boolean isLevelChange = false;
    protected boolean allowOrderChunks = true;

    public boolean isLevelChange() {
        return isLevelChange;
    }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (!this.isOnline()) {
            return false;
        }

        Location from = this.getLocation();

        if (location.level != null && from.getLevel().getId() != location.level.getId()) {
            this.getDummyBossBars().values().forEach(DummyBossBar::destroy);  //游戏崩溃问题
            for (Entity entity : this.getLevel().getEntities()) {
                if (entity.getViewers().containsKey(this.getLoaderId())) {
                    entity.despawnFrom(this);
                }
            }
            this.isLevelChange = true;
        }
        if (super.teleport(location, cause) && this.levelChangeLoadScreen) {
            if (from.getLevel().getId() != location.level.getId() && this.spawned) {
                //this.isLevelChange = true;
                //this.nextAllowSendTop = System.currentTimeMillis() + 2000;  //2秒后才运行发送Top
                this.allowOrderChunks = false;

                ChangeDimensionPacket changeDimensionPacket1 = new ChangeDimensionPacket();
                changeDimensionPacket1.dimension = 2;
                changeDimensionPacket1.x = (float) this.getX();
                changeDimensionPacket1.y = (float) this.getY() + this.getEyeHeight();
                changeDimensionPacket1.z = (float) this.getZ();
                this.dataPacket(changeDimensionPacket1);

                StopSoundPacket stopSoundPacket = new StopSoundPacket();
                stopSoundPacket.name = "portal.travel";
                stopSoundPacket.stopAll = false;
                this.dataPacket(stopSoundPacket);

                PlaySoundPacket playSoundPacket0 = new PlaySoundPacket();
                playSoundPacket0.name = "random.screenshot";
                playSoundPacket0.x = this.getFloorX();
                playSoundPacket0.y = this.getFloorY();
                playSoundPacket0.z = this.getFloorZ();
                playSoundPacket0.pitch = 1;
                playSoundPacket0.volume = 1;
                dataPacket(playSoundPacket0);

                this.forceSendEmptyChunks();
                this.getServer().getScheduler().scheduleDelayedTask(() -> {
                    PlayStatusPacket statusPacket0 = new PlayStatusPacket();
                    statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
                    dataPacket(statusPacket0);
                }, 10);

                this.getServer().getScheduler().scheduleDelayedTask(() -> {
                    ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                    changeDimensionPacket.dimension = 0;
                    changeDimensionPacket.x = (float) this.getX();
                    changeDimensionPacket.y = (float) this.getY() + this.getEyeHeight();
                    changeDimensionPacket.z = (float) this.getZ();
                    dataPacket(changeDimensionPacket);

                    dataPacket(stopSoundPacket);

                    PlaySoundPacket playSoundPacket = new PlaySoundPacket();
                    playSoundPacket.name = "camera.take_picture";
                    playSoundPacket.x = this.getFloorX();
                    playSoundPacket.y = this.getFloorY();
                    playSoundPacket.z = this.getFloorZ();
                    playSoundPacket.pitch = 1;
                    playSoundPacket.volume = 1;
                    dataPacket(playSoundPacket);

                    this.allowOrderChunks = true;
                }, 20);
            }
            return true;
        }

        return false;
    }

    @Override
    protected boolean orderChunks() {
        if (!allowOrderChunks) return false;
        return super.orderChunks();
    }

    public void forceSpawn() {
        if (!this.skin.isValid()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " must have a valid skin set");
        }

        //this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, this.getLoginChainData().getXUID(), this.getViewers().values());

        AddPlayerPacket pk = new AddPlayerPacket();
        pk.uuid = this.getUniqueId();
        pk.username = this.getNameTag();
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float)this.x;
        pk.y = (float)this.y;
        pk.z = (float)this.z;
        pk.speedX = (float)this.motionX;
        pk.speedY = (float)this.motionY;
        pk.speedZ = (float)this.motionZ;
        pk.yaw = (float)this.yaw;
        pk.pitch = (float)this.pitch;
        pk.item = this.getInventory().getItemInHand();
        pk.metadata = this.dataProperties;
        Server.broadcastPacket(this.getViewers().values(), pk);

        this.inventory.sendArmorContents(this.getViewers().values());

        if (this.riding != null) {
            Server.getInstance().getScheduler().scheduleTask(SynapseAPI.getInstance(), () -> {
                if (this.riding == null) return;
                SetEntityLinkPacket pk1 = new SetEntityLinkPacket();
                pk1.vehicleUniqueId = this.riding.getId();
                pk1.riderUniqueId = this.getId();
                pk1.type = SetEntityLinkPacket.TYPE_RIDE;
                Server.broadcastPacket(this.getViewers().values(), pk1);
            });
        }
    }

    public void forceSpawnTo(Player player) {
        if (!this.skin.isValid()) {
            throw new IllegalStateException(this.getClass().getSimpleName() + " must have a valid skin set");
        }

        //this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, this.getLoginChainData().getXUID(), this.getViewers().values());

        AddPlayerPacket pk = new AddPlayerPacket();
        pk.uuid = this.getUniqueId();
        pk.username = this.getNameTag();
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float)this.x;
        pk.y = (float)this.y;
        pk.z = (float)this.z;
        pk.speedX = (float)this.motionX;
        pk.speedY = (float)this.motionY;
        pk.speedZ = (float)this.motionZ;
        pk.yaw = (float)this.yaw;
        pk.pitch = (float)this.pitch;
        pk.item = this.getInventory().getItemInHand();
        pk.metadata = this.dataProperties;
        player.dataPacket(pk);

        this.inventory.sendArmorContents(player);

        if (this.riding != null) {
            Server.getInstance().getScheduler().scheduleTask(SynapseAPI.getInstance(), () -> {
                if (this.riding == null) return;
                SetEntityLinkPacket pk1 = new SetEntityLinkPacket();
                pk1.vehicleUniqueId = this.riding.getId();
                pk1.riderUniqueId = this.getId();
                pk1.type = SetEntityLinkPacket.TYPE_RIDE;
                player.dataPacket(pk1);
            });
        }
    }

    protected boolean callPacketReceiveEvent(DataPacket packet) {
        DataPacketReceiveEvent ev = new DataPacketReceiveEvent(this, packet);
        this.server.getPluginManager().callEvent(ev);
        return !ev.isCancelled();
    }

    @Override
    public void handleDataPacket(DataPacket packet) {
        if (!this.isSynapseLogin) {
            super.handleDataPacket(packet);
            return;
        }
        Timing dataPacketTiming = handlePlayerDataPacketTimings.getOrDefault(packet.pid(), TimingsManager.getTiming("SynapseEntry - HandlePlayerDataPacket - " + packet.getClass().getSimpleName()));
        dataPacketTiming.startTiming();

        switch (packet.pid()) {
            case ProtocolInfo.LOGIN_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                if (this.loggedIn) {
                    break;
                }

                LoginPacket loginPacket = (LoginPacket) packet;

                String message;
                if (loginPacket.getProtocol() < ProtocolInfo.CURRENT_PROTOCOL) {
                    if (loginPacket.getProtocol() < ProtocolInfo.CURRENT_PROTOCOL) {
                        message = "disconnectionScreen.outdatedClient";

                        this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_CLIENT);
                    } else {
                        message = "disconnectionScreen.outdatedServer";

                        this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_SERVER);
                    }
                    this.close("", message, false);
                    break;
                }

                this.protocol = loginPacket.protocol;
                this.username = TextFormat.clean(loginPacket.username);
                this.originName = this.username;
                this.displayName = this.username;
                this.iusername = this.username.toLowerCase();
                this.setDataProperty(new StringEntityData(DATA_NAMETAG, this.username), false);

                setLoginChainData(ClientChainData12NetEase.read(loginPacket));
                if (this.loginChainData.getClientUUID() != null) {  //网易认证通过！
                    this.isNetEaseClient = true;
                    this.getServer().getLogger().notice(this.username + TextFormat.RED + " 中国版验证通过！");
                } else {  //国际版普通认证
                    try {
                        this.getServer().getLogger().notice(this.username + TextFormat.YELLOW + " 正在解析为国际版！");
                        setLoginChainData(ClientChainData12.read(loginPacket));
                    } catch (Exception e) {
                        this.getServer().getLogger().notice(this.username + TextFormat.RED + " 解析时出现问题，采用紧急解析方案！" + e.getMessage());
                        setLoginChainData(ClientChainData12Urgency.read(loginPacket));
                    }
                }
                if (this.server.getOnlinePlayers().size() >= this.server.getMaxPlayers() && this.kick(PlayerKickEvent.Reason.SERVER_FULL, "disconnectionScreen.serverFull", false)) {
                    break;
                }

                this.randomClientId = loginPacket.clientId;

                this.uuid = loginPacket.clientUUID;
                this.rawUUID = Binary.writeUUID(this.uuid);

                boolean valid = true;
                int len = loginPacket.username.length();
                if (len > 16 || len < 2) {
                    valid = false;
                }

                /*
                for (int i = 0; i < len && valid; i++) {
                    char c = loginPacket.username.charAt(i);
                    if ((c >= 'a' && c <= 'z') ||
                            (c >= 'A' && c <= 'Z') ||
                            (c >= '0' && c <= '9') ||
                            c == '_' || c == ' '
                            ) {
                        continue;
                    }

                    valid = false;
                    break;
                }*/

                if (!valid || Objects.equals(this.iusername, "rcon") || Objects.equals(this.iusername, "console")) {
                    this.close("", "disconnectionScreen.invalidName");

                    break;
                }

                if (!loginPacket.skin.isValid()) {
                    this.close("", "disconnectionScreen.invalidSkin");
                    break;
                } else {
                    this.setSkin(loginPacket.getSkinLegacy());
                }

                PlayerPreLoginEvent playerPreLoginEvent;
                this.server.getPluginManager().callEvent(playerPreLoginEvent = new PlayerPreLoginEvent(this, "Plugin reason"));
                if (playerPreLoginEvent.isCancelled()) {
                    this.close("", playerPreLoginEvent.getKickMessage());

                    break;
                }

                Player playerInstance = this;
                this.preLoginEventTask = new AsyncTask() {

                    private PlayerAsyncPreLoginEvent e;

                    @Override
                    public void onRun() {
                        e = new PlayerAsyncPreLoginEvent(playerInstance, username, uuid, getAddress(), getPort());
                        server.getPluginManager().callEvent(e);
                    }

                    @Override
                    public void onCompletion(Server server) {
                        playerInstance.completePreLoginEventTask(e);
                    }
                };

                this.server.getScheduler().scheduleAsyncTask(this.preLoginEventTask);

                this.processLogin();
                break;
            case ProtocolInfo.MAP_INFO_REQUEST_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                MapInfoRequestPacket pk = (MapInfoRequestPacket) packet;
                Item mapItem = null;

                for (Item item1 : this.inventory.getContents().values()) {
                    if (item1 instanceof ItemMap && ((ItemMap) item1).getMapId() == pk.mapId) {
                        mapItem = item1;
                    }
                }

                if (mapItem == null) {
                    final Player player = this;
                    this.getServer().getScheduler().scheduleAsyncTask(SynapseAPI.getInstance(), new AsyncTask() {
                        @Override
                        public void onRun() {
                            level.getBlockEntities().values().stream().filter(be -> be instanceof BlockEntityItemFrame && ((BlockEntityItemFrame) be).getItem() instanceof ItemMap && ((ItemMap) ((BlockEntityItemFrame) be).getItem()).getMapId() == pk.mapId)
                                    .forEach(be -> ((ItemMap) ((BlockEntityItemFrame) be).getItem()).sendImage(player));
                        }
                    });
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
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                LevelSoundEventPacket levelSoundEventPacket = (LevelSoundEventPacket) packet;
                SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
                        LevelSoundEventEnum.fromV12(levelSoundEventPacket.sound),
                        new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
                        levelSoundEventPacket.extraData,
                        levelSoundEventPacket.pitch,
                        "minecraft:player",
                        levelSoundEventPacket.isBabyMob,
                        levelSoundEventPacket.isGlobal);
                this.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    this.getLevel().getChunkPlayers(this.getFloorX() >> 4, this.getFloorZ() >> 4).values().stream()
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
            default:
                //Server.getInstance().getLogger().notice("Received Data Packet: " + packet.getClass().getSimpleName());
                super.handleDataPacket(packet);
        }

        dataPacketTiming.stopTiming();
        if (!handlePlayerDataPacketTimings.containsKey(packet.pid()))
            handlePlayerDataPacketTimings.put(packet.pid(), dataPacketTiming);

    }

    protected void setLoginChainData(LoginChainData loginChainData) {
    	this.loginChainData = loginChainData;
    }

    public String getOriginName() {
        return originName;
    }

    public long nextForceSpawn = System.currentTimeMillis();
    private final Timing updateSynapsePlayerTiming = TimingsManager.getTiming("updateSynapsePlayerTiming");

    @Override
    public boolean onUpdate(int currentTick) {
        this.updateSynapsePlayerTiming.startTiming();
        if (!this.isSynapseLogin) {
            boolean update = super.onUpdate(currentTick);
            this.updateSynapsePlayerTiming.stopTiming();
            return update;
        }

        /*if (this.loggedIn && this.synapseSlowLoginUntil != -1 && System.currentTimeMillis() >= this.synapseSlowLoginUntil) {
            this.processLogin();
        }*/
        boolean update = super.onUpdate(currentTick);
        this.updateSynapsePlayerTiming.stopTiming();
        return update;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public void sendCommandData() {
        AvailableCommandsPacket pk = new AvailableCommandsPacket();
        Map<String, CommandDataVersions> data = new HashMap<>();
        for (Command command : this.server.getCommandMap().getCommands().values()) {
            if (!command.testPermissionSilent(this)) {
                continue;
            }
            CommandDataVersions data0 = command.generateCustomCommandData(this);
            data.put(command.getName(), data0);
        }
        pk.commands = data;
        this.dataPacket(pk, true);
    }

    public SynapsePlayer setCleanTextColor(boolean cleanTextColor) {
        this.cleanTextColor = cleanTextColor;
        return this;
    }

    public boolean isCleanTextColor() {
        return cleanTextColor;
    }

    @Override
    public boolean dataPacket(DataPacket packet) {
        if (!this.isSynapseLogin) return super.dataPacket(packet);
        /*if (!this.isFirstTimeLogin && packet instanceof ResourcePacksInfoPacket) {
            this.processLogin();
            return -1;
        }*/
        packet = DataPacketEidReplacer.replace(packet, this.getId(), Long.MAX_VALUE);
        packet.setHelper(AbstractProtocol.fromRealProtocol(this.protocol).getHelper());

        DataPacketSendEvent ev = new DataPacketSendEvent(this, packet);
        this.server.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return false;
        }

        //packet.encode(); encoded twice?
        //this.server.getLogger().warning("Send to player: " + Binary.bytesToHexString(new byte[]{packet.getBuffer()[0]}) + "  len: " + packet.getBuffer().length);
        if (this.cleanTextColor) {
            if (packet.pid() == ProtocolInfo.TEXT_PACKET) {
                packet = packet.clone();
                if (packet instanceof TextPacket) {
                    ((TextPacket) packet).message = TextFormat.clean(((TextPacket) packet).message);
                } else if (packet instanceof TextPacket14) {
                    ((TextPacket14) packet).message = TextFormat.clean(((TextPacket14) packet).message);
                } else if (packet instanceof TextPacket17) {
                    ((TextPacket17) packet).message = TextFormat.clean(((TextPacket17) packet).message);
                } else if (packet instanceof TextPacket116100NE) {
                    ((TextPacket116100NE) packet).message = TextFormat.clean(((TextPacket116100NE) packet).message);
                } else if (packet instanceof TextPacket116100) {
                    ((TextPacket116100) packet).message = TextFormat.clean(((TextPacket116100) packet).message);
                }
            }
        }

        this.interfaz.putPacket(this, packet);
        return true;
    }

    @Override
    public boolean directDataPacket(DataPacket packet) {
        if (!this.isSynapseLogin) return super.directDataPacket(packet);

        packet = DataPacketEidReplacer.replace(packet, this.getId(), Long.MAX_VALUE);

        DataPacketSendEvent ev = new DataPacketSendEvent(this, packet);
        this.server.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return false;
        }

        this.interfaz.putPacket(this, packet, false, true);
        return true;
    }

    public void sendFullPlayerListData(boolean self) {
        FastPlayerListPacket pk = new FastPlayerListPacket();
        pk.sendTo = this.getUniqueId();
        pk.type = PlayerListPacket.TYPE_ADD;
        List<FastPlayerListPacket.Entry> entries = new ArrayList<>();
        for (Player p : this.getServer().getOnlinePlayers().values()) {
            if (!self && p == this) {
                continue;
            }
            entries.add(new FastPlayerListPacket.Entry(p.getUniqueId(), p.getId(), p.getDisplayName()));
        }

        pk.entries = entries.stream().toArray(FastPlayerListPacket.Entry[]::new);

        this.getSynapseEntry().sendDataPacket(pk);
    }

    public void sendLevelSoundEvent(LevelSoundEventEnum levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
        if (levelSound == null || levelSound.getV12() == -1) return;
        LevelSoundEventPacket pk = new LevelSoundEventPacket();
        pk.sound = levelSound.getV12();
        pk.x = (float) pos.x;
        pk.y = (float) pos.y;
        pk.z = (float) pos.z;
        pk.extraData = extraData;
        pk.pitch = pitch;
        pk.isBabyMob = isBabyMob;
        pk.isGlobal = isGlobal;
        this.dataPacket(pk);
    }

    /*
    @Override
    public void sendData(Player player, EntityMetadata data) {
        if (player != this || player.spawned) super.sendData(player, data);
    }*/

    public Long2ObjectMap<BlobTrack> getClientCacheTrack() {
        return null;
    }

    @Override
    public ArrayList<Item> getCreativeItems() {
        return CreativeItemsPalette.getCreativeItems(AbstractProtocol.fromRealProtocol(this.protocol));
    }

    public JsonObject getCachedExtra() {
        return cachedExtra;
    }

    public JsonObject getTransferExtra() {
        return transferExtra;
    }

    /**
     * 重写这个，主要是为了在开启网络广播玩家移动包的情况下，对玩家通过this.sendPosition来强制更新玩家位置
     */
    @Override
    protected void updateMovement() {
        double diffPosition = (this.x - this.lastX) * (this.x - this.lastX) + (this.y - this.lastY) * (this.y - this.lastY) + (this.z - this.lastZ) * (this.z - this.lastZ);
        double diffRotation = (this.yaw - this.lastYaw) * (this.yaw - this.lastYaw) + (this.pitch - this.lastPitch) * (this.pitch - this.lastPitch);

        double diffMotion = (this.motionX - this.lastMotionX) * (this.motionX - this.lastMotionX) + (this.motionY - this.lastMotionY) * (this.motionY - this.lastMotionY) + (this.motionZ - this.lastMotionZ) * (this.motionZ - this.lastMotionZ);

        if (diffPosition > 0.0001 || diffRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
            this.lastX = this.x;
            this.lastY = this.y;
            this.lastZ = this.z;

            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;

            this.sendPosition(new Vector3(x, y, z), yaw, pitch, MovePlayerPacket.MODE_NORMAL, this.getViewers().values().toArray(new Player[0]));
        }

        if (diffMotion > 0.0025 || (diffMotion > 0.0001 && this.getMotion().lengthSquared() <= 0.0001)) { //0.05 ** 2
            this.lastMotionX = this.motionX;
            this.lastMotionY = this.motionY;
            this.lastMotionZ = this.motionZ;

            this.addMotion(this.motionX, this.motionY, this.motionZ);
        }
    }

    public void sendNetworkSettings() {

    }
}
