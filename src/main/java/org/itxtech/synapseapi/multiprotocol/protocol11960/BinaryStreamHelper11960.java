package org.itxtech.synapseapi.multiprotocol.protocol11960;

import org.itxtech.synapseapi.multiprotocol.protocol11950.BinaryStreamHelper11950;

public class BinaryStreamHelper11960 extends BinaryStreamHelper11950 {
    public static BinaryStreamHelper11960 create() {
        return new BinaryStreamHelper11960();
    }

    @Override
    public String getGameVersion() {
        return "1.19.60";
    }
}
