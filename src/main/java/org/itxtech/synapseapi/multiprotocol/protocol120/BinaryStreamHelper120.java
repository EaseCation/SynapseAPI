package org.itxtech.synapseapi.multiprotocol.protocol120;

import org.itxtech.synapseapi.multiprotocol.protocol11980.BinaryStreamHelper11980;

public class BinaryStreamHelper120 extends BinaryStreamHelper11980 {
    public static BinaryStreamHelper120 create() {
        return new BinaryStreamHelper120();
    }

    @Override
    public String getGameVersion() {
        return "1.20.0";
    }
}
