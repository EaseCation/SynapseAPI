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

    //区分数据包是否为1.6之后的新版，用于解决包头大改的包区分问题
    public boolean is16Newer() {
        return getAbstractProtocol().ordinal() >= AbstractProtocol.PROTOCOL_16.ordinal();
    }

}
