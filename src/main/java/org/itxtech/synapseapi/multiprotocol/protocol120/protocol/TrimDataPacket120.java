package org.itxtech.synapseapi.multiprotocol.protocol120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@ToString
public class TrimDataPacket120 extends Packet120 {
    public static final int NETWORK_ID = ProtocolInfo.TRIM_DATA_PACKET;

    public TrimPattern[] trimPatterns = new TrimPattern[0];
    public TrimMaterial[] trimMaterials = new TrimMaterial[0];

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

        putUnsignedVarInt(trimPatterns.length);
        for (TrimPattern pattern : trimPatterns) {
            putString(pattern.itemId);
            putString(pattern.patternId);
        }

        putUnsignedVarInt(trimMaterials.length);
        for (TrimMaterial material : trimMaterials) {
            putString(material.materialId);
            putString(material.color);
            putString(material.itemId);
        }
    }

    @RequiredArgsConstructor
    @Value
    public static class TrimPattern {
        String itemId;
        String patternId;
    }

    @RequiredArgsConstructor
    @Value
    public static class TrimMaterial {
        String materialId;
        String color;
        String itemId;
    }
}
