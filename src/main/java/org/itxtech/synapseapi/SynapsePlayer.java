package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.*;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.*;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.itxtech.synapseapi.camera.CameraManager;
import org.itxtech.synapseapi.dialogue.NPCDialoguePlayerHandler;
import org.itxtech.synapseapi.event.player.SynapsePlayerConnectEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerJavaCustomPayloadEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerPreChatEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTransferEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerUnexpectedBehaviorEvent;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadMessenger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraInstruction;
import org.itxtech.synapseapi.multiprotocol.common.drawer.Shape;
import org.itxtech.synapseapi.multiprotocol.common.level.DimensionDefinition;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.TextPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12Urgency;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.TextPacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.TextPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.ChangeDimensionPacket12120;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;
import org.itxtech.synapseapi.messaging.java.JavaCustomPayloadEnvelope;
import org.itxtech.synapseapi.multiprotocol.protocol126.protocol.TextPacket126;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.TextPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.TextPacket17;
import org.itxtech.synapseapi.multiprotocol.utils.*;
import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLogoutPacket;
import org.itxtech.synapseapi.utils.BlobTrack;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.ClientData.Entry;
import org.itxtech.synapseapi.utils.DataPacketEidReplacer;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.itxtech.synapseapi.SynapseSharedConstants.DATA_VERSION;
import static org.itxtech.synapseapi.SynapseSharedConstants.NETWORK_STACK_LATENCY_TELEMETRY;

/**
 * Created by boybook on 16/6/24.
 */
public class SynapsePlayer extends Player {

    public static final int SYNAPSE_PLAYER_ENTITY_ID = 1;

    static final int INCOMING_PACKET_BATCH_PER_TICK = 2; // usually max 1 per tick, but transactions may arrive separately
    static final int INCOMING_PACKET_BATCH_MAX_BUDGET = 100 * INCOMING_PACKET_BATCH_PER_TICK; // enough to account for a 5-second lag spike

    protected UUID sessionId;
    public boolean isSynapseLogin;
    protected SynapseEntry synapseEntry;
    protected boolean isFirstTimeLogin;
    private boolean cleanTextColor;

    protected String originName;
    protected LoginChainData loginChainData;
    protected boolean betaClient;
    protected JsonObject cachedExtra = new JsonObject();
    protected final JsonObject transferExtra = new JsonObject();
    protected int dummyDimension;
    protected int transferDimension = -1;
    protected int loadingScreenId = ThreadLocalRandom.current().nextInt(0xffff, 0xfffffff);

    protected boolean emoting;

    int rakNetLatency;

    /**
     * At most this many more packets can be received.
     * If this reaches zero, any additional packets received will cause the player to be kicked from the server.
     * This number is increased every tick up to a maximum limit.
     *
     * @see #INCOMING_PACKET_BATCH_PER_TICK
     * @see #INCOMING_PACKET_BATCH_MAX_BUDGET
     */
    int incomingPacketBatchBudget = INCOMING_PACKET_BATCH_MAX_BUDGET;
    long lastPacketBudgetUpdateTimeNs;

    int violationIncomingThread;

    long lastAuthInputPacketTick = -1;
    float lastAuthInputY;
    float lastAuthInputYaw;
    float lastAuthInputPitch;

    public final List<byte[]> outboundQueue = new ArrayList<>();

    public SynapsePlayer(SourceInterface interfaz, SynapseEntry synapseEntry, Long clientID, InetSocketAddress socketAddress) {
        super(interfaz, clientID, socketAddress);
        this.synapseEntry = synapseEntry;
        this.isSynapseLogin = this.synapseEntry != null;
        lastPacketBudgetUpdateTimeNs = System.nanoTime();
    }

    public boolean isSynapseLogin() {
        return isSynapseLogin;
    }

    @Override
    public boolean isFirstTimeLogin() {
        return isFirstTimeLogin;
    }

    public AbstractProtocol getAbstractProtocol() {
        return AbstractProtocol.fromRealProtocol(getProtocol());
    }

    public boolean isNeedLevelChangeLoadScreen() {
        return false;
    }

    @Override
    public boolean isJavaClient() {
        return loginChainData != null
                && loginChainData.getViaProxyAuthToken() != null
                && !loginChainData.getViaProxyAuthToken().isEmpty();
    }

    @Override
    public boolean supportsExplicitItemUseHand() {
        return this.isJavaClient() && super.supportsExplicitItemUseHand();
    }

    protected final void rebindStartedJavaItemUseHand(boolean offhandAllowed, ItemUseHand interactionHand) {
        if (JavaItemUseRouting.shouldRebindStartedUse(
                offhandAllowed, interactionHand,
                this.isUsingItem(), this.getUsingItemHand())) {
            this.setUsingItem(true);
        }
    }

    /**
     * Java 客户端的 maybeBackOffFromEdge(adjustMovementForSneaking) 按 0.05 步进回退边缘，
     * 使其潜行站在虚空边缘时玩家中心可比基岩多探出近 0.05（实测 ~0.044）。服务端信任客户端上报的
     * playerPos、并用与基岩相同的 0.6 盒做"防止把方块放进玩家自身"判定时，身体侧的放置点会落在收缩盒
     * 覆盖范围外而漏判，于是 Java 客户端能放出基岩放不出的方块。
     * <p>
     * 这里仅当 {@code Java 客户端 + 潜行 + 在地面 + 脚下 footprint 部分悬空（边缘探出）} 时补偿：
     * 只朝"有支撑"的水平方向把判定盒外扩 {@link #JAVA_PLACEMENT_BOX_MARGIN}，绝不朝虚空/正前(悬空)侧扩。
     * 这样既复刻基岩对身体侧放置的拒绝，又不影响向前延伸搭路与普通贴方块放置。其余情况沿用默认 -0.01。
     */
    public static double JAVA_PLACEMENT_BOX_MARGIN = 0.01D;
    private static final double SUPPORT_PROBE_DEPTH = 0.06D; // 略大于探边步进，探脚下支撑

    @Override
    public AxisAlignedBB adjustPlacementSelfCollisionBox(AxisAlignedBB bb) {
        if (this.isJavaClient() && this.isSneaking() && this.isOnGround()) {
            boolean supXMin = supportedUnder(bb, -1, 0);
            boolean supXMax = supportedUnder(bb, 1, 0);
            boolean supZMin = supportedUnder(bb, 0, -1);
            boolean supZMax = supportedUnder(bb, 0, 1);
            boolean overhang = !(supXMin && supXMax && supZMin && supZMax); // 至少一侧悬空 = 边缘探出
            if (overhang) {
                double m = JAVA_PLACEMENT_BOX_MARGIN;
                double minX = bb.getMinX() - (supXMin ? m : 0);
                double maxX = bb.getMaxX() + (supXMax ? m : 0);
                double minZ = bb.getMinZ() - (supZMin ? m : 0);
                double maxZ = bb.getMaxZ() + (supZMax ? m : 0);
                // 在定向偏移结果上再叠加与 BE 默认一致的 -0.01 收缩
                return new SimpleAxisAlignedBB(minX, bb.getMinY(), minZ, maxX, bb.getMaxY(), maxZ)
                        .expand(-0.01, -0.01, -0.01);
            }
        }
        return bb.expand(-0.01, -0.01, -0.01);
    }

