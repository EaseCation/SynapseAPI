package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.utils.PlayerViolationListener;

public class SynapsePlayerViolationListener implements PlayerViolationListener {
    @Override
    public void onCommandRequest(Player player) {
        player.addViolationLevel(5, "cmd_req_chat");
    }

    @Override
    public void onChatTooFast(Player player) {
        player.addViolationLevel(5, "chat_fast");
    }

    @Override
    public void onChatTooLong(Player player) {
        player.addViolationLevel(25, "chat_long");
    }
}
