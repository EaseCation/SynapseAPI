package org.itxtech.synapseapi.multiprotocol.protocol116100ne;

import org.itxtech.synapseapi.multiprotocol.protocol11620.BinaryStreamHelper11620;

public class BinaryStreamHelper116100NE extends BinaryStreamHelper11620 {

    public static BinaryStreamHelper116100NE create() {
        return new BinaryStreamHelper116100NE();
    }

    @Override
    public String getGameVersion() {
        return "1.16.100";
    }
}
