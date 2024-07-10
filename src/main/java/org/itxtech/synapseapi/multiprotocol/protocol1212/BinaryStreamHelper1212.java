package org.itxtech.synapseapi.multiprotocol.protocol1212;

import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol121.BinaryStreamHelper121;

@Log4j2
public class BinaryStreamHelper1212 extends BinaryStreamHelper121 {
    public static BinaryStreamHelper1212 create() {
        return new BinaryStreamHelper1212();
    }

    @Override
    public String getGameVersion() {
        return "1.21.2";
    }
}
