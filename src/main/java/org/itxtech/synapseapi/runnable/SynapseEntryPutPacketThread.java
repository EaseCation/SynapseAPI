package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.MainLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.CompatibilityPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.SynapseMetrics;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.utils.PacketLogger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

import static org.itxtech.synapseapi.SynapseSharedConstants.CLIENTBOUND_PACKET_LOGGING;

/**
 * org.itxtech.synapseapi.runnable
 * ===============
 * author: boybook
 * SynapseAPI Project
 * itxTech
 * ===============
 */
@Log4j2
public class SynapseEntryPutPacketThread extends Thread {
    // 使用独立递增 ID，避免把 System.nanoTime() 同时当作协议标识和延迟计时来源。
    private static final AtomicLong BATCH_TAIL_LATENCY_ID = new AtomicLong(1);
    private static SynapseMetrics METRICS;

    private final SynapseInterface synapseInterface;
    private final Queue<Entry> queue = new LinkedBlockingQueue<>();
    private final Queue<BroadcastEntry> broadcastQueue = new LinkedBlockingQueue<>();

    private final boolean isAutoCompress;
    private long tickUseTime = 0;
    private long lastWarning;
    private boolean isRunning = true;

    public SynapseEntryPutPacketThread(SynapseInterface synapseInterface) {
        super("SynapseEntryPutPacketThread");
        this.synapseInterface = synapseInterface;
        this.isAutoCompress = SynapseAPI.getInstance().isAutoCompress();
        this.start();
    }

    public void addMainToThread(SynapsePlayer player, DataPacket packet) {
        if (player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        this.queue.offer(new Entry(player, packet));
    }

    /**
     * 将数据包和它在 DataPacketSendEvent 中登记的尾部 NSL 请求一起入队。
     * 回调只有在该数据包成功加入 outboundQueue 后才会转入当前 Batch。
     */
    public void addMainToThread(SynapsePlayer player, DataPacket packet,
                                List<LongConsumer> batchTailLatencyCallbacks) {
        if (player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        this.queue.offer(new Entry(player, packet, batchTailLatencyCallbacks));
    }

    public void addMainToThreadBroadcast(SynapsePlayer[] players, DataPacket[] packets) {
        if(players.length == 0 || packets.length == 0) {
            return;
        }

        this.broadcastQueue.offer(new BroadcastEntry(players, packets));
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    private static final List<AbstractProtocol> fullProtocols = Arrays.stream(AbstractProtocol.getValues())
            .filter(protocol -> protocol != AbstractProtocol.PROTOCOL_11)
            .toList();

    private static class BatchPacketEntry {
        private final DataPacket normal;
        private final DataPacket netease;

        private BatchPacketEntry(DataPacket normal, DataPacket netease) {
            this.normal = normal;
            this.netease = netease;
        }

        private DataPacket getNormalVersion() {
            return normal;
        }

        private DataPacket getNetEaseVersion() {
            return netease != null ? netease : normal;
        }
    }

    @Override
    public void run() {
        Long2ObjectMap<SynapsePlayer> queuedPlayers = new Long2ObjectOpenHashMap<>();

        Network network = Server.getInstance().getNetwork();
        while (this.isRunning) {
//            long start = System.currentTimeMillis();

            SynapseMetrics metrics = METRICS;
            boolean hasMetrics = metrics != null;

            Entry entry;
            while ((entry = queue.poll()) != null) {
                try {
                    if (!entry.player.isClosed() || entry.packet.pid() == ProtocolInfo.DISCONNECT_PACKET) {
                        DataPacket old = entry.packet;

                        entry.packet = PacketRegister.getCompatiblePacket(entry.packet, (entry.player).getProtocol(), entry.player.isNetEaseClient());

                        if (entry.packet == null) {
                            MainLogger.getLogger().info("NULL PACKET " + old.getClass().getSimpleName());
                            continue;
                        }

                        if (entry.packet != old) { //数据包进行了对应版本的转换
                            entry.packet.neteaseMode = entry.player.isNetEaseClient();
                        }

                        if (!entry.packet.isEncoded) {
                            entry.packet.setHelper(AbstractProtocol.fromRealProtocol(entry.player.getProtocol()).getHelper());
                            entry.packet.tryEncode();
                        }

                        if (entry.packet instanceof BatchPacket batch) {
                            List<byte[]> outboundQueue = entry.player.outboundQueue;
                            if (!outboundQueue.isEmpty()) {
                                Compressor compressor = Compressor.byProtocol(entry.player.getProtocol());
                                RedirectPacket pk = new RedirectPacket();
                                pk.compressionAlgorithm = compressor.getAlgorithm();
                                pk.sessionId = entry.player.getSessionId();
                                pk.mcpeBuffer = batchPackets(outboundQueue, compressor);
                                outboundQueue.clear();

                                int bytes = pk.mcpeBuffer.length;
                                network.addUploadStatistic(bytes);
                                if (hasMetrics) {
                                    metrics.bytesOut(bytes);
                                }

                                this.synapseInterface.putPacket(pk);
                            }

                            RedirectPacket pk = new RedirectPacket();
                            pk.compressionAlgorithm = entry.player.getServer().getCompressor().getAlgorithm();
                            pk.sessionId = entry.player.getSessionId();

                            if (hasMetrics) {
                                Track[] tracks = batch.tracks;
                                if (tracks != null) {
                                    for (Track track : tracks) {
                                        metrics.packetOut(track.packetId, track.size);
                                    }
                                } else {
                                    metrics.packetOut(batch.pid(), 1 + batch.payload.length); //TODO: check me
                                }
                            }

                            pk.mcpeBuffer = Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batch.payload);

                            int bytes = pk.mcpeBuffer.length;
                            network.addUploadStatistic(bytes);
                            if (hasMetrics) {
                                metrics.bytesOut(bytes);
                            }

                            this.synapseInterface.putPacket(pk);
                        } else if (this.isAutoCompress) {
                            byte[] buffer = entry.packet.getBuffer();

                            if (hasMetrics) {
                                metrics.packetOut(entry.packet instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) entry.packet).origin.pid() : entry.packet.pid(), buffer.length);
                            }

                            if (CLIENTBOUND_PACKET_LOGGING && log.isTraceEnabled()) {
                                PacketLogger.handleClientboundPacket(entry.player, entry.packet);
                            }

                            entry.player.outboundQueue.add(buffer);
                            // 必须先加入包，再登记它的回调；这样尾部 NSL 不可能跑到该包前面。
                            entry.player.appendBatchTailLatencyCallbacks(entry.batchTailLatencyCallbacks);

                            queuedPlayers.put(entry.player.getId(), entry.player);
                        } else {
                            RedirectPacket pk = new RedirectPacket();
                            pk.compressionAlgorithm = entry.player.getServer().getCompressor().getAlgorithm();
                            pk.sessionId = entry.player.getSessionId();
                            pk.mcpeBuffer = entry.packet.getBuffer();

                            int bytes = pk.mcpeBuffer.length;
                            network.addUploadStatistic(bytes);
                            if (hasMetrics) {
                                metrics.packetOut(entry.packet instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) entry.packet).origin.pid() : entry.packet.pid(), bytes);
                                metrics.bytesOut(bytes);
                            }

                            this.synapseInterface.putPacket(pk);
                        }
                    }
                } catch (Exception e) {
                    MainLogger.getLogger().alert("Catch exception when put single packet", e);
                    if (entry.packet.stack != null)
                        MainLogger.getLogger().alert("Main thread stack", entry.packet.stack);
                } finally {
                    if (entry.packet != null) entry.packet.stack = null;
                }
            }

