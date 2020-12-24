package org.itxtech.synapseapi.multiprotocol.protocol114;

import org.itxtech.synapseapi.multiprotocol.protocol113.BinaryStreamHelper113;

public class BinaryStreamHelper114 extends BinaryStreamHelper113 {

    public static BinaryStreamHelper114 create() {
        return new BinaryStreamHelper114();
    }

    @Override
    public String getGameVersion() {
        return "1.14.0";
    }
}
