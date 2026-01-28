package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;

import javax.annotation.Nullable;

public class StructureTemplateDataResponsePacket113 extends Packet113 {
    public static final int NETWORK_ID = ProtocolInfo.STRUCTURE_TEMPLATE_DATA_EXPORT_PACKET;

    public static final int TYPE_FAILURE = 0;
    public static final int TYPE_EXPORT = 1;
    public static final int TYPE_QUERY = 2;
    /**
     * @since 1.19.50
     * @deprecated since 1.21.10
     */
    public static final int TYPE_IMPORT = 3;

    public String name;
    /// CompoundTag
    @Nullable
    public byte[] nbt;
    public int type = TYPE_FAILURE;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        putString(name);
        putOptional(nbt, BinaryStream::put);
        putByte(type);
    }
}
