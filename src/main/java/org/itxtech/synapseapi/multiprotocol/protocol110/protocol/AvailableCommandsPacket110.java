package org.itxtech.synapseapi.multiprotocol.protocol110.protocol;

import cn.nukkit.command.data.*;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class AvailableCommandsPacket110 extends Packet110 {

    public static final byte NETWORK_ID = ProtocolInfo.AVAILABLE_COMMANDS_PACKET;

    private static final ObjIntConsumer<BinaryStream> WRITE_BYTE = (s, v) -> s.putByte((byte) v);
    private static final ObjIntConsumer<BinaryStream> WRITE_SHORT = BinaryStream::putLShort;
    private static final ObjIntConsumer<BinaryStream> WRITE_INT = BinaryStream::putLInt;

    public static final int ARG_FLAG_VALID = 0x100000;
    public static final int ARG_FLAG_ENUM = 0x200000;
    public static final int ARG_FLAG_POSTFIX = 0x1000000;
    public static final int ARG_FLAG_SOFT_ENUM = 0x4000000;

    public static final int ARG_TYPE_INT = 1;
    public static final int ARG_TYPE_FLOAT = 2;
    public static final int ARG_TYPE_VALUE = 3;
    public static final int ARG_TYPE_WILDCARD_INT = 4;
    public static final int ARG_TYPE_OPERATOR = 5;
    public static final int ARG_TYPE_TARGET = 6;
    public static final int ARG_TYPE_WILDCARD_TARGET = 7;

    public static final int ARG_TYPE_FILE_PATH = 15;

    public static final int ARG_TYPE_INT_RANGE = 19;

    public static final int ARG_TYPE_STRING = 28;
    public static final int ARG_TYPE_POSITION = 30;

    public static final int ARG_TYPE_MESSAGE = 33;
    public static final int ARG_TYPE_RAWTEXT = 35;
    public static final int ARG_TYPE_JSON = 38;
    public static final int ARG_TYPE_COMMAND = 45;

    public Map<String, CommandDataVersions> commands;

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

        LinkedHashSet<String> enumValuesSet = new LinkedHashSet<>();
        LinkedHashSet<String> postFixesSet = new LinkedHashSet<>();
        LinkedHashSet<CommandEnum> enumsSet = new LinkedHashSet<>();
        LinkedHashSet<CommandEnum> softEnumsSet = new LinkedHashSet<>();

        // List of enums which aren't directly referenced by any vanilla command.
        // This is used for the "CommandName" enum, which is a magic enum used by the "command" argument type.
        Set<String> commandNames = new HashSet<>(commands.keySet());
        commandNames.add("help");
        commandNames.add("?");

        commands.forEach((name, data) -> {
            CommandData cmdData = data.versions.get(0);

            if (cmdData.aliases != null) {
                enumsSet.add(cmdData.aliases);

                enumValuesSet.addAll(cmdData.aliases.getValues().keySet());

                commandNames.addAll(cmdData.aliases.getValues().keySet());
            }

            for (CommandOverload overload : cmdData.overloads.values()) {
                for (CommandParameter parameter : overload.input.parameters) {
                    if (parameter.enumData != null) {
                        if (parameter.enumData.isSoft()) {
                            softEnumsSet.add(parameter.enumData);
                        } else {
                            enumsSet.add(parameter.enumData);

                            enumValuesSet.addAll(parameter.enumData.getValues().keySet());
                        }
                    }

                    if (parameter.postFix != null) {
                        postFixesSet.add(parameter.postFix);
                    }
                }
            }
        });

        enumsSet.add(new CommandEnum("CommandName", commandNames));
        enumValuesSet.addAll(commandNames);

        List<String> enumValues = new ArrayList<>(enumValuesSet);
        List<String> postFixes = new ArrayList<>(postFixesSet);
        List<CommandEnum> enums = new ArrayList<>(enumsSet);
        List<CommandEnum> softEnums = new ArrayList<>(softEnumsSet);

        this.putUnsignedVarInt(enumValues.size());
        enumValues.forEach(this::putString);

        this.putUnsignedVarInt(postFixes.size());
        postFixes.forEach(this::putString);

        ObjIntConsumer<BinaryStream> indexWriter;
        if (enumValues.size() < 256) {
            indexWriter = WRITE_BYTE;
        } else if (enumValues.size() < 65536) {
            indexWriter = WRITE_SHORT;
        } else {
            indexWriter = WRITE_INT;
        }

        this.putUnsignedVarInt(enums.size());
        enums.forEach((cmdEnum) -> {
            putString(cmdEnum.getName());

            Set<String> values = cmdEnum.getValues().keySet();
            putUnsignedVarInt(values.size());

            for (String val : values) {
                int i = enumValues.indexOf(val);

                if (i < 0) {
                    throw new IllegalStateException("Enum value '" + val + "' not found");
                }

                indexWriter.accept(this, i);
            }
        });

        putUnsignedVarInt(commands.size());

        commands.forEach((name, cmdData) -> {
            CommandData data = cmdData.versions.get(0);

            putString(name);
            putString(data.description);
            int flags = 0;
            for (CommandFlag flag : data.flags) {
                flags |= 1 << flag.ordinal();
            }
            putByte((byte) flags);
            putByte((byte) data.permission.ordinal());

            putLInt(data.aliases == null ? -1 : enums.indexOf(data.aliases));

            putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                putUnsignedVarInt(overload.input.parameters.length);

                for (CommandParameter parameter : overload.input.parameters) {
                    putString(parameter.name);

                    int type = 0;
                    if (parameter.postFix != null) {
                        int i = postFixes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }
                        type = ARG_FLAG_POSTFIX | i;
                    } else {
                        type |= ARG_FLAG_VALID;
                        if (parameter.enumData != null) {
                            if (parameter.enumData.isSoft()) {
                                type |= ARG_FLAG_SOFT_ENUM | softEnums.indexOf(parameter.enumData);
                            } else {
                                type |= ARG_FLAG_ENUM | enums.indexOf(parameter.enumData);
                            }
                        } else {
                            type |= parameter.type.getId();
                        }
                    }

                    putLInt(type);
                    putBoolean(parameter.optional);
                    int options = 0;
                    for (CommandParamOption option : parameter.options) {
                        options |= 1 << option.ordinal();
                    }
                    putByte((byte) options);
                }
            }
        });

        this.putUnsignedVarInt(softEnums.size());
        softEnums.forEach(cmdEnum -> {
            this.putString(cmdEnum.getName());

            Set<String> values = cmdEnum.getValues().keySet();
            this.putUnsignedVarInt(values.size());
            values.forEach(this::putString);
        });
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.AvailableCommandsPacket.class);

        cn.nukkit.network.protocol.AvailableCommandsPacket packet = (cn.nukkit.network.protocol.AvailableCommandsPacket) pk;

        this.commands = packet.commands;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AvailableCommandsPacket.class;
    }
}
