package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.command.data.CommandVersion;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.CommandOriginData;
import cn.nukkit.network.protocol.types.CommandOriginData.Origin;
import lombok.ToString;

import java.util.UUID;

@ToString
public class CommandRequestPacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.COMMAND_REQUEST_PACKET;

    public String command;
    public CommandOriginData data;
    public boolean internal;
    public CommandVersion version;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.command = this.getString();

        Origin type = this.getEnum(Origin::byName);
        UUID uuid = this.getUUID();
        String requestId = this.getString();
        long playerEntityUniqueId = this.getLLong();
        this.data = new CommandOriginData(type, uuid, requestId, playerEntityUniqueId);

        this.internal = this.getBoolean();
        this.version = this.getEnum(CommandVersion::byName);
    }

    @Override
    public void encode() {
    }
}
