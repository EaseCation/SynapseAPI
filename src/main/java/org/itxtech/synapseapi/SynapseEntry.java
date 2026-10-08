package org.itxtech.synapseapi;

import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.event.player.SynapsePlayerCreationEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTooManyPacketsInBatchEvent;
import org.itxtech.synapseapi.messaging.StandardMessenger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.InteractPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.network.SynLibInterface;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.*;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.ClientData.Entry;
import org.itxtech.synapseapi.utils.DataPacketEidReplacer;
import org.itxtech.synapseapi.utils.PacketLogger;

import javax.annotation.Nullable;
import io.netty.channel.Channel;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

/**
 * @author boybook
 */
@Log4j2
public class SynapseEntry {
    public static final int MAX_SIZE = 12 * 1024 * 1024; // 12MB

    public static final int[] PACKET_COUNT_LIMIT = new int[ProtocolInfo.COUNT];
    private static final int PACKET_TYPE_COUNT = ProtocolInfo.COUNT - 512; // 目前没有ID大于0x1ff的包, 节省一半内存. 未来不够用了再改
    public static final int[] globalPacketCountThisTick = new int[PACKET_TYPE_COUNT];

    static {
        Arrays.fill(PACKET_COUNT_LIMIT, 10);
        PACKET_COUNT_LIMIT[ProtocolInfo.INVENTORY_TRANSACTION_PACKET] = 64 * 9 + 64 * 9 + 64; // extreme case (shift-click crafting): 64x9 (inputs) + 64x9 (output on crafting grid) + 64 (outputs to main slot)
        PACKET_COUNT_LIMIT[ProtocolInfo.CRAFTING_EVENT_PACKET] = 64;
        PACKET_COUNT_LIMIT[ProtocolInfo.PACKET_PY_RPC] = 50; //TODO: batch rpc
        PACKET_COUNT_LIMIT[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET] = 15000; // 1.18.0 facepalm
        PACKET_COUNT_LIMIT[ProtocolInfo.PLAYER_ACTION_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.ANIMATE_PACKET] = 64;
        PACKET_COUNT_LIMIT[ProtocolInfo.INTERACT_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.RESOURCE_PACK_CHUNK_REQUEST_PACKET] = 1000;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET] = 1;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2] = 1;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = 30;
        PACKET_COUNT_LIMIT[ProtocolInfo.COMMAND_REQUEST_PACKET] = 5;
        PACKET_COUNT_LIMIT[ProtocolInfo.SETTINGS_COMMAND_PACKET] = 5;
        PACKET_COUNT_LIMIT[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.SET_ACTOR_LINK_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.MAP_INFO_REQUEST_PACKET] = 64;
    }

    private final SynapseAPI synapse;
    @Nullable
    private final ServerInputDispatcher inputDispatcher;
    @Nullable
    private final Map<UUID, ServerInputDispatcher.Session> inputSessions;
    @Nullable
    private final Int2ObjectMap<int[]> inputPacketCounts;
    private int inputCountTick = Integer.MIN_VALUE;
    private static int inputGlobalCountTick = Integer.MIN_VALUE;
    private boolean enable;
    private String serverIp;
    private int port;
    private boolean isMainServer;
    private String password;
    private SynapseInterface synapseInterface;
    private boolean verified = false;
    private long lastLogin;
    private long lastUpdate;
    private long lastRecvInfo;
    private final Map<UUID, SynapsePlayer> players = new ConcurrentHashMap<>();
    private SynLibInterface synLibInterface;
    private ClientData clientData;
    private String serverDescription;
    private JsonObject metadata = new JsonObject();

    public SynapseEntry(SynapseAPI synapse, String serverIp, int port, boolean isMainServer, String password, String serverDescription) {
        this.synapse = synapse;
        this.inputDispatcher = synapse.isMainThreadInput() ? synapse.getServer().getInputDispatcher() : null;
        this.inputSessions = inputDispatcher == null ? null : new ConcurrentHashMap<>();
        this.inputPacketCounts = inputDispatcher == null ? null : new Int2ObjectOpenHashMap<>();
        this.serverIp = serverIp;
        this.port = port;
        this.isMainServer = isMainServer;
        this.password = password;
        if (this.password.length() != 16) {
            synapse.getLogger().warning("You must use a 16 bit length key!");
            synapse.getLogger().warning("This SynapseAPI Entry will not be enabled!");
            enable = false;
            return;
        }
        this.serverDescription = serverDescription;

        this.synapseInterface = new SynapseInterface(this, this.serverIp, this.port);
        this.synLibInterface = new SynLibInterface(this.synapseInterface);
        this.lastLogin = System.currentTimeMillis();
        this.lastUpdate = System.currentTimeMillis();
        this.lastRecvInfo = System.currentTimeMillis();
        this.getSynapse().getServer().getScheduler().scheduleRepeatingTask(SynapseAPI.getInstance(), new Ticker(this), 1);

        Thread asyncTicker = new Thread(new AsyncTicker(), "SynapseAPI Async Ticker");
        if (this.inputDispatcher != null) this.synapseInterface.getClient().setInboundConsumerThread(asyncTicker);
        asyncTicker.start();
/*
        this.getSynapse().getServer().getScheduler().scheduleRepeatingTask(SynapseAPI.getInstance(), new Task() {
            @Override
            public void onRun(int currentTick) {
                if (Server.getInstance().isRunning() && asyncTicker == null || !asyncTicker.isAlive()) {
                    getSynapse().getLogger().warning("检测到 SynapseAPI 线程 " + getHash() + " 已停止运行！正在重新启动线程！");
                    asyncTicker = new Thread(new AsyncTicker(), "SynapseAPI Async Ticker");
                    asyncTicker.start();
                }
            }
        }, 10);
*/
    }

    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public SynapseAPI getSynapse() {
        return this.synapse;
    }

    public boolean isMainThreadInputEnabled() {
        return this.inputDispatcher != null;
    }

    /** 原连接的存活和身份由本机通道决定，不读取客户端声明。 */
    public boolean isCurrentInputConnection(@Nullable Channel channel) {
        return channel != null && channel == this.synapseInterface.getClient().getSession().getChannel()
                && channel.isActive();
    }

    public boolean isEnable() {
        return enable;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public boolean isVerified() {
        return verified;
    }

    public SynapseInterface getSynapseInterface() {
        return synapseInterface;
    }

    public void shutdown() {
        invalidateInputSessions();
        if (this.synapseInterface != null) {
            this.synapseInterface.markClosing();
        }

        if (this.verified) {
            DisconnectPacket pk = new DisconnectPacket();
            pk.type = DisconnectPacket.TYPE_GENERIC;
            pk.message = "Server closed";
            this.sendDataPacket(pk);
            this.getSynapse().getLogger().debug("Synapse client has disconnected from Synapse synapse");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
        }
        if (this.synapseInterface != null) this.synapseInterface.shutdown();
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public void sendDataPacket(SynapseDataPacket pk) {
        this.synapseInterface.putPacket(pk);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastRecvInfo() {
        return lastRecvInfo;
    }

    @Deprecated
    public void broadcastPacket(SynapsePlayer[] players, DataPacket packet) {
        this.broadcastPacket(players, packet, false);
    }

    @Deprecated
    public void broadcastPacket(SynapsePlayer[] players, DataPacket packet, boolean direct) {
        packet.tryEncode();
        BroadcastPacket broadcastPacket = new BroadcastPacket();
        broadcastPacket.direct = direct;
        broadcastPacket.payload = packet.getBuffer();
        int length = players.length;
        UUID[] sessionIds = new UUID[length];
        for (int i = 0; i < length; i++) {
            sessionIds[i] = players[i].getSessionId();
        }
        broadcastPacket.sessionIds = sessionIds;
        this.sendDataPacket(broadcastPacket);
    }

    public boolean isMainServer() {
        return isMainServer;
    }

    public void setMainServer(boolean mainServer) {
        isMainServer = mainServer;
    }

    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
    }

    public String getHash() {
        return this.serverIp + ":" + this.port;
    }

    public void connect() {
        this.getSynapse().getLogger().info("Try login to server: {}", this.getHash());
        this.verified = false;
        ConnectPacket pk = new ConnectPacket();
        pk.password = this.password;
        pk.isMainServer = this.isMainServer();
        pk.description = this.serverDescription;
        pk.maxPlayers = this.getSynapse().getServer().getMaxPlayers();
        pk.protocol = SynapseInfo.CURRENT_PROTOCOL;
        pk.metadata = this.metadata;
        this.sendDataPacket(pk);
    }

    public class AsyncTicker implements Runnable {
        private long tickUseTime;
        private long lastWarning = 0;

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            while (Server.getInstance().isRunning()) {
                try {
                    threadTick();
                } catch (Exception e) {
                    Server.getInstance().getLogger().error("SynapseEntry async tick exception", e);
                }
                tickUseTime = System.currentTimeMillis() - startTime;
                if (tickUseTime < 10) {
                    if (inputDispatcher != null) {
                        if (synapseInterface.getClient().getExternalQueue().isEmpty()) {
                            LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(10 - tickUseTime));
                        }
                        Thread.interrupted();
                    } else {
                        try {
                            Thread.sleep(10 - tickUseTime);
                        } catch (InterruptedException ignore) {}
                    }
                } else if (System.currentTimeMillis() - lastWarning >= 5000) {
                    Server.getInstance().getLogger().warning("SynapseEntry<" + getHash() + "> Async Thread is overloading! TPS: " + getTicksPerSecond() + " tickUseTime: " + tickUseTime);
                    lastWarning = System.currentTimeMillis();
                }
                startTime = System.currentTimeMillis();
            }
        }

        public double getTicksPerSecond() {
            long more = this.tickUseTime - 10;
            if (more <= 0) return 100;
            return NukkitMath.round(10d / this.tickUseTime, 3) * 100;
        }
    }

    /**
     * 这个Ticker运行在主线程
     */
    public class Ticker implements Runnable {
        private final SynapseEntry entry;

        private Ticker(SynapseEntry entry) {
            this.entry = entry;
        }

        @Override
        public void run() {
            if (!verified && System.currentTimeMillis() - lastLogin >= 3000) {
                getSynapse().getLogger().info("Trying to re-login to Synapse Server: " + getHash());
                synapseInterface.getClient().setNeedAuth(true);
                lastLogin = System.currentTimeMillis();
            }

            if (inputDispatcher != null) {
                return;
            }

            Arrays.fill(globalPacketCountThisTick, 0);

            PlayerLoginPacket playerLoginPacket;
            while ((playerLoginPacket = playerLoginQueue.poll()) != null) {
                handlePlayerLogin(playerLoginPacket);
            }

            RedirectPacketEntry redirectPacketEntry;
            Int2ObjectMap<int[]> playerPacketCountThisTick = new Int2ObjectOpenHashMap<>();
            while ((redirectPacketEntry = redirectPacketQueue.poll()) != null) {
                handleRedirectOnMain(redirectPacketEntry, playerPacketCountThisTick);
            }

            PlayerLogoutPacket playerLogoutPacket;
            while ((playerLogoutPacket = playerLogoutQueue.poll()) != null) {
                handlePlayerLogout(playerLogoutPacket);
            }
        }
    }

    private void handlePlayerLogin(PlayerLoginPacket packet) {
        handlePlayerLogin(packet, null);
    }

    private void handlePlayerLogin(PlayerLoginPacket packet, @Nullable ServerInputDispatcher.Session inputSession) {
        if (inputDispatcher != null) {
            prepareInputCounts();
        }
        globalPacketCountThisTick[ProtocolInfo.LOGIN_PACKET]++;
        UUID uuid = packet.uuid;
        UUID sessionId = packet.sessionId;
        for (Player player : synapse.getServer().getOnlinePlayerList()) {
            if (uuid.equals(player.getUniqueId())) {
                if (inputDispatcher != null && player instanceof SynapsePlayer previous
                        && previous.getSynapseEntry() == this && !previous.isInputSessionActive()) {
                    // 旧来源已退休，关闭原对象后允许当前连接重建；活跃重复登录仍拒绝。
                    previous.close("", "disconnectionScreen.disconnected", true);
                    players.remove(previous.getSessionId(), previous);
                    continue;
                }
                try {
                    player.kick(PlayerKickEvent.Reason.NEW_CONNECTION, "disconnectionScreen.loggedinOtherLocation", false);
                } catch (Exception exception) {
                    log.throwing(exception);
                }
                PlayerLogoutPacket logout = new PlayerLogoutPacket();
                logout.sessionId = sessionId;
                logout.reason = "disconnectionScreen.serverIdConflict";
                sendDataPacket(logout);
                return;
            }
        }
        InetSocketAddress address = InetSocketAddress.createUnresolved(packet.address, packet.port);
        Class<? extends SynapsePlayer> clazz = determinePlayerClass(packet.protocol);
        SynapsePlayerCreationEvent event = new SynapsePlayerCreationEvent(synLibInterface, clazz, clazz,
                ThreadLocalRandom.current().nextLong(), address);
        synapse.getServer().getPluginManager().callEvent(event);
        if (inputSession != null && packet.receivedChannel != null
                && !this.isCurrentInputConnection(packet.receivedChannel)) return;
        clazz = event.getPlayerClass();
        try {
            Constructor<? extends SynapsePlayer> constructor = clazz.getConstructor(SourceInterface.class,
                    SynapseEntry.class, Long.class, InetSocketAddress.class);
            SynapsePlayer player = constructor.newInstance(synLibInterface, this, event.getClientId(), event.getSocketAddress());
            player.setUniqueId(uuid);
            player.setSessionId(sessionId);
            player.bindInputSession(inputSession, packet.receivedChannel);
            players.put(sessionId, player);
            synapse.getServer().addPlayer(address, player);
            player.handleLoginPacket(packet);
        } catch (Exception exception) {
            synapse.getServer().getLogger().logException(exception);
        }
    }

    private void handleRedirectOnMain(RedirectPacketEntry entry, Int2ObjectMap<int[]> counts) {
        if (entry.player.isClosed()) {
            return;
        }
        DataPacket packet = DataPacketEidReplacer.replaceBack(entry.dataPacket,
                SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID, entry.player.getId());
        if (inputDispatcher != null && packet instanceof IPlayerAuthInputPacket input) {
            long tick = input.getTick();
            if (entry.player.lastAuthInputPacketTick > tick) {
                entry.player.setViolated("input_tick");
                entry.player.onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "input_tick", String.valueOf(tick));
                return;
            }
            entry.player.lastAuthInputPacketTick = tick;
        }
        globalPacketCountThisTick[packet.pid()]++;
        int[] counter = counts.get((int) entry.player.getLoaderId());
        if (counter == null) {
            counts.put((int) entry.player.getLoaderId(), counter = new int[]{1});
        }
        counter[0]++;
        if (entry.player.isOnline() && counter[0] > 10000) {
            entry.player.onPacketViolation(PacketViolationReason.RECEIVING_PACKETS_TOO_FAST, "sync");
            return;
        }
        if (SERVERBOUND_PACKET_LOGGING && log.isTraceEnabled()) {
            PacketLogger.handleServerboundPacket(entry.player, packet);
        }
        if (inputDispatcher == null) {
            entry.player.handleDataPacket(packet);
        } else {
            entry.player.handleInputDataPacket(packet, entry.movementEpoch);
        }
    }

    private void handlePlayerLogout(PlayerLogoutPacket packet) {
        Player player = players.get(packet.sessionId);
        if (player != null) {
            player.close("", packet.reason, true);
            removePlayer(packet.sessionId);
        }
    }

    private void prepareInputCounts() {
        int tick = synapse.getServer().getTick();
        if (inputGlobalCountTick != tick) {
            inputGlobalCountTick = tick;
            Arrays.fill(globalPacketCountThisTick, 0);
        }
        if (inputCountTick != tick) {
            inputCountTick = tick;
            inputPacketCounts.clear();
        }
    }

    private void submitInput(ServerInputDispatcher.Session session, int bytes, Runnable action) {
        submitInput(session, bytes, context -> action.run(), false);
    }

    private void submitInput(ServerInputDispatcher.Session session, int bytes, ServerInputTask task, boolean betweenTicks) {
        submitInput(session, bytes, task, betweenTicks, 0);
    }

    private void submitInput(ServerInputDispatcher.Session session, int bytes, ServerInputTask task, boolean betweenTicks, long receivedNanos) {
        ServerInputDispatcher.OfferResult result = inputDispatcher.offer(session, task, bytes, betweenTicks, receivedNanos);
        if (result == ServerInputDispatcher.OfferResult.OVERLOADED) {
            synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                if (inputSessions.remove(session.getId(), session)) {
                    PlayerLogoutPacket logout = new PlayerLogoutPacket();
                    logout.sessionId = session.getId();
                    logout.reason = "disconnectionScreen.serverFull";
                    sendDataPacket(logout);
                    handlePlayerLogout(logout);
                }
            });
        }
    }

    private void publishRedirect(SynapsePlayer player, DataPacket packet, long movementEpoch, long receivedNanos) {
        RedirectPacketEntry entry = new RedirectPacketEntry(player, packet, movementEpoch);
        if (inputDispatcher == null) {
            redirectPacketQueue.offer(entry);
            return;
        }
        ServerInputDispatcher.Session session = inputSessions.get(player.getSessionId());
        if (session == null || !session.isActive()) {
            return;
        }
        if (synapse.getServer().isPrimaryThread()) {
            // 未就绪连接的原始批包在自己的任务内完成，不能把子包追加到队尾而重排。
            prepareInputCounts();
            handleRedirectOnMain(entry, inputPacketCounts);
        } else {
            int bytes = packet.getCount();
            boolean betweenTicks = BetweenTickPackets.supports(packet);
            submitInput(session, bytes, new ServerInputTask() {
                @Override
                public boolean isValid() {
                    return inputSessions.get(player.getSessionId()) == session
                            && players.get(player.getSessionId()) == player && player.isAcceptingInputPackets();
                }

                @Override
                public boolean canRunBetweenTicks() {
                    return BetweenTickPackets.canRunBetweenTicks(player, packet);
                }

                @Override
                public void run(InboundContext context) {
                    if (this.isValid() && !player.isViolated()) {
                        prepareInputCounts();
                        handleRedirectOnMain(entry, inputPacketCounts);
                    }
                }
            }, betweenTicks, receivedNanos);
        }
    }

    public void invalidateInputSessions() {
        if (inputDispatcher != null) {
            List<SynapsePlayer> stalePlayers = inputSessions.keySet().stream()
                    .map(players::get).filter(Objects::nonNull).toList();
            inputSessions.values().forEach(inputDispatcher::invalidate);
            inputSessions.clear();
            if (!stalePlayers.isEmpty()) {
                synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                    for (SynapsePlayer player : stalePlayers) {
                        if (players.get(player.getSessionId()) == player) {
                            player.close("", "disconnectionScreen.disconnected", true);
                            players.remove(player.getSessionId(), player);
                        }
                    }
                });
            }
        }
    }

    private Class<? extends SynapsePlayer> determinePlayerClass(int protocol) {
        return AbstractProtocol.fromRealProtocol(protocol).getPlayerClass();
    }

    /**
     * 这个Ticker运行在异步线程
     */
    public void threadTick() {
        this.synapseInterface.process();
        /*this.playerBatchPacketCounter.forEach((uuid, count) -> {
            if (count > INCOMING_PACKET_BATCH_MAX_BUDGET) {
                synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                    SynapsePlayer player = this.players.get(uuid);
                    new SynapsePlayerTooManyBatchPacketsEvent(player, count).call();
                    player.onPacketViolation(PacketViolationReason.VIOLATION_OVER_THRESHOLD);
                });
            }
        });*/
        if (!this.getSynapseInterface().isConnected() || !this.verified) {
            return;
        }
        long time = System.currentTimeMillis();

        if ((time - this.lastUpdate) >= 5000) {  //Heartbeat!
            this.lastUpdate = time;
            HeartbeatPacket pk = new HeartbeatPacket();
            pk.tps = this.getSynapse().getServer().getTicksPerSecondAverage();
            pk.load = this.getSynapse().getServer().getTickUsageAverage();
            pk.upTime = (System.currentTimeMillis() - Nukkit.START_TIME) / 1000;
            this.sendDataPacket(pk);
            //this.getSynapse().getServer().getLogger().debug(time + " -> Sending Heartbeat Packet to " + this.getHash());
        }
        /*
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(10) + 1; i++) {
            InformationPacket test = new InformationPacket();
            test.type = InformationPacket.TYPE_PLUGIN_MESSAGE;
            test.message = getRandomString(1024 * (ThreadLocalRandom.current().nextInt(20) + 110));
            this.sendDataPacket(test);
        }*/

        long finalTime = System.currentTimeMillis();
        //long usedTime = finalTime - time;
        //this.getSynapse().getServer().getLogger().warning(time + " -> threadTick 用时 " + usedTime + " 毫秒");
        if (((finalTime - this.lastUpdate) >= 30000) && this.synapseInterface.isConnected()) {  //30 seconds timeout
            this.getSynapse().getLogger().warn("Synapse client {} has disconnected due to timeout (30s)", this.getHash());
            this.synapseInterface.reconnect();
        }
    }

    public void removePlayer(UUID sessionId) {
        this.players.remove(sessionId);
    }

    /** 主线程退出立即失效原代际，残留任务不能继续挡住全局 FIFO 队头。 */
    void handlePlayerQuit(SynapsePlayer player) {
        if (inputDispatcher == null) {
            return;
        }
        ServerInputDispatcher.Session session = player.getInputSession();
        if (session != null) {
            inputDispatcher.invalidate(session);
            inputSessions.remove(session.getId(), session);
        }
        players.remove(player.getSessionId(), player);
    }

    private final Queue<PlayerLoginPacket> playerLoginQueue = new LinkedBlockingQueue<>();
    private final Queue<PlayerLogoutPacket> playerLogoutQueue = new LinkedBlockingQueue<>();
    private final Queue<RedirectPacketEntry> redirectPacketQueue = new LinkedBlockingQueue<>();

    public void handleDataPacket(SynapseDataPacket pk) {
        //this.getSynapse().getLogger().warning("Received packet " + pk.pid() + "(" + pk.getClass().getSimpleName() + ") from " + this.serverIp + ":" + this.port);
        HANDLER:
        switch (pk.pid()) {
            case SynapseInfo.DISCONNECT_PACKET:
                invalidateInputSessions();
                DisconnectPacket disconnectPacket = (DisconnectPacket) pk;
                this.verified = false;
                switch (disconnectPacket.type) {
                    case DisconnectPacket.TYPE_GENERIC:
                        this.getSynapse().getLogger().info("Synapse Client has disconnected due to " + disconnectPacket.message);
                        this.synapseInterface.reconnect();
                        break;
                    case DisconnectPacket.TYPE_WRONG_PROTOCOL:
                        this.getSynapse().getLogger().error(disconnectPacket.message);
                        break;
                }
                break;
            case SynapseInfo.CONNECTION_STATUS_PACKET:
                ConnectionStatusPacket connectionStatusPacket = (ConnectionStatusPacket) pk;
                switch (connectionStatusPacket.type) {
                    case ConnectionStatusPacket.TYPE_LOGIN_SUCCESS:
                        this.getSynapse().getLogger().info("Login success to " + this.serverIp + ":" + this.port);
                        this.verified = true;
                        break;
                    case ConnectionStatusPacket.TYPE_LOGIN_FAILED:
                        this.getSynapse().getLogger().info("Login failed to " + this.serverIp + ":" + this.port);
                        break;
                }
                break;
            case SynapseInfo.INFORMATION_PACKET:
                InformationPacket informationPacket = (InformationPacket) pk;
                ClientData clientData = new ClientData();
                for (Pair<String, Entry> client : informationPacket.clientList) {
                    clientData.clientList.put(client.left(), client.right());
                }
                this.clientData = clientData;
                this.lastRecvInfo = System.currentTimeMillis();
                break;
            case SynapseInfo.PLAYER_LATENCY_PACKET:
                PlayerLatencyPacket playerLatencyPacket = (PlayerLatencyPacket) pk;
                for (ObjectIntPair<UUID> entry : playerLatencyPacket.pings) {
                    SynapsePlayer player = this.players.get(entry.left());
                    if (player != null) {
                        player.rakNetLatency = entry.rightInt();
                    }
                }
                break;
            case SynapseInfo.PLAYER_LOGIN_PACKET:
                PlayerLoginPacket loginPacket = (PlayerLoginPacket) pk;
                synapse.getServer().getNetwork().addDownloadStatistic(loginPacket.cachedLoginPacket.length);
                if (inputDispatcher == null) {
                    this.playerLoginQueue.offer(loginPacket);
                } else {
                    ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(loginPacket.sessionId);
                    ServerInputDispatcher.Session previous = inputSessions.putIfAbsent(loginPacket.sessionId, session);
                    if (previous == null) {
                        Channel loginChannel = loginPacket.receivedChannel != null ? loginPacket.receivedChannel
                                : this.synapseInterface.getClient().getSession().getChannel();
                        submitInput(session, loginPacket.cachedLoginPacket.length, () -> {
                            try {
                                if (!isCurrentInputConnection(loginChannel)) return;
                                handlePlayerLogin(loginPacket, session);
                            } finally {
                                SynapsePlayer created = players.get(loginPacket.sessionId);
                                if (!isCurrentInputConnection(loginChannel) && created != null
                                        && created.getInputSession() == session) {
                                    created.close("", "disconnectionScreen.disconnected", true);
                                    players.remove(loginPacket.sessionId, created);
                                }
                                if (!isCurrentInputConnection(loginChannel) || created == null || created.isClosed()) {
                                    inputDispatcher.invalidate(session);
                                    inputSessions.remove(loginPacket.sessionId, session);
                                }
                            }
                        });
                    } else {
                        inputDispatcher.invalidate(previous);
                        synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                            PlayerLogoutPacket logout = new PlayerLogoutPacket();
                            logout.sessionId = loginPacket.sessionId;
                            logout.reason = "disconnectionScreen.serverIdConflict";
                            handlePlayerLogout(logout);
                            sendDataPacket(logout);
                        });
                    }
                }
                break;
            case SynapseInfo.REDIRECT_PACKET:
                RedirectPacket redirectPacket = (RedirectPacket) pk;
                synapse.getServer().getNetwork().addDownloadStatistic(redirectPacket.mcpeBuffer.length);

                SynapsePlayer player = this.players.get(redirectPacket.sessionId);
                if (inputDispatcher != null) {
                    ServerInputDispatcher.Session session = inputSessions.get(redirectPacket.sessionId);
                    if (session == null || !session.isActive()) {
                        break;
                    }
                }
                if (inputDispatcher != null && player == null && !synapse.getServer().isPrimaryThread()) {
                    ServerInputDispatcher.Session session = inputSessions.get(redirectPacket.sessionId);
                    if (session != null && session.isActive()) {
                        // 仅登录首批缺少 Player 的情况延后解码，正常连接保持原异步解包路径。
                        submitInput(session, redirectPacket.mcpeBuffer.length, context -> {
                            if (inputSessions.get(redirectPacket.sessionId) == session) {
                                handleDataPacket(redirectPacket);
                            }
                        }, false, redirectPacket.receivedNanos);
                    }
                    break;
                }
                if (player != null && !player.isClosed() && !player.isViolated()) {
                    // 同一原始批包共享接入代际，解包期间发生传送也不能给后半批旧输入换代。
                    long movementEpoch = inputDispatcher == null ? 0 : player.getMovementEpoch();
                    DataPacket pk0 = PacketRegister.getFullPacket(redirectPacket.mcpeBuffer, redirectPacket.protocol);
                    //Server.getInstance().getLogger().info("to server : " + pk0.getClass().getName());
                    if (pk0 != null) {
                        //pk0.decode();
                        if (pk0.pid() == ProtocolInfo.BATCH_PACKET) {
/*                          // 批包速率检测已移到Nemisys
                            if (player.incomingPacketBatchBudget <= 0) {
                                long nowNs = System.nanoTime();
                                long timeSinceLastUpdateNs = nowNs - player.lastPacketBudgetUpdateTimeNs;
                                if (timeSinceLastUpdateNs > 50_000_000) {
                                    int ticksSinceLastUpdate = (int) (timeSinceLastUpdateNs / 50_000_000);
                                    // If the server takes an abnormally long time to process a tick, add the budget for time difference to compensate.
                                    // This extra budget may be very large, but it will disappear the next time a normal update occurs.
                                    // This ensures that backlogs during a large lag spike don't cause everyone to get kicked.
                                    // As long as all the backlogged packets are processed before the next tick, everything should be OK for clients behaving normally.
                                    player.incomingPacketBatchBudget = Math.min(player.incomingPacketBatchBudget, SynapsePlayer.INCOMING_PACKET_BATCH_MAX_BUDGET) + SynapsePlayer.INCOMING_PACKET_BATCH_PER_TICK * 2 * ticksSinceLastUpdate;
                                    player.lastPacketBudgetUpdateTimeNs = nowNs;
                                }

                                if (player.incomingPacketBatchBudget <= 0) {
                                    player.setViolated("net_batch_fast");
                                    synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                        new SynapsePlayerTooManyBatchPacketsEvent(player, SynapsePlayer.INCOMING_PACKET_BATCH_MAX_BUDGET + 1).call();
                                        player.onPacketViolation(PacketViolationReason.RECEIVING_BATCHES_TOO_FAST, "async");
                                    });
                                    break;
                                }
                            }
                            player.incomingPacketBatchBudget--;
*/

                            List<DataPacket> packets = processBatch((BatchPacket) pk0, redirectPacket.protocol, player.isNetEaseClient(), redirectPacket.compressionAlgorithm);
                            if (packets == null) {
                                player.setViolated("packet_bad_batch");
                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                    player.onPacketViolation(PacketViolationReason.MALFORMED_PACKET, "batch");
                                });
                                break;
                            }
                            // Server.getInstance().getLogger().info("tick: " + Server.getInstance().getTick() + " to server " + packets.size() + " packets");

                            short[] packetCount = new short[PACKET_TYPE_COUNT];
                            boolean tooManyPackets = false;
                            for (DataPacket subPacket : packets) {
                                int packetId = subPacket.pid();
                                if (packetId >= PACKET_TYPE_COUNT) {
                                    player.setViolated("packet_bad_id" + packetId);
                                    synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                        player.onPacketViolation(PacketViolationReason.MALFORMED_PACKET, "pid" + packetId);
                                    });
                                    break HANDLER;
                                }
                                try {
                                    if (packetId == ProtocolInfo.INTERACT_PACKET
                                            && (subPacket instanceof InteractPacket113 interactPacket && interactPacket.action == InteractPacket113.ACTION_MOUSEOVER
                                            || subPacket instanceof InteractPacket121130 interactPacket121130 && interactPacket121130.action == InteractPacket121130.ACTION_MOUSEOVER)) {
                                        // 看向实体的交互包不稳定因此不参与计数
                                    } else if (packetCount[packetId]++ > PACKET_COUNT_LIMIT[packetId]) {
                                        tooManyPackets = true;
                                        continue;
                                    }

                                    publishRedirect(player, subPacket, movementEpoch, redirectPacket.receivedNanos);

                                    if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && player.isOnline()) {
                                        //玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                        boolean serverAuthoritativeMovement = player.isServerAuthoritativeMovementEnabled();
                                        if (!serverAuthoritativeMovement && subPacket instanceof MovePlayerPacket) {
                                            // 判断是否和玩家自身在附近区块，过滤 TP 后客户端发来的旧坐标包
                                            if (!isPositionNearPlayer(((MovePlayerPacket) subPacket).x, ((MovePlayerPacket) subPacket).y, ((MovePlayerPacket) subPacket).z, player)) {
                                                continue;
                                            }
                                            ((MovePlayerPacket) subPacket).eid = player.getId();
                                            subPacket.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                            MovePlayerPacket116100NE newMovePacket = null;
                                            for (Player viewer : new ObjectArrayList<>(player.getViewers().values())) {
                                                if (viewer.getProtocol() >= AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                                                    if (newMovePacket == null) {
                                                        newMovePacket = new MovePlayerPacket116100NE();
                                                        newMovePacket.fromDefault(subPacket);
                                                        newMovePacket.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                                    }
                                                    viewer.dataPacket(newMovePacket);
                                                } else {
                                                    viewer.dataPacket(subPacket);
                                                }
                                            }
                                        } else if (!serverAuthoritativeMovement && subPacket instanceof MovePlayerPacket116100NE) {
                                            // 判断是否和玩家自身在附近区块，过滤 TP 后客户端发来的旧坐标包
                                            if (!isPositionNearPlayer(((MovePlayerPacket116100NE) subPacket).x, ((MovePlayerPacket116100NE) subPacket).y, ((MovePlayerPacket116100NE) subPacket).z, player)) {
                                                continue;
                                            }
                                            ((MovePlayerPacket116100NE) subPacket).eid = player.getId();
                                            subPacket.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                            MovePlayerPacket oldMovePacket = null;
                                            for (Player viewer : new ObjectArrayList<>(player.getViewers().values())) {
                                                if (viewer.getProtocol() < AbstractProtocol.PROTOCOL_116_100.getProtocolStart()) {
                                                    if (oldMovePacket == null) {
                                                        oldMovePacket = new MovePlayerPacket();
                                                        oldMovePacket.eid = ((MovePlayerPacket116100NE) subPacket).eid;
                                                        oldMovePacket.x = ((MovePlayerPacket116100NE) subPacket).x;
                                                        oldMovePacket.y = ((MovePlayerPacket116100NE) subPacket).y;
                                                        oldMovePacket.z = ((MovePlayerPacket116100NE) subPacket).z;
                                                        oldMovePacket.yaw = ((MovePlayerPacket116100NE) subPacket).yaw;
                                                        oldMovePacket.headYaw = ((MovePlayerPacket116100NE) subPacket).headYaw;
                                                        oldMovePacket.pitch = ((MovePlayerPacket116100NE) subPacket).pitch;
                                                        oldMovePacket.mode = ((MovePlayerPacket116100NE) subPacket).mode;
                                                        oldMovePacket.onGround = ((MovePlayerPacket116100NE) subPacket).onGround;
                                                        oldMovePacket.ridingEid = ((MovePlayerPacket116100NE) subPacket).ridingEid;
                                                        oldMovePacket.teleportCause = ((MovePlayerPacket116100NE) subPacket).teleportCause;
                                                        oldMovePacket.entityType = ((MovePlayerPacket116100NE) subPacket).teleportItem;
//                                                        oldMovePacket.frame = ((MovePlayerPacket116100NE) subPacket).frame;
                                                        oldMovePacket.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                                    }
                                                    viewer.dataPacket(oldMovePacket);
                                                } else {
                                                    viewer.dataPacket(subPacket);
                                                }
                                            }
                                        } else if (serverAuthoritativeMovement && subPacket instanceof IPlayerAuthInputPacket authInputPacket) {
                                            long tick = authInputPacket.getTick();
                                            if (player.lastAuthInputPacketTick > tick) {
                                                player.setViolated("input_tick");
                                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> player.onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "input_tick", String.valueOf(tick)));
                                                break HANDLER;
                                            }
                                            player.lastAuthInputPacketTick = tick;

                                            // 判断是否和玩家自身在附近区块，过滤 TP 后客户端发来的旧坐标包
                                            if (!isPositionNearPlayer(authInputPacket.getX(), authInputPacket.getY(), authInputPacket.getZ(), player)) {
                                                continue;
                                            }
                                            if (authInputPacket.getDeltaX() != 0 || authInputPacket.getDeltaZ() != 0 || authInputPacket.getY() != player.lastAuthInputY || authInputPacket.getYaw() != player.lastAuthInputYaw || authInputPacket.getPitch() != player.lastAuthInputPitch) {
                                                player.lastAuthInputY = authInputPacket.getY();
                                                player.lastAuthInputYaw = authInputPacket.getYaw();
                                                player.lastAuthInputPitch = authInputPacket.getPitch();
                                                //Server.getInstance().getLogger().info(player.getName() + ": nkY=" + player.getY() + " y=" + authInputPacket.y + " deltaX=" + authInputPacket.deltaX + " deltaY=" + authInputPacket.deltaY + " deltaZ=" + authInputPacket.deltaZ + " moveVecX=" + authInputPacket.moveVecX + " moveVecZ=" + authInputPacket.moveVecZ);
                                                MovePlayerPacket packet = new MovePlayerPacket();
                                                packet.eid = player.getId();
                                                packet.x = authInputPacket.getX();
                                                packet.y = authInputPacket.getY();
                                                packet.z = authInputPacket.getZ();
                                                packet.yaw = authInputPacket.getYaw();
                                                packet.headYaw = authInputPacket.getHeadYaw();
                                                packet.pitch = authInputPacket.getPitch();
//                                                packet.frame = tick;
                                                packet.mode = authInputPacket.hasFlag(PlayerAuthInputFlags.HANDLED_TELEPORT) ? MovePlayerPacket.MODE_TELEPORT : MovePlayerPacket.MODE_NORMAL;
                                                packet.onGround = player.onGround;
                                                long ridingEid = authInputPacket.getPredictedVehicleEntityUniqueId();
                                                if (ridingEid != 0) {
                                                    packet.ridingEid = ridingEid;
                                                } else {
                                                    Entity riding = player.riding;
                                                    if (riding != null) {
                                                        packet.ridingEid = riding.getId();
                                                    }
                                                }
                                                packet.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                                for (Player viewer : new ObjectArrayList<>(player.getViewers().values())) {
                                                    viewer.dataPacket(packet);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Server.getInstance().getLogger().alert("Error while handling packet " + subPacket.getClass().getSimpleName() + " for " + player.getName() + " (" + player.getAddress() + ")", e);
                                }
                                //Server.getInstance().getLogger().info("C => S  " + subPacket.getClass().getSimpleName());
                            }
                            if (tooManyPackets) {
//                                log.warn("SubChunkRequestPacket: {}", packetCount[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET]);
                                Server.getInstance().getPluginManager().callEvent(new SynapsePlayerTooManyPacketsInBatchEvent(player, packetCount));
                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> player.addViolationLevel(60, "net_pib_si"));

                                //判断连续触发
                                if ((player.violationIncomingThread += 60) > 100) {
                                    player.setViolated("net_pib_mu");
                                    synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                        player.onPacketViolation(PacketViolationReason.TOO_MANY_PACKETS_IN_BATCH, "async");
                                    });
                                    break HANDLER;
                                }
                            } else {
                                player.violationIncomingThread = player.getViolationLevel();
                            }
                        } else {
                            publishRedirect(player, pk0, movementEpoch, redirectPacket.receivedNanos);
                            if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && !player.isServerAuthoritativeMovementEnabled()
                                    && pk0 instanceof MovePlayerPacket movePacket) {
                                // 玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                // 判断是否和玩家自身在附近区块，过滤 TP 后客户端发来的旧坐标包
                                if (isPositionNearPlayer(movePacket.x, movePacket.y, movePacket.z, player)) {
                                    movePacket.eid = player.getId();
                                    player.getViewers().values().forEach(viewer -> viewer.dataPacket(pk0));
                                }
                            }
                        }
                    }
                }
                break;
            case SynapseInfo.PLAYER_LOGOUT_PACKET:
                PlayerLogoutPacket logout = (PlayerLogoutPacket) pk;
                if (inputDispatcher == null) {
                    this.playerLogoutQueue.offer(logout);
                } else {
                    ServerInputDispatcher.Session session = inputSessions.get(logout.sessionId);
                    if (session != null && session.isActive()) {
                        submitInput(session, 0, () -> {
                            handlePlayerLogout(logout);
                            inputDispatcher.invalidate(session);
                            inputSessions.remove(logout.sessionId, session);
                        });
                    }
                }
                break;
            case SynapseInfo.PLUGIN_MESSAGE_PACKET:
                PluginMessagePacket messagePacket = (PluginMessagePacket) pk;

                this.synapse.getMessenger().dispatchIncomingMessage(this, messagePacket.channel, messagePacket.data);
                break;
        }
    }

    /**
     * 检查数据包坐标是否接近玩家服务端位置
     * 用于网络层过滤 TP 后客户端发来的旧坐标包
     *
     * @param packetX 数据包中的X坐标
     * @param packetY 数据包中的Y坐标
     * @param packetZ 数据包中的Z坐标
     * @param player  玩家实例
     * @return 坐标是否在合理范围内（XZ偏差不超过6，Y偏差不超过6格）
     */
    private static boolean isPositionNearPlayer(float packetX, float packetY, float packetZ, SynapsePlayer player) {
        int yThreshold = player.isGliding() ? 20 : 6;

        return Math.abs(packetX - player.x) <= 6
                && Math.abs(packetZ - player.z) <= 6
                && Math.abs((int) packetY - (int) player.y) <= yThreshold;
    }

    private static class RedirectPacketEntry {
        private final SynapsePlayer player;
        private final DataPacket dataPacket;
        private final long movementEpoch;

        private RedirectPacketEntry(SynapsePlayer player, DataPacket dataPacket, long movementEpoch) {
            this.player = player;
            this.dataPacket = dataPacket;
            this.movementEpoch = movementEpoch;
        }
    }

    @Nullable
    public static List<DataPacket> processBatch(BatchPacket packet, int protocol, boolean netease, byte compressionAlgorithm) {
        byte[] payload = packet.payload;

        byte[] data;
        try {
            if (protocol >= 554) {
//            if (protocol >= 649) {
                Compressor compressor = Compressor.get(compressionAlgorithm);
                if (compressor == null) {
                    compressor = Compressor.NONE;
                }
                data = compressor.decompress(payload);
            } else if (protocol >= 407) {
                data = Network.inflateRaw(payload, MAX_SIZE);
            } else {
                data = Zlib.inflate(payload, MAX_SIZE);
            }
        } catch (Exception e) {
            // malformed batch
            return null;
        }

        int len = data.length;
        BinaryStream stream = new BinaryStream(data);
        AbstractProtocol apl = AbstractProtocol.fromRealProtocol(protocol);
        boolean v1180 = apl == AbstractProtocol.PROTOCOL_118;
        int count = 0;
        List<DataPacket> packets = new ObjectArrayList<>();
        try {
//            boolean tooManyPackets = false;
            while (stream.offset < len) {
                count++;
                if (count >= 1300) {
                    // too many packets in batch
//                    throw new ProtocolException("Illegal batch with " + count + " packets");
                    return null;
//                    tooManyPackets = true;
                }

                byte[] buf = stream.getByteArray();
                if (buf.length == 0) {
                    // empty packet
                    return null;
                }

                AbstractProtocol.PacketHeadData head = apl.tryDecodePacketHead(buf, false);
                if (head != null) {
                    int pid = head.getPid();
                    if (pid <= 0 || pid >= PACKET_TYPE_COUNT || pid == ProtocolInfo.BATCH_PACKET) {
                        // invalid packet
                        return null;
                    }

                    if (v1180 && pid == ProtocolInfo.SUB_CHUNK_REQUEST_PACKET) {
                        // 1.18.0的子区块请求包不参与计数 (1.18.0每tick可能会发送上万个子区块请求包, 1.18.10修复)
                        count--;
                    }

                    try {
                        DataPacket pk;
                        if ((pk = PacketRegister.getPacket(head.getPid(), protocol)) != null) {
                            pk.setBuffer(buf, head.getStartOffset());
                            pk.setHelper(apl.getHelper());
                            pk.neteaseMode = netease;
                            pk.decode();
                            packets.add(pk);
                            if (PACKET_EOF_DEBUG && !pk.feof()) {
                                ByteBuf wrapped = Unpooled.wrappedBuffer(buf);
                                log.warn("{} {} {}\n{}", protocol, netease, head.getPid(), ByteBufUtil.prettyHexDump(wrapped));
                                wrapped.release();
                            }
                        }
                    } catch (Exception e) {
                        MainLogger.getLogger().logException(e);
                        // malformed packet
                        if (PACKET_EOF_DEBUG) {
                            ByteBuf wrapped = Unpooled.wrappedBuffer(buf);
                            log.error("{} {} {}\n{}", protocol, netease, head.getPid(), ByteBufUtil.prettyHexDump(wrapped));
                            wrapped.release();
                        }
                        return null;
                    }
                }
            }
//            if (tooManyPackets) {
//                log.warn("too many packets in batch: {}", count);
//            }
            return packets;
        } catch (Exception e) {
            if (Nukkit.DEBUG > 0) {
//                Server.getInstance().getLogger().debug("BatchPacket 0x" + Binary.bytesToHexString(packet.payload));
                Server.getInstance().getLogger().logException(e);
            }
            // malformed batch
        }
        return null;
    }

    public void sendPluginMessage(Plugin plugin, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(this.synapse.getMessenger(), plugin, channel, message);

        PluginMessagePacket pk = new PluginMessagePacket();
        pk.channel = channel;
        pk.data = message;

        this.sendDataPacket(pk);
    }

    @Override
    public String toString() {
        return "SynapseEntry" +
                "\nenable=" + enable +
                "\nserverIp=" + serverIp +
                "\nport=" + port +
                "\nverified=" + verified +
                "\nlastUpdate=" + lastUpdate +
                "\nlastRecvInfo=" + lastRecvInfo +
                "\nclientData=" + clientData;
    }
}
