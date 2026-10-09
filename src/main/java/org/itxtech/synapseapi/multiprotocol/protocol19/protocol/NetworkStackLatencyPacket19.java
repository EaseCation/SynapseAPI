package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.UUID;

@ToString
public class NetworkStackLatencyPacket19 extends Packet19 {

    public long timestamp;
    public boolean isFromServer;
    // JE 扩展确认的请求身份；普通基岩包不带此可选尾部。
    @Nullable public UUID boundaryIdentifier;

    @Override
    public int pid() {
        return ProtocolInfo.NETWORK_STACK_LATENCY_PACKET;
    }

    @Override
    public void decode() {
        timestamp = this.getLLong();
        isFromServer = this.getBoolean();
        boundaryIdentifier = this.getCount() - this.getOffset() == Long.BYTES * 2
                ? new UUID(this.getLLong(), this.getLLong()) : null;
    }

    @Override
    public void encode() {
        this.reset();
        this.putLLong(timestamp);
        this.putBoolean(isFromServer);
        if (boundaryIdentifier != null) {
            this.putLLong(boundaryIdentifier.getMostSignificantBits());
            this.putLLong(boundaryIdentifier.getLeastSignificantBits());
        }
    }
}
