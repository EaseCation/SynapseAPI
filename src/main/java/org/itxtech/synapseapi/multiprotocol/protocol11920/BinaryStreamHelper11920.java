package org.itxtech.synapseapi.multiprotocol.protocol11920;

import org.itxtech.synapseapi.multiprotocol.protocol11910.BinaryStreamHelper11910;

public class BinaryStreamHelper11920 extends BinaryStreamHelper11910 {

    public static BinaryStreamHelper11920 create() {
        return new BinaryStreamHelper11920();
    }

    @Override
    public String getGameVersion() {
        return "1.19.20";
    }


}
