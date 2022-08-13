package org.itxtech.synapseapi.multiprotocol.protocol11710.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.NPCRequestPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class NPCRequestPacket11710 extends Packet11710 {
    public static final int NETWORK_ID = ProtocolInfo.NPC_REQUEST_PACKET;

    public static final int TYPE_SET_ACTION = 0;
    public static final int TYPE_EXECUTE_COMMAND_ACTION = 1;
    public static final int TYPE_EXECUTE_CLOSING_COMMANDS = 2;
    public static final int TYPE_SET_NAME = 3;
    public static final int TYPE_SET_SKIN = 4;
    public static final int TYPE_SET_INTERACTION_TEXT = 5;
    public static final int TYPE_EXECUTE_OPENING_COMMANDS = 6;

    public long entityRuntimeId;
    public int type;
    public String command;
    public int actionIndex;
    public String sceneName = "";

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        entityRuntimeId = getEntityRuntimeId();
        type = getByte();
        command = getString();
        actionIndex = getByte();
        sceneName = getString();
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, NPCRequestPacket.class);

        NPCRequestPacket packet = (NPCRequestPacket) pk;
        this.entityRuntimeId = packet.entityRuntimeId;
        this.type = packet.type;
        this.command = packet.command;
        this.actionIndex = packet.actionIndex;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return NPCRequestPacket.class;
    }
}
