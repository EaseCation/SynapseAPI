package org.itxtech.synapseapi.multiprotocol.protocol11950;

import org.itxtech.synapseapi.multiprotocol.protocol11940.BinaryStreamHelper11940;

public class BinaryStreamHelper11950 extends BinaryStreamHelper11940 {
    public static BinaryStreamHelper11950 create() {
        return new BinaryStreamHelper11950();
    }

    @Override
    public String getGameVersion() {
        return "1.19.50";
    }
}
