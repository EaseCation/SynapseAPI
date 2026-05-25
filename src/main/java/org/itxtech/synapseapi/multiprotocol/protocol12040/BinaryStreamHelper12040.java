package org.itxtech.synapseapi.multiprotocol.protocol12040;

import org.itxtech.synapseapi.multiprotocol.protocol12030.BinaryStreamHelper12030;

public class BinaryStreamHelper12040 extends BinaryStreamHelper12030 {
    public static BinaryStreamHelper12040 create() {
        return new BinaryStreamHelper12040();
    }

    @Override
    public String getGameVersion() {
        return "1.20.40";
    }
}
