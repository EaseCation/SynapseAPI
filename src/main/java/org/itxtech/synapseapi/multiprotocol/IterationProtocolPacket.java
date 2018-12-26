package org.itxtech.synapseapi.multiprotocol;

import cn.nukkit.network.protocol.DataPacket;

public abstract class IterationProtocolPacket extends DataPacket {

    public DataPacket toDefault() {
        return this;
    }

    public DataPacket fromDefault(DataPacket pk) {
        return this;
    }

    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        return fromDefault(pk);
    }

    public abstract AbstractProtocol getAbstractProtocol();

}
