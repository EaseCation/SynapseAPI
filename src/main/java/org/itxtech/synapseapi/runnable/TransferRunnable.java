package org.itxtech.synapseapi.runnable;

import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.network.protocol.spp.TransferPacket;

/**
 * Created by boybook on 16/9/26.
 */
public class TransferRunnable implements Runnable {

    private final SynapsePlayer player;
    private final String hash;

    public TransferRunnable(SynapsePlayer player, String hash) {
        this.player = player;
        this.hash = hash;
    }

    @Override
    public void run() {
        TransferPacket pk = new TransferPacket();
        pk.sessionId = this.player.getSessionId();
        pk.clientHash = hash;

        this.player.getSynapseEntry().sendDataPacket(pk);
    }
}
