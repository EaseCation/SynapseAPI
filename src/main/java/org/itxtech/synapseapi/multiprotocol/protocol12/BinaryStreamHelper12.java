package org.itxtech.synapseapi.multiprotocol.protocol12;

import cn.nukkit.utils.BinaryStream.BinaryStreamHelper;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedBinaryStreamHelper;

public class BinaryStreamHelper12 extends BinaryStreamHelper implements AdvancedBinaryStreamHelper {

    public static BinaryStreamHelper12 create() {
        return new BinaryStreamHelper12();
    }

    protected AbstractProtocol protocol;

    @Override
    public final void setProtocol(AbstractProtocol protocol) {
        if (this.protocol == null) {
            this.protocol = protocol;
        }
    }
}
