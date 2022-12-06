package org.itxtech.synapseapi.multiprotocol.protocol11940;

import org.itxtech.synapseapi.multiprotocol.protocol11930.BinaryStreamHelper11930;

public class BinaryStreamHelper11940 extends BinaryStreamHelper11930 {

    public static BinaryStreamHelper11940 create() {
        return new BinaryStreamHelper11940();
    }

    @Override
    public String getGameVersion() {
        return "1.19.40";
    }
}
