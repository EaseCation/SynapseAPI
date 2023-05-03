package org.itxtech.synapseapi;

import cn.nukkit.Nukkit;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Network;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.JsonUtil;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.event.player.SynapsePlayerCreationEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTooManyBatchPacketsEvent;
import org.itxtech.synapseapi.event.player.SynapsePlayerTooManyPacketsInBatchEvent;
import org.itxtech.synapseapi.messaging.StandardMessenger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.network.SynLibInterface;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.*;
import org.itxtech.synapseapi.utils.ClientData;
import org.itxtech.synapseapi.utils.DataPacketEidReplacer;
import org.itxtech.synapseapi.utils.PacketLogger;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    public static int[] globalPacketCountThisTick = new int[ProtocolInfo.COUNT];

    static {
        Arrays.fill(PACKET_COUNT_LIMIT, 10);
        PACKET_COUNT_LIMIT[ProtocolInfo.INVENTORY_TRANSACTION_PACKET] = 64 * 9 + 64 * 9 + 64; // extreme case (shift-click crafting): 64x9 (inputs) + 64x9 (output on crafting grid) + 64 (outputs to main slot)
        PACKET_COUNT_LIMIT[ProtocolInfo.CRAFTING_EVENT_PACKET] = 64;
        PACKET_COUNT_LIMIT[ProtocolInfo.PACKET_PY_RPC] = 50;
        PACKET_COUNT_LIMIT[ProtocolInfo.SUB_CHUNK_REQUEST_PACKET] = 100;
        PACKET_COUNT_LIMIT[ProtocolInfo.PLAYER_ACTION_PACKET] = 15;
        PACKET_COUNT_LIMIT[ProtocolInfo.ANIMATE_PACKET] = 15;
        PACKET_COUNT_LIMIT[ProtocolInfo.INTERACT_PACKET] = 20;
        PACKET_COUNT_LIMIT[ProtocolInfo.RESOURCE_PACK_CHUNK_REQUEST_PACKET] = 1000;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET] = 30;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2] = 30;
        PACKET_COUNT_LIMIT[ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3] = 30;
        PACKET_COUNT_LIMIT[ProtocolInfo.COMMAND_REQUEST_PACKET] = 5;
        PACKET_COUNT_LIMIT[ProtocolInfo.SETTINGS_COMMAND_PACKET] = 5;
    }

    // private final Timing handleDataPacketTiming = TimingsManager.getTiming("SynapseEntry - HandleDataPacket");
    // private final Timing handleRedirectPacketTiming = TimingsManager.getTiming("SynapseEntry - HandleRedirectPacket");

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
    private final Map<UUID, Integer> networkLatency = new ConcurrentHashMap<>();
    private SynLibInterface synLibInterface;
    private ClientData clientData;
    private String serverDescription;
    private Thread asyncTicker;

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

        this.asyncTicker = new Thread(new AsyncTicker(), "SynapseAPI Async Ticker");
        this.asyncTicker.start();

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

    public int getNetworkLatency(UUID uuid) {
        return this.networkLatency.getOrDefault(uuid, 0);
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
        broadcastPacket.entries = new ObjectArrayList<>();
        for (SynapsePlayer player : players) {
            broadcastPacket.entries.add(player.getUniqueId());
        }
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
        this.getSynapse().getLogger().notice("Connecting " + this.getHash());
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
                threadTick();
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
                getSynapse().getLogger().notice("Trying to re-login to Synapse Server: " + getHash());
                synapseInterface.getClient().setNeedAuth(true);
                lastLogin = System.currentTimeMillis();
            }

            PlayerLoginPacket playerLoginPacket;
            Random random = ThreadLocalRandom.current();
            while ((playerLoginPacket = playerLoginQueue.poll()) != null) {
                int protocol = playerLoginPacket.protocol;
                InetSocketAddress socketAddress = InetSocketAddress.createUnresolved(playerLoginPacket.address, playerLoginPacket.port);

                Class<? extends SynapsePlayer> clazz = determinePlayerClass(protocol);
                SynapsePlayerCreationEvent ev = new SynapsePlayerCreationEvent(synLibInterface, clazz, clazz, random.nextLong(), socketAddress);
                getSynapse().getServer().getPluginManager().callEvent(ev);
                clazz = ev.getPlayerClass();

                try {
                    Constructor<? extends SynapsePlayer> constructor = clazz.getConstructor(SourceInterface.class, SynapseEntry.class, Long.class, InetSocketAddress.class);
                    SynapsePlayer player = constructor.newInstance(synLibInterface, this.entry, ev.getClientId(), ev.getSocketAddress());
                    player.setUniqueId(playerLoginPacket.uuid);
                    players.put(playerLoginPacket.uuid, player);
                    getSynapse().getServer().addPlayer(socketAddress, player);
                    player.handleLoginPacket(playerLoginPacket);
                } catch (Exception e) {
                    Server.getInstance().getLogger().logException(e);
                }
            }

            RedirectPacketEntry redirectPacketEntry;
            Arrays.fill(globalPacketCountThisTick, 0);
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
                    redirectPacketEntry.player.onPacketViolation(PacketViolationReason.RECEIVING_PACKETS_TOO_FAST);
                    continue;
                }
                if (SERVERBOUND_PACKET_LOGGING && log.isTraceEnabled()) {
                    PacketLogger.handleServerboundPacket(redirectPacketEntry.player, packet);
                }
                redirectPacketEntry.player.handleDataPacket(packet);
            }

            PlayerLogoutPacket playerLogoutPacket;
            while ((playerLogoutPacket = playerLogoutQueue.poll()) != null) {
                UUID uuid1 = playerLogoutPacket.uuid;
                Player player = players.get(uuid1);
                if (player != null) {
                    player.close(playerLogoutPacket.reason, playerLogoutPacket.reason, true);
                    removePlayer(uuid1);
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
            this.synapseInterface.reconnect();
        }
    }

    public void removePlayer(SynapsePlayer player) {
        this.removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
        this.networkLatency.remove(uuid);
    }

    private final Queue<PlayerLoginPacket> playerLoginQueue = new LinkedBlockingQueue<>();
    private final Queue<PlayerLogoutPacket> playerLogoutQueue = new LinkedBlockingQueue<>();
    private final Queue<RedirectPacketEntry> redirectPacketQueue = new LinkedBlockingQueue<>();

    public void handleDataPacket(SynapseDataPacket pk) {
        // this.handleDataPacketTiming.startTiming();
        //this.getSynapse().getLogger().warning("Received packet " + pk.pid() + "(" + pk.getClass().getSimpleName() + ") from " + this.serverIp + ":" + this.port);
        HANDLER:
        switch (pk.pid()) {
            case SynapseInfo.DISCONNECT_PACKET:
                DisconnectPacket disconnectPacket = (DisconnectPacket) pk;
                this.verified = false;
                switch (disconnectPacket.type) {
                    case DisconnectPacket.TYPE_GENERIC:
                        this.getSynapse().getLogger().notice("Synapse Client has disconnected due to " + disconnectPacket.message);
                        this.synapseInterface.reconnect();
                        break;
                    case DisconnectPacket.TYPE_WRONG_PROTOCOL:
                        this.getSynapse().getLogger().error(disconnectPacket.message);
                        break;
                }
                break;
            case SynapseInfo.INFORMATION_PACKET:
                InformationPacket informationPacket = (InformationPacket) pk;
                switch (informationPacket.type) {
                    case InformationPacket.TYPE_LOGIN:
                        if (informationPacket.message.equals(InformationPacket.INFO_LOGIN_SUCCESS)) {
                            this.getSynapse().getLogger().notice("Login success to " + this.serverIp + ":" + this.port);
                            this.verified = true;
                        } else if (informationPacket.message.equals(InformationPacket.INFO_LOGIN_FAILED)) {
                            this.getSynapse().getLogger().notice("Login failed to " + this.serverIp + ":" + this.port);
                        }
                        break;
                    case InformationPacket.TYPE_CLIENT_DATA:
                        try {
                            this.clientData = JsonUtil.COMMON_JSON_MAPPER.readValue(informationPacket.message, ClientData.class);
                        } catch (JsonProcessingException e) {
                            synapse.getServer().getLogger().logException(e);
                        }
                        this.lastRecvInfo = System.currentTimeMillis();
                        //this.getSynapse().getLogger().debug("Received ClientData from " + this.serverIp + ":" + this.port);
                        break;
                    case InformationPacket.TYPE_PLAYER_NETWORK_LATENCY:
                        try {
                            Map<UUID, Integer> pings = JsonUtil.COMMON_JSON_MAPPER.readValue(informationPacket.message, NETWORK_LATENCY_TYPE_REFERENCE);
                            pings.forEach((uuid, ping) -> {
                                if (this.players.containsKey(uuid)) {
                                    this.networkLatency.put(uuid, ping);
                                    //this.getSynapse().getLogger().debug("更新玩家 " + uuid + " 的PING=" + ping);
                                }
                            });
                        } catch (JsonProcessingException e) {
                            synapse.getServer().getLogger().logException(e);
                        }
                        break;
                }
                break;
            case SynapseInfo.PLAYER_LOGIN_PACKET:
                this.playerLoginQueue.offer((PlayerLoginPacket)pk);
                break;
            case SynapseInfo.REDIRECT_PACKET:
                RedirectPacket redirectPacket = (RedirectPacket) pk;

                UUID uuid = redirectPacket.uuid;
                SynapsePlayer player = this.players.get(uuid);
                if (player != null && !player.violated) {
                    DataPacket pk0 = PacketRegister.getFullPacket(redirectPacket.mcpeBuffer, redirectPacket.protocol);
                    //Server.getInstance().getLogger().info("to server : " + pk0.getClass().getName());
                    if (pk0 != null) {
                        // this.handleRedirectPacketTiming.startTiming();
                        //pk0.decode();
                        if (pk0.pid() == ProtocolInfo.BATCH_PACKET) {
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
                                    player.violated = true;
                                    synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                        new SynapsePlayerTooManyBatchPacketsEvent(player, SynapsePlayer.INCOMING_PACKET_BATCH_MAX_BUDGET + 1).call();
                                        player.onPacketViolation(PacketViolationReason.RECEIVING_BATCHES_TOO_FAST);
                                    });
                                    break;
                                }
                            }
                            player.incomingPacketBatchBudget--;

                            List<DataPacket> packets = processBatch((BatchPacket) pk0, redirectPacket.protocol, player.isNetEaseClient());
                            if (packets == null) {
                                player.violated = true;
                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                    player.onPacketViolation(PacketViolationReason.MALFORMED_PACKET);
                                });
                                break;
                            }
                            // Server.getInstance().getLogger().info("tick: " + Server.getInstance().getTick() + " to server " + packets.size() + " packets");

                            short[] packetCount = new short[ProtocolInfo.COUNT - 512]; // 目前没有ID大于0x1ff的包, 节省一半内存. 未来不够用了再改
                            boolean tooManyPackets = false;
                            for (DataPacket subPacket : packets) {
                                int packetId = subPacket.pid();
                                if (packetId >= ProtocolInfo.COUNT - 512) {
                                    player.violated = true;
                                    synapse.getServer().getScheduler().scheduleTask(synapse, () -> {
                                        player.onPacketViolation(PacketViolationReason.MALFORMED_PACKET);
                                    });
                                    break HANDLER;
                                }
                                try {
                                    if (packetCount[packetId]++ > PACKET_COUNT_LIMIT[packetId]) {
                                        tooManyPackets = true;
                                        continue;
                                    }

                                    this.redirectPacketQueue.offer(new RedirectPacketEntry(player, subPacket));

                                    if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && player.isOnline()) {
                                        //玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                        if (subPacket instanceof MovePlayerPacket) {
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
                                        } else if (subPacket instanceof MovePlayerPacket116100NE) {
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
                                        } else if (subPacket instanceof IPlayerAuthInputPacket) {
                                            IPlayerAuthInputPacket authInputPacket = (IPlayerAuthInputPacket) subPacket;
                                            //判断是否和玩家自身在附近区块
                                            /*if (Math.abs((int) authInputPacket.getX() >> 4 - player.getFloorX() >> 4) > 4
                                                    || Math.abs((int) authInputPacket.getZ() >> 4 - player.getFloorZ() >> 4) > 4
                                                    || Math.abs((int) authInputPacket.getY() - player.getFloorY()) > 100
                                            ) {
                                                continue;
                                            }*/
                                            if (authInputPacket.getDeltaX() != 0 || authInputPacket.getDeltaZ() != 0 || authInputPacket.getDeltaY() - player.getEyeHeight() != player.getY() || authInputPacket.getHeadYaw() != player.getYaw() || authInputPacket.getPitch() != player.getPitch()) {
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
                                                //packet.ridingEid = authInputPacket.ridingEid;
                                                //packet.int1 = authInputPacket.int1;
                                                //packet.int2 = authInputPacket.int2;
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
                                Server.getInstance().getPluginManager().callEvent(new SynapsePlayerTooManyPacketsInBatchEvent(player, packetCount));
                                synapse.getServer().getScheduler().scheduleTask(synapse, () -> player.violation += 35);
                            }
                        } else {
                            this.redirectPacketQueue.offer(new RedirectPacketEntry(player, pk0));
                            if (SynapseAPI.getInstance().isNetworkBroadcastPlayerMove() && pk0 instanceof MovePlayerPacket) {
                                //玩家体验优化：直接不经过主线程广播玩家移动，插件过度干预可能会造成移动鬼畜问题
                                ((MovePlayerPacket) pk0).eid = player.getId();
                                player.getViewers().values().forEach(viewer -> viewer.dataPacket(pk0));
                            }
                        }
                        // this.handleRedirectPacketTiming.stopTiming();
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
        //this.handleDataPacketTiming.stopTiming();
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
    public static List<DataPacket> processBatch(BatchPacket packet, int protocol, boolean netease) {
        byte[] data;
        try {
            if (protocol < 407) data = Zlib.inflate(packet.payload, MAX_SIZE);
            else data = Network.inflateRaw(packet.payload, MAX_SIZE);
        } catch (Exception e) {
            // malformed batch
            return null;
        }

        int len = data.length;
        BinaryStream stream = new BinaryStream(data);
        AbstractProtocol apl = AbstractProtocol.fromRealProtocol(protocol);
        int count = 0;
        List<DataPacket> packets = new ObjectArrayList<>();
        try {
            while (stream.offset < len) {
                count++;
                if (count >= 1300) {
                    // too many packets in batch
//                    throw new ProtocolException("Illegal batch with " + count + " packets");
                    return null;
                }

                byte[] buf = stream.getByteArray();
                AbstractProtocol.PacketHeadData head = apl.tryDecodePacketHead(buf, false);
                if (head != null) {
                    int pid = head.getPid();
                    if (pid <= 0 || pid >= ProtocolInfo.COUNT || pid == ProtocolInfo.BATCH_PACKET) {
                        // invalid packet
                        return null;
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
