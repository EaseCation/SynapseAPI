package org.itxtech.synapseapi.multiprotocol.protocol113;

import cn.nukkit.command.data.CommandData;
import cn.nukkit.command.data.CommandDataVersions;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandOverload;
import cn.nukkit.command.data.CommandParamOption;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol112.BinaryStreamHelper112;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.AvailableCommandsPacket113;

import java.util.List;
import java.util.Map;

//todo: skin
public class BinaryStreamHelper113 extends BinaryStreamHelper112 {

    public static BinaryStreamHelper113 create() {
        return new BinaryStreamHelper113();
    }

    @Override
    public String getGameVersion() {
        return "1.13.0";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 2;
        int ARG_TYPE_VALUE = 3;
        int ARG_TYPE_WILDCARD_INT = 4;
        int ARG_TYPE_OPERATOR = 5;
        int ARG_TYPE_TARGET = 6;
        int ARG_TYPE_WILDCARD_TARGET = 7;
        int ARG_TYPE_FILE_PATH = 14;
        int ARG_TYPE_STRING = 29;
        int ARG_TYPE_POSITION = 37;
        int ARG_TYPE_MESSAGE = 41;
        int ARG_TYPE_RAWTEXT = 43;
        int ARG_TYPE_JSON = 47;
        int ARG_TYPE_COMMAND = 54;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.WILDCARD_TARGET, ARG_TYPE_WILDCARD_TARGET);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
        this.registerCommandParameterType(CommandParamType.FILE_PATH, ARG_TYPE_FILE_PATH);

        this.registerCommandParameterType(CommandParamType.COMPARE_OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.INTEGER_RANGE, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.EQUIPMENT_SLOT, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.BLOCK_STATES, ARG_TYPE_STRING);
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
            stream.putByte((byte) flags);
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
