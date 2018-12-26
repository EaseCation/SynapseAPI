package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.Packet14;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.Packet15;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.Packet16;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.Packet17;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.Packet18;

/**
 * org.itxtech.synapseapi.multiprotocol
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public enum AbstractProtocol {

    PROTOCOL_11, PROTOCOL_12, PROTOCOL_14, PROTOCOL_15, PROTOCOL_16, PROTOCOL_17, PROTOCOL_18;

	public static AbstractProtocol fromRealProtocol(int protocol) {
		if (protocol <= 113) {
			return PROTOCOL_11;
		} else if (protocol < 261) { //1.4.0 starts at 261.
			return PROTOCOL_12;
		} else if (protocol < 270) {
			return PROTOCOL_14; // in future, 1.5.0 maybe start at270 
		} else if (protocol < 282) {
            return PROTOCOL_15;
        } else if (protocol < 290) {
            return PROTOCOL_16;
        } else if (protocol < 312) {
            return PROTOCOL_17;
        } else {
            return PROTOCOL_18;
        }
	}

    /**
     * 获取前一个协议版本
     * 无法到1.2，最前为1.4
     * @return 前一个协议版本
     */
    public AbstractProtocol previous() {
        switch (this) {
            case PROTOCOL_15:
                return PROTOCOL_14;
            case PROTOCOL_16:
                return PROTOCOL_15;
            case PROTOCOL_17:
                return PROTOCOL_16;
            case PROTOCOL_18:
                return PROTOCOL_17;
            default:
                return null;
        }
    }

    public AbstractProtocol next() {
        switch (this) {
            case PROTOCOL_11:
                return PROTOCOL_12;
            case PROTOCOL_12:
                return PROTOCOL_14;
            case PROTOCOL_14:
                return PROTOCOL_15;
            case PROTOCOL_15:
                return PROTOCOL_16;
            case PROTOCOL_16:
                return PROTOCOL_17;
            case PROTOCOL_17:
                return PROTOCOL_18;
            default:
                return null;
        }
    }

    public Class<? extends DataPacket> getPacketClass() {
        switch (this) {
            case PROTOCOL_12:
                return DataPacket.class;
            case PROTOCOL_14:
                return Packet14.class;
            case PROTOCOL_15:
                return Packet15.class;
            case PROTOCOL_16:
                return Packet16.class;
            case PROTOCOL_17:
                return Packet17.class;
            case PROTOCOL_18:
                return Packet18.class;
            default:
                return null;
        }
    }

    public static boolean isPacketOriginal(DataPacket packet) {
	    return !(packet instanceof IterationProtocolPacket);
    }

    public PacketHeadData tryDecodePacketHead(byte[] data) {
        if (data[0] == (byte) 0xfe) return new PacketHeadData(BatchPacket.NETWORK_ID, 1);
        if (this == PROTOCOL_11) return null;
        BinaryStream stream = new BinaryStream(data);
        try {
            switch (this) {
                case PROTOCOL_12:
                case PROTOCOL_14:
                case PROTOCOL_15:
                    int pid = (int) stream.getUnsignedVarInt();
                    return new PacketHeadData(pid, 3);
                case PROTOCOL_16:
                case PROTOCOL_17:
                case PROTOCOL_18:
                    int pid1 = (int) stream.getUnsignedVarInt();
                    return new PacketHeadData(pid1, stream.offset);
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().logException(e);
        }
        return null;
    }

    public class PacketHeadData {

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

}
