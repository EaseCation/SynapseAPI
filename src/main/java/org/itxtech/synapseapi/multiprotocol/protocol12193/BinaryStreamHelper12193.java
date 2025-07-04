package org.itxtech.synapseapi.multiprotocol.protocol12193;

import org.itxtech.synapseapi.multiprotocol.protocol12190.BinaryStreamHelper12190;

public class BinaryStreamHelper12193 extends BinaryStreamHelper12190 {
    public static BinaryStreamHelper12193 create() {
        return new BinaryStreamHelper12193();
    }

    @Override
    public String getGameVersion() {
        return "1.21.93";
    }
}
