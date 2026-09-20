package org.itxtech.synapseapi.network;

import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.network.protocol.PacketSequence;

import java.util.List;

public record OutboundPacket(DataPacket packet, List<byte[]> buffers, boolean batchTail) {
    public boolean requiresBatch() {
        return batchTail || packet instanceof PacketSequence;
    }

    public void discard() {
        PacketSequence.discard(packet);
    }
}
