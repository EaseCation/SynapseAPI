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
import org.itxtech.synapseapi.NetworkStackLatencyBoundaryCallback;
import org.itxtech.synapseapi.NetworkStackLatencyBoundaryFailure;
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

    public boolean supportsAfterPacketLatency() {
        return this.isAutoCompress;
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
        addMainToThread(player, packet, List.of(), batchTailLatencyCallbacks);
    }

    /**
     * 将数据包和两种 NSL 边界请求一起入队。
     */
    public void addMainToThread(SynapsePlayer player, DataPacket packet,
                                List<NetworkStackLatencyBoundaryCallback> afterPacketLatencyCallbacks,
                                List<LongConsumer> batchTailLatencyCallbacks) {
        if (player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        Entry entry = new Entry(player, packet, afterPacketLatencyCallbacks,
                batchTailLatencyCallbacks);
        if (!this.queue.offer(entry)) {
            dropBoundaryCallbacks(afterPacketLatencyCallbacks,
                    NetworkStackLatencyBoundaryFailure.QUEUE_REJECTED);
        }
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
        Long2ObjectMap<List<PendingBoundary>> pendingBoundaries = new Long2ObjectOpenHashMap<>();

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
                            dropBoundaryCallbacks(entry.afterPacketLatencyCallbacks,
                                    NetworkStackLatencyBoundaryFailure.PROTOCOL_CONVERSION_FAILED);
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
                            dropBoundaryCallbacks(entry.afterPacketLatencyCallbacks,
                                    NetworkStackLatencyBoundaryFailure.UNSUPPORTED_TRANSPORT);
                            List<byte[]> outboundQueue = entry.player.outboundQueue;
                            if (!outboundQueue.isEmpty()) {
                                List<PendingBoundary> boundaries =
                                        pendingBoundaries.remove(entry.player.getId());
                                Compressor compressor = Compressor.byProtocol(entry.player.getProtocol());
                                RedirectPacket pk = new RedirectPacket();
                                pk.compressionAlgorithm = compressor.getAlgorithm();
                                pk.sessionId = entry.player.getSessionId();
                                try {
                                    pk.mcpeBuffer = batchPackets(outboundQueue, compressor);
                                } catch (IOException exception) {
                                    outboundQueue.clear();
                                    dropPendingBoundaries(boundaries,
                                            NetworkStackLatencyBoundaryFailure.BATCH_COMPRESSION_FAILED);
                                    throw exception;
                                }
                                outboundQueue.clear();

                                try {
                                    int bytes = pk.mcpeBuffer.length;
                                    network.addUploadStatistic(bytes);
                                    if (hasMetrics) {
                                        metrics.bytesOut(bytes);
                                    }
                                    this.synapseInterface.putPacket(pk);
                                } catch (RuntimeException exception) {
                                    dropPendingBoundaries(boundaries,
                                            NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                                    throw exception;
                                }
                                completePendingBoundaries(boundaries);
                                queuedPlayers.remove(entry.player.getId());
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
                            // 当前包后置 NSL 必须在当前 packet 后立即编码入队。
                            PendingBoundary pendingBoundary = appendAfterPacketLatency(
                                    entry.player, entry.player.outboundQueue,
                                    entry.afterPacketLatencyCallbacks);
                            if (pendingBoundary != null) {
                                addPendingBoundary(pendingBoundaries,
                                        entry.player.getId(), pendingBoundary);
                            }
                            // Batch-tail NSL 继续延后到当前 outboundQueue 全部收集完成。
                            entry.player.appendBatchTailLatencyCallbacks(entry.batchTailLatencyCallbacks);

                            queuedPlayers.put(entry.player.getId(), entry.player);
                        } else {
                            dropBoundaryCallbacks(entry.afterPacketLatencyCallbacks,
                                    NetworkStackLatencyBoundaryFailure.UNSUPPORTED_TRANSPORT);
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
                    } else {
                        dropBoundaryCallbacks(entry.afterPacketLatencyCallbacks,
                                NetworkStackLatencyBoundaryFailure.PLAYER_CLOSED);
                    }
                } catch (Exception e) {
                    dropBoundaryCallbacks(entry.afterPacketLatencyCallbacks,
                            NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
                    MainLogger.getLogger().alert("Catch exception when put single packet", e);
                    if (entry.packet != null && entry.packet.stack != null)
                        MainLogger.getLogger().alert("Main thread stack", entry.packet.stack);
                } finally {
                    if (entry.packet != null) entry.packet.stack = null;
                }
            }

            for (SynapsePlayer player : queuedPlayers.values()) {
                List<PendingBoundary> boundaries = pendingBoundaries.remove(player.getId());
                List<byte[]> outboundQueue = player.outboundQueue;
                if (outboundQueue.isEmpty()) {
                    dropPendingBoundaries(boundaries,
                            NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                    continue;
                }

                appendBatchTailLatency(player, outboundQueue);
                Compressor compressor = Compressor.byProtocol(player.getProtocol());
                byte[] buffer;
                try {
                    buffer = batchPackets(outboundQueue, compressor);
                } catch (IOException exception) {
                    outboundQueue.clear();
                    dropPendingBoundaries(boundaries,
                            NetworkStackLatencyBoundaryFailure.BATCH_COMPRESSION_FAILED);
                    log.throwing(exception);
                    continue;
                }
                outboundQueue.clear();

                RedirectPacket pk = new RedirectPacket();
                pk.compressionAlgorithm = compressor.getAlgorithm();
                pk.mcpeBuffer = buffer;
                pk.sessionId = player.getSessionId();
                try {
                    this.synapseInterface.putPacket(pk);
                } catch (RuntimeException exception) {
                    dropPendingBoundaries(boundaries,
                            NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                    log.throwing(exception);
                    continue;
                }
                completePendingBoundaries(boundaries);

                int bytes = buffer.length;
                network.addUploadStatistic(bytes);
                if (hasMetrics) {
                    metrics.bytesOut(bytes);
                }
            }
            queuedPlayers.clear();
            for (List<PendingBoundary> boundaries : pendingBoundaries.values()) {
                dropPendingBoundaries(boundaries,
                        NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
            }
            pendingBoundaries.clear();

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
        private final List<NetworkStackLatencyBoundaryCallback> afterPacketLatencyCallbacks;
        private final List<LongConsumer> batchTailLatencyCallbacks;
        private DataPacket packet;

        public Entry(SynapsePlayer player, DataPacket packet) {
            this(player, packet, List.of(), List.of());
        }

        public Entry(SynapsePlayer player, DataPacket packet,
                     List<LongConsumer> batchTailLatencyCallbacks) {
            this(player, packet, List.of(), batchTailLatencyCallbacks);
        }

        public Entry(SynapsePlayer player, DataPacket packet,
                     List<NetworkStackLatencyBoundaryCallback> afterPacketLatencyCallbacks,
                     List<LongConsumer> batchTailLatencyCallbacks) {
            this.player = player;
            this.packet = packet;
            this.afterPacketLatencyCallbacks = afterPacketLatencyCallbacks == null
                    ? List.of() : List.copyOf(afterPacketLatencyCallbacks);
            this.batchTailLatencyCallbacks = batchTailLatencyCallbacks == null
                    ? List.of() : List.copyOf(batchTailLatencyCallbacks);
        }
    }

    private record PendingBoundary(SynapsePlayer player, long timestamp,
                                   List<NetworkStackLatencyBoundaryCallback> callbacks) {
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

    private static PendingBoundary appendAfterPacketLatency(
            SynapsePlayer player, List<byte[]> outboundQueue,
            List<NetworkStackLatencyBoundaryCallback> callbacks) {
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        long timestamp = nextBoundaryTimestamp();
        DataPacket packet = createLatencyPacket(player, timestamp);
        if (packet == null) {
            dropBoundaryCallbacks(callbacks,
                    NetworkStackLatencyBoundaryFailure.PROTOCOL_CONVERSION_FAILED);
            return null;
        }
        try {
            appendLatencyPacket(player, outboundQueue, packet, timestamp);
        } catch (Exception exception) {
            MainLogger.getLogger().warning("Cannot append after-packet latency packet: "
                    + exception.getMessage());
            dropBoundaryCallbacks(callbacks,
                    NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
            return null;
        }
        return new PendingBoundary(player, timestamp, List.copyOf(callbacks));
    }

    /**
     * 将一个协议兼容的 NSL 直接编码到当前 outboundQueue 尾部。
     * 此处不能调用 player.dataPacket()，否则会重新触发发送事件并进入下一轮异步队列。
     * 该保证只覆盖 autoCompress 的普通 outboundQueue，不改写预压缩 BatchPacket 和广播压缩路径。
     */
    private static void appendBatchTailLatency(SynapsePlayer player, List<byte[]> outboundQueue) {
        List<LongConsumer> callbacks = player.drainBatchTailLatencyCallbacks();
        if (callbacks.isEmpty()) {
            return;
        }
        long timestamp = nextBoundaryTimestamp();
        DataPacket packet = createLatencyPacket(player, timestamp);
        if (packet == null) {
            return;
        }
        try {
            appendLatencyPacket(player, outboundQueue, packet, timestamp);
        } catch (Exception exception) {
            MainLogger.getLogger().warning("Cannot append batch-tail latency packet: "
                    + exception.getMessage());
            return;
        }
        for (LongConsumer callback : callbacks) {
            try {
                callback.accept(timestamp);
            } catch (Throwable throwable) {
                MainLogger.getLogger().warning("batch-tail latency callback failed: "
                        + throwable.getMessage());
            }
        }
    }

    private static long nextBoundaryTimestamp() {
        return Math.floorMod(BATCH_TAIL_LATENCY_ID.getAndIncrement(), 1_000_000_000L);
    }

    private static DataPacket createLatencyPacket(SynapsePlayer player, long timestamp) {
        NetworkStackLatencyPacket19 latencyPacket = new NetworkStackLatencyPacket19();
        latencyPacket.timestamp = timestamp;
        latencyPacket.isFromServer = true;
        DataPacket packet = PacketRegister.getCompatiblePacket(
                latencyPacket, player.getProtocol(), player.isNetEaseClient());
        if (packet == null) {
            MainLogger.getLogger().warning("Cannot create latency packet for protocol "
                    + player.getProtocol());
        }
        return packet;
    }

    private static void appendLatencyPacket(SynapsePlayer player, List<byte[]> outboundQueue,
                                            DataPacket packet, long timestamp) {
        packet.setHelper(AbstractProtocol.fromRealProtocol(player.getProtocol()).getHelper());
        packet.neteaseMode = player.isNetEaseClient();
        packet.tryEncode();
        byte[] buffer = packet.getBuffer();
        outboundQueue.add(buffer);
        player.recordApplicationBoundaryTimestamp(timestamp);
        SynapseMetrics metrics = METRICS;
        if (metrics != null) {
            int packetId = packet instanceof CompatibilityPacket16
                    ? ((CompatibilityPacket16) packet).origin.pid() : packet.pid();
            try {
                metrics.packetOut(packetId, buffer.length);
            } catch (RuntimeException exception) {
                MainLogger.getLogger().warning("Cannot record latency packet metric: "
                        + exception.getMessage());
            }
        }
    }

    private static void addPendingBoundary(
            Long2ObjectMap<List<PendingBoundary>> pendingBoundaries,
            long playerId, PendingBoundary boundary) {
        List<PendingBoundary> boundaries = pendingBoundaries.get(playerId);
        if (boundaries == null) {
            boundaries = new ArrayList<>();
            pendingBoundaries.put(playerId, boundaries);
        }
        boundaries.add(boundary);
    }

    private static void completePendingBoundaries(List<PendingBoundary> boundaries) {
        if (boundaries == null) {
            return;
        }
        for (PendingBoundary boundary : boundaries) {
            for (NetworkStackLatencyBoundaryCallback callback : boundary.callbacks()) {
                try {
                    callback.onAppended(boundary.timestamp());
                } catch (Throwable throwable) {
                    MainLogger.getLogger().warning("after-packet latency callback failed: "
                            + throwable.getMessage());
                }
            }
        }
    }

    private static void dropPendingBoundaries(
            List<PendingBoundary> boundaries,
            NetworkStackLatencyBoundaryFailure reason) {
        if (boundaries == null) {
            return;
        }
        for (PendingBoundary boundary : boundaries) {
            boundary.player().forgetApplicationBoundaryTimestamp(boundary.timestamp());
            dropBoundaryCallbacks(boundary.callbacks(), reason);
        }
    }

    private static void dropBoundaryCallbacks(
            List<NetworkStackLatencyBoundaryCallback> callbacks,
            NetworkStackLatencyBoundaryFailure reason) {
        if (callbacks == null) {
            return;
        }
        for (NetworkStackLatencyBoundaryCallback callback : callbacks) {
            try {
                callback.onDropped(reason);
            } catch (Throwable throwable) {
                MainLogger.getLogger().warning("after-packet latency drop callback failed: "
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
