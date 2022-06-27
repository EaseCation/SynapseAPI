package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.network.Network;
import cn.nukkit.network.protocol.*;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
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
//        if (packet instanceof LevelChunkPacket) {
//            byte[] bytes = ((LevelChunkPacket) packet).data;
//            if (bytes.length == 25676 && bytes[0] == 5) {
//                log.fatal("Corrupted chunk packet? (no cache, no sub-req)", new Throwable());
//                return;
//            }
//        }

        //if (packet.pid() == ProtocolInfo.GAME_RULES_CHANGED_PACKET) return;
//        switch (packet.pid()) {
            //case ProtocolInfo.SET_COMMANDS_ENABLED_PACKET:
            //case ProtocolInfo.AVAILABLE_COMMANDS_PACKET:
        //    case ProtocolInfo.MOVE_PLAYER_PACKET:
        //    case ProtocolInfo.SET_ENTITY_MOTION_PACKET:
        //    case ProtocolInfo.MOVE_ENTITY_PACKET:
        //    case ProtocolInfo.MOVE_ENTITY_DELTA_PACKET:
            //case ProtocolInfo.MOB_EQUIPMENT_PACKET:
            //case ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET:
        //    case ProtocolInfo.MOB_EFFECT_PACKET:
       //     case ProtocolInfo.TICK_SYNC_PACKET:
            //case ProtocolInfo.INVENTORY_SLOT_PACKET:
            //case ProtocolInfo.INVENTORY_CONTENT_PACKET:
            //case ProtocolInfo.CRAFTING_DATA_PACKET:
            //case ProtocolInfo.SET_TITLE_PACKET:
            //case ProtocolInfo.TEXT_PACKET:
            //case ProtocolInfo.UPDATE_BLOCK_PACKET:
            //case ProtocolInfo.LEVEL_EVENT_PACKET:
            //case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
            //case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2:
            //case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3:
        //    case ProtocolInfo.SET_PLAYER_GAME_TYPE_PACKET:
            //case ProtocolInfo.AVAILABLE_COMMANDS_PACKET:
                //case ProtocolInfo.SET_ENTITY_DATA_PACKET:
                //case ProtocolInfo.ADVENTURE_SETTINGS_PACKET:
                //case ProtocolInfo.UPDATE_ATTRIBUTES_PACKET:
                //log.warn("blocked packet", new Throwable());
//                return;
//        }

        if (player.getSynapseEntry().getSynapse().isRecordPacketStack()) packet.stack = new Throwable();
        this.queue.offer(new Entry(player, packet, needACK, immediate));

        /*if (!(packet instanceof MoveEntityPacket)
                && !(packet instanceof MovePlayerPacket)
                && !(packet instanceof SetEntityDataPacket)
                && !(packet instanceof UpdateAttributesPacket)
                && !(packet instanceof LevelEventPacket)
                && !(packet instanceof MobEffectPacket)
                && !(packet instanceof SetTimePacket)
                && !(packet instanceof EmotePacket116)
        ) {
            Server.getInstance().getLogger().debug("SynapseEntryPutPacketThread Offer: " + packet.getClass().getSimpleName());
            Server.getInstance().getLogger().logException(new Throwable());
        }*/
        //if (packet.pid() == ProtocolInfo.CONTAINER_CLOSE_PACKET) Server.getInstance().getLogger().warning("Send ContainerClosePacket: " + packet);
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

    private static final List<AbstractProtocol> fullProtocols = new ArrayList<AbstractProtocol>(Arrays.asList(AbstractProtocol.values())){{
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

                        if (entry.packet != old) { //数据包进行了对应版本的转换
                            entry.packet.neteaseMode = entry.player.isNetEaseClient();
                        }

                        if (!entry.packet.isEncoded) {
                            entry.packet.setHelper(AbstractProtocol.fromRealProtocol(entry.player.getProtocol()).getHelper());
                            entry.packet.tryEncode();

                            /*if (entry.packet.pid() == ProtocolInfo.AVAILABLE_COMMANDS_PACKET) {
                                Server.getInstance().getLogger().warning("AvailableCommandsPacket");
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
                                if ((entry.player).getProtocol() < 407) {
                                    buffer = deflate(
                                            Binary.appendBytes(Binary.writeUnsignedVarInt(buffer.length), buffer),
                                            Server.getInstance().networkCompressionLevel
                                    );
                                } else {
                                    buffer = Network.deflateRaw(
                                            Binary.appendBytes(Binary.writeUnsignedVarInt(buffer.length), buffer),
                                            Server.getInstance().networkCompressionLevel
                                    );
                                }
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
                        BatchPacket batch = batchPackets(packets.stream().map(BatchPacketEntry::getNormalVersion).toArray(DataPacket[]::new), protocol);
                        if (batch != null) {
                            if (haveNetEasePacket[0]) {
                                BatchPacket batchNetEase = batchPackets(packets.stream().map(BatchPacketEntry::getNetEaseVersion).toArray(DataPacket[]::new), protocol);
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

    private static class Entry {
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

    private BatchPacket batchPackets(DataPacket[] packets, AbstractProtocol protocol) {
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
            }
            byte[] data;
            data = Binary.appendBytes(payload);

            BatchPacket packet = new BatchPacket();
            if (protocol.isZlibRaw()) {
                packet.payload = Network.deflateRaw(data, Server.getInstance().networkCompressionLevel);
            } else {
                packet.payload = Zlib.deflate(data, Server.getInstance().networkCompressionLevel);
            }


            return packet;
        } catch (Exception e) {
            MainLogger.getLogger().logException(e);
        }

        return null;
    }
}
