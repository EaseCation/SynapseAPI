package org.itxtech.synapseapi.multiprotocol.protocol11960.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.CommandOriginData;
import cn.nukkit.network.protocol.types.CommandOriginData.Origin;
import lombok.ToString;

import java.util.UUID;

@ToString
public class CommandRequestPacket11960 extends Packet11960 {
    public static final int NETWORK_ID = ProtocolInfo.COMMAND_REQUEST_PACKET;

    public String command;
    public CommandOriginData data;
    public boolean internal;
    public int version;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.command = this.getString();

        Origin type = Origin.getValues()[(int) this.getUnsignedVarInt()];
        UUID uuid = this.getUUID();
        String requestId = this.getString();
        long playerEntityUniqueId = 0;
        if (type == Origin.DEV_CONSOLE || type == Origin.TEST) {
            playerEntityUniqueId = this.getEntityUniqueId();
        }
        this.data = new CommandOriginData(type, uuid, requestId, playerEntityUniqueId);

        this.internal = this.getBoolean();
        this.version = this.getVarInt();
    }

    @Override
    public void encode() {
    }
}
