package org.itxtech.synapseapi.multiprotocol.protocol12130;

import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol12120.BinaryStreamHelper12120;

@Log4j2
public class BinaryStreamHelper12130 extends BinaryStreamHelper12120 {
    public static BinaryStreamHelper12130 create() {
        return new BinaryStreamHelper12130();
    }

    @Override
    public String getGameVersion() {
        return "1.21.30";
    }
}
