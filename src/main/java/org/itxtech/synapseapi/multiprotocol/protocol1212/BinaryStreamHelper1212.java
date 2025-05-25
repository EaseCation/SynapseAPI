package org.itxtech.synapseapi.multiprotocol.protocol1212;

import org.itxtech.synapseapi.multiprotocol.protocol121.BinaryStreamHelper121;

public class BinaryStreamHelper1212 extends BinaryStreamHelper121 {
    public static BinaryStreamHelper1212 create() {
        return new BinaryStreamHelper1212();
    }

    @Override
    public String getGameVersion() {
        return "1.21.2";
    }

    @Override
    public boolean isNetEase() {
        return true;
    }
}