    // 在 bb 指定水平边的脚下薄条里探测是否有实心支撑
    private boolean supportedUnder(AxisAlignedBB bb, int sx, int sz) {
        double feetY = bb.getMinY();
        double minX = sx > 0 ? bb.getMaxX() - 1e-3 : bb.getMinX();
        double maxX = sx < 0 ? bb.getMinX() + 1e-3 : bb.getMaxX();
        double minZ = sz > 0 ? bb.getMaxZ() - 1e-3 : bb.getMinZ();
        double maxZ = sz < 0 ? bb.getMinZ() + 1e-3 : bb.getMaxZ();
        AxisAlignedBB probe = new SimpleAxisAlignedBB(minX, feetY - SUPPORT_PROBE_DEPTH, minZ, maxX, feetY, maxZ);
        return this.level.getCollisionCubes(this, probe, false).length > 0;
    }

    public int nextDummyDimension() {
        this.dummyDimension++;
        if (this.dummyDimension < DimensionDefinition.NETEASE_BUILTIN_DIMENSION_FIRST) {
            this.dummyDimension = DimensionDefinition.NETEASE_BUILTIN_DIMENSION_FIRST;
        } else if (this.dummyDimension > DimensionDefinition.NETEASE_BUILTIN_DIMENSION_LAST) {
            this.dummyDimension = DimensionDefinition.NETEASE_BUILTIN_DIMENSION_FIRST;
        }
        return this.dummyDimension;
    }

