package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.AsyncEvent;
import cn.nukkit.event.HandlerList;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SynapsePlayerBatchSendEvent extends SynapsePlayerEvent implements AsyncEvent {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final List<DataPacket> packets;
    private final List<DataPacket> additionalPackets = new ArrayList<>(1);

    public SynapsePlayerBatchSendEvent(SynapsePlayer player, List<DataPacket> packets) {
        super(player);
        this.packets = packets;
    }

    public List<DataPacket> getPackets() {
        return packets;
    }

    public List<DataPacket> getAdditionalPackets() {
        return additionalPackets;
    }

    public void append(DataPacket packet) {
        additionalPackets.add(Objects.requireNonNull(packet, "packet"));
    }
}
