package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;

import javax.annotation.Nullable;

public class StructureTemplateDataResponsePacket112 extends Packet112 {
    public static final int NETWORK_ID = ProtocolInfo.STRUCTURE_TEMPLATE_DATA_EXPORT_PACKET;

    public String name;
    /// CompoundTag
    @Nullable
    public byte[] nbt;

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
    }
}
