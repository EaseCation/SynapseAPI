package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PacketViolationWarningPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET;

    public PacketViolationType type;
    public PacketViolationSeverity severity;
    public int packetId;
    public String context;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.type = PacketViolationType.values()[this.getVarInt() + 1];
        this.severity = PacketViolationSeverity.values()[this.getVarInt() + 1];
        this.packetId = this.getVarInt();
        this.context = this.getString();
    }

    @Override
    public void encode() {
        this.reset();
        this.putVarInt(this.type.ordinal() - 1);
        this.putVarInt(this.severity.ordinal() - 1);
        this.putVarInt(this.packetId);
        this.putString(this.context);
    }

    public enum PacketViolationType {
        UNKNOWN,
        MALFORMED_PACKET
    }

    public enum PacketViolationSeverity {
        UNKNOWN,
        WARNING,
        FINAL_WARNING,
        TERMINATING_CONNECTION
    }
}
