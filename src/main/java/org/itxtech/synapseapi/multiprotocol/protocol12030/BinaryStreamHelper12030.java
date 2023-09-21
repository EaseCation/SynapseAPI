package org.itxtech.synapseapi.multiprotocol.protocol12030;

import org.itxtech.synapseapi.multiprotocol.protocol12010.BinaryStreamHelper12010;

public class BinaryStreamHelper12030 extends BinaryStreamHelper12010 {
    public static BinaryStreamHelper12030 create() {
        return new BinaryStreamHelper12030();
    }

    @Override
    public String getGameVersion() {
        return "1.20.30";
    }

}
