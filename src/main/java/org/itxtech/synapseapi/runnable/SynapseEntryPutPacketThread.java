package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.MainLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;
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
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;
import org.itxtech.synapseapi.utils.PacketLogger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import javax.annotation.Nullable;

import static cn.nukkit.SharedConstants.BREAKPOINT_DEBUGGING;
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
    private static final AtomicLong BATCH_TAIL_LATENCY_ID = new AtomicLong(1);
    private static SynapseMetrics METRICS;
    private final Long2ObjectMap<List<PendingBoundary>> pendingBoundaries = new Long2ObjectOpenHashMap<>();

    private final SynapseInterface synapseInterface;
    private final Queue<PacketEntry> queue = new LinkedBlockingQueue<>();

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
        if (BREAKPOINT_DEBUGGING && player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        this.queue.offer(new ForwardEntry(player, packet));
    }

    public boolean supportsAfterPacketLatency() {
        return this.isAutoCompress;
    }

    public void addMainToThread(SynapsePlayer player, DataPacket packet,
                                List<LongConsumer> batchTailLatencyCallbacks) {
        addMainToThread(player, packet, List.of(), batchTailLatencyCallbacks);
    }

    public void addMainToThread(SynapsePlayer player, DataPacket packet,
                                List<NetworkStackLatencyBoundaryCallback> afterPacketLatencyCallbacks,
                                List<LongConsumer> batchTailLatencyCallbacks) {
        if (BREAKPOINT_DEBUGGING && player.getSynapseEntry().getSynapse().isRecordPacketStack()) {
            packet.stack = new Throwable();
        }
        ForwardEntry entry = new ForwardEntry(player, packet,
                afterPacketLatencyCallbacks, batchTailLatencyCallbacks);
        if (!this.queue.offer(entry)) {
            dropBoundaryCallbacks(afterPacketLatencyCallbacks,
                    NetworkStackLatencyBoundaryFailure.QUEUE_REJECTED);
        }
    }

    public void addTransferBarrier(SynapsePlayer player, SynapseDataPacket packet) {
        this.queue.offer(new TransferEntry(player, packet));
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        Long2ObjectMap<SynapsePlayer> queuedPlayers = new Long2ObjectOpenHashMap<>();
        Set<SynapsePlayer> blockedPlayerQueues = Collections.newSetFromMap(new WeakHashMap<>());
        Set<SynapsePlayer> failedPlayerQueues = Collections.newSetFromMap(new WeakHashMap<>());

        Network network = Server.getInstance().getNetwork();
        while (this.isRunning) {
//            long start = System.currentTimeMillis();

            SynapseMetrics metrics = METRICS;
            boolean hasMetrics = metrics != null;

            PacketEntry packetEntry;
            while ((packetEntry = queue.poll()) != null) {
                if (packetEntry instanceof TransferEntry(SynapsePlayer player, SynapseDataPacket packet)) {
                    queuedPlayers.remove(player.getId());

                    if (player.isClosed()) {
                        clearPlayerOutboundState(player, NetworkStackLatencyBoundaryFailure.PLAYER_CLOSED);
                        failedPlayerQueues.remove(player);
                    } else if (blockedPlayerQueues.contains(player)) {
                        clearPlayerOutboundState(player, NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                        failedPlayerQueues.remove(player);
                        log.warn("Ignoring duplicate transfer marker for player: {}", player.getName());
                    } else if (failedPlayerQueues.remove(player)) {
                        clearPlayerOutboundState(player, NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                        scheduleTransferRecovery(player);
                        log.error("Cannot transfer player because a queued player packet failed: {}", player.getName());
                    } else {
                        try {
                            flushPlayerOutboundQueue(player, network, metrics);
                            blockedPlayerQueues.add(player);
                            this.synapseInterface.putPacket(packet);
                        } catch (Exception e) {
                            blockedPlayerQueues.remove(player);
                            clearPlayerOutboundState(player, NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                            scheduleTransferRecovery(player);
                            log.error("Failed to flush player packets or submit transfer: {}", player.getName(), e);
                        }
                    }
                    break;
                }

                ForwardEntry entry = (ForwardEntry) packetEntry;
                if (blockedPlayerQueues.contains(entry.player)) {
                    dropBoundaryCallbacks(entry.after, NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
                    entry.packet.stack = null;
                    continue;
                }

                try {
                    if (!entry.player.isClosed() || entry.packet.pid() == ProtocolInfo.DISCONNECT_PACKET) {
                        DataPacket old = entry.packet;

                        entry.packet = PacketRegister.getCompatiblePacket(entry.packet, (entry.player).getProtocol(), entry.player.isNetEaseClient());

                        if (entry.packet == null) {
                            MainLogger.getLogger().info("NULL PACKET " + old.getClass().getSimpleName());
                            dropBoundaryCallbacks(entry.after, NetworkStackLatencyBoundaryFailure.PROTOCOL_CONVERSION_FAILED);
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
                            dropBoundaryCallbacks(entry.after,
                                    NetworkStackLatencyBoundaryFailure.UNSUPPORTED_TRANSPORT);
                            flushPlayerOutboundQueue(entry.player, network, metrics);
                            queuedPlayers.remove(entry.player.getId());

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
                            PendingBoundary boundary = appendAfterPacketLatency(
                                    entry.player, entry.player.outboundQueue, entry.after);
                            if (boundary != null) {
                                pendingBoundaries.computeIfAbsent(entry.player.getId(),
                                        ignored -> new ArrayList<>()).add(boundary);
                            }
                            entry.player.appendBatchTailLatencyCallbacks(entry.tail);
                            queuedPlayers.put(entry.player.getId(), entry.player);
                        } else {
                            dropBoundaryCallbacks(entry.after,
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
                        dropBoundaryCallbacks(entry.after,
                                NetworkStackLatencyBoundaryFailure.PLAYER_CLOSED);
                    }
                } catch (Exception e) {
                    dropBoundaryCallbacks(entry.after, NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
                    dropPendingBoundaries(takePendingBoundaries(entry.player.getId()), NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
                    failedPlayerQueues.add(entry.player);
                    MainLogger.getLogger().alert("Catch exception when put single packet", e);
                    if (entry.packet != null && entry.packet.stack != null)
                        MainLogger.getLogger().alert("Main thread stack", entry.packet.stack);
                } finally {
                    if (entry.packet != null) entry.packet.stack = null;
                }
            }

            for (SynapsePlayer player : queuedPlayers.values()) {
                try {
                    flushPlayerOutboundQueue(player, network, metrics);
                } catch (Exception e) {
                    failedPlayerQueues.add(player);
                    log.error("Failed to flush queued player packets: {}", player.getName(), e);
                }
            }
            queuedPlayers.clear();

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

    private void scheduleTransferRecovery(SynapsePlayer player) {
        try {
            Server.getInstance().getScheduler().scheduleTask(SynapseAPI.getInstance(), () -> {
                if (!player.isOnline()) {
                    return;
                }
                try {
                    player.rejoinGame("disconnectionScreen.internalError");
                } catch (Exception e) {
                    log.error("Failed to recover player after transfer failure: {}", player.getName(), e);
                    try {
                        player.close("", "disconnectionScreen.internalError");
                    } catch (Exception closeException) {
                        log.error("Failed to close player after transfer recovery failed: {}", player.getName(), closeException);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Failed to schedule transfer recovery: {}", player.getName(), e);
        }
    }

    private void flushPlayerOutboundQueue(SynapsePlayer player, Network network, @Nullable SynapseMetrics metrics) {
        List<PendingBoundary> boundaries = takePendingBoundaries(player.getId());
        List<byte[]> outboundQueue = player.outboundQueue;
        if (outboundQueue.isEmpty()) {
            dropPendingBoundaries(boundaries, NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
            return;
        }

        appendBatchTailLatency(player, outboundQueue);
        Compressor compressor = Compressor.byProtocol(player.getProtocol());
        byte[] buffer;
        try {
            buffer = batchPackets(outboundQueue, compressor);
        } catch (IOException e) {
            dropPendingBoundaries(boundaries, NetworkStackLatencyBoundaryFailure.BATCH_COMPRESSION_FAILED);
            log.warn("Failed to batch player packets, falling back to individual packets: {}", player.getName(), e);
            Iterator<byte[]> iterator = outboundQueue.iterator();
            while (iterator.hasNext()) {
                byte[] packetBuffer = iterator.next();
                RedirectPacket packet = new RedirectPacket();
                packet.compressionAlgorithm = compressor.getAlgorithm();
                packet.mcpeBuffer = packetBuffer;
                packet.sessionId = player.getSessionId();
                this.synapseInterface.putPacket(packet);
                iterator.remove();

                int bytes = packetBuffer.length;
                network.addUploadStatistic(bytes);
                if (metrics != null) {
                    metrics.bytesOut(bytes);
                }
            }
            return;
        }

        RedirectPacket packet = new RedirectPacket();
        packet.compressionAlgorithm = compressor.getAlgorithm();
        packet.mcpeBuffer = buffer;
        packet.sessionId = player.getSessionId();
        try {
            this.synapseInterface.putPacket(packet);
        } catch (RuntimeException exception) {
            dropPendingBoundaries(boundaries,
                    NetworkStackLatencyBoundaryFailure.TRANSPORT_FAILED);
            throw exception;
        }
        outboundQueue.clear();
        completePendingBoundaries(boundaries);

        int bytes = buffer.length;
        network.addUploadStatistic(bytes);
        if (metrics != null) {
            metrics.bytesOut(bytes);
        }
    }

    private void clearPlayerOutboundState(SynapsePlayer player,
                                          NetworkStackLatencyBoundaryFailure reason) {
        player.outboundQueue.clear();
        dropPendingBoundaries(takePendingBoundaries(player.getId()), reason);
    }

    private interface PacketEntry {
        SynapsePlayer player();

        BinaryStream packet();
    }

    private static class ForwardEntry implements PacketEntry {
        private final SynapsePlayer player;
        private DataPacket packet;
        private final List<NetworkStackLatencyBoundaryCallback> after;
        private final List<LongConsumer> tail;

        public ForwardEntry(SynapsePlayer player, DataPacket packet) {
            this(player, packet, List.of(), List.of());
        }

        public ForwardEntry(SynapsePlayer player, DataPacket packet,
                            List<NetworkStackLatencyBoundaryCallback> after,
                            List<LongConsumer> tail) {
            this.player = player;
            this.packet = packet;
            this.after = after == null ? List.of() : List.copyOf(after);
            this.tail = tail == null ? List.of() : List.copyOf(tail);
        }

        @Override
        public SynapsePlayer player() {
            return player;
        }

        @Override
        public BinaryStream packet() {
            return packet;
        }
    }

    private record TransferEntry(SynapsePlayer player, SynapseDataPacket packet) implements PacketEntry {
    }


    private record PendingBoundary(SynapsePlayer player, long timestamp,
                                   List<NetworkStackLatencyBoundaryCallback> callbacks) {
    }

    private List<PendingBoundary> takePendingBoundaries(long playerId) {
        return pendingBoundaries.remove(playerId);
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
                    MainLogger.getLogger().warning("Latency boundary callback failed: "
                            + throwable.getMessage());
                }
            }
        }
    }

    private static void dropPendingBoundaries(List<PendingBoundary> boundaries,
                                              NetworkStackLatencyBoundaryFailure reason) {
        if (boundaries == null) {
            return;
        }
        for (PendingBoundary boundary : boundaries) {
            boundary.player().forgetApplicationBoundaryTimestamp(boundary.timestamp());
            dropBoundaryCallbacks(boundary.callbacks(), reason);
        }
    }

    private static void dropBoundaryCallbacks(List<NetworkStackLatencyBoundaryCallback> callbacks,
                                              NetworkStackLatencyBoundaryFailure reason) {
        if (callbacks == null) {
            return;
        }
        for (NetworkStackLatencyBoundaryCallback callback : callbacks) {
            try {
                callback.onDropped(reason);
            } catch (Throwable throwable) {
                MainLogger.getLogger().warning("Latency boundary drop callback failed: "
                        + throwable.getMessage());
            }
        }
    }

    private static PendingBoundary appendAfterPacketLatency(
            SynapsePlayer player, List<byte[]> outboundQueue,
            List<NetworkStackLatencyBoundaryCallback> callbacks) {
        if (callbacks == null || callbacks.isEmpty()) {
            return null;
        }
        long timestamp = nextBoundaryTimestamp();
        try {
            appendLatencyPacket(player, outboundQueue, createLatencyPacket(player, timestamp), timestamp);
            return new PendingBoundary(player, timestamp, List.copyOf(callbacks));
        } catch (Exception exception) {
            dropBoundaryCallbacks(callbacks, NetworkStackLatencyBoundaryFailure.ENCODE_FAILED);
            return null;
        }
    }

    private static void appendBatchTailLatency(SynapsePlayer player, List<byte[]> outboundQueue) {
        List<LongConsumer> callbacks = player.drainBatchTailLatencyCallbacks();
        if (callbacks.isEmpty()) {
            return;
        }
        long timestamp = nextBoundaryTimestamp();
        try {
            appendLatencyPacket(player, outboundQueue, createLatencyPacket(player, timestamp), timestamp);
            for (LongConsumer callback : callbacks) {
                callback.accept(timestamp);
            }
        } catch (Exception exception) {
            MainLogger.getLogger().warning("Cannot append batch-tail latency packet: "
                    + exception.getMessage());
        }
    }

    private static long nextBoundaryTimestamp() {
        return Math.floorMod(BATCH_TAIL_LATENCY_ID.getAndIncrement(), 1_000_000_000L);
    }

    private static DataPacket createLatencyPacket(SynapsePlayer player, long timestamp) {
        NetworkStackLatencyPacket19 latency = new NetworkStackLatencyPacket19();
        latency.timestamp = timestamp;
        latency.isFromServer = true;
        return PacketRegister.getCompatiblePacket(latency, player.getProtocol(), player.isNetEaseClient());
    }

    private static void appendLatencyPacket(SynapsePlayer player, List<byte[]> outboundQueue,
                                            DataPacket packet, long timestamp) {
        if (packet == null) {
            throw new IllegalStateException();
        }
        packet.setHelper(AbstractProtocol.fromRealProtocol(player.getProtocol()).getHelper());
        packet.neteaseMode = player.isNetEaseClient();
        packet.tryEncode();
        byte[] buffer = packet.getBuffer();
        outboundQueue.add(buffer);
        player.recordApplicationBoundaryTimestamp(timestamp);

        SynapseMetrics metrics = METRICS;
        if (metrics != null) {
            int packetId = packet instanceof CompatibilityPacket16 compatibilityPacket
                    ? compatibilityPacket.origin.pid() : packet.pid();
            metrics.packetOut(packetId, buffer.length);
        }
    }

    public double getTicksPerSecond() {
        long more = this.tickUseTime - 10;
        if (more <= 0) return 100;
        return NukkitMath.round(10d / this.tickUseTime, 3) * 100;
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
