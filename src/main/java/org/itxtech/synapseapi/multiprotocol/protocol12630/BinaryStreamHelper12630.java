package org.itxtech.synapseapi.multiprotocol.protocol12630;

import org.itxtech.synapseapi.multiprotocol.protocol12620.BinaryStreamHelper12620;

public class BinaryStreamHelper12630 extends BinaryStreamHelper12620 {
    public static BinaryStreamHelper12630 create() {
        return new BinaryStreamHelper12630();
    }

    @Override
    public String getGameVersion() {
        return "1.26.30";
    }
}
