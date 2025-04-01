package org.itxtech.synapseapi.multiprotocol.protocol12180;

import org.itxtech.synapseapi.multiprotocol.protocol12170.BinaryStreamHelper12170;

public class BinaryStreamHelper12180 extends BinaryStreamHelper12170 {
    public static BinaryStreamHelper12180 create() {
        return new BinaryStreamHelper12180();
    }

    @Override
    public String getGameVersion() {
        return "1.21.80";
    }

}
