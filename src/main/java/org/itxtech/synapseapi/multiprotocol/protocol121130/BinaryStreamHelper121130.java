package org.itxtech.synapseapi.multiprotocol.protocol121130;

import cn.nukkit.command.data.*;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol121124.BinaryStreamHelper121124;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AvailableCommandsPacket121130;
import org.itxtech.synapseapi.multiprotocol.utils.CommandPostfixFixer;

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
    public void putCommandData(BinaryStream stream, Map<String, CommandDataVersions> commands, List<CommandEnum> enums, List<String> postFixes, List<CommandEnum> softEnums, List<ChainedSubCommandData> chainedSubCommands) {
        stream.putUnsignedVarInt(commands.size());

        commands.forEach((name, cmdData) -> {
            CommandData data = cmdData.versions.getFirst();

            stream.putString(name, 1000);
            stream.putString(data.description, 1000);
            int flags = 0;
            for (CommandFlag flag : data.flags) {
                flags |= 1 << flag.ordinal();
            }
            stream.putLShort(flags);
            stream.putEnum(data.permission, CommandPermission::getName);

            stream.putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            stream.putArray(data.chainedSubCommandData.values(), (bs, chainedSubCommand) -> {
                bs.putLInt(chainedSubCommands.indexOf(chainedSubCommand));
            }, 250);

            stream.putArray(data.overloads.values(), (bs, overload) -> {
                bs.putBoolean(overload.chaining);

                bs.putUnsignedVarInt(overload.input.parameters.length);
                for (CommandParameter parameter : overload.input.parameters) {
                    bs.putString(parameter.name);

                    int type = 0;
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(CommandPostfixFixer.normalizePostfix(parameter.postFix));
                        // The compatibility layout omits suffixes beyond the verified native mapping slots.
                        // Advertise those parameters as strings instead of forming an invalid postfix symbol.
                        // The server still receives the original input;
                        // only client-side integer-suffix validation/completion is relaxed.
                        // Resolve STRING through this helper;
                        // legacy packet constants can differ from the active version's registered command parameter type IDs.
                        type = i >= 0 ? AvailableCommandsPacket121130.ARG_FLAG_POSTFIX | i
                                : AvailableCommandsPacket121130.ARG_FLAG_VALID | getCommandParameterTypeId(CommandParamType.STRING, AvailableCommandsPacket121130.ARG_TYPE_STRING);
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

                    bs.putLInt(type);
                    bs.putBoolean(parameter.optional);
                    int options = 0;
                    for (CommandParamOption option : parameter.options) {
                        options |= 1 << option.ordinal();
                    }
                    bs.putByte((byte) options);
                }
            }, 250);
        });
    }
}
