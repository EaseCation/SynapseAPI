package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.math.Mth;
import cn.nukkit.utils.PlayerViolationListener;

public class SynapsePlayerViolationListener implements PlayerViolationListener {
    @Override
    public void onCommandRequest(Player player, int length) {
        int extra = 0;
        if (length > 30) {
            float score = length / 15f;
            extra = Mth.clamp((int) Mth.square(score), 1, 15);
        }
        player.addViolationLevel(5 + extra, "cmd_req_chat");
    }

    @Override
    public void onChatTooFast(Player player, int length) {
        int extra = 0;
        if (length > 20) {
            float score = length / 10f;
            extra = Mth.clamp((int) Mth.square(score), 1, 25);
        }
        player.addViolationLevel(5 + extra, "chat_fast");
    }

    @Override
    public void onChatTooLong(Player player, int length) {
        int extra = 0;
        if (length > 512) {
            float score = (length - 512) / 5f;
            extra = Mth.clamp((int) Mth.square(score), 1, 50);
        }
        player.addViolationLevel(25 + extra, "chat_long");
    }
}
