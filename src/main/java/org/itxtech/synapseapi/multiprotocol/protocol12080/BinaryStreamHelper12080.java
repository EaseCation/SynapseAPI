package org.itxtech.synapseapi.multiprotocol.protocol12080;

import org.itxtech.synapseapi.multiprotocol.protocol12070.BinaryStreamHelper12070;

public class BinaryStreamHelper12080 extends BinaryStreamHelper12070 {
    public static BinaryStreamHelper12080 create() {
        return new BinaryStreamHelper12080();
    }

    @Override
    public String getGameVersion() {
        return "1.20.80";
    }
}
