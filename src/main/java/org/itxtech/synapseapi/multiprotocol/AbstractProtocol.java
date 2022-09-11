package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.BinaryStream.BinaryStreamHelper;
import org.itxtech.synapseapi.*;
import org.itxtech.synapseapi.multiprotocol.protocol110.BinaryStreamHelper110;
import org.itxtech.synapseapi.multiprotocol.protocol110.protocol.Packet110;
import org.itxtech.synapseapi.multiprotocol.protocol111.BinaryStreamHelper111;
import org.itxtech.synapseapi.multiprotocol.protocol111.protocol.Packet111;
import org.itxtech.synapseapi.multiprotocol.protocol112.BinaryStreamHelper112;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.Packet112;
import org.itxtech.synapseapi.multiprotocol.protocol113.BinaryStreamHelper113;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.Packet113;
import org.itxtech.synapseapi.multiprotocol.protocol114.BinaryStreamHelper114;
import org.itxtech.synapseapi.multiprotocol.protocol11460.BinaryStreamHelper11460;
import org.itxtech.synapseapi.multiprotocol.protocol11460.protocol.Packet11460;
import org.itxtech.synapseapi.multiprotocol.protocol116.BinaryStreamHelper116;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.Packet116;
import org.itxtech.synapseapi.multiprotocol.protocol116100.BinaryStreamHelper116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.Packet116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.BinaryStreamHelper116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.Packet116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol11620.BinaryStreamHelper11620;
import org.itxtech.synapseapi.multiprotocol.protocol11620.protocol.Packet11620;
import org.itxtech.synapseapi.multiprotocol.protocol116200.BinaryStreamHelper116200;
import org.itxtech.synapseapi.multiprotocol.protocol116200.protocol.Packet116200;
import org.itxtech.synapseapi.multiprotocol.protocol116210.BinaryStreamHelper116210;
import org.itxtech.synapseapi.multiprotocol.protocol116210.protocol.Packet116210;
import org.itxtech.synapseapi.multiprotocol.protocol116220.BinaryStreamHelper116220;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.Packet116220;
import org.itxtech.synapseapi.multiprotocol.protocol117.BinaryStreamHelper117;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.Packet117;
import org.itxtech.synapseapi.multiprotocol.protocol11710.BinaryStreamHelper11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.Packet11710;
import org.itxtech.synapseapi.multiprotocol.protocol11730.BinaryStreamHelper11730;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.Packet11730;
import org.itxtech.synapseapi.multiprotocol.protocol11740.BinaryStreamHelper11740;
import org.itxtech.synapseapi.multiprotocol.protocol118.BinaryStreamHelper118;
import org.itxtech.synapseapi.multiprotocol.protocol118.protocol.Packet118;
import org.itxtech.synapseapi.multiprotocol.protocol11810.BinaryStreamHelper11810;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.Packet11810;
import org.itxtech.synapseapi.multiprotocol.protocol11830.BinaryStreamHelper11830;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.Packet11830;
import org.itxtech.synapseapi.multiprotocol.protocol119.BinaryStreamHelper119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.Packet119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.BinaryStreamHelper11910;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.Packet11910;
import org.itxtech.synapseapi.multiprotocol.protocol11920.BinaryStreamHelper11920;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.Packet11920;
import org.itxtech.synapseapi.multiprotocol.protocol11921.BinaryStreamHelper11921;
import org.itxtech.synapseapi.multiprotocol.protocol11921.protocol.Packet11921;
import org.itxtech.synapseapi.multiprotocol.protocol12.BinaryStreamHelper12;
import org.itxtech.synapseapi.multiprotocol.protocol14.BinaryStreamHelper14;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.Packet14;
import org.itxtech.synapseapi.multiprotocol.protocol15.BinaryStreamHelper15;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.Packet15;
import org.itxtech.synapseapi.multiprotocol.protocol16.BinaryStreamHelper16;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.Packet16;
import org.itxtech.synapseapi.multiprotocol.protocol17.BinaryStreamHelper17;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.Packet17;
import org.itxtech.synapseapi.multiprotocol.protocol18.BinaryStreamHelper18;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.Packet18;
import org.itxtech.synapseapi.multiprotocol.protocol19.BinaryStreamHelper19;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.Packet19;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedBinaryStreamHelper;

