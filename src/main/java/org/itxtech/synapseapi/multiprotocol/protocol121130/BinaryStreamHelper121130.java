package org.itxtech.synapseapi.multiprotocol.protocol121130;

import cn.nukkit.command.data.*;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol121124.BinaryStreamHelper121124;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AvailableCommandsPacket121130;

import java.util.List;
import java.util.Map;

public class BinaryStreamHelper121130 extends BinaryStreamHelper121124 {
    public static BinaryStreamHelper121130 create() {
        return new BinaryStreamHelper121130();
    }

    @Override
    public String getGameVersion() {
        return "1.21.130";
    }

    @Override
    public boolean isNetEase() {
        return false;
    }

    @Override
    public void putCommandData(BinaryStream stream, Map<String, CommandDataVersions> commands, List<CommandEnum> enums, List<String> postFixes, List<CommandEnum> softEnums, List<ChainedSubCommandData> chainedSubCommands) {
        stream.putUnsignedVarInt(commands.size());

        commands.forEach((name, cmdData) -> {
            CommandData data = cmdData.versions.get(0);

            stream.putString(name);
            stream.putString(data.description);
            int flags = 0;
            for (CommandFlag flag : data.flags) {
                flags |= 1 << flag.ordinal();
            }
            stream.putLShort(flags);
            stream.putEnum(data.permission, CommandPermission::getName);

            stream.putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            stream.putUnsignedVarInt(data.chainedSubCommandData.size());
            for (ChainedSubCommandData chainedSubCommand : data.chainedSubCommandData.values()) {
                stream.putLInt(chainedSubCommands.indexOf(chainedSubCommand));
            }

            stream.putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                stream.putBoolean(overload.chaining);

                stream.putUnsignedVarInt(overload.input.parameters.length);
                for (CommandParameter parameter : overload.input.parameters) {
                    stream.putString(parameter.name);

                    int type = 0;
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }
                        type = AvailableCommandsPacket121130.ARG_FLAG_POSTFIX | i;
                    } else {
                        type |= AvailableCommandsPacket121130.ARG_FLAG_VALID;
                        if (parameter.enumData != null) {
                            if (parameter.enumData.isSoft()) {
                                type |= AvailableCommandsPacket121130.ARG_FLAG_SOFT_ENUM | softEnums.indexOf(parameter.enumData);
                            } else {
                                type |= AvailableCommandsPacket121130.ARG_FLAG_ENUM | enums.indexOf(parameter.enumData);
                            }
                        } else {
                            type |= this.getCommandParameterTypeId(parameter.type, AvailableCommandsPacket121130.ARG_TYPE_INT);
                        }
                    }

                    stream.putLInt(type);
                    stream.putBoolean(parameter.optional);
                    int options = 0;
                    for (CommandParamOption option : parameter.options) {
                        options |= 1 << option.ordinal();
                    }
                    stream.putByte((byte) options);
                }
            }
        });
    }
}
