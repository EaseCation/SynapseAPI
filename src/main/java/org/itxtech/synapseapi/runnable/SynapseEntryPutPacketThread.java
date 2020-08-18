package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

/**
 * org.itxtech.synapseapi.runnable
 * ===============
 * author: boybook
 * SynapseAPI Project
 * itxTech
 * ===============
 */
public class SynapseEntryPutPacketThread extends Thread {

    private final SynapseInterface synapseInterface;
    private final Queue<Entry> queue = new LinkedBlockingQueue<>();
    private final Queue<BroadcastEntry> broadcastQueue = new LinkedBlockingQueue<>();

    private final Deflater deflater = new Deflater(Server.getInstance().networkCompressionLevel);
    private final byte[] buf = new byte[1024];

    private final boolean isAutoCompress;
    private long tickUseTime = 0;
    private boolean isRunning = true;

    public SynapseEntryPutPacketThread(SynapseInterface synapseInterface) {
        super("SynapseEntryPutPacketThread");
        this.synapseInterface = synapseInterface;
        this.isAutoCompress = SynapseAPI.getInstance().isAutoCompress();
        this.start();
    }

    private static final Map<Integer, String> entityDataTypes = new HashMap<Integer, String>() {
        {
            put(0, "DATA_TYPE_BYTE");
            put(1, "DATA_TYPE_SHORT");
            put(2, "DATA_TYPE_INT");
            put(3, "DATA_TYPE_FLOAT");
            put(4, "DATA_TYPE_STRING");
            put(5, "DATA_TYPE_SLOT");
            put(6, "DATA_TYPE_POS");
            put(7, "DATA_TYPE_LONG");
            put(8, "DATA_TYPE_VECTOR3F");
        }
    };

