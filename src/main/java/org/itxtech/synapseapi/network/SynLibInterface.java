package org.itxtech.synapseapi.network;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.List;
import java.util.function.LongConsumer;

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
        if (player instanceof SynapsePlayer) this.synapseInterface.getPutPacketThread().addMainToThread((SynapsePlayer) player, packet);
        else throw new RuntimeException("putPacket (not SynapsePlayer) to SynLibInterface");
        return 0;  //这个返回值在nk中并没有被用到
    }

    /**
     * 将尾部 NSL 回调与当前数据包放入同一个出站 Entry，避免出站线程在数据包入队前消费回调。
     */
    public Integer putPacket(SynapsePlayer player, DataPacket packet,
                             List<LongConsumer> batchTailLatencyCallbacks) {
        this.synapseInterface.getPutPacketThread().addMainToThread(
                player, packet, batchTailLatencyCallbacks);
        return 0;
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
