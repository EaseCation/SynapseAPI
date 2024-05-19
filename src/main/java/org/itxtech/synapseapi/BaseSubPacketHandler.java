package org.itxtech.synapseapi;

import org.itxtech.synapseapi.event.player.netease.SynapsePlayerNetEaseStoreBuySuccEvent;
import org.itxtech.synapseapi.network.protocol.mod.AnimationEmotePacket;
import org.itxtech.synapseapi.network.protocol.mod.ServerSubPacketHandler;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;

public class BaseSubPacketHandler implements ServerSubPacketHandler {
    protected final SynapsePlayer player;

    public BaseSubPacketHandler(SynapsePlayer player) {
        this.player = player;
    }

    @Override
    public void handle(StoreBuySuccessPacket packet) {
        new SynapsePlayerNetEaseStoreBuySuccEvent(player).call();
    }

    @Override
    public void handle(AnimationEmotePacket packet) {
        String emote = packet.emoteName();

        if (emote.length() > 100) {
            player.addViolationLevel(60, "emote_long_cn");
            return;
        }
        if (!player.emoteRequest()) {
            player.addViolationLevel(10, "emote_req_cn");
            return;
        }

        if (player.isSpectator()) {
            return;
        }
        if (player.emoting) {
            return;
        }

        player.playAnimation(emote, player.getId());
        player.getViewers().values().forEach(v -> v.playAnimation(emote, player.getId()));
    }
}
