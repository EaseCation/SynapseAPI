package org.itxtech.synapseapi.multiprotocol.protocol11810;

import org.itxtech.synapseapi.multiprotocol.protocol118.BinaryStreamHelper118;

public class BinaryStreamHelper11810 extends BinaryStreamHelper118 {

    public static BinaryStreamHelper11810 create() {
        return new BinaryStreamHelper11810();
    }

    @Override
    public String getGameVersion() {
        return "1.18.10";
    }


}
