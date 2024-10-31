package org.itxtech.synapseapi.network.protocol.spp;

import it.unimi.dsi.fastutil.Pair;
import org.itxtech.synapseapi.utils.ClientData.Entry;

/**
 * Created by boybook on 16/6/24.
 */
public class InformationPacket extends SynapseDataPacket {

    public static final int NETWORK_ID = SynapseInfo.INFORMATION_PACKET;

    public Pair<String, Entry>[] clientList;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.clientList.length);
        for (Pair<String, Entry> client : clientList) {
            this.putString(client.left());

            Entry data = client.right();
            this.putString(data.getIp());
            this.putShort(data.getPort());
            this.putVarInt(data.getPlayerCount());
            this.putVarInt(data.getMaxPlayers());
            this.putString(data.getDescription());
        }
    }

    @Override
    public void decode() {
        int length = (int) getUnsignedVarInt();
        this.clientList = new Pair[length];
        for (int i = 0; i < length; i++) {
            this.clientList[i] = Pair.of(getString(), new Entry(
                    getString(),
                    getShort(),
                    getVarInt(),
                    getVarInt(),
                    getString()
            ));
        }
    }
}
