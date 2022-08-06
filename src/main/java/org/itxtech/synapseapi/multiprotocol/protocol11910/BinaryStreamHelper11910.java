package org.itxtech.synapseapi.multiprotocol.protocol11910;

import org.itxtech.synapseapi.multiprotocol.protocol119.BinaryStreamHelper119;

public class BinaryStreamHelper11910 extends BinaryStreamHelper119 {

    public static BinaryStreamHelper11910 create() {
        return new BinaryStreamHelper11910();
    }

    @Override
    public String getGameVersion() {
        return "1.19.10";
    }


}
