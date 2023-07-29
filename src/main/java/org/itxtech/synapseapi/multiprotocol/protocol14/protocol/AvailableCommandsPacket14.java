package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.command.data.*;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.Packet16;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class AvailableCommandsPacket14 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.AVAILABLE_COMMANDS_PACKET;

    public static final int ARG_FLAG_VALID = 0x100000;
    public static final int ARG_FLAG_ENUM = 0x200000;
    public static final int ARG_FLAG_TEMPLATE = 0x01000000;

    public static final int ARG_TYPE_INT = 0x01;
    public static final int ARG_TYPE_FLOAT = 0x02;
    public static final int ARG_TYPE_VALUE = 0x03;
    public static final int ARG_TYPE_WILDCARD_INT = 0x04;
    public static final int ARG_TYPE_TARGET = 0x05;
    public static final int ARG_TYPE_WILDCARD_TARGET = 0x06;

    public static final int ARG_TYPE_STRING = 0x0f;
    public static final int ARG_TYPE_POSITION = 0x10;

    public static final int ARG_TYPE_MESSAGE = 0x13;
    public static final int ARG_TYPE_RAWTEXT = 0x15;
    public static final int ARG_TYPE_JSON = 0x18;
    public static final int ARG_TYPE_COMMAND = 0x1f;

    private static final Map<CommandParamType, Integer> v12To16ArgTypeTable = new HashMap<CommandParamType, Integer>(){{
        put(CommandParamType.INT, ARG_TYPE_INT);
        put(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        put(CommandParamType.VALUE, ARG_TYPE_VALUE);
        put(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        put(CommandParamType.TARGET, ARG_TYPE_TARGET);
        put(CommandParamType.STRING, ARG_TYPE_STRING);
        put(CommandParamType.POSITION, ARG_TYPE_POSITION);
        put(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        put(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        put(CommandParamType.JSON, ARG_TYPE_JSON);
        put(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        put(CommandParamType.COMMAND, ARG_TYPE_COMMAND);

    }};

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

        LinkedHashSet<String> enumValues = new LinkedHashSet<>();
        LinkedHashSet<String> postFixes = new LinkedHashSet<>();
        LinkedHashSet<CommandEnum> enums = new LinkedHashSet<>();

        // List of enums which aren't directly referenced by any vanilla command.
        // This is used for the "CommandName" enum, which is a magic enum used by the "command" argument type.
        Set<String> commandNames = new HashSet<>(commands.keySet());
        commandNames.add("help");
        commandNames.add("?");

        commands.forEach((name, data) -> {
            CommandData cmdData = data.versions.get(0);

            if (cmdData.aliases != null) {
                enums.add(new CommandEnum(cmdData.aliases.getName(), cmdData.aliases.getValues().keySet()));

                enumValues.addAll(cmdData.aliases.getValues().keySet());

                commandNames.addAll(cmdData.aliases.getValues().keySet());
            }

            for (CommandOverload overload : cmdData.overloads.values()) {
                for (CommandParameter parameter : overload.input.parameters) {
                    if (parameter.enumData != null) {
                        enums.add(new CommandEnum(parameter.enumData.getName(), parameter.enumData.getValues().keySet()));

                        enumValues.addAll(parameter.enumData.getValues().keySet());
                    }

                    if (parameter.postFix != null) {
                        postFixes.add(parameter.postFix);
                    }
                }
            }
        });

        enums.add(new CommandEnum("CommandName", commandNames));
        enumValues.addAll(commandNames);

        List<String> enumIndexes = new ArrayList<>(enumValues);
        List<String> enumDataIndexes = enums.stream().map(CommandEnum::getName).toList();
        List<String> fixesIndexes = new ArrayList<>(postFixes);

        this.putUnsignedVarInt(enumValues.size());
        for (String enumValue : enumValues) {
            putString(enumValue);
        }

        this.putUnsignedVarInt(postFixes.size());
        for (String postFix : postFixes) {
            putString(postFix);
        }

        this.putUnsignedVarInt(enums.size());
        enums.forEach((cmdEnum) -> {
            putString(cmdEnum.getName());

            Set<String> values = cmdEnum.getValues().keySet();
            putUnsignedVarInt(values.size());

            for (String val : values) {
                int i = enumIndexes.indexOf(val);

                if (i < 0) {
                    throw new IllegalStateException("Enum value '" + val + "' not found");
                }

                if (enums.size() < 256) {
                    putByte((byte) i);
                } else if (enums.size() < 65536) {
                    putShort(i);
                } else {
                    putLInt(i);
                }
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

            putLInt(data.aliases == null ? -1 : enumDataIndexes.indexOf(data.aliases.getName()));

            putUnsignedVarInt(data.overloads.size());
            for (CommandOverload overload : data.overloads.values()) {
                putUnsignedVarInt(overload.input.parameters.length);

                for (CommandParameter parameter : overload.input.parameters) {
                    putString(parameter.name);

                    int type;
                    int translatedType = v12To16ArgTypeTable.getOrDefault(parameter.type, ARG_TYPE_INT);
                    if (parameter.enumData != null) {
                        type = ARG_FLAG_ENUM | ARG_FLAG_VALID | enumDataIndexes.indexOf(parameter.enumData.getName());
                    } else if (parameter.postFix != null) {
                        int i = fixesIndexes.indexOf(parameter.postFix);
                        if (i < 0) {
                            throw new IllegalStateException("Postfix '" + parameter.postFix + "' isn't in postfix array");
                        }

                        type = (ARG_FLAG_VALID | translatedType) << 24 | i;
                    } else {
                        type = translatedType | ARG_FLAG_VALID;
                    }

                    putLInt(type);
                    putBoolean(parameter.optional);
                }
            }
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
