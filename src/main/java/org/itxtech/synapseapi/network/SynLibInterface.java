package org.itxtech.synapseapi.network;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NetworkStackLatencyPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.network.protocol.spp.RedirectTraceData;

/**
 * Created by boybook on 16/6/24.
 */
public class SynLibInterface implements SourceInterface {

    private final SynapseInterface synapseInterface;

    public SynLibInterface(SynapseInterface synapseInterface) {
        this.synapseInterface = synapseInterface;
    }

    @Override
    public int getNetworkLatency(Player player) {
        if (!(player instanceof SynapsePlayer synapsePlayer)) {
            return 0;
        }
        return synapsePlayer.getRakNetLatency();
    }

    @Override
    public void emergencyShutdown() {
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Integer putPacket(Player player, DataPacket packet) {
        return this.putPacket(player, packet, false);
    }

    @Override
    public Integer putPacket(Player player, DataPacket packet, boolean needACK) {
        return this.putPacket(player, packet, needACK, false);
    }

    @Override
    public Integer putPacket(Player player, DataPacket packet, boolean needACK, boolean immediate) {
        if (player instanceof SynapsePlayer synapsePlayer) {
            RedirectTraceData traceData = null;
            if (packet instanceof NetworkStackLatencyPacket16 latencyPacket16) {
                traceData = latencyPacket16.traceData;
            } else if (packet instanceof NetworkStackLatencyPacket19 latencyPacket19) {
                traceData = latencyPacket19.traceData;
            }
            this.synapseInterface.getPutPacketThread().addMainToThread(synapsePlayer, packet, traceData);
        } else throw new RuntimeException("putPacket (not SynapsePlayer) to SynLibInterface");
        return 0;  //这个返回值在nk中并没有被用到
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void close(Player player, String reason) {

    }

    @Override
    public void close(Player player) {

    }

    @Override
    public void shutdown() {

    }
}
