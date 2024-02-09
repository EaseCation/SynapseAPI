package org.itxtech.synapseapi.multiprotocol.protocol12060;

import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol12050.BinaryStreamHelper12050;

@Log4j2
public class BinaryStreamHelper12060 extends BinaryStreamHelper12050 {
    public static BinaryStreamHelper12060 create() {
        return new BinaryStreamHelper12060();
    }

    @Override
    public String getGameVersion() {
        return "1.20.60";
    }

}
