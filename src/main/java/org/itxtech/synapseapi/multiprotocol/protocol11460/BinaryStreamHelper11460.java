package org.itxtech.synapseapi.multiprotocol.protocol11460;

import org.itxtech.synapseapi.multiprotocol.protocol114.BinaryStreamHelper114;

//todo: skin
public class BinaryStreamHelper11460 extends BinaryStreamHelper114 {

    public static BinaryStreamHelper11460 create() {
        return new BinaryStreamHelper11460();
    }

    @Override
    public String getGameVersion() {
        return "1.14.60";
    }


}
