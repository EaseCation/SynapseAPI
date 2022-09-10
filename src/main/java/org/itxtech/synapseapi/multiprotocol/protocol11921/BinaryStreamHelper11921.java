package org.itxtech.synapseapi.multiprotocol.protocol11921;

import org.itxtech.synapseapi.multiprotocol.protocol11920.BinaryStreamHelper11920;

// 这个版本有与自定义方块相关的改动, 我们目前不使用所以不受影响
public class BinaryStreamHelper11921 extends BinaryStreamHelper11920 {

    public static BinaryStreamHelper11921 create() {
        return new BinaryStreamHelper11921();
    }

    @Override
    public String getGameVersion() {
        return "1.19.21";
    }


}
