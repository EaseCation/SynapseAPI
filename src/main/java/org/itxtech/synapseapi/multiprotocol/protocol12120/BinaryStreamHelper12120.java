package org.itxtech.synapseapi.multiprotocol.protocol12120;

import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.utils.BinaryStream;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol1212.BinaryStreamHelper1212;

@Log4j2
public class BinaryStreamHelper12120 extends BinaryStreamHelper1212 {
    public static BinaryStreamHelper12120 create() {
        return new BinaryStreamHelper12120();
    }

    @Override
    public String getGameVersion() {
        return "1.21.20";
    }

    @Override
    public void putEntityLink(BinaryStream stream, EntityLink link) {
        stream.putEntityUniqueId(link.fromEntityUniquieId);
        stream.putEntityUniqueId(link.toEntityUniquieId);
        stream.putByte(link.type);
        stream.putBoolean(link.immediate);
        stream.putBoolean(link.riderInitiated);
        stream.putLFloat(link.vehicleAngularVelocity);
    }
}