    public void addMainToThread(SynapsePlayer player, DataPacket packet, boolean needACK, boolean immediate) {
        if (player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        this.queue.offer(new Entry(player, packet, needACK, immediate));

        /*
        if (!(packet instanceof SetEntityDataPacket)) {
            Server.getInstance().getLogger().debug("SynapseEntryPutPacketThread Offer: " + packet.getClass().getSimpleName());
            Server.getInstance().getLogger().logException(new Throwable());
        }*/
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

    private static List<AbstractProtocol> fullProtocols = new ArrayList<AbstractProtocol>(Arrays.asList(AbstractProtocol.values())){{
        remove(AbstractProtocol.PROTOCOL_11);
    }};

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
        while (this.isRunning) {
            long start = System.currentTimeMillis();
            Entry entry;
            while ((entry = queue.poll()) != null) {
                try {
                    if (!entry.player.closed) {
                        RedirectPacket pk = new RedirectPacket();
                        pk.uuid = entry.player.getUniqueId();
                        pk.direct = entry.immediate;

                        /*if(entry.packet.pid() == UpdateAttributesPacket.NETWORK_ID) {
                            MainLogger.getLogger().info("ENTITY: "+((UpdateAttributesPacket) entry.packet).entityId);
                        }*/

                        //MainLogger.getLogger().notice("PACKET: "+entry.packet.getClass().getName());
                        DataPacket old = entry.packet;

                        pk.reliability = old.reliability.ordinal();
                        pk.channel = old.getChannel();

                        entry.packet = PacketRegister.getCompatiblePacket(entry.packet, (entry.player).getProtocol(), entry.player.isNetEaseClient());

                        if (entry.packet == null) {
                            MainLogger.getLogger().info("NULL PACKET " + old.getClass().getSimpleName());
                            continue;
                        }

                        if (!entry.packet.isEncoded) {
                            entry.packet.encode();
                            entry.packet.isEncoded = true;
                            /*
                            if (entry.packet instanceof MoveEntityPacket) {
                                Server.getInstance().getLogger().warning("MoveEntityPacket: " + ((MoveEntityPacket) entry.packet).eid + " " + ((MoveEntityPacket) entry.packet).x + "," + ((MoveEntityPacket) entry.packet).y + "," + ((MoveEntityPacket) entry.packet).z + ",");
                            }*/
                        }
/*
                        Server.getInstance().getLogger().warning("Sending Data Packet: " + entry.packet.getClass().getSimpleName() + " " + Binary.bytesToHexString(entry.packet.getBuffer()));

                        if(entry.packet instanceof BatchPacket) {
                            for(DataPacket packet : PacketRegister.decodeBatch((BatchPacket) entry.packet)) {
                                MainLogger.getLogger().notice("BATCH PACKET: "+packet.getClass().getName());
                            }
                        } else {
                            MainLogger.getLogger().notice("PACKET: "+entry.packet.getClass().getName());
                        }*/

                        if (!(entry.packet instanceof BatchPacket) && this.isAutoCompress) {
                            byte[] buffer = entry.packet.getBuffer();
                            try {
                                buffer = deflate(
                                        Binary.appendBytes(Binary.writeUnsignedVarInt(buffer.length), buffer),
                                        Server.getInstance().networkCompressionLevel);
                                pk.mcpeBuffer = Binary.appendBytes((byte) 0xfe, buffer);
                                /*if (entry.packet.pid() == ProtocolInfo.RESOURCE_PACKS_INFO_PACKET)
                                    Server.getInstance().getLogger().notice("ResourcePacksInfoPacket length=" + buffer.length + " " + Binary.bytesToHexString(buffer));*/
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            //Server.getInstance().getLogger().notice("S => C  " + entry.packet.getClass().getSimpleName());
                        } else {
                            pk.mcpeBuffer = entry.packet instanceof BatchPacket ? Binary.appendBytes((byte) 0xfe, ((BatchPacket) entry.packet).payload) : entry.packet.getBuffer();
                        }
                        //if (pk.reliability != RakNetReliability.RELIABLE_ORDERED.ordinal() || pk.channel != 0)
                        //    Server.getInstance().getLogger().info("reliability: " + pk.reliability + "  channel: " + pk.channel);
                        this.synapseInterface.putPacket(pk);
                        //Server.getInstance().getLogger().warning("SynapseEntryPutPacketThread PutPacket");
                        /*try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }*/
                    }
                } catch (Exception e) {
                    MainLogger.getLogger().alert("Catch exception when put single packet", e);
                    if (entry.packet.stack != null)
                        MainLogger.getLogger().alert("Main thread stack", entry.packet.stack);
                } finally {
                    if (entry.packet != null) entry.packet.stack = null;
                }
            }

            BroadcastEntry entry1;
            while ((entry1 = broadcastQueue.poll()) != null) {
                try {
                    //筛选出需要进行batch包装的协议，避免不需要的多余的包装浪费性能
                    List<SynapsePlayer> players = Arrays.stream(entry1.player).filter(Objects::nonNull).collect(Collectors.toList());
                    boolean haveNetEasePlayer = players.stream().anyMatch(SynapsePlayer::isNetEaseClient);
                    boolean[] haveNetEasePacket = new boolean[]{false};
                    Map<AbstractProtocol, List<BatchPacketEntry>> needPackets =
                            fullProtocols.stream()
                                    .filter(protocol ->
                                            players.stream()
                                                    .anyMatch(p -> AbstractProtocol.fromRealProtocol(p.getProtocol()) == protocol))
                                    .collect(Collectors.toMap(Function.identity(), v -> new ArrayList<>()));

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
                                //System.out.println("packet: "+packet.getClass().getName());
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

                    Map<AbstractProtocol, byte[][]> finalData = new HashMap<>();
                    needPackets.forEach((protocol, packets) -> {
                        BatchPacket batch = batchPackets(packets.stream().map(BatchPacketEntry::getNormalVersion).toArray(DataPacket[]::new));
                        if (batch != null) {
                            if (haveNetEasePacket[0]) {
                                BatchPacket batchNetEase = batchPackets(packets.stream().map(BatchPacketEntry::getNetEaseVersion).toArray(DataPacket[]::new));
                                if (batchNetEase != null) {
                                    finalData.put(protocol, new byte[][]{Binary.appendBytes((byte) 0xfe, batch.payload), Binary.appendBytes((byte) 0xfe, batchNetEase.payload)});
                                } else {
                                    finalData.put(protocol, new byte[][]{Binary.appendBytes((byte) 0xfe, batch.payload)});
                                }
                            } else {
                                finalData.put(protocol, new byte[][]{Binary.appendBytes((byte) 0xfe, batch.payload)});
                            }
                        }
                    });

                    for(SynapsePlayer player : entry1.player) {
                        if(player.closed) {
                            continue;
                        }

                        AbstractProtocol protocol = AbstractProtocol.fromRealProtocol(player.getProtocol());
                        if (finalData.containsKey(protocol)) {
                            RedirectPacket pk = new RedirectPacket();
                            pk.protocol = player.getProtocol();
                            pk.uuid = player.getUniqueId();
                            pk.channel = DataPacket.CHANNEL_BATCH;
                            byte[][] datas = finalData.get(protocol);
                            if (datas.length >= 2 && player.isNetEaseClient()) {
                                pk.mcpeBuffer = datas[1];
                            } else {
                                pk.mcpeBuffer = datas[0];
                            }
                            this.synapseInterface.putPacket(pk);
                        }
                    }
                } catch (Exception e) {
                    Server.getInstance().getLogger().alert("Catch exception when Synapse Entry Put Packet: " + e.getMessage());
                    Server.getInstance().getLogger().logException(e);
                }
            }

            tickUseTime = System.currentTimeMillis() - start;
            if (tickUseTime < 10){
                try {
                    Thread.sleep(10 - tickUseTime);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }

    private byte[] deflate(byte[] data, int level) throws Exception {
        if (deflater == null) throw new IllegalArgumentException("No deflate for level "+level+" !");
        deflater.reset();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        while (!deflater.finished()) {
            int i = deflater.deflate(buf);
            bos.write(buf, 0, i);
        }
        //Deflater::end is called the time when the process exits.
        return bos.toByteArray();
    }

    private class Entry {
        private SynapsePlayer player;
        private DataPacket packet;
        private boolean needACK;
        private boolean immediate;
        public Entry(SynapsePlayer player, DataPacket packet, boolean needACK, boolean immediate) {
            this.player = player;
            this.packet = packet;
            this.needACK = needACK;
            this.immediate = immediate;
        }
    }

    private static class BroadcastEntry {
        private SynapsePlayer[] player;
        private DataPacket[] packet;

        public BroadcastEntry(SynapsePlayer[] player, DataPacket[] packet) {
            this.player = player;
            this.packet = packet;
        }
    }

    public double getTicksPerSecond() {
        long more = this.tickUseTime - 10;
        if (more < 0) return 100;
        return NukkitMath.round(10f / (double)this.tickUseTime, 3) * 100;
    }

    private BatchPacket batchPackets(DataPacket[] packets) {
        try {
            byte[][] payload = new byte[packets.length * 2][];
            for (int i = 0; i < packets.length; i++) {
                DataPacket p = packets[i];
                if (!p.isEncoded) {
                    p.encode();
                }
                byte[] buf = p.getBuffer();
                payload[i * 2] = Binary.writeUnsignedVarInt(buf.length);
                payload[i * 2 + 1] = buf;
            }
            byte[] data;
            data = Binary.appendBytes(payload);

            byte[] compressed = Zlib.deflate(data, Server.getInstance().networkCompressionLevel);
            BatchPacket packet = new BatchPacket();
            packet.payload = compressed;

            return packet;
        } catch (Exception e) {
            MainLogger.getLogger().logException(e);
        }

        return null;
    }
}
