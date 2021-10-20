package org.itxtech.synapseapi.multiprotocol.protocol11740;

import org.itxtech.synapseapi.multiprotocol.protocol11730.BinaryStreamHelper11730;

public class BinaryStreamHelper11740 extends BinaryStreamHelper11730 {

    public static BinaryStreamHelper11740 create() {
        return new BinaryStreamHelper11740();
    }

    @Override
    public String getGameVersion() {
        return "1.17.40";
    }

}
