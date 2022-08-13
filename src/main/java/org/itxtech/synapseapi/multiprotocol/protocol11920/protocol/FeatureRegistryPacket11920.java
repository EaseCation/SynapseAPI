package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.AllArgsConstructor;

public class FeatureRegistryPacket11920 extends Packet11920 {

    public static final int NETWORK_ID = ProtocolInfo.FEATURE_REGISTRY_PACKET;

    public Entry[] entries = new Entry[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putUnsignedVarInt(entries.length);
        for (Entry entry : entries) {
            putString(entry.featureName);
            putString(entry.featureJson);
        }
    }

    @AllArgsConstructor
    public static class Entry {
        public String featureName;
        public String featureJson;
    }
}