    public int nextLoadingScreenId() {
        return loadingScreenId++;
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
            super.handleDataPacket(SynapseAPI.getPacket(packet.cachedLoginPacket));
            return;
        }
        this.isFirstTimeLogin = packet.isFirstTime;
        this.cachedExtra = packet.extra;
        // 从上一个服务器传递过来的dummyDimension，用于发送子区块的时候使用
        if (this.cachedExtra != null && this.cachedExtra.has("dummyDimension")) {
            this.dummyDimension = this.cachedExtra.get("dummyDimension").getAsInt();
            this.getServer().getLogger().debug("[DummyDimension] 从上一服务端收到玩家 " + Optional.ofNullable(packet.extra).map(extra -> extra.get("username")).map(JsonElement::getAsString).orElse("null") + " 的dummyDimension: " + this.dummyDimension);
        }
        SynapsePlayerConnectEvent ev;
        this.server.getPluginManager().callEvent(ev = new SynapsePlayerConnectEvent(this, this.isFirstTimeLogin));
        if (!ev.isCancelled()) {
            this.protocol = packet.protocol;
            try {
                DataPacket pk = packet.decodedLoginPacket;
                if (pk == null) {
                    close("", "disconnect.loginFailed");
                    return;
                }
                if (pk instanceof org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) {
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).isFirstTimeLogin = packet.isFirstTime;
                    if (packet.extra.has("username")) ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).username = packet.extra.get("username").getAsString();
                    ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).clientUUID = packet.uuid;
                    if (packet.extra.has("xuid")) ((org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket) pk).xuid = packet.extra.get("xuid").getAsString();
                    this.isNetEaseClient = Optional.ofNullable(packet.extra.get("netease")).orElseGet(() -> new JsonPrimitive(false)).getAsBoolean();
                }
                this.handleDataPacket(pk);

                if (cachedExtra != null) {
                    JsonElement viewDistance = cachedExtra.get("viewDistance");
                    if (viewDistance != null) {
                        int distance = viewDistance.getAsInt();
                        if (distance >= 4 && distance <= 96) {
                            this.viewDistance = distance;
                            this.chunkRadius = Math.min(this.viewDistance, this.getMaxViewDistance());
                        }
                    }

                    JsonElement dataVersion = cachedExtra.get("DataVersion");
                    if (dataVersion != null && !checkDataVersion(dataVersion.getAsInt())) {
                        return;
                    }
                    JsonElement blocksChecksum = cachedExtra.get("blocks_checksum");
                    if (blocksChecksum != null && !checkBlockRegistryChecksum(blocksChecksum.getAsLong())) {
                        return;
                    }
                    JsonElement itemsChecksum = cachedExtra.get("items_checksum");
                    if (itemsChecksum != null && !checkItemRegistryChecksum(itemsChecksum.getAsLong())) {
                        return;
                    }
                    JsonElement biomesChecksum = cachedExtra.get("biomes_checksum");
                    if (biomesChecksum != null && !checkBiomeRegistryChecksum(biomesChecksum.getAsLong())) {
                        return;
                    }
                    JsonElement entitiesChecksum = cachedExtra.get("entities_checksum");
                    if (entitiesChecksum != null && !checkEntityRegistryChecksum(entitiesChecksum.getAsLong())) {
                        return;
                    }
                    JsonElement camerasChecksum = cachedExtra.get("cameras_checksum");
                    if (camerasChecksum != null && !checkCameraRegistryChecksum(camerasChecksum.getAsLong())) {
                        return;
                    }
                }
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
                this.close("", "disconnectionScreen.internalError.cantConnect");
            }
        }
    }

    protected boolean checkDataVersion(int previousDataVersion) {
        if (DATA_VERSION == previousDataVersion) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的数据版本 {} 与本服 {} 不同", getName(), previousDataVersion, DATA_VERSION);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    protected boolean checkBlockRegistryChecksum(long previousChecksum) {
        long checksum = AdvancedGlobalBlockPalette.getBlockRegistryChecksum();
        if (checksum == previousChecksum) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的方块注册表 {} 与本服 {} 不同", getName(), previousChecksum, checksum);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    protected boolean checkItemRegistryChecksum(long previousChecksum) {
        long checksum = AdvancedRuntimeItemPalette.getItemRegistryChecksum();
        if (checksum == previousChecksum) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的物品注册表 {} 与本服 {} 不同", getName(), previousChecksum, checksum);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    protected boolean checkBiomeRegistryChecksum(long previousChecksum) {
        long checksum = BiomeDefinitions.getBiomeRegistryChecksum();
        if (checksum == previousChecksum) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的生物群系注册表 {} 与本服 {} 不同", getName(), previousChecksum, checksum);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    protected boolean checkEntityRegistryChecksum(long previousChecksum) {
        long checksum = AvailableEntityIdentifiersPalette.getEntityRegistryChecksum();
        if (checksum == previousChecksum) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的实体注册表 {} 与本服 {} 不同", getName(), previousChecksum, checksum);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    protected boolean checkCameraRegistryChecksum(long previousChecksum) {
        long checksum = CameraManager.getCameraRegistryChecksum();
        if (checksum == previousChecksum) {
            return true;
        }

        SynapseAPI.getInstance().getLogger().info("玩家 {} 触发原生跨服由于先前的相机注册表 {} 与本服 {} 不同", getName(), previousChecksum, checksum);
        rejoinGame("disconnectionScreen.blockMismatch");
        return false;
    }

    public void rejoinGame() {
        rejoinGame("disconnectionScreen.restartClient");
    }

    public void rejoinGame(String reason) {
        String address = getLoginChainData().getServerAddress();
        if (address == null || !address.contains(":")) {
            close("", reason);
            return;
        }

        String[] split = address.split(":", 2);
        TransferPacket packet = new TransferPacket();
        packet.address = split[0];
        packet.port = Integer.parseInt(split[1]);
        dataPacket(packet);
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
            this.kick(PlayerKickEvent.Reason.NOT_WHITELISTED, "Server is white-listed", false);

            return;
        } else if (this.isBanned()) {
            this.kick(PlayerKickEvent.Reason.NAME_BANNED, "You are banned", false);
            return;
        } else if (this.server.getIPBans().isBanned(this.getAddress())) {
            this.kick(PlayerKickEvent.Reason.IP_BANNED, "You are banned", false);
            return;
        }

        if (this.hasPermission(Server.BROADCAST_CHANNEL_USERS)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, this);
        }
        if (this.hasPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)) {
            this.server.getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this);
        }

        for (Player p : this.server.getOnlinePlayerList()) {
            if (p != this && (p.getName() != null && p.getName().equalsIgnoreCase(this.getName()) ||
                    this.getUniqueId().equals(p.getUniqueId()))) {
                p.kick(PlayerKickEvent.Reason.NEW_CONNECTION, "disconnectionScreen.loggedinOtherLocation", false);

                this.close("", "disconnectionScreen.serverIdConflict");

                PlayerLogoutPacket pk = new PlayerLogoutPacket();
                pk.sessionId = getSessionId();
                pk.reason = "disconnectionScreen.serverIdConflict";
                this.getSynapseEntry().sendDataPacket(pk);
                return;
            }
        }

        CompoundTag nbt = offlinePlayerData != null ? offlinePlayerData : this.server.getOfflinePlayerData(this.username);
        if (nbt == null) {
            this.close("", "disconnectionScreen.worldCorruption");

            return;
        }

        this.playedBefore = (nbt.getLong("lastPlayed") - nbt.getLong("firstPlayed")) > 1;

        boolean alive = true;

        nbt.putString("NameTag", this.username);

        if (0 >= nbt.getFloat("Health")) {
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
                .set(Type.WORLD_IMMUTABLE, isAdventure() || isSpectator())
                .set(Type.MINE, !isAdventure() && !isSpectator())
                .set(Type.BUILD, !isAdventure() && !isSpectator())
                .set(Type.AUTO_JUMP, true)
                .set(Type.ALLOW_FLIGHT, isCreative() || isSpectator())
                .set(Type.NO_CLIP, isSpectator())
                .set(Type.FLYING, isSpectator())
                .set(Type.NO_PVM, isSpectator())
                .set(Type.NO_MVP, isSpectator())
                .set(Type.DOORS_AND_SWITCHED, !isSpectator())
                .set(Type.OPEN_CONTAINERS, !isSpectator())
                .set(Type.ATTACK_PLAYERS, !isSpectator())
                .set(Type.ATTACK_MOBS, !isSpectator())
                .set(Type.INSTABUILD, isCreative() || isSpectator())
                .set(Type.INVULNERABLE, isCreative() || isSpectator())
                .set(Type.OPERATOR, isOp())
                .set(Type.TELEPORT, hasPermission("nukkit.command.teleport"));

        Level oldLevel = this.level;
        Level level = this.server.getLevelByName(nbt.getString("Level"));
        if (level != null) {
            level = level.getDimension(Dimension.byIdOrDefault(nbt.getInt("DimensionId", DimensionID.OVERWORLD))) ;
        }
        if (level == null || !alive) {
            this.setLevel(this.server.getDefaultLevel());
            nbt.putString("Level", this.level.getName());
            nbt.putInt("DimensionId", this.level.getDimension().getId());
            nbt.getList("Pos", DoubleTag.class)
                    .add(new DoubleTag("", this.level.getSpawnLocation().x))
                    .add(new DoubleTag("", this.level.getSpawnLocation().y))
                    .add(new DoubleTag("", this.level.getSpawnLocation().z));
        } else {
            this.setLevel(level);
        }
        if (oldLevel != this.level) {
            oldLevel.onPlayerRemove(this);
            this.level.onPlayerAdd(this);
        }

        nbt.putLong("lastPlayed", System.currentTimeMillis() / 1000);

        if (this.server.getAutoSave()) {
            this.server.saveOfflinePlayerData(this.username, nbt, true);
        }

        this.sendPlayStatus(PlayStatusPacket.LOGIN_SUCCESS); // 发送status=0的PlayStatusPacket后客户端会回一个ClientCacheStatusPacket
        this.server.onPlayerLogin(this);
        ListTag<DoubleTag> posList = nbt.getList("Pos", DoubleTag.class);

        super.init(this.level.getChunk((int) posList.get(0).data >> 4, (int) posList.get(2).data >> 4, true), nbt);

        if (!this.namedTag.contains("foodLevel")) {
            this.namedTag.putInt("foodLevel", 20);
        }
        int foodLevel = this.namedTag.getInt("foodLevel");
        if (!this.namedTag.contains("foodSaturationLevel")) {
            this.namedTag.putFloat("foodSaturationLevel", 5);
        }
        float foodSaturationLevel = this.namedTag.getFloat("foodSaturationLevel");
        this.foodData = new PlayerFood(this, foodLevel, foodSaturationLevel);

        if (this.isSpectator()) {
            this.keepMovement = true;
            this.onGround = false;
        }

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

        this.inventory.setHeldItemIndex(0);

        if (this.isSpectator()) {
            this.keepMovement = true;
            this.onGround = false;
        }

        Level level;
        if (this.spawnPosition == null && this.namedTag.contains("SpawnLevel") && (level = this.server.getLevelByName(this.namedTag.getString("SpawnLevel"))) != null) {
            level = level.getDimension(Dimension.byIdOrDefault(this.namedTag.getInt("SpawnDimension", DimensionID.OVERWORLD))) ;
            this.spawnPosition = new Position(this.namedTag.getInt("SpawnX"), this.namedTag.getInt("SpawnY"), this.namedTag.getInt("SpawnZ"), level);
        }
        if (this.spawnBlockPosition == null && this.namedTag.contains("SpawnBlockPositionLevel")) {
            level = this.server.getLevelByName(this.namedTag.getString("SpawnBlockPositionLevel"));
            if (level != null) {
                this.spawnBlockPosition = new Position(this.namedTag.getInt("SpawnBlockPositionX"), this.namedTag.getInt("SpawnBlockPositionY"), this.namedTag.getInt("SpawnBlockPositionZ"), level);
            }
        }

        Position spawnPosition = this.getSpawn();
        int dimension = this.level.getDimension().getId();
        if (this.isFirstTimeLogin) {
            dummyDimension = dimension;

            tryDisruptIllegalClientBeforeStartGame();

            sendDimensionData();
            sendJigsawStructureData();
            sendVoxelShapes();

            DataPacket startGamePacket = generateStartGamePacket(spawnPosition);
            this.dataPacket(startGamePacket);

            this.syncEntityProperties();
            this.sendItemComponents();
            this.sendBiomeDefinitionList();
            this.sendAvailableEntityIdentifiers();
            this.initializeWorldClocks();
            this.syncFeatureRegistry();
            this.sendCameraPresets();
        } else {
            this.locallyInitialized = true;
            sentSkins.add(getUniqueId());

            if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                ackPacket.entityId = getId();
                dataPacket(ackPacket);
            } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                ackPacket.entityId = getId();
                dataPacket(ackPacket);
            }

            if (this.isJavaClient()) {
                // Java 客户端：始终发送真实维度以退出 loading 状态
                // ViaBedrock 通过备用维度名称处理同维度切换
                dummyDimension = dimension;

                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    ChangeDimensionPacket12120 changeDimensionPacket = new ChangeDimensionPacket12120();
                    changeDimensionPacket.dimension = getDummyDimension();
                    changeDimensionPacket.x = (float) getX();
                    changeDimensionPacket.y = (float) getY();
                    changeDimensionPacket.z = (float) getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket);
                } else {
                    ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                    changeDimensionPacket.dimension = getDummyDimension();
                    changeDimensionPacket.x = (float) getX();
                    changeDimensionPacket.y = (float) getY();
                    changeDimensionPacket.z = (float) getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket);
                }

                PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                ackPacket.entityId = getId();
                dataPacket(ackPacket);
            } else if (dimension != DimensionID.OVERWORLD && getDummyDimension() != DimensionID.OVERWORLD) {
                forceSendEmptyChunks(chunkRadius, 0, 0, getDummyDimension());

                dummyDimension = dimension;

                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    ChangeDimensionPacket12120 changeDimensionPacket = new ChangeDimensionPacket12120();
                    changeDimensionPacket.dimension = getDummyDimension();
                    changeDimensionPacket.x = (float) getX();
                    changeDimensionPacket.y = (float) getY();
                    changeDimensionPacket.z = (float) getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket);
                } else {
                    ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                    changeDimensionPacket.dimension = getDummyDimension();
                    changeDimensionPacket.x = (float) getX();
                    changeDimensionPacket.y = (float) getY();
                    changeDimensionPacket.z = (float) getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket);
                }

                if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                    PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                    ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                    PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                    ackPacket.action = PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                }
            }

            sendPosition(this, yaw, pitch, MovePlayerPacket.MODE_TELEPORT);

            GameRulesChangedPacket packet = new GameRulesChangedPacket();
            packet.gameRules = this.level.getGameRules();
            this.dataPacket(packet);

            SetPlayerGameTypePacket pk = new SetPlayerGameTypePacket();
            pk.gamemode = getClientFriendlyGamemode(gamemode);
            this.dataPacket(pk);

            long flags = 1 << Entity.DATA_FLAG_IMMOBILE;
            this.sendData(this, new EntityMetadata().putLong(Entity.DATA_FLAGS, flags));

            ChunkRadiusUpdatedPacket dp = new ChunkRadiusUpdatedPacket();
            dp.radius = viewDistance;
            dataPacket(dp);

            unlockInput();
