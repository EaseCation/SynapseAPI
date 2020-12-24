package org.itxtech.synapseapi.multiprotocol.protocol116200;

import org.itxtech.synapseapi.multiprotocol.protocol116100.BinaryStreamHelper116100;

public class BinaryStreamHelper116200 extends BinaryStreamHelper116100 {

    public static BinaryStreamHelper116200 create() {
        return new BinaryStreamHelper116200();
    }

    @Override
    public String getGameVersion() {
        return "1.16.200";
    }
}
