package org.itxtech.synapseapi.multiprotocol.protocol12140;

import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol12130.BinaryStreamHelper12130;

@Log4j2
public class BinaryStreamHelper12140 extends BinaryStreamHelper12130 {
    public static BinaryStreamHelper12140 create() {
        return new BinaryStreamHelper12140();
    }

    @Override
    public String getGameVersion() {
        return "1.21.40";
    }
}
