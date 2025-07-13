package org.itxtech.synapseapi.multiprotocol.protocol121100;

import org.itxtech.synapseapi.multiprotocol.protocol12193.BinaryStreamHelper12193;

public class BinaryStreamHelper121100 extends BinaryStreamHelper12193 {
    public static BinaryStreamHelper121100 create() {
        return new BinaryStreamHelper121100();
    }

    @Override
    public String getGameVersion() {
        return "1.21.100";
    }
}
