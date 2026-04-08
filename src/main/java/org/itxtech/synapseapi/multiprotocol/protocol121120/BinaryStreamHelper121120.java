package org.itxtech.synapseapi.multiprotocol.protocol121120;

import org.itxtech.synapseapi.multiprotocol.protocol121111.BinaryStreamHelper121111;

public class BinaryStreamHelper121120 extends BinaryStreamHelper121111 {
    public static BinaryStreamHelper121120 create() {
        return new BinaryStreamHelper121120();
    }

    @Override
    public String getGameVersion() {
        return "1.21.120";
    }

    @Override
    public boolean isNetEase() {
        return false;
    }
}
