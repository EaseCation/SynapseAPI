package org.itxtech.synapseapi.multiprotocol.protocol11620;

import org.itxtech.synapseapi.multiprotocol.protocol116.BinaryStreamHelper116;

public class BinaryStreamHelper11620 extends BinaryStreamHelper116 {

    public static BinaryStreamHelper11620 create() {
        return new BinaryStreamHelper11620();
    }

    @Override
    public String getGameVersion() {
        return "1.16.20";
    }
}
