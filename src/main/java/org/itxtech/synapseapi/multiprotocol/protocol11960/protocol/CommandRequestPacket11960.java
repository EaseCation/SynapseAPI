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
    public int version;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.command = this.getString();

        Origin type = Origin.values0()[this.getVarInt()];
        UUID uuid = null;
        try {
            uuid = this.getUUID();
        } catch (Exception e) {
            this.setOffset(this.getOffset() - 16);
        }
        String requestId = this.getString();
        Long varLong = null;
        if (type == Origin.DEV_CONSOLE || type == Origin.TEST) {
            varLong = this.getVarLong();
        }
        this.data = new CommandOriginData(type, uuid, requestId, varLong);

        this.version = this.getVarInt();
    }

    @Override
    public void encode() {
    }
}
