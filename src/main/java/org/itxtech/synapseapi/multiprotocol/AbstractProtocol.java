package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.*;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.Packet110;
import org.itxtech.synapseapi.multiprotocol.protocol111.protocol.Packet111;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.Packet112;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.Packet14;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.Packet15;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.Packet16;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.Packet17;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.Packet18;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.Packet19;

/**
 * org.itxtech.synapseapi.multiprotocol
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public enum AbstractProtocol {

    PROTOCOL_11(0, null, null),
    PROTOCOL_12(114, DataPacket.class, SynapsePlayer.class),
    PROTOCOL_14(261, Packet14.class, SynapsePlayer14.class),
    PROTOCOL_15(270, Packet15.class, SynapsePlayer14.class),
    PROTOCOL_16(282, Packet16.class, SynapsePlayer16.class),
    PROTOCOL_17(290, Packet17.class, SynapsePlayer17.class),
    PROTOCOL_18(312, Packet18.class, SynapsePlayer18.class),
    PROTOCOL_19(332, Packet19.class, SynapsePlayer19.class),
    PROTOCOL_110(340, Packet110.class, SynapsePlayer19.class),
    PROTOCOL_111(354, Packet111.class, SynapsePlayer19.class),
    PROTOCOL_112(361, Packet112.class, SynapsePlayer112.class);

    private final int protocolStart;
    private final Class<? extends DataPacket> packetClass;
    private final Class<? extends SynapsePlayer> playerClass;

    AbstractProtocol(int protocolStart, Class<? extends DataPacket> packetClass, Class<? extends SynapsePlayer> playerClass) {
        this.protocolStart = protocolStart;
        this.packetClass = packetClass;
        this.playerClass = playerClass;
    }

    public static AbstractProtocol fromRealProtocol(int protocol) {
        for (int i = values().length - 1; i >= 0; i--) {
            if (protocol >= values()[i].protocolStart) return values()[i];
        }
        return PROTOCOL_12; //Might never happen
	}

    public int getProtocolStart() {
        return protocolStart;
    }

    /**
     * 获取前一个协议版本
     * 无法到1.2，最前为1.4
     * @return 前一个协议版本
     */
    public AbstractProtocol previous() {
        int previous = this.ordinal() - 1;
        return previous > 1 ? values()[previous] : null;
    }

    public AbstractProtocol next() {
        int next = this.ordinal() + 1;
        return next < values().length ? values()[next] : null;
    }

    public Class<? extends DataPacket> getPacketClass() {
        return this.packetClass;
    }

    public static boolean isPacketOriginal(DataPacket packet) {
	    return !(packet instanceof IterationProtocolPacket);
    }

    public PacketHeadData tryDecodePacketHead(byte[] data) {
        if (data[0] == (byte) 0xfe) {
            return new PacketHeadData(BatchPacket.NETWORK_ID, 1);
        } else if (data.length > 1 && data[0] == (byte) 0x78 && data[1] == (byte) 0xda) {
            return new PacketHeadData(BatchPacket.NETWORK_ID, 0);
        }
        if (this == PROTOCOL_11) return null;
        BinaryStream stream = new BinaryStream(data);
        try {
            if (this.ordinal() <= PROTOCOL_15.ordinal()) {
                int pid = (int) stream.getUnsignedVarInt();
                return new PacketHeadData(pid, 3);
            } else {
                int pid1 = (int) stream.getUnsignedVarInt();
                return new PacketHeadData(pid1, stream.offset);
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().logException(e);
        }
        return null;
    }

    public static class PacketHeadData {

        private final int pid;
        private final int startOffset;

        private PacketHeadData(int pid, int startOffset) {
            this.pid = pid;
            this.startOffset = startOffset;
        }

        public int getPid() {
            return pid;
        }

        public int getStartOffset() {
            return startOffset;
        }
    }

    public Class<? extends SynapsePlayer> getPlayerClass() {
        return playerClass;
    }

}