//            clearCameraInstruction();
            showHud();
            clearAimAssist();
            resetControlScheme();
        }
        this.sendFogStack();

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

        spawnPosition.level.sendDifficulty(this);

        this.setEnableClientCommand(true);
        this.getAdventureSettings().update();

        this.setMovementSpeed(DEFAULT_SPEED, false);
        this.sendAttributes();
        //this.setNameTagVisible(true);
        this.setDataFlag(DATA_FLAG_CAN_SHOW_NAMETAG, true, false);
        //this.setNameTagAlwaysVisible(true);
        this.dataProperties.putBoolean(DATA_ALWAYS_SHOW_NAMETAG, true);
        this.setDataFlag(DATA_FLAG_ALWAYS_SHOW_NAMETAG, true, false);
        //this.setCanClimb(true);
        this.setDataFlag(DATA_FLAG_CAN_CLIMB, true, false);
        if (this.isSpectator()) {
            this.setDataFlag(DATA_FLAG_SILENT, true, false);
            this.setDataFlag(DATA_FLAG_HAS_COLLISION, false, false);
            this.setDataFlag(DATA_FLAG_PUSH_TOWARDS_CLOSEST_SPACE, false, false);
        } else {
            this.setDataFlag(DATA_FLAG_SILENT, false, false);
            this.setDataFlag(DATA_FLAG_HAS_COLLISION, true, false);
            this.setDataFlag(DATA_FLAG_PUSH_TOWARDS_CLOSEST_SPACE, true, false);
        }

        this.server.getLogger().info(this.getServer().getLanguage().translate("nukkit.player.logIn",
                TextFormat.AQUA + this.username + TextFormat.WHITE,
                this.getAddress(),
                this.getPort(),
                this.id,
                this.level.getName(),
                NukkitMath.round(this.x, 4),
                NukkitMath.round(this.y, 4),
                NukkitMath.round(this.z, 4)));

        if (this.isOp()) {
            this.setRemoveFormat(false);
        }

        this.forceMovement = this.teleportPosition = this.getPosition();

        this.server.addOnlinePlayer(this);
        this.server.onPlayerCompleteLoginSequence(this);

        if (NETWORK_STACK_LATENCY_TELEMETRY) {
            this.ping();
        }
    }

	protected DataPacket generateStartGamePacket(Position spawnPosition) {
		StartGamePacket startGamePacket = new StartGamePacket();
		startGamePacket.entityUniqueId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.entityRuntimeId = SYNAPSE_PLAYER_ENTITY_ID;
		startGamePacket.playerGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.x = (float) this.x;
		startGamePacket.y = (float) this.y;
		startGamePacket.z = (float) this.z;
		startGamePacket.yaw = (float) this.yaw;
		startGamePacket.pitch = (float) this.pitch;
		startGamePacket.seed = -1;
		startGamePacket.dimension = (byte) (this.level.getDimension().getId() & 0xff);
		startGamePacket.worldGamemode = getClientFriendlyGamemode(this.gamemode);
		startGamePacket.difficulty = this.level.getDifficulty();
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
        this.dataPacket(CraftingPacketManager.getCachedCraftingPacket(AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient()));
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
            // this.sendMessage(TextFormat.GRAY + " -> " + clientData.getDescription());
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

            this.clearSubChunkQueues();

            this.removeAllChunks();

            this.removeAllShapes();

            if (!this.sentSkins.isEmpty()) {  // 跨服时，移除所有已发送的PlayerList（皮肤）
                PlayerListPacket pk = new PlayerListPacket();
                pk.type = PlayerListPacket.TYPE_REMOVE;
                pk.entries = this.sentSkins.stream()
                        .filter(uuid -> !uuid.equals(getUniqueId()))
                        .map(PlayerListPacket.Entry::new)
                        .toArray(PlayerListPacket.Entry[]::new);
                this.dataPacket(pk);
                this.sentSkins.clear();
            }

            // 用于连续跨服的情况下，防止卡在loading screen
            boolean isLevelChanging = this.isLevelChange || !this.spawned;

            this.getDummyBossBars().values().forEach(DummyBossBar::destroy);
            this.getDummyBossBars().clear();
            this.teleportPosition = null;
            this.isLevelChange = true;

            preChangeDimensionScreen(true);

            if (this.isJavaClient()) {
                // Java/ViaBedrock does not need the Bedrock preload loading screen.
                // Only pass the current dummy dimension to the target server for chunk streaming.
                this.transferExtra.addProperty("dummyDimension", this.dummyDimension);
            } else if (this.isNeedLevelChangeLoadScreen()) {
                if (!isLevelChanging) {
                    transferDimension = this.nextDummyDimension();
                }

                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    ChangeDimensionPacket12120 changeDimensionPacket1 = new ChangeDimensionPacket12120();
                    changeDimensionPacket1.dimension = this.getDummyDimension();
                    changeDimensionPacket1.x = 0;
                    changeDimensionPacket1.y = 32767;
                    changeDimensionPacket1.z = 0;
                    changeDimensionPacket1.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket1);
                } else {
                    ChangeDimensionPacket changeDimensionPacket1 = new ChangeDimensionPacket();
                    changeDimensionPacket1.dimension = this.getDummyDimension();
                    changeDimensionPacket1.x = 0;
                    changeDimensionPacket1.y = 32767;
                    changeDimensionPacket1.z = 0;
                    changeDimensionPacket1.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket1);
                }

                // 传递给下一个服务器玩家的虚拟维度信息
                this.transferExtra.addProperty("dummyDimension", this.dummyDimension);
