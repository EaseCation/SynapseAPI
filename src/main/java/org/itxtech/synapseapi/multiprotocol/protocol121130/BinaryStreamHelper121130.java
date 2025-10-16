package org.itxtech.synapseapi.multiprotocol.protocol121130;

import org.itxtech.synapseapi.multiprotocol.protocol121120.BinaryStreamHelper121120;

public class BinaryStreamHelper121130 extends BinaryStreamHelper121120 {
    public static BinaryStreamHelper121130 create() {
        return new BinaryStreamHelper121130();
    }

    @Override
    public String getGameVersion() {
        return "1.21.130";
    }
}
