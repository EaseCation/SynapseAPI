package org.itxtech.synapseapi.multiprotocol.protocol112.protocol;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.StructureSettings;
import lombok.ToString;

@ToString
public class StructureTemplateDataRequestPacket112 extends Packet112 {
    public static final int NETWORK_ID = ProtocolInfo.STRUCTURE_TEMPLATE_DATA_REQUEST_PACKET;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_EXPORT_FROM_SAVE_MODE = 1;
    public static final int TYPE_EXPORT_FROM_LOAD_MODE = 2;
    public static final int TYPE_QUERY_SAVED_STRUCTURE = 3;

    public String name;
    public int x;
    public int y;
    public int z;
    public StructureSettings settings;
    public int type = TYPE_NONE;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        name = getString();
        BlockVector3 pos = getBlockVector3();
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        settings = helper.getStructureSettings(this);
        type = getByte();
    }

    @Override
    public void encode() {
    }
}