/*
                if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                    PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                    ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                    PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                    ackPacket.action = PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                }
*/
            } else if (this.getDummyDimension() != DimensionID.OVERWORLD) {
                this.dummyDimension = DimensionID.OVERWORLD;

                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    ChangeDimensionPacket12120 changeDimensionPacket1 = new ChangeDimensionPacket12120();
                    changeDimensionPacket1.dimension = this.getDummyDimension();
                    changeDimensionPacket1.x = 0;
                    changeDimensionPacket1.y = 32767;
                    changeDimensionPacket1.z = 0;
                    changeDimensionPacket1.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket1);
                } else {
                    ChangeDimensionPacket changeDimensionPacket1 = new ChangeDimensionPacket();
                    changeDimensionPacket1.dimension = this.getDummyDimension();
                    changeDimensionPacket1.x = 0;
                    changeDimensionPacket1.y = 32767;
                    changeDimensionPacket1.z = 0;
                    changeDimensionPacket1.loadingScreenId = nextLoadingScreenId();
                    dataPacket(changeDimensionPacket1);
                }
/*
                if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                    PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                    ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                    PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                    ackPacket.action = PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                }
*/
            }

            Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                @Override
                public void onRun(int currentTick) {
                    org.itxtech.synapseapi.network.protocol.spp.TransferPacket pk = new org.itxtech.synapseapi.network.protocol.spp.TransferPacket();
                    pk.sessionId = getSessionId();
                    pk.clientHash = hash;
                    pk.extra = transferExtra;
                    pk.extra.addProperty("username", originName);
                    pk.extra.addProperty("xuid", getLoginChainData().getXUID());
                    pk.extra.addProperty("netease", isNetEaseClient());
                    pk.extra.addProperty("blob_cache", getClientCacheTrack() != null);
                    //跨服时，传输到目标服务器当前玩家安装上的材质
                    JsonArray resPacks = new JsonArray();
                    getResourcePacks().keySet().forEach(resPacks::add);
                    pk.extra.add("res_packs", resPacks);
                    JsonArray behPacks = new JsonArray();
                    getResourcePacks().keySet().forEach(behPacks::add);
                    pk.extra.add("beh_packs", behPacks);
                    pk.extra.addProperty("viewDistance", viewDistance);
                    pk.extra.addProperty("viewDistanceMax", getClientMaxViewDistance());
                    pk.extra.addProperty("DataVersion", DATA_VERSION);
                    pk.extra.addProperty("blocks_checksum", AdvancedGlobalBlockPalette.getBlockRegistryChecksum());
                    pk.extra.addProperty("items_checksum", AdvancedRuntimeItemPalette.getItemRegistryChecksum());
                    pk.extra.addProperty("biomes_checksum", BiomeDefinitions.getBiomeRegistryChecksum());
                    pk.extra.addProperty("entities_checksum", AvailableEntityIdentifiersPalette.getEntityRegistryChecksum());
                    pk.extra.addProperty("cameras_checksum", CameraManager.getCameraRegistryChecksum());
                    getSynapseEntry().sendDataPacket(pk);
                }
            }, 1);
            // this.sendMessage(TextFormat.GRAY + "(synapse) -> " + clientData.getDescription());

            return true;
        }
        this.sendMessage(TextFormat.GRAY + "[transfer] client not found");
        return false;
    }

    @Override
    public void transfer(InetSocketAddress address) {
        String hostName = address.getHostString();
        int port = address.getPort();
        TransferPacket pk = new TransferPacket();
        pk.address = hostName;
        pk.port = port;
        this.dataPacket(pk);
        //String message = "Transferred to " + hostName + ":" + port;
        //this.close(message, message, false);
    }

    @Override
    protected boolean checkTeleportPosition() {
        if (this.teleportPosition != null) {

            int chunkX = (int) this.teleportPosition.x >> 4;
            int chunkZ = (int) this.teleportPosition.z >> 4;

            long centerChunkIndex = Level.chunkHash(chunkX, chunkZ);
            if (!this.usedChunks.get(centerChunkIndex)) {
                if (this.teleportChunkLoaded) {
                    this.lastImmobile = this.isImmobile();
                }

                this.teleportChunkIndex = centerChunkIndex;
                this.teleportChunkLoaded = false;
            }

            for (int X = -1; X <= 1; ++X) {
                for (int Z = -1; Z <= 1; ++Z) {
                    long index = Level.chunkHash(chunkX + X, chunkZ + Z);
                    if (!this.usedChunks.get(index)) {
                        return false;
                    }
                }
            }

            //Weather
            this.getLevel().sendWeather(this);
            //Update time
            this.getLevel().sendTime(this);

            this.getServer().getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), () -> this.getLevel().sendTime(this), 10);

            if (this.isLevelChange) {
                PlayStatusPacket statusPacket0 = new PlayStatusPacket();
                statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
                this.dataPacket(statusPacket0);

                //DummyBossBar
                this.getDummyBossBars().values().forEach(DummyBossBar::reshow);
            }

            this.spawnToAll();
            this.isLevelChange = false;
            this.forceMovement = this.teleportPosition;
            this.teleportPosition = null;
            return true;
        }

        return false;
    }

    @Override
    protected void forceSendEmptyChunks(int chunkRadius, int centerChunkX, int centerChunkZ) {
        this.forceSendEmptyChunks(this.chunkRadius, centerChunkX, centerChunkZ, getDummyDimension());
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

        boolean isLevelChanging = this.isLevelChange;
        if (location.level != null && from.getLevel() != location.level) {
            this.getDummyBossBars().values().forEach(DummyBossBar::destroy);  //游戏崩溃问题
            for (Entity entity : this.getLevel().getEntities()) {
                if (entity.getViewers().containsKey(this.getLoaderId())) {
                    entity.despawnFrom(this);
                }
            }
            this.isLevelChange = true;
        }
        if (super.teleport(location, cause)) {
            if (location.level != null && from.getLevel() != location.level && this.spawned) {
                preChangeDimensionScreen(false);

                Dimension newDimension = location.level.getDimension();

                if (this.isJavaClient()) {
                    // Java 客户端：始终发送真实维度
                    // ViaBedrock 通过备用维度名称处理同维度切换
                    this.dummyDimension = newDimension.getId();
                    this.isLevelChange = true;

                    if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                        ChangeDimensionPacket12120 changeDimensionPacket = new ChangeDimensionPacket12120();
                        changeDimensionPacket.dimension = this.getDummyDimension();
                        changeDimensionPacket.x = (float) this.getX();
                        changeDimensionPacket.y = (float) this.getY();
                        changeDimensionPacket.z = (float) this.getZ();
                        changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                        this.dataPacket(changeDimensionPacket);
                    } else {
                        ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                        changeDimensionPacket.dimension = this.getDummyDimension();
                        changeDimensionPacket.x = (float) this.getX();
                        changeDimensionPacket.y = (float) this.getY();
                        changeDimensionPacket.z = (float) this.getZ();
                        changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                        this.dataPacket(changeDimensionPacket);
                    }

                    PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                    ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);

                    postTeleport(true);
                    return true;
                }

                if (!this.isNeedLevelChangeLoadScreen() || newDimension != Dimension.OVERWORLD) {
                    if (from.getLevel().getDimension() != newDimension) {
                        this.dummyDimension = newDimension.getId();

                        if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                            ChangeDimensionPacket12120 changeDimensionPacket = new ChangeDimensionPacket12120();
                            changeDimensionPacket.dimension = this.getDummyDimension();
                            changeDimensionPacket.x = (float) this.getX();
                            changeDimensionPacket.y = (float) this.getY();
                            changeDimensionPacket.z = (float) this.getZ();
                            changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                            this.dataPacket(changeDimensionPacket);
                        } else {
                            ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                            changeDimensionPacket.dimension = this.getDummyDimension();
                            changeDimensionPacket.x = (float) this.getX();
                            changeDimensionPacket.y = (float) this.getY();
                            changeDimensionPacket.z = (float) this.getZ();
                            changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                            this.dataPacket(changeDimensionPacket);
                        }

                        if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                            PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                            ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                            ackPacket.entityId = getId();
                            dataPacket(ackPacket);
                        } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                            PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                            ackPacket.action = PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK;
                            ackPacket.entityId = getId();
                            dataPacket(ackPacket);
                        }
                    }

                    postTeleport(true);
                    return true;
                }

                // 用于连续切换世界的情况下，防止卡在loading screen
                if (!isLevelChanging) {
                    this.nextDummyDimension();
                }
                this.isLevelChange = true;

                if (getProtocol() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    ChangeDimensionPacket12120 changeDimensionPacket = new ChangeDimensionPacket12120();
                    changeDimensionPacket.dimension = this.getDummyDimension();
                    changeDimensionPacket.x = (float) this.getX();
                    changeDimensionPacket.y = (float) this.getY();
                    changeDimensionPacket.z = (float) this.getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    this.dataPacket(changeDimensionPacket);
                } else {
                    ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                    changeDimensionPacket.dimension = this.getDummyDimension();
                    changeDimensionPacket.x = (float) this.getX();
                    changeDimensionPacket.y = (float) this.getY();
                    changeDimensionPacket.z = (float) this.getZ();
                    changeDimensionPacket.loadingScreenId = nextLoadingScreenId();
                    this.dataPacket(changeDimensionPacket);
                }

                if (getProtocol() >= AbstractProtocol.PROTOCOL_119_50.getProtocolStart()) {
                    PlayerActionPacket119 ackPacket = new PlayerActionPacket119();
                    ackPacket.action = PlayerActionPacket.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                } else if (isNetEaseClient() && getProtocol() >= AbstractProtocol.PROTOCOL_118.getProtocolStart()) {
                    PlayerActionPacket14 ackPacket = new PlayerActionPacket14();
                    ackPacket.action = PlayerActionPacket14.ACTION_DIMENSION_CHANGE_ACK;
                    ackPacket.entityId = getId();
                    dataPacket(ackPacket);
                }

                StopSoundPacket stopSoundPacket = new StopSoundPacket();
                stopSoundPacket.name = "portal.travel";
                stopSoundPacket.stopAll = false;
                this.dataPacket(stopSoundPacket);
            }

            postTeleport(true);
            return true;
        }

        return false;
    }

    @Override
    protected void postTeleport(boolean synapse) {
        if (!synapse) {
            return;
        }
        super.postTeleport(synapse);
    }

    @Override
    protected boolean orderChunks() {
        if (!allowOrderChunks) return false;
        return super.orderChunks();
    }

    /*@Override
    public void spawnTo(Player player) {
        if (this.getSkin().isPersona() && player.isNetEaseClient() && player.getProtocol() == AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            // 2026-02 中国版3.7BUG：如果是persona皮肤，则需要重发，否则会显示为史蒂夫
            this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, player);
        }
        super.spawnTo(player);
    }*/

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

        switch (packet.pid()) {
            case ProtocolInfo.LOGIN_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                if (this.loggedIn) {
                    onPacketViolation(PacketViolationReason.ALREADY_LOGGED_IN);
                    break;
                }

                LoginPacket loginPacket = (LoginPacket) packet;

                this.protocol = loginPacket.protocol;

                if (loginPacket.username != null) {
                    this.username = TextFormat.clean(loginPacket.username);
                    this.originName = this.username;
                    this.displayName = this.username;
                    this.iusername = this.username.toLowerCase();
                }

                if (loginPacket.getProtocol() < AbstractProtocol.FIRST_ALLOW_LOGIN_PROTOCOL.getProtocolStart()) {
                    this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_CLIENT);
                    this.close("", "disconnectionScreen.outdatedClient");
                    break;
                }
                if (loginPacket.getProtocol() > AbstractProtocol.LAST_ALLOW_LOGIN_PROTOCOL.getProtocolStart()) {
                    this.sendPlayStatus(PlayStatusPacket.LOGIN_FAILED_SERVER);
                    this.close("", "disconnectionScreen.outdatedServer");
                    break;
                }

                this.setDataProperty(new StringEntityData(DATA_NAMETAG, this.username), false);

                setLoginChainData(ClientChainData12NetEase.read(loginPacket));
                if (this.loginChainData.getClientUUID() != null) {  //网易认证通过！
                    this.isNetEaseClient = true;
                    this.getServer().getLogger().info(this.username + TextFormat.RED + " 中国版验证通过！");
                } else {  //国际版普通认证
                    try {
                        this.getServer().getLogger().info(this.username + TextFormat.YELLOW + " 正在解析为国际版！");
                        setLoginChainData(ClientChainData12.read(loginPacket));
                    } catch (Exception e) {
                        this.getServer().getLogger().info(this.username + TextFormat.RED + " 解析时出现问题，采用紧急解析方案！", e);
                        setLoginChainData(ClientChainData12Urgency.read(loginPacket));
                    }
                }
                if (this.server.getOnlinePlayerCount() >= this.server.getMaxPlayers() && this.kick(PlayerKickEvent.Reason.SERVER_FULL, "disconnectionScreen.serverFull", false)) {
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
                    this.setSkin(loginPacket.skin);
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

                this.server.getScheduler().scheduleAsyncTask(SynapseAPI.getInstance(), this.preLoginEventTask);

                this.processLogin();
                break;
            case ProtocolInfo.MAP_INFO_REQUEST_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                MapInfoRequestPacket pk = (MapInfoRequestPacket) packet;
                Item mapItem = null;

                for (Item item1 : this.offhandInventory.getContents().values()) {
                    if (item1 instanceof ItemMap && ((ItemMap) item1).getMapId() == pk.mapId) {
                        mapItem = item1;
                    }
                }

                if (mapItem == null) {
                    for (Item item1 : this.inventory.getContents().values()) {
                        if (item1 instanceof ItemMap && ((ItemMap) item1).getMapId() == pk.mapId) {
                            mapItem = item1;
                        }
                    }
                }

                if (mapItem == null) {
                    for (BlockEntity be : this.level.getBlockEntities().values()) {
                        if (be instanceof BlockEntityItemFrame) {
                            BlockEntityItemFrame itemFrame1 = (BlockEntityItemFrame) be;

                            if (itemFrame1.getItem() instanceof ItemMap && ((ItemMap) itemFrame1.getItem()).getMapId() == pk.mapId) {
                                ((ItemMap) itemFrame1.getItem()).sendImage(this);
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
            case ProtocolInfo.SCRIPT_MESSAGE_PACKET:
                if (!callPacketReceiveEvent(packet) || !this.isJavaClient()
                        || !(packet instanceof ScriptMessagePacket11810 scriptMessagePacket)) {
                    break;
                }
                JavaCustomPayloadEnvelope.decode(scriptMessagePacket.messageId, scriptMessagePacket.value)
                        .ifPresent(payload -> {
                            JavaCustomPayloadMessenger messenger = SynapseAPI.getInstance().getJavaCustomPayloadMessenger();
                            messenger.dispatchIncomingMessage(this, payload.channel(), payload.payload());
                            this.server.getPluginManager().callEvent(
                                    new SynapsePlayerJavaCustomPayloadEvent(this, payload.channel(), payload.payload()));
                        });
                break;
            /*case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
                if (!callPacketReceiveEvent(packet)) break;
                LevelSoundEventPacket levelSoundEventPacket = (LevelSoundEventPacket) packet;
                LevelSoundEventEnum sound = LevelSoundEventEnum.fromV12(levelSoundEventPacket.sound);
                SynapsePlayerBroadcastLevelSoundEvent event = new SynapsePlayerBroadcastLevelSoundEvent(this,
                        sound,
                        new Vector3(levelSoundEventPacket.x, levelSoundEventPacket.y, levelSoundEventPacket.z),
                        sound.translateExtraDataFromClient(levelSoundEventPacket.extraData, AbstractProtocol.fromRealProtocol(getProtocol()), isNetEaseClient()),
                        levelSoundEventPacket.pitch,
                        EntityFullNames.PLAYER,
                        levelSoundEventPacket.isBabyMob,
                        levelSoundEventPacket.isGlobal);
                if (this.isSpectator()) {
                    event.setCancelled();
                }
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
                break;*/
            default:
                //Server.getInstance().getLogger().notice("Received Data Packet: " + packet.getClass().getSimpleName());
                super.handleDataPacket(packet);
        }
    }

    protected void setLoginChainData(LoginChainData loginChainData) {
    	this.loginChainData = loginChainData;
    }

    public String getOriginName() {
        return originName;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (!this.isSynapseLogin) {
            boolean update = super.onUpdate(currentTick);
            return update;
        }

        /*if (this.loggedIn && this.synapseSlowLoginUntil != -1 && System.currentTimeMillis() >= this.synapseSlowLoginUntil) {
            this.processLogin();
        }*/
        boolean update = super.onUpdate(currentTick);
        return update;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public UUID getSessionId() {
        return sessionId;
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
        packet = DataPacketEidReplacer.replace(packet, this.getId(), SYNAPSE_PLAYER_ENTITY_ID);
        packet.setHelper(AbstractProtocol.fromRealProtocol(this.protocol).getHelper());
        packet.neteaseMode = isNetEaseClient();

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
                } else if (packet instanceof TextPacket121 pk) {
                    pk.message = TextFormat.clean(pk.message);
                } else if (packet instanceof TextPacket121130 pk) {
                    pk.message = TextFormat.clean(pk.message);
                } else if (packet instanceof TextPacket126 pk) {
                    pk.message = TextFormat.clean(pk.message);
                }
            }
        }

        this.interfaz.putPacket(this, packet);
        return true;
    }

    public void sendLevelSoundEvent(int levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
        sendLevelSoundEvent(levelSound, pos, extraData, pitch, entityIdentifier, isBabyMob, isGlobal, -1);
    }

    public void sendLevelSoundEvent(int levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal, long entityUniqueId) {
//        if (levelSound == null || levelSound.getV12() == -1) return;
        LevelSoundEventPacket pk = new LevelSoundEventPacket();
        pk.sound = levelSound;
        pk.x = (float) pos.x;
        pk.y = (float) pos.y;
        pk.z = (float) pos.z;
        pk.extraData = extraData;
        pk.pitch = pitch;
        pk.isBabyMob = isBabyMob;
        pk.isGlobal = isGlobal;
        this.dataPacket(pk);
    }

    public Long2ObjectMap<BlobTrack> getClientCacheTrack() {
        return null;
    }

    @Override
    public List<Item> getCreativeItems() {
        return CreativeItemsPalette.getCreativeItems(AbstractProtocol.fromRealProtocol(this.protocol), this.isNetEaseClient());
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
    public void updateMovement() {
        double diffPosition = (this.x - this.lastX) * (this.x - this.lastX) + (this.y - this.lastY) * (this.y - this.lastY) + (this.z - this.lastZ) * (this.z - this.lastZ);
        double diffRotation = (this.yaw - this.lastYaw) * (this.yaw - this.lastYaw) + (this.pitch - this.lastPitch) * (this.pitch - this.lastPitch);

        double diffMotion = (this.motionX - this.lastMotionX) * (this.motionX - this.lastMotionX) + (this.motionY - this.lastMotionY) * (this.motionY - this.lastMotionY) + (this.motionZ - this.lastMotionZ) * (this.motionZ - this.lastMotionZ);

//        if (diffPosition > 0.0001 || diffRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
        if (diffPosition != 0 || diffRotation != 0) {
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
        // 1.13+
    }

    public void sendPyRpcData(Value data) {
        //SynapsePlayer16
    }

    public void modNotifyToClient(String modName, String systemName, String eventName, MapValue eventData) {
        //SynapsePlayer16
    }

    public void modNotifyToClientEncrypted(String modName, String systemName, String eventName, String data, Function<String, String> encMethod) {
    }

    protected void clearSubChunkQueues() {
        // 1.18+
    }

    @Override
    public boolean isNetEaseClient() {
        if (SynapseSharedConstants.FORCE_NETEASE_PLAYER) {
            return true;
        }
        return super.isNetEaseClient();
    }

    protected void sendItemComponents() {
        // 1.16.100+
    }

    @Override
    public long getLocalEntityId() {
        return SYNAPSE_PLAYER_ENTITY_ID;
    }

    public NPCDialoguePlayerHandler getNpcDialoguePlayerHandler() {
        return null;
    }

    public void requestPing() {
        // SynapsePlayer19 实现
    }

    public void ping() {
        // 1.6+
    }

    /**
     * @return ns
     */
    public long getLatency() {
        return -1;
    }

    /**
     * Nemisys RakNet latency
     * @return ms
     */
    public int getRakNetLatency() {
        return rakNetLatency;
    }

    /**
     * Unreliable remote tick (Server Authoritative Movement is required).
     * @param tick client tick count
     */
    protected void onClientTickUpdated(long tick) {
    }

    public boolean isServerAuthoritativeBlockBreakingEnabled() {
        return false;
    }

    public boolean isServerAuthoritativeSoundEnabled() {
        return false;
    }

    public void onUnexpectedBehavior(String tag) {
        onUnexpectedBehavior(tag, "");
    }

    public void onUnexpectedBehavior(String tag, String context) {
        new SynapsePlayerUnexpectedBehaviorEvent(this, tag, context).call();
//        onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, tag, context);
    }

    protected void sendDimensionData() {
        // 1.18.30+
    }

    @Override
    public int getDummyDimension() {
        if (!isNetEaseClient() && dummyDimension >= DimensionDefinition.NETEASE_BUILTIN_DIMENSION_FIRST) {
            return dummyDimension - DimensionDefinition.NETEASE_BUILTIN_DIMENSION_FIRST + DimensionID.CUSTOM_DIMENSION;
        }
        return dummyDimension;
    }

    protected void sendBiomeDefinitionList() {
        // 1.8+
    }

    protected void sendAvailableEntityIdentifiers() {
        // 1.12+
    }

    protected void syncEntityProperties() {
        // 1.17+
    }

    public boolean emoteRequest() {
        // 1.16+
        return true;
    }

    public void addSubPacketHandler(ServerSubPacketHandler<?> handler) {
        // NE 1.6+
    }

    protected void syncFeatureRegistry() {
        // 1.19.20+
    }

    /**
     * CameraPresets and CameraAimAssistPresets
     */
    protected void sendCameraPresets() {
        // 1.19.70+
    }

    public void startCameraInstruction(CameraInstruction instruction) {
        // 1.20.30+
    }

    public void clearCameraInstruction() {
        // 1.20.30+
    }

    protected void preChangeDimensionScreen(boolean transfer) {
        if (riding != null) {
            riding.dismountEntity(this);
        }

        if (!isNeedLevelChangeLoadScreen()) {
            requestCloseFormWindow();
        }
    }

    public boolean isBetaClient() {
        return betaClient;
    }

    protected void tryDisruptIllegalClientBeforeStartGame() {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(float viewAngleX, float viewAngleZ) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(float viewAngleX, float viewAngleZ, String presetId) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(float distance) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(float distance, String presetId) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(int mode, float viewAngleX, float viewAngleZ, float distance) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(int mode, float viewAngleX, float viewAngleZ, float distance, String presetId) {
    }

    /**
     * @since 1.21.50
     */
    public void setAimAssist(String presetId) {
    }

    /**
     * @since 1.21.50
     */
    public void clearAimAssist() {
    }

    protected void sendJigsawStructureData() {
    }

    /**
     * Adds a new shape to the world.
     * @param shape the shape to be added
     * @since 1.21.90
     */
    public void addShape(Shape shape) {
    }

    /**
     * Removes an instance of a shape from the world.
     * @param shape the shape to be removed
     * @since 1.21.90
     */
    public void removeShape(Shape shape) {
    }

    /**
     * Removes all shapes from the world.
     * @since 1.21.90
     */
    public void removeAllShapes() {
    }

    @Override
    public void preChat(String message) {
        SynapsePlayerPreChatEvent event = new SynapsePlayerPreChatEvent(this, message);
        event.call();
        if (event.isCancelled()) {
            return;
        }
        chat(event.getMessage());
    }

    int getClientMaxViewDistance() {
        return -1;
    }

    protected void sendVoxelShapes() {
    }

    protected void initializeWorldClocks() {
    }
}
