package org.itxtech.synapseapi.multiprotocol.protocol12190;

import org.itxtech.synapseapi.multiprotocol.protocol12180.BinaryStreamHelper12180;

public class BinaryStreamHelper12190 extends BinaryStreamHelper12180 {
    public static BinaryStreamHelper12190 create() {
        return new BinaryStreamHelper12190();
    }

    @Override
    public String getGameVersion() {
        return "1.21.90";
    }
}
