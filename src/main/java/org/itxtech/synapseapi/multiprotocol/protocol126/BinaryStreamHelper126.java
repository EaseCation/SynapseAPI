package org.itxtech.synapseapi.multiprotocol.protocol126;

import org.itxtech.synapseapi.multiprotocol.protocol121130.BinaryStreamHelper121130;

public class BinaryStreamHelper126 extends BinaryStreamHelper121130 {
    public static BinaryStreamHelper126 create() {
        return new BinaryStreamHelper126();
    }

    @Override
    public String getGameVersion() {
        return "1.26.0";
    }
}
