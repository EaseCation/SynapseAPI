package org.itxtech.synapseapi.multiprotocol.protocol113;

import org.itxtech.synapseapi.multiprotocol.protocol112.BinaryStreamHelper112;

//todo: skin
public class BinaryStreamHelper113 extends BinaryStreamHelper112 {

    public static BinaryStreamHelper113 create() {
        return new BinaryStreamHelper113();
    }

    @Override
    public String getGameVersion() {
        return "1.13.0";
    }


}
