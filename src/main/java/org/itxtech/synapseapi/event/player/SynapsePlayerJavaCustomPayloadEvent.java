package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.Objects;

/**
 * 经 ViaBedrock ScriptMessage 桥转发且已受大小限制的 Java C2S 自定义载荷。
 * 载荷保持不透明，消费者必须显式选择频道并自行校验内容。
 */
public final class SynapsePlayerJavaCustomPayloadEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final String channel;
    private final byte[] payload;

    public SynapsePlayerJavaCustomPayloadEvent(final SynapsePlayer player, final String channel, final byte[] payload) {
        super(player);
        this.channel = Objects.requireNonNull(channel, "channel");
        this.payload = Objects.requireNonNull(payload, "payload").clone();
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public String getChannel() {
        return this.channel;
    }

    /**
     * 返回防御性复制，避免一个监听器修改其他监听器看到的数据。
     */
    public byte[] getPayload() {
        return this.payload.clone();
    }
}
