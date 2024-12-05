package org.itxtech.synapseapi.multiprotocol.protocol120.protocol;

import cn.nukkit.item.armortrim.TrimMaterial;
import cn.nukkit.item.armortrim.TrimPattern;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

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
            putString(pattern.itemName());
            putString(pattern.name());
        }

        putUnsignedVarInt(trimMaterials.length);
        for (TrimMaterial material : trimMaterials) {
            putString(material.name());
            putString(material.color().toString());
            putString(material.itemName());
        }
    }
}
