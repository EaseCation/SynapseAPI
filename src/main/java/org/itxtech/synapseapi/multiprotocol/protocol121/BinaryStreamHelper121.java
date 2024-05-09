package org.itxtech.synapseapi.multiprotocol.protocol121;

import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol12080.BinaryStreamHelper12080;

@Log4j2
public class BinaryStreamHelper121 extends BinaryStreamHelper12080 {
    public static BinaryStreamHelper121 create() {
        return new BinaryStreamHelper121();
    }

    @Override
    public String getGameVersion() {
        return "1.21.0";
    }
}
