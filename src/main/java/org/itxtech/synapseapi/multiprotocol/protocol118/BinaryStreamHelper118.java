package org.itxtech.synapseapi.multiprotocol.protocol118;

import org.itxtech.synapseapi.multiprotocol.protocol11740.BinaryStreamHelper11740;

public class BinaryStreamHelper118 extends BinaryStreamHelper11740 {

    public static BinaryStreamHelper118 create() {
        return new BinaryStreamHelper118();
    }

    @Override
    public String getGameVersion() {
        return "1.18.0";
    }


}
