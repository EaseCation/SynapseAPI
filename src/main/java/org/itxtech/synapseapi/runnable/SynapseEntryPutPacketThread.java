package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.CompatibilityPacket16;
import org.itxtech.synapseapi.event.player.SynapsePlayerBatchSendEvent;
import org.itxtech.synapseapi.network.OutboundPacket;
import org.itxtech.synapseapi.network.protocol.PacketSequence;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.SynapseMetrics;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.protocol.spp.SynapseDataPacket;
import org.itxtech.synapseapi.utils.PacketLogger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
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
    private static SynapseMetrics METRICS;

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
        if (BREAKPOINT_DEBUGGING && player.getSynapseEntry().getSynapse().isRecordPacketStack()) {
            packet.stack = new Throwable();
        }
        if (!enqueue(new ForwardEntry(player, packet))) {
            PacketSequence.discard(packet);
            packet.stack = null;
        }
    }

    public boolean supportsPacketSequences() {
        return this.isAutoCompress;
    }

    public void addTransferBarrier(SynapsePlayer player, SynapseDataPacket packet) {
        enqueue(new TransferEntry(player, packet));
    }

    private boolean enqueue(PacketEntry entry) {
        return isRunning && queue.offer(entry);
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
        try {
            while (this.isRunning) {
    //            long start = System.currentTimeMillis();

                SynapseMetrics metrics = METRICS;

                PacketEntry packetEntry;
                while (this.isRunning && (packetEntry = queue.poll()) != null) {
                    if (packetEntry instanceof TransferEntry(SynapsePlayer player, SynapseDataPacket packet)) {
                        queuedPlayers.remove(player.getId());

                        if (player.isClosed()) {
                            clearPlayerOutboundState(player);
                            failedPlayerQueues.remove(player);
                        } else if (blockedPlayerQueues.contains(player)) {
                            clearPlayerOutboundState(player);
                            failedPlayerQueues.remove(player);
                            log.warn("Ignoring duplicate transfer marker for player: {}", player.getName());
                        } else if (failedPlayerQueues.remove(player)) {
                            clearPlayerOutboundState(player);
                            scheduleTransferRecovery(player);
                            log.error("Cannot transfer player because a queued player packet failed: {}", player.getName());
                        } else {
                            try {
                                flushPlayerOutboundQueue(player, network, metrics);
                                blockedPlayerQueues.add(player);
                                this.synapseInterface.putPacket(packet);
                            } catch (Exception e) {
                                blockedPlayerQueues.remove(player);
                                clearPlayerOutboundState(player);
                                scheduleTransferRecovery(player);
                                log.error("Failed to flush player packets or submit transfer: {}", player.getName(), e);
                            }
                        }
                        break;
                    }

                    ForwardEntry entry = (ForwardEntry) packetEntry;
                    if (blockedPlayerQueues.contains(entry.player)) {
                        PacketSequence.discard(entry.packet);
                        entry.packet.stack = null;
                        continue;
                    }
                    try {
                        if (entry.player.isClosed() && entry.packet.pid() != ProtocolInfo.DISCONNECT_PACKET) {
                            PacketSequence.discard(entry.packet);
                            continue;
                        }
                        if (entry.packet instanceof BatchPacket batch) {
                            flushPlayerOutboundQueue(entry.player, network, metrics);
                            queuedPlayers.remove(entry.player.getId());
                            forwardBatch(entry.player, batch, network, metrics);
                        } else if (this.isAutoCompress) {
                            OutboundPacket packet = encodePacket(entry.player, entry.packet, false);
                            if (packet == null) {
                                if (entry.packet instanceof PacketSequence) {
                                    failedPlayerQueues.add(entry.player);
                                }
                                continue;
                            }
                            entry.player.outboundQueue.add(packet);
                            queuedPlayers.put(entry.player.getId(), entry.player);
                            PacketSequence.appended(entry.packet);
                        } else if (entry.packet instanceof PacketSequence) {
                            PacketSequence.discard(entry.packet);
                        } else {
                            OutboundPacket packet = encodePacket(entry.player, entry.packet, false);
                            if (packet == null) {
                                continue;
                            }
                            RedirectPacket redirect = new RedirectPacket();
                            redirect.compressionAlgorithm = entry.player.getServer().getCompressor().getAlgorithm();
                            redirect.sessionId = entry.player.getSessionId();
                            redirect.mcpeBuffer = packet.buffers().getFirst();
                            this.synapseInterface.putPacket(redirect);
                            network.addUploadStatistic(redirect.mcpeBuffer.length);
                            if (metrics != null) {
                                metrics.bytesOut(redirect.mcpeBuffer.length);
                            }
                        }
                    } catch (Exception exception) {
                        PacketSequence.discard(entry.packet);
                        failedPlayerQueues.add(entry.player);
                        log.error("Failed to encode or submit player packet: {}", entry.player.getName(), exception);
                        if (entry.packet.stack != null) {
                            log.error("Main thread packet creation stack", entry.packet.stack);
                        }
                    } finally {
                        entry.packet.stack = null;
                    }
                }

                for (SynapsePlayer player : queuedPlayers.values()) {
                    try {
                        if (!isRunning) {
                            clearPlayerOutboundState(player);
                            continue;
                        }
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
        } finally {
            setRunning(false);
            PacketEntry remaining;
            while ((remaining = queue.poll()) != null) {
                if (remaining instanceof ForwardEntry entry) {
                    PacketSequence.discard(entry.packet);
                    entry.packet.stack = null;
                }
            }
            for (SynapsePlayer player : queuedPlayers.values()) {
                clearPlayerOutboundState(player);
            }
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

    private void forwardBatch(SynapsePlayer player, BatchPacket batch, Network network, @Nullable SynapseMetrics metrics) {
        if (metrics != null) {
            Track[] tracks = batch.tracks;
            if (tracks != null) {
                for (Track track : tracks) {
                    metrics.packetOut(track.packetId, track.size);
                }
            } else {
                metrics.packetOut(batch.pid(), 1 + batch.payload.length);
            }
        }
        RedirectPacket packet = new RedirectPacket();
        packet.compressionAlgorithm = player.getServer().getCompressor().getAlgorithm();
        packet.sessionId = player.getSessionId();
        packet.mcpeBuffer = Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, batch.payload);
        this.synapseInterface.putPacket(packet);
        network.addUploadStatistic(packet.mcpeBuffer.length);
        if (metrics != null) {
            metrics.bytesOut(packet.mcpeBuffer.length);
        }
    }

    private void flushPlayerOutboundQueue(SynapsePlayer player, Network network, @Nullable SynapseMetrics metrics) {
        List<OutboundPacket> outboundQueue = player.outboundQueue;
        if (outboundQueue.isEmpty()) {
            return;
        }
        if (player.isClosed()) {
            discardSequences(outboundQueue);
        } else {
            List<DataPacket> packets = new ArrayList<>(outboundQueue.size());
            for (OutboundPacket packet : outboundQueue) {
                packets.add(packet.packet());
            }
            SynapsePlayerBatchSendEvent event = new SynapsePlayerBatchSendEvent(player, packets);
            try {
                event.call();
                for (DataPacket additional : event.getAdditionalPackets()) {
                    OutboundPacket packet = encodePacket(player, additional, true);
                    if (packet != null) {
                        outboundQueue.add(packet);
                        PacketSequence.appended(additional);
                    }
                }
            } catch (Exception exception) {
                for (DataPacket additional : event.getAdditionalPackets()) {
                    PacketSequence.discard(additional);
                }
                discardTailPackets(outboundQueue);
                log.error("Batch send event failed for player: {}", player.getName(), exception);
            }
        }
        if (outboundQueue.isEmpty()) {
            return;
        }

        Compressor compressor = Compressor.byProtocol(player.getProtocol());
        byte[] buffer;
        try {
            try {
                buffer = batchPackets(outboundQueue, compressor);
            } catch (IOException exception) {
                discardSequences(outboundQueue);
                log.warn("Failed to batch player packets, falling back to ordinary packets: {}", player.getName(), exception);
                Iterator<OutboundPacket> iterator = outboundQueue.iterator();
                while (iterator.hasNext()) {
                    OutboundPacket original = iterator.next();
                    byte[] packetBuffer = original.buffers().getFirst();
                    RedirectPacket packet = new RedirectPacket();
                    packet.compressionAlgorithm = compressor.getAlgorithm();
                    packet.mcpeBuffer = packetBuffer;
                    packet.sessionId = player.getSessionId();
                    this.synapseInterface.putPacket(packet);
                    iterator.remove();
                    network.addUploadStatistic(packetBuffer.length);
                    if (metrics != null) {
                        metrics.bytesOut(packetBuffer.length);
                    }
                }
                return;
            }
            RedirectPacket packet = new RedirectPacket();
            packet.compressionAlgorithm = compressor.getAlgorithm();
            packet.mcpeBuffer = buffer;
            packet.sessionId = player.getSessionId();
            this.synapseInterface.putPacket(packet);
            outboundQueue.clear();
        } catch (RuntimeException exception) {
            discardSequences(outboundQueue);
            throw exception;
        }
        network.addUploadStatistic(buffer.length);
        if (metrics != null) {
            metrics.bytesOut(buffer.length);
        }
    }

    private static void clearPlayerOutboundState(SynapsePlayer player) {
        for (OutboundPacket packet : player.outboundQueue) {
            packet.discard();
        }
        player.outboundQueue.clear();
    }

    private static void discardSequences(List<OutboundPacket> packets) {
        Iterator<OutboundPacket> iterator = packets.iterator();
        while (iterator.hasNext()) {
            OutboundPacket packet = iterator.next();
            if (packet.requiresBatch()) {
                packet.discard();
                iterator.remove();
            }
        }
    }

    private static void discardTailPackets(List<OutboundPacket> packets) {
        Iterator<OutboundPacket> iterator = packets.iterator();
        while (iterator.hasNext()) {
            OutboundPacket packet = iterator.next();
            if (packet.batchTail()) {
                packet.discard();
                iterator.remove();
            }
        }
    }

    private interface PacketEntry {
        SynapsePlayer player();

        BinaryStream packet();
    }

    private record ForwardEntry(SynapsePlayer player, DataPacket packet) implements PacketEntry {
    }

    private record TransferEntry(SynapsePlayer player, SynapseDataPacket packet) implements PacketEntry {
    }

    @Nullable
    private static OutboundPacket encodePacket(SynapsePlayer player, DataPacket original, boolean batchTail) {
        try {
            if (!(original instanceof PacketSequence)) {
                byte[] buffer = encodeSinglePacket(player, original);
                return buffer == null ? null : new OutboundPacket(original, List.of(buffer), batchTail);
            }
            List<byte[]> buffers = new ArrayList<>();
            Deque<DataPacket> pending = new ArrayDeque<>();
            pending.add(original);
            while (!pending.isEmpty()) {
                DataPacket packet = pending.removeFirst();
                if (packet instanceof PacketSequence sequence) {
                    List<DataPacket> children = sequence.getPackets();
                    for (int index = children.size() - 1; index >= 0; index--) {
                        pending.addFirst(children.get(index));
                    }
                } else {
                    byte[] buffer = encodeSinglePacket(player, packet);
                    if (buffer == null) {
                        PacketSequence.discard(original);
                        return null;
                    }
                    buffers.add(buffer);
                }
            }
            return new OutboundPacket(original, buffers, batchTail);
        } catch (RuntimeException exception) {
            PacketSequence.discard(original);
            throw exception;
        }
    }

    @Nullable
    private static byte[] encodeSinglePacket(SynapsePlayer player, DataPacket packet) {
        if (packet instanceof BatchPacket) {
            return null;
        }
        packet = player.prepareOutboundPacket(packet);
        packet = PacketRegister.getCompatiblePacket(packet, player.getProtocol(), player.isNetEaseClient());
        if (packet == null) {
            return null;
        }
        packet.setHelper(AbstractProtocol.fromRealProtocol(player.getProtocol()).getHelper());
        packet.neteaseMode = player.isNetEaseClient();
        packet.tryEncode();
        byte[] buffer = packet.getBuffer();
        SynapseMetrics metrics = METRICS;
        if (metrics != null) {
            int packetId = packet instanceof CompatibilityPacket16 compatibilityPacket
                    ? compatibilityPacket.origin.pid() : packet.pid();
            metrics.packetOut(packetId, buffer.length);
        }
        if (CLIENTBOUND_PACKET_LOGGING && log.isTraceEnabled()) {
            PacketLogger.handleClientboundPacket(player, packet);
        }
        return buffer;
    }

    public double getTicksPerSecond() {
        long more = this.tickUseTime - 10;
        if (more <= 0) return 100;
        return NukkitMath.round(10d / this.tickUseTime, 3) * 100;
    }

    private static byte[] batchPackets(List<OutboundPacket> packets, Compressor compressor) throws IOException {
        int count = 0;
        for (OutboundPacket packet : packets) {
            count += packet.buffers().size();
        }
        byte[][] payload = new byte[count * 2][];
        int index = 0;
        for (OutboundPacket packet : packets) {
            for (byte[] buffer : packet.buffers()) {
                payload[index++] = Binary.writeUnsignedVarInt(buffer.length);
                payload[index++] = buffer;
            }
        }
        byte[] result = compressor.compress(payload, Server.getInstance().networkCompressionLevel);
        return Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, result);
    }

    public static void setMetrics(SynapseMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics");
        METRICS = metrics;
    }
}
