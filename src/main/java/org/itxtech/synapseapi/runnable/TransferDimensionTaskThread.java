package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.PlayStatusPacket;
import cn.nukkit.utils.TextFormat;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.network.protocol.spp.TransferPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * org.itxtech.synapseapi.runnable
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class TransferDimensionTaskThread extends Thread {

    private List<Entry> sendPlayStatusList = new ArrayList<>();
    private List<Entry> transferList = new ArrayList<>();

    public TransferDimensionTaskThread() {
        super("TransferDimensionTaskThread");
    }

    private class Entry {
        private long eventTime;
        private SynapsePlayer player;
        private String hash;

        private Entry(SynapsePlayer player, String hash) {
            this.eventTime = System.currentTimeMillis() + 500;
            this.player = player;
            this.hash = hash;
        }

        private void doPlayStatus() {
            PlayStatusPacket statusPacket0 = new PlayStatusPacket();
            statusPacket0.status = PlayStatusPacket.PLAYER_SPAWN;
            this.player.dataPacket(statusPacket0);
            //this.player.sendTitle(TextFormat.GRAY.toString() + TextFormat.OBFUSCATED.toString() + "AB" + TextFormat.RESET + " Transfering " + TextFormat.GRAY.toString() + TextFormat.OBFUSCATED.toString() + "BA", "", 1, 20 * 5, 10);
            this.eventTime = System.currentTimeMillis() + 500;
        }

        private void doTransfer() {
            TransferPacket pk = new TransferPacket();
            pk.uuid = this.player.getUniqueId();
            pk.clientHash = hash;
            pk.extra.addProperty("username", player.getOriginName());
            pk.extra.addProperty("xuid", player.getLoginChainData().getXUID());
            pk.extra.addProperty("netease", player.isNetEaseClient());
            pk.extra.addProperty("blob_cache", player.getClientCacheTrack() != null);
            this.player.getSynapseEntry().sendDataPacket(pk);
        }
    }

    public void queue(SynapsePlayer player, String hash) {
        this.sendPlayStatusList.add(new Entry(player, hash));
    }

    @Override
    public void run() {
        while (true) {
            long start = System.currentTimeMillis();
            try {
                for (Entry entry: new ArrayList<>(sendPlayStatusList)) {
                    if (entry.eventTime <= System.currentTimeMillis()) {
                        entry.doPlayStatus();
                        sendPlayStatusList.remove(entry);
                        transferList.add(entry);
                    }
                }
                for (Entry entry: new ArrayList<>(transferList)) {
                    if (entry.eventTime <= System.currentTimeMillis()) {
                        entry.doTransfer();
                        transferList.remove(entry);
                    }
                }
            } catch (Exception e) {
                Server.getInstance().getLogger().logException(e);
            }
            long time = System.currentTimeMillis() - start;
            if (time < 50) {
                try {
                    Thread.sleep(50 - time);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }
}
