package org.itxtech.synapseapi.network;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLogoutPacket;

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

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void close(Player player, String reason) {
        // 玩家在 Nukkit 侧断开时，必须通知 Synapse 代理同步断开该会话，
        // 否则代理仍会继续转发该玩家的双向数据包，形成"半开连接"（客户端卡死、服务端持续处理僵尸包）。
        // 注意：DisconnectPacket 走 PutPacketThread 异步队列发送，本包若同步直发会先于它到达代理，
        // 导致会话先被关闭、踢出界面无法展示，因此延迟几 tick 再发送，保证"先看到界面、后断开连接"
        if (player instanceof SynapsePlayer synapsePlayer) {
            Server.getInstance().getScheduler().scheduleDelayedTask(SynapseAPI.getInstance(), () -> {
                PlayerLogoutPacket pk = new PlayerLogoutPacket();
                pk.sessionId = synapsePlayer.getSessionId();
                pk.reason = reason;
                synapsePlayer.getSynapseEntry().sendDataPacket(pk);
            }, 5);
        }
    }

    @Override
    public void close(Player player) {
        close(player, "");
    }

    @Override
    public void shutdown() {

    }
}
