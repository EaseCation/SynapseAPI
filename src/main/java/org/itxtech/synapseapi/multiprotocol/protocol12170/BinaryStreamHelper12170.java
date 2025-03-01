package org.itxtech.synapseapi.multiprotocol.protocol12170;

import org.itxtech.synapseapi.multiprotocol.protocol12160.BinaryStreamHelper12160;

public class BinaryStreamHelper12170 extends BinaryStreamHelper12160 {
    public static BinaryStreamHelper12170 create() {
        return new BinaryStreamHelper12170();
    }

    @Override
    public String getGameVersion() {
        return "1.21.70";
    }

}
