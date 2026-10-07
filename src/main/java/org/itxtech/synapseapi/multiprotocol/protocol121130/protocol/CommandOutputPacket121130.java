package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.command.data.CommandOutputType;
import cn.nukkit.network.protocol.CommandOutputPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.CommandOriginData;
import cn.nukkit.network.protocol.types.CommandOriginData.Origin;
import cn.nukkit.network.protocol.types.CommandOutputMessage;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class CommandOutputPacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.COMMAND_OUTPUT_PACKET;

    public CommandOriginData originData;
    public CommandOutputType outputType = CommandOutputType.NONE;
    public int successCount;
    public CommandOutputMessage[] messages;
    @Nullable
    public String data;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();

        this.putEnum(originData.type, origin -> origin.getNameOrDefault(Origin.PLAYER));
        this.putUUID(originData.uuid);
        this.putString(originData.requestId);
        this.putLLong(originData.playerEntityUniqueId);

        this.putEnum(this.outputType, CommandOutputType::getName);
        this.putLInt(this.successCount);

        this.putUnsignedVarInt(this.messages.length);
        for (CommandOutputMessage message : this.messages) {
            this.putString(message.messageId);
            this.putBoolean(message.successful);

            this.putUnsignedVarInt(message.parameters.length);
            for (String parameter : message.parameters) {
                this.putString(parameter);
            }
        }

        this.putOptional(this.data, BinaryStream::putString);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        CommandOutputPacket packet = (CommandOutputPacket) pk;
        this.originData = packet.originData;
        this.outputType = packet.outputType;
        this.successCount = packet.successCount;
        this.messages = packet.messages;
        this.data = packet.data;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return CommandOutputPacket.class;
    }
}
