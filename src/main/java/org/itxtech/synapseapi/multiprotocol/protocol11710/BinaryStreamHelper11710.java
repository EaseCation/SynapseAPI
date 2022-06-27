package org.itxtech.synapseapi.multiprotocol.protocol11710;

import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParamOption;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.AvailableCommandsPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol117.BinaryStreamHelper117;

import java.util.List;
import java.util.Map;

public class BinaryStreamHelper11710 extends BinaryStreamHelper117 {

    public static BinaryStreamHelper11710 create() {
        return new BinaryStreamHelper11710();
    }

    @Override
    public String getGameVersion() {
        return "1.17.10";
    }

    @Override
    public void putCommandData(BinaryStream stream, Map<String, CommandDataVersions> commands, List<CommandEnum> enums, List<String> postFixes, List<CommandEnum> softEnums) {
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
            stream.putByte((byte) data.permission.ordinal());

            stream.putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            stream.putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                stream.putUnsignedVarInt(overload.input.parameters.length);

                for (CommandParameter parameter : overload.input.parameters) {
                    stream.putString(parameter.name);

                    int type = 0;
                    int translatedType = this.getCommandParameterTypeId(parameter.type, AvailableCommandsPacket113.ARG_TYPE_INT);
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }
                        type = AvailableCommandsPacket113.ARG_FLAG_POSTFIX | i;
                    } else {
                        type |= AvailableCommandsPacket113.ARG_FLAG_VALID;
                        if (parameter.enumData != null) {
                            if (parameter.enumData.isSoft()) {
                                type |= AvailableCommandsPacket113.ARG_FLAG_SOFT_ENUM | softEnums.indexOf(parameter.enumData);
                            } else {
                                type |= AvailableCommandsPacket113.ARG_FLAG_ENUM | enums.indexOf(parameter.enumData);
                            }
                        } else {
                            type |= translatedType;
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