/**
 * org.itxtech.synapseapi.multiprotocol
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public enum AbstractProtocol {

    PROTOCOL_11(0, null, null, BinaryStreamHelper.getInstance()),
    PROTOCOL_12(114, DataPacket.class, SynapsePlayer.class, BinaryStreamHelper12.create()),
    PROTOCOL_14(261, Packet14.class, SynapsePlayer14.class, BinaryStreamHelper14.create()),
    PROTOCOL_15(270, Packet15.class, SynapsePlayer14.class, BinaryStreamHelper15.create()),
    PROTOCOL_16(282, Packet16.class, SynapsePlayer16.class, BinaryStreamHelper16.create()),
    PROTOCOL_17(290, Packet17.class, SynapsePlayer17.class, BinaryStreamHelper17.create()),
    PROTOCOL_18(312, Packet18.class, SynapsePlayer18.class, BinaryStreamHelper18.create()),
    PROTOCOL_19(332, Packet19.class, SynapsePlayer19.class, BinaryStreamHelper19.create()),
    PROTOCOL_110(340, Packet110.class, SynapsePlayer19.class, BinaryStreamHelper110.create()),
    PROTOCOL_111(354, Packet111.class, SynapsePlayer19.class, BinaryStreamHelper111.create()),
    PROTOCOL_112(361, Packet112.class, SynapsePlayer112.class, BinaryStreamHelper112.create()),
    PROTOCOL_113(388, Packet113.class, SynapsePlayer113.class, BinaryStreamHelper113.create()),
    PROTOCOL_114(389, Packet113.class, SynapsePlayer113.class, BinaryStreamHelper114.create()),
    PROTOCOL_114_60(390, Packet11460.class, SynapsePlayer113.class, BinaryStreamHelper11460.create()),
    PROTOCOL_116(407, Packet116.class, SynapsePlayer116.class, BinaryStreamHelper116.create(), true),
    PROTOCOL_116_20(408, Packet11620.class, SynapsePlayer116.class, BinaryStreamHelper11620.create(), true),
    PROTOCOL_116_100_NE(410, Packet116100NE.class, SynapsePlayer116100.class, BinaryStreamHelper116100NE.create(), true), // 1.16.100.51 beta (使用407的数据)
    PROTOCOL_116_100(419, Packet116100.class, SynapsePlayer116100.class, BinaryStreamHelper116100.create(), true),
    PROTOCOL_116_200(422, Packet116200.class, SynapsePlayer116100.class, BinaryStreamHelper116200.create(), true), // 事实上网易用的是后一个版本的 beta
    PROTOCOL_116_210(428, Packet116210.class, SynapsePlayer116100.class, BinaryStreamHelper116210.create(), true),
    PROTOCOL_116_220(431, Packet116220.class, SynapsePlayer116100.class, BinaryStreamHelper116220.create(), true),
    PROTOCOL_117(440, Packet117.class, SynapsePlayer116100.class, BinaryStreamHelper117.create(), true),
    PROTOCOL_117_10(448, Packet11710.class, SynapsePlayer116100.class, BinaryStreamHelper11710.create(), true),
    PROTOCOL_117_30(465, Packet11730.class, SynapsePlayer116100.class, BinaryStreamHelper11730.create(), true),
    PROTOCOL_117_40(471, Packet11730.class, SynapsePlayer116100.class, BinaryStreamHelper11740.create(), true),
    PROTOCOL_118(475, Packet118.class, SynapsePlayer116100.class, BinaryStreamHelper118.create(), true),
    PROTOCOL_118_10(486, Packet11810.class, SynapsePlayer116100.class, BinaryStreamHelper11810.create(), true),
    PROTOCOL_118_30(503, Packet11830.class, SynapsePlayer116100.class, BinaryStreamHelper11830.create(), true),
    PROTOCOL_119(527, Packet119.class, SynapsePlayer116100.class, BinaryStreamHelper119.create(), true),
    PROTOCOL_119_10(534, Packet11910.class, SynapsePlayer116100.class, BinaryStreamHelper11910.create(), true),
    PROTOCOL_119_20(544, Packet11920.class, SynapsePlayer116100.class, BinaryStreamHelper11920.create(), true),
    PROTOCOL_119_21(545, Packet11921.class, SynapsePlayer116100.class, BinaryStreamHelper11921.create(), true),
    ;

    private final int protocolStart;
    private final Class<? extends DataPacket> packetClass;
    private final Class<? extends SynapsePlayer> playerClass;
    private final BinaryStreamHelper helper;
    private final boolean zlibRaw;

    AbstractProtocol(int protocolStart, Class<? extends DataPacket> packetClass, Class<? extends SynapsePlayer> playerClass, BinaryStreamHelper helper) {
        this(protocolStart, packetClass, playerClass, helper, false);
    }

    AbstractProtocol(int protocolStart, Class<? extends DataPacket> packetClass, Class<? extends SynapsePlayer> playerClass, BinaryStreamHelper helper, boolean zlibRaw) {
        this.protocolStart = protocolStart;
        this.packetClass = packetClass;
        this.playerClass = playerClass;
        this.helper = helper;
        this.zlibRaw = zlibRaw;

        if (helper instanceof AdvancedBinaryStreamHelper) {
            ((AdvancedBinaryStreamHelper) helper).setProtocol(this);
        }
    }

    public static AbstractProtocol fromRealProtocol(int protocol) {
        for (int i = values0().length - 1; i >= 0; i--) {
            if (protocol >= values0()[i].protocolStart) return values0()[i];
        }
        return PROTOCOL_12; //Might never happen
	}

    public int getProtocolStart() {
        return protocolStart;
    }

    public BinaryStreamHelper getHelper() {
        return this.helper;
    }

    public boolean isZlibRaw() {
        return zlibRaw;
    }

    /**
     * 获取前一个协议版本
     * 无法到1.2，最前为1.4
     * @return 前一个协议版本
     */
    public AbstractProtocol previous() {
        int previous = this.ordinal() - 1;
        return previous > 1 ? values0()[previous] : null;
    }

    public AbstractProtocol next() {
        int next = this.ordinal() + 1;
        return next < values0().length ? values0()[next] : null;
    }

    public Class<? extends DataPacket> getPacketClass() {
        return this.packetClass;
    }

    public static boolean isPacketOriginal(DataPacket packet) {
	    return !(packet instanceof IterationProtocolPacket);
    }

    public PacketHeadData tryDecodePacketHead(byte[] data, boolean maybeBatch) {
        if (maybeBatch) {
            //System.out.println(Binary.bytesToHexString(new byte[]{data[0]}));
            if (data[0] == (byte) 0xfe) {
                return new PacketHeadData(BatchPacket.NETWORK_ID, 1);
            } else if (data[0] == (byte) 0x78) {
                return new PacketHeadData(BatchPacket.NETWORK_ID, 0);
            }/* else if (this.ordinal() >= PROTOCOL_116.ordinal()) {
                return new PacketHeadData(BatchPacket.NETWORK_ID, 0);
            }*/
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

    private static final AbstractProtocol[] $VALUES0 = values();

    public static AbstractProtocol[] values0() {
        return $VALUES0;
    }
}
