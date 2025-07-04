package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
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
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.SynapseMetrics;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.utils.PacketLogger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
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

        while (this.isRunning) {
//            long start = System.currentTimeMillis();

            SynapseMetrics metrics = METRICS;
            boolean hasMetrics = metrics != null;

            Entry entry;
            while ((entry = queue.poll()) != null) {
                try {
                    if (!entry.player.closed) {
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
                                RedirectPacket pk = new RedirectPacket();
                                pk.sessionId = entry.player.getSessionId();
                                pk.mcpeBuffer = batchPackets(outboundQueue, entry.player.getProtocol() >= 407);
                                outboundQueue.clear();

                                if (hasMetrics) {
                                    metrics.bytesOut(pk.mcpeBuffer.length);
                                }

                                this.synapseInterface.putPacket(pk);
                            }

                            RedirectPacket pk = new RedirectPacket();
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

                            if (hasMetrics) {
                                metrics.bytesOut(pk.mcpeBuffer.length);
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

                            queuedPlayers.put(entry.player.getId(), entry.player);
                        } else {
                            RedirectPacket pk = new RedirectPacket();
                            pk.sessionId = entry.player.getSessionId();
                            pk.mcpeBuffer = entry.packet.getBuffer();

                            if (hasMetrics) {
                                metrics.packetOut(entry.packet instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) entry.packet).origin.pid() : entry.packet.pid(), pk.mcpeBuffer.length);
                                metrics.bytesOut(pk.mcpeBuffer.length);
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

                byte[] buffer;
                try {
                    buffer = batchPackets(outboundQueue, player.getProtocol() >= 407);
                } catch (IOException e) {
                    log.throwing(e);
                    continue;
                }
                outboundQueue.clear();

                RedirectPacket pk = new RedirectPacket();
                pk.mcpeBuffer = buffer;
                pk.sessionId = player.getSessionId();
                this.synapseInterface.putPacket(pk);

                if (hasMetrics) {
                    metrics.bytesOut(buffer.length);
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

                                metrics.bytesOut(pk.mcpeBuffer.length);
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
        private DataPacket packet;

        public Entry(SynapsePlayer player, DataPacket packet) {
            this.player = player;
            this.packet = packet;
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
            byte[] data;
            data = Binary.appendBytes(payload);

            BatchPacket packet = new BatchPacket();
            if (protocol.isZlibRaw()) {
                packet.payload = Network.deflateRaw(data, Server.getInstance().networkCompressionLevel);
            } else {
                packet.payload = Zlib.deflate(data, Server.getInstance().networkCompressionLevel);
            }
            packet.tracks = tracks;
            return packet;
        } catch (Exception e) {
            MainLogger.getLogger().logException(e);
        }

        return null;
    }

    private static byte[] batchPackets(List<byte[]> packets, boolean zlibRaw) throws IOException {
        int count = packets.size();
        byte[][] payload = new byte[count * 2][];
        for (int i = 0; i < count; i++) {
            byte[] buffer = packets.get(i);
            int idx = i * 2;
            payload[idx] = Binary.writeUnsignedVarInt(buffer.length);
            payload[idx + 1] = buffer;
        }
        byte[] data = Binary.appendBytes(payload);
        byte[] result;
        if (zlibRaw) {
            result = Network.deflateRaw(data, Server.getInstance().networkCompressionLevel);
        } else {
            result = Zlib.deflate(data, Server.getInstance().networkCompressionLevel);
        }
        return Binary.appendBytes((byte) ProtocolInfo.BATCH_PACKET, result);
    }

    public static void setMetrics(SynapseMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics");
        METRICS = metrics;
    }
}