            for (SynapsePlayer player : queuedPlayers.values()) {
                List<byte[]> outboundQueue = player.outboundQueue;
                if (outboundQueue.isEmpty()) {
                    continue;
                }

                appendBatchTailLatency(player, outboundQueue);
                Compressor compressor = Compressor.byProtocol(player.getProtocol());
                byte[] buffer;
                try {
                    buffer = batchPackets(outboundQueue, compressor);
                } catch (IOException e) {
                    log.throwing(e);
                    continue;
                }
                outboundQueue.clear();

                RedirectPacket pk = new RedirectPacket();
                pk.compressionAlgorithm = compressor.getAlgorithm();
                pk.mcpeBuffer = buffer;
                pk.sessionId = player.getSessionId();
                this.synapseInterface.putPacket(pk);

                int bytes = buffer.length;
                network.addUploadStatistic(bytes);
                if (hasMetrics) {
                    metrics.bytesOut(bytes);
                }
            }
            queuedPlayers.clear();

            BroadcastEntry entry1;
            while ((entry1 = broadcastQueue.poll()) != null) {
                try {
                    //筛选出需要进行batch包装的协议，避免不需要的多余的包装浪费性能
                    List<SynapsePlayer> players = Arrays.stream(entry1.player).filter(Objects::nonNull).toList();
                    boolean haveNetEasePlayer = players.stream().anyMatch(SynapsePlayer::isNetEaseClient);
                    boolean[] haveNetEasePacket = new boolean[]{false};
                    Map<AbstractProtocol, List<BatchPacketEntry>> needPackets =
                            fullProtocols.stream()
                                    .filter(protocol ->
                                            players.stream()
                                                    .anyMatch(p -> AbstractProtocol.fromRealProtocol(p.getProtocol()) == protocol))
                                    .collect(Collectors.toMap(Function.identity(), v -> new ObjectArrayList<>()));

                    for (DataPacket targetPk : entry1.packet) {
                        /*RedirectPacket pk = new RedirectPacket();
                        pk.uuid = entry.player.getUniqueId();
                        pk.direct = entry.immediate;*/

                        if (targetPk.pid() == BatchPacket.NETWORK_ID) {
                            needPackets.forEach((protocol, packets) -> packets.add(new BatchPacketEntry(targetPk, null)));
                            continue;
                        }

                        needPackets.forEach((protocol, packets) -> {
                            try {
                                DataPacket packet = PacketRegister.getCompatiblePacket(targetPk, protocol, false);
                                DataPacket neteaseVersion = (haveNetEasePlayer && PacketRegister.isNetEaseSpecial(protocol, targetPk.pid())) ? PacketRegister.getCompatiblePacket(targetPk, protocol, true) : null;
                                if (neteaseVersion != null) haveNetEasePacket[0] = true;
                                if (packet != null) packets.add(new BatchPacketEntry(packet, neteaseVersion));
                            } catch (Exception e) {
                                MainLogger.getLogger().alert("Catch exception when put broadcast packet", e);
                                if (targetPk.stack != null) {
                                    MainLogger.getLogger().alert("Main thread stack", targetPk.stack);
                                }
                            } finally {
                                targetPk.stack = null;
                            }
                        });
                    }

                    Map<AbstractProtocol, Pair<byte[][], Track[][]>> finalData = new EnumMap<>(AbstractProtocol.class);
                    needPackets.forEach((protocol, packets) -> {
                        DataPacket[] dataPackets = packets.stream().map(BatchPacketEntry::getNormalVersion).toArray(DataPacket[]::new);
                        BatchPacket batch = batchPackets(dataPackets, protocol);
                        if (batch != null) {
                            if (haveNetEasePacket[0]) {
                                DataPacket[] neteasePackets = packets.stream().map(BatchPacketEntry::getNetEaseVersion).toArray(DataPacket[]::new);
                                BatchPacket batchNetEase = batchPackets(neteasePackets, protocol);
                                if (batchNetEase != null) {
                                    finalData.put(protocol, Pair.of(new byte[][]{Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batch.payload), Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batchNetEase.payload)}, new Track[][]{batch.tracks, batchNetEase.tracks}));
                                } else {
                                    finalData.put(protocol, Pair.of(new byte[][]{Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batch.payload)}, new Track[][]{batch.tracks}));
                                }
                            } else {
                                finalData.put(protocol, Pair.of(new byte[][]{Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batch.payload)}, new Track[][]{batch.tracks}));
                            }
                        }
                    });

                    for (SynapsePlayer player : entry1.player) {
                        if (player.closed) {
                            continue;
                        }

                        AbstractProtocol protocol = AbstractProtocol.fromRealProtocol(player.getProtocol());
                        Pair<byte[][], Track[][]> pair = finalData.get(protocol);
                        if (pair != null) {
                            RedirectPacket pk = new RedirectPacket();
                            pk.compressionAlgorithm = player.getServer().getCompressor().getAlgorithm();
                            pk.protocol = player.getProtocol();
                            pk.sessionId = player.getSessionId();

                            byte[][] datas = pair.getLeft();
                            Track[][] trackPairs = pair.getRight();
                            Track[] tracks;
                            if (datas.length >= 2 && player.isNetEaseClient()) {
                                pk.mcpeBuffer = datas[1];
                                tracks = trackPairs[1];
                            } else {
                                pk.mcpeBuffer = datas[0];
                                tracks = trackPairs[0];
                            }

                            if (hasMetrics) {
                                for (Track track : tracks) {
                                    metrics.packetOut(track.packetId, track.size);
                                }

                                int bytes = pk.mcpeBuffer.length;
                                network.addUploadStatistic(bytes);
                                metrics.bytesOut(bytes);
                            }

                            this.synapseInterface.putPacket(pk);
                        }
                    }
                } catch (Exception e) {
                    Server.getInstance().getLogger().alert("Catch exception when Synapse Entry Put Packet: ", e);
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            /*tickUseTime = System.currentTimeMillis() - start;
            if (tickUseTime < 10){
                try {
                    Thread.sleep(10 - tickUseTime);
                } catch (InterruptedException e) {
                    //ignore
                }
            }*/ /*else if (System.currentTimeMillis() - lastWarning >= 5000) {
                Server.getInstance().getLogger().warning("SynapseOutgoing<" + synapseInterface.getSynapse().getHash() + "> Async Thread is overloading! TPS: " + getTicksPerSecond() + " tickUseTime: " + tickUseTime);
                lastWarning = System.currentTimeMillis();
            }*/
        }
    }

    private static class Entry {
        private final SynapsePlayer player;
        // 与 packet 同时入队，只有 packet 成功进入当前 Batch 后才会被转交执行。
        private final List<LongConsumer> batchTailLatencyCallbacks;
        private DataPacket packet;

        public Entry(SynapsePlayer player, DataPacket packet) {
            this(player, packet, List.of());
        }

        public Entry(SynapsePlayer player, DataPacket packet,
                     List<LongConsumer> batchTailLatencyCallbacks) {
            this.player = player;
            this.packet = packet;
            this.batchTailLatencyCallbacks = batchTailLatencyCallbacks == null
                    ? List.of() : List.copyOf(batchTailLatencyCallbacks);
        }
    }

    private static class BroadcastEntry {
        private final SynapsePlayer[] player;
        private final DataPacket[] packet;

        public BroadcastEntry(SynapsePlayer[] player, DataPacket[] packet) {
            this.player = player;
            this.packet = packet;
        }
    }

    public double getTicksPerSecond() {
        long more = this.tickUseTime - 10;
        if (more <= 0) return 100;
        return NukkitMath.round(10d / this.tickUseTime, 3) * 100;
    }

    private static BatchPacket batchPackets(DataPacket[] packets, AbstractProtocol protocol) {
        Track[] tracks = new Track[packets.length];
        try {
            byte[][] payload = new byte[packets.length * 2][];
            for (int i = 0; i < packets.length; i++) {
                DataPacket p = packets[i];
                int idx = i * 2;
                if (!p.isEncoded) {
                    p.setHelper(protocol.getHelper());
                    p.tryEncode();
                }
                byte[] buf = p.getBuffer();
                payload[idx] = Binary.writeUnsignedVarInt(buf.length);
                payload[idx + 1] = buf;

                tracks[i] = new Track(p instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) p).origin.pid() : p.pid(), p.getCount());
            }

            BatchPacket packet = new BatchPacket();
            packet.payload = protocol.getCompressor().compress(payload, Server.getInstance().networkCompressionLevel);
            packet.tracks = tracks;
            return packet;
        } catch (Exception e) {
            MainLogger.getLogger().logException(e);
        }

        return null;
    }

    /**
     * 将一个协议兼容的 NSL 直接编码到当前 outboundQueue 尾部。
     * 此处不能调用 player.dataPacket()，否则会重新触发发送事件并进入下一轮异步队列。
     * 回调在 NSL 入队后、Batch 压缩前执行，同一批次的所有请求共享同一个 timestamp。
     * 该保证只覆盖 autoCompress 的普通 outboundQueue，不改写预压缩 BatchPacket 和广播压缩路径。
     */
    private static void appendBatchTailLatency(SynapsePlayer player, List<byte[]> outboundQueue) {
        List<LongConsumer> callbacks = player.drainBatchTailLatencyCallbacks();
        if (callbacks.isEmpty()) {
            return;
        }

        long timestamp = Math.floorMod(BATCH_TAIL_LATENCY_ID.getAndIncrement(), 1_000_000_000L);
        NetworkStackLatencyPacket19 latencyPacket = new NetworkStackLatencyPacket19();
        latencyPacket.timestamp = timestamp;
        latencyPacket.isFromServer = true;
        DataPacket packet;
        try {
            packet = PacketRegister.getCompatiblePacket(
                    latencyPacket, player.getProtocol(), player.isNetEaseClient());
            if (packet == null) {
                MainLogger.getLogger().warning("Cannot append batch-tail latency packet for protocol "
                        + player.getProtocol());
                return;
            }
            packet.setHelper(AbstractProtocol.fromRealProtocol(player.getProtocol()).getHelper());
            packet.neteaseMode = player.isNetEaseClient();
            packet.tryEncode();
            outboundQueue.add(packet.getBuffer());
            player.onBatchTailNetworkStackLatencyAppended();
        } catch (Exception exception) {
            MainLogger.getLogger().warning("Cannot append batch-tail latency packet: "
                    + exception.getMessage());
            return;
        }

        for (LongConsumer callback : callbacks) {
            try {
                callback.accept(timestamp);
            } catch (Throwable throwable) {
                MainLogger.getLogger().warning("Batch-tail latency callback failed: "
                        + throwable.getMessage());
            }
        }
    }

    private static byte[] batchPackets(List<byte[]> packets, Compressor compressor) throws IOException {
        int count = packets.size();
        byte[][] payload = new byte[count * 2][];
        for (int i = 0; i < count; i++) {
            byte[] buffer = packets.get(i);
            int idx = i * 2;
            payload[idx] = Binary.writeUnsignedVarInt(buffer.length);
            payload[idx + 1] = buffer;
        }
        byte[] result = compressor.compress(payload, Server.getInstance().networkCompressionLevel);
        return Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, result);
    }

    public static void setMetrics(SynapseMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics");
        METRICS = metrics;
    }
}
