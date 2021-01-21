package org.itxtech.synapseapi.multiprotocol.protocol116;

import org.itxtech.synapseapi.multiprotocol.protocol11460.BinaryStreamHelper11460;

public class BinaryStreamHelper116 extends BinaryStreamHelper11460 {

    public static BinaryStreamHelper116 create() {
        return new BinaryStreamHelper116();
    }

    /**
     * 从1.16开始可以使用通配符星号. (e.g. "*")
     * @return base game version
     */
    @Override
    public String getGameVersion() {
        return "1.16.0";
    }


}
