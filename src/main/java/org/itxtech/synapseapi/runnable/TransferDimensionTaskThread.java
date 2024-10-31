package org.itxtech.synapseapi.runnable;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.PlayStatusPacket;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.network.protocol.spp.TransferPacket;

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

    private final List<Entry> sendPlayStatusList = new ObjectArrayList<>();
    private final List<Entry> transferList = new ObjectArrayList<>();

    public TransferDimensionTaskThread() {
        super("TransferDimensionTaskThread");
        setDaemon(true);
    }

    private static class Entry {
        private long eventTime;
        private final SynapsePlayer player;
        private final String hash;
        private final JsonObject extra;

        private Entry(SynapsePlayer player, String hash, JsonObject extra) {
            this.eventTime = System.currentTimeMillis() + 500;
            this.player = player;
            this.hash = hash;
            this.extra = extra;
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
            pk.sessionId = this.player.getSessionId();
            pk.clientHash = hash;
            if (this.extra != null) pk.extra = extra;
            pk.extra.addProperty("username", player.getOriginName());
            pk.extra.addProperty("xuid", player.getLoginChainData().getXUID());
            pk.extra.addProperty("netease", player.isNetEaseClient());
            pk.extra.addProperty("blob_cache", player.getClientCacheTrack() != null);
            this.player.getSynapseEntry().sendDataPacket(pk);
        }
    }

    public void queue(SynapsePlayer player, String hash, JsonObject extra) {
        this.sendPlayStatusList.add(new Entry(player, hash, extra));
    }

    @Override
    public void run() {
        while (true) {
            long start = System.currentTimeMillis();
            try {
                for (Entry entry: new ObjectArrayList<>(sendPlayStatusList)) {
                    if (entry.eventTime <= System.currentTimeMillis()) {
                        entry.doPlayStatus();
                        sendPlayStatusList.remove(entry);
                        transferList.add(entry);
                    }
                }
                for (Entry entry: new ObjectArrayList<>(transferList)) {
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
