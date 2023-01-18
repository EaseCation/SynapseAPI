package org.itxtech.synapseapi.multiprotocol.protocol11830ne;

import org.itxtech.synapseapi.multiprotocol.protocol11830.BinaryStreamHelper11830;

public class BinaryStreamHelper11830NE extends BinaryStreamHelper11830 {

    public static BinaryStreamHelper11830NE create() {
        return new BinaryStreamHelper11830NE();
    }

    @Override
    public String getGameVersion() {
        return "1.18.30";
    }

}
