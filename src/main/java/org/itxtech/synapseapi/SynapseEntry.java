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
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import com.fasterxml.jackson.core.type.TypeReference;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.event.player.SynapsePlayerCreationEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTooManyPacketsInBatchEvent;
import org.itxtech.synapseapi.messaging.StandardMessenger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.network.SynLibInterface;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.*;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.ClientData.Entry;
import org.itxtech.synapseapi.utils.DataPacketEidReplacer;
import org.itxtech.synapseapi.utils.PacketLogger;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import static org.itxtech.synapseapi.SynapseSharedConstants.SERVERBOUND_PACKET_LOGGING;

/**
 * @author boybook
 */
@Log4j2
public class SynapseEntry {
    private static final TypeReference<Map<UUID, Integer>> NETWORK_LATENCY_TYPE_REFERENCE = new TypeReference<Map<UUID, Integer>>() {
    };

    public static final int MAX_SIZE = 12 * 1024 * 1024; // 12MB

    public static final int[] PACKET_COUNT_LIMIT = new int[ProtocolInfo.COUNT];
    private static final int PACKET_TYPE_COUNT = ProtocolInfo.COUNT - 512; // 目前没有ID大于0x1ff的包, 节省一半内存. 未来不够用了再改
    public static final int[] globalPacketCountThisTick = new int[PACKET_TYPE_COUNT];

