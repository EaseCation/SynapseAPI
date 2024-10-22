package org.itxtech.synapseapi.multiprotocol.protocol12150;

import org.itxtech.synapseapi.multiprotocol.protocol12140.BinaryStreamHelper12140;

public class BinaryStreamHelper12150 extends BinaryStreamHelper12140 {
    public static BinaryStreamHelper12150 create() {
        return new BinaryStreamHelper12150();
    }

    @Override
    public String getGameVersion() {
        return "1.21.50";
    }
}
