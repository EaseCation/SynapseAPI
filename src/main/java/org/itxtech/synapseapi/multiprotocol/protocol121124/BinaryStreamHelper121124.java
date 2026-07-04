package org.itxtech.synapseapi.multiprotocol.protocol121124;

import org.itxtech.synapseapi.multiprotocol.protocol121120.BinaryStreamHelper121120;

public class BinaryStreamHelper121124 extends BinaryStreamHelper121120 {
    public static BinaryStreamHelper121124 create() {
        return new BinaryStreamHelper121124();
    }

    @Override
    public String getGameVersion() {
        return "1.21.124";
    }

    @Override
    public boolean isNetEase() {
        return true;
    }
}