    static {
        Arrays.fill(PACKET_COUNT_LIMIT, 10);
        PACKET_COUNT_LIMIT[ProtocolInfo.INVENTORY_TRANSACTION_PACKET] = 64 * 9 + 64 * 9 + 64; // extreme case (shift-click crafting): 64x9 (inputs) + 64x9 (output on crafting grid) + 64 (outputs to main slot)
        PACKET_COUNT_LIMIT[ProtocolInfo.CRAFTING_EVENT_PACKET] = 64;
        PACKET_COUNT_LIMIT[ProtocolInfo.PACKET_PY_RPC] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET] = 15000; // 1.18.0 facepalm
        PACKET_COUNT_LIMIT[ProtocolInfo.PLAYER_ACTION_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.ANIMATE_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.INTERACT_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.RESOURCE_PACK_CHUNK_REQUEST_PACKET] = 1000;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET] = 0;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2] = 0;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = 30;
        PACKET_COUNT_LIMIT[ProtocolInfo.COMMAND_REQUEST_PACKET] = 5;
        PACKET_COUNT_LIMIT[ProtocolInfo.SETTINGS_COMMAND_PACKET] = 5;
        PACKET_COUNT_LIMIT[ProtocolInfo.NETWORK_STACK_LATENCY_PACKET] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.SET_ACTOR_LINK_PACKET] = 50;
    }

    private final SynapseAPI synapse;
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
    private final Map<UUID, SynapsePlayer> players = new Object2ObjectOpenHashMap<>();
    private SynLibInterface synLibInterface;
    private ClientData clientData;
    private String serverDescription;

    public SynapseEntry(SynapseAPI synapse, String serverIp, int port, boolean isMainServer, String password, String serverDescription) {
        this.synapse = synapse;
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

    public void broadcastPacket(SynapsePlayer[] players, DataPacket packet) {
        this.broadcastPacket(players, packet, false);
    }

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
                    try{
                        Thread.sleep(10 - tickUseTime);
                    } catch (InterruptedException ignore) {}
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

            Arrays.fill(globalPacketCountThisTick, 0);

            PlayerLoginPacket playerLoginPacket;
            Random random = ThreadLocalRandom.current();
            LOGIN:
            while ((playerLoginPacket = playerLoginQueue.poll()) != null) {
                globalPacketCountThisTick[ProtocolInfo.LOGIN_PACKET]++;

                UUID uuid = playerLoginPacket.uuid;
                UUID sessionId = playerLoginPacket.sessionId;

                for (Player player : synapse.getServer().getOnlinePlayerList()) {
                    if (uuid.equals(player.getUniqueId())) {
                        try {
                            player.kick(PlayerKickEvent.Reason.NEW_CONNECTION, "disconnectionScreen.loggedinOtherLocation", false);
                        } catch (Exception e) {
                            log.throwing(e);
                        }

                        PlayerLogoutPacket pk = new PlayerLogoutPacket();
                        pk.sessionId = sessionId;
                        pk.reason = "disconnectionScreen.serverIdConflict";
                        sendDataPacket(pk);
                        continue LOGIN;
                    }
                }

                int protocol = playerLoginPacket.protocol;
                InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(playerLoginPacket.address, playerLoginPacket.port);

                Class<? extends SynapsePlayer> clazz = determinePlayerClass(protocol);
                SynapsePlayerCreationEvent ev = new SynapsePlayerCreationEvent(synLibInterface, clazz, clazz, random.nextLong(), socketAddress);
                getSynapse().getServer().getPluginManager().callEvent(ev);
                clazz = ev.getPlayerClass();

                try {
                    Constructor<? extends SynapsePlayer> constructor = clazz.getConstructor(SourceInterface.class, SynapseEntry.class, Long.class, InetSocketAddress.class);
                    SynapsePlayer player = constructor.newInstance(synLibInterface, this.entry, ev.getClientId(), ev.getSocketAddress());
                    player.setUniqueId(uuid);
                    player.setSessionId(sessionId);
                    players.put(sessionId, player);
                    getSynapse().getServer().addPlayer(socketAddress, player);
                    player.handleLoginPacket(playerLoginPacket);
                } catch (Exception e) {
                    Server.getInstance().getLogger().logException(e);
                }
            }

            RedirectPacketEntry redirectPacketEntry;
            Int2ObjectMap<int[]> playerPacketCountThisTick = new Int2ObjectOpenHashMap<>();
            while ((redirectPacketEntry = redirectPacketQueue.poll()) != null) {
                //Server.getInstance().getLogger().warning("C => S  " + redirectPacketEntry.dataPacket.getClass().getSimpleName());
                DataPacket packet = DataPacketEidReplacer.replaceBack(redirectPacketEntry.dataPacket, SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID, redirectPacketEntry.player.getId());
                globalPacketCountThisTick[packet.pid()]++;
                int[] counter = playerPacketCountThisTick.get((int) redirectPacketEntry.player.getLoaderId());
                if (counter == null) {
                    playerPacketCountThisTick.put((int) redirectPacketEntry.player.getLoaderId(), counter = new int[]{1});
                }
                counter[0]++;
                if (redirectPacketEntry.player.isOnline() && counter[0] > 10000) {
                    redirectPacketEntry.player.onPacketViolation(PacketViolationReason.RECEIVING_PACKETS_TOO_FAST, "sync");
                    continue;
                }
                if (SERVERBOUND_PACKET_LOGGING && log.isTraceEnabled()) {
                    PacketLogger.handleServerboundPacket(redirectPacketEntry.player, packet);
                }
                redirectPacketEntry.player.handleDataPacket(packet);
            }

            PlayerLogoutPacket playerLogoutPacket;
            while ((playerLogoutPacket = playerLogoutQueue.poll()) != null) {
                UUID sessionId = playerLogoutPacket.sessionId;
                Player player = players.get(sessionId);
                if (player != null) {
                    player.close("", playerLogoutPacket.reason, true);
                    removePlayer(sessionId);
                }
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

    private final Queue<PlayerLoginPacket> playerLoginQueue = new LinkedBlockingQueue<>();
    private final Queue<PlayerLogoutPacket> playerLogoutQueue = new LinkedBlockingQueue<>();
    private final Queue<RedirectPacketEntry> redirectPacketQueue = new LinkedBlockingQueue<>();

    public void handleDataPacket(SynapseDataPacket pk) {
        //this.getSynapse().getLogger().warning("Received packet " + pk.pid() + "(" + pk.getClass().getSimpleName() + ") from " + this.serverIp + ":" + this.port);
        HANDLER:
        switch (pk.pid()) {
            case SynapseInfo.DISCONNECT_PACKET:
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
                this.playerLoginQueue.offer((PlayerLoginPacket)pk);
                break;
            case SynapseInfo.REDIRECT_PACKET:
                RedirectPacket redirectPacket = (RedirectPacket) pk;

                SynapsePlayer player = this.players.get(redirectPacket.sessionId);
                if (player != null && !player.isViolated()) {
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
                                    if (subPacket instanceof InteractPacket113 interactPacket && interactPacket.action == InteractPacket113.ACTION_MOUSEOVER) {
                                        // 看向实体的交互包不稳定因此不参与计数
                                    } else if (packetCount[packetId]++ > PACKET_COUNT_LIMIT[packetId]) {
                                        tooManyPackets = true;
                                        continue;
                                    }

                                    this.redirectPacketQueue.offer(new RedirectPacketEntry(player, subPacket));

                                    if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && player.isOnline()) {
                                        //玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                        boolean serverAuthoritativeMovement = player.isServerAuthoritativeMovementEnabled();
                                        if (!serverAuthoritativeMovement && subPacket instanceof MovePlayerPacket) {
                                            //判断是否和玩家自身在附近区块
                                            /*if (Math.abs((int) ((MovePlayerPacket) subPacket).x >> 4 - player.getFloorX() >> 4) > 4
                                                    || Math.abs((int) ((MovePlayerPacket) subPacket).z >> 4 - player.getFloorZ() >> 4) > 4
                                                    || Math.abs((int) ((MovePlayerPacket) subPacket).y - player.getFloorY()) > 100
                                            ) {
                                                continue;
                                            }*/
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
                                            //判断是否和玩家自身在附近区块
                                            /*if (Math.abs((int) ((MovePlayerPacket116100NE) subPacket).x >> 4 - player.getFloorX() >> 4) > 4
                                                    || Math.abs((int) ((MovePlayerPacket116100NE) subPacket).z >> 4 - player.getFloorZ() >> 4) > 4
                                                    || Math.abs((int) ((MovePlayerPacket116100NE) subPacket).y - player.getFloorY()) > 100
                                            ) {
                                                continue;
                                            }*/
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
                                                        oldMovePacket.setChannel(DataPacket.CHANNEL_PLAYER_MOVING);
                                                    }
                                                    viewer.dataPacket(oldMovePacket);
                                                } else {
                                                    viewer.dataPacket(subPacket);
                                                }
                                            }
                                        } else if (serverAuthoritativeMovement && subPacket instanceof IPlayerAuthInputPacket) {
                                            IPlayerAuthInputPacket authInputPacket = (IPlayerAuthInputPacket) subPacket;

                                            long tick = authInputPacket.getTick();
                                            if (player.lastAuthInputPacketTick > tick) {
                                                player.setViolated("input_tick");
                                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                                    player.onPacketViolation(PacketViolationReason.IMPOSSIBLE_BEHAVIOR, "input_tick", String.valueOf(tick));
                                                });
                                                break HANDLER;
                                            }
                                            player.lastAuthInputPacketTick = tick;

                                            //判断是否和玩家自身在附近区块
                                            /*if (Math.abs((int) authInputPacket.getX() >> 4 - player.getFloorX() >> 4) > 4
                                                    || Math.abs((int) authInputPacket.getZ() >> 4 - player.getFloorZ() >> 4) > 4
                                                    || Math.abs((int) authInputPacket.getY() - player.getFloorY()) > 100
                                            ) {
                                                continue;
                                            }*/
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
                                                packet.mode = MovePlayerPacket.MODE_NORMAL;
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
                            this.redirectPacketQueue.offer(new RedirectPacketEntry(player, pk0));
                            if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && !player.isServerAuthoritativeMovementEnabled() && pk0 instanceof MovePlayerPacket) {
                                //玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                ((MovePlayerPacket) pk0).eid = player.getId();
                                player.getViewers().values().forEach(viewer -> viewer.dataPacket(pk0));
                            }
                        }
                    }
                }
                break;
            case SynapseInfo.PLAYER_LOGOUT_PACKET:
                this.playerLogoutQueue.offer((PlayerLogoutPacket)pk);
                break;
            case SynapseInfo.PLUGIN_MESSAGE_PACKET:
                PluginMessagePacket messagePacket = (PluginMessagePacket) pk;

                this.synapse.getMessenger().dispatchIncomingMessage(this, messagePacket.channel, messagePacket.data);
                break;
        }
    }

    private static class RedirectPacketEntry {
        private final SynapsePlayer player;
        private final DataPacket dataPacket;

        private RedirectPacketEntry(SynapsePlayer player, DataPacket dataPacket) {
            this.player = player;
            this.dataPacket = dataPacket;
        }
    }

    @Nullable
    public static List<DataPacket> processBatch(BatchPacket packet, int protocol, boolean netease, byte compressionAlgorithm) {
        byte[] payload = packet.payload;

        byte[] data;
        try {
            if (protocol >= 649) {
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
                        }
                    } catch (Exception e) {
                        MainLogger.getLogger().logException(e);
                        // malformed packet
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
