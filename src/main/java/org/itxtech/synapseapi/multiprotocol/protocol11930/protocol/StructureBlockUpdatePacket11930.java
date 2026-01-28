package org.itxtech.synapseapi.multiprotocol.protocol11930.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.StructureBlockUpdatePacket;
import cn.nukkit.network.protocol.types.StructureEditorData;
import lombok.ToString;

@ToString
public class StructureBlockUpdatePacket11930 extends Packet11930 {
    public static final int NETWORK_ID = ProtocolInfo.COMMAND_BLOCK_UPDATE_PACKET;

    public int x;
    public int y;
    public int z;
    public StructureEditorData data;
    public boolean powered;
    public boolean waterlogged;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        BlockVector3 pos = getBlockVector3();
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        data = getStructureEditorData();
        powered = getBoolean();
        waterlogged = getBoolean();
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket toDefault() {
        StructureBlockUpdatePacket packet = new StructureBlockUpdatePacket();
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        packet.data = this.data;
        packet.powered = this.powered;
        packet.waterlogged = this.waterlogged;
        return packet;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return StructureBlockUpdatePacket.class;
    }
}
