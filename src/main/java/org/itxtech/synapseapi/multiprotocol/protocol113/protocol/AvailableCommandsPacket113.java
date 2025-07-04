package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.command.data.*;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.SequencedHashSet;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class AvailableCommandsPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.AVAILABLE_COMMANDS_PACKET;

    private static final ObjIntConsumer<BinaryStream> WRITE_BYTE = (s, v) -> s.putByte((byte) v);
    private static final ObjIntConsumer<BinaryStream> WRITE_SHORT = BinaryStream::putLShort;
    private static final ObjIntConsumer<BinaryStream> WRITE_INT = BinaryStream::putLInt;

    public static final int ARG_FLAG_VALID = 0x100000;
    public static final int ARG_FLAG_ENUM = 0x200000;
    /**
     * It can only be applied to integer parameters.
     */
    public static final int ARG_FLAG_POSTFIX = 0x1000000;
    /**
     * List of dynamic command enums, also referred to as "soft" enums.
     * These can be dynamically updated mid-game without resending this packet. (UpdateSoftEnumPacket)
     */
    public static final int ARG_FLAG_SOFT_ENUM = 0x4000000;

    public static final int ARG_TYPE_INT = 1;
    public static final int ARG_TYPE_FLOAT = 3;
    public static final int ARG_TYPE_VALUE = 4;
    public static final int ARG_TYPE_WILDCARD_INT = 5;
    public static final int ARG_TYPE_OPERATOR = 6;
    public static final int ARG_TYPE_COMPARE_OPERATOR = 7;
    public static final int ARG_TYPE_TARGET = 8;
    public static final int ARG_TYPE_WILDCARD_TARGET = 10;
    public static final int ARG_TYPE_FILE_PATH = 17;
    public static final int ARG_TYPE_FULL_INTEGER_RANGE = 23;
    public static final int ARG_TYPE_EQUIPMENT_SLOT = 38;
    public static final int ARG_TYPE_STRING = 39;
    public static final int ARG_TYPE_BLOCK_POSITION = 47;
    public static final int ARG_TYPE_POSITION = 48;
    public static final int ARG_TYPE_MESSAGE = 51;
    public static final int ARG_TYPE_RAWTEXT = 53;
    public static final int ARG_TYPE_JSON = 57;
    public static final int ARG_TYPE_BLOCK_STATES = 67;
    public static final int ARG_TYPE_COMMAND = 70;

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

        List<String> enumValues = new SequencedHashSet<>();
        List<String> postFixes = new SequencedHashSet<>();
        List<CommandEnum> enums = new SequencedHashSet<>();
        List<CommandEnum> softEnums = new SequencedHashSet<>();
        List<LongObjectPair<Set<CommandEnumConstraint>>> enumConstraints = new SequencedHashSet<>();

        // List of enums which aren't directly referenced by any vanilla command.
        // This is used for the "CommandName" enum, which is a magic enum used by the "command" argument type.
        Set<String> commandNames = new HashSet<>(commands.keySet());
        commandNames.add("help");
        commandNames.add("?");

        commands.forEach((name, data) -> {
            CommandData cmdData = data.versions.get(0);

            if (cmdData.aliases != null) {
                enums.add(cmdData.aliases);

                enumValues.addAll(cmdData.aliases.getValues().keySet());

                commandNames.addAll(cmdData.aliases.getValues().keySet());
            }

            for (CommandOverload overload : cmdData.overloads.values()) {
                for (CommandParameter parameter : overload.input.parameters) {
                    if (parameter.enumData != null) {
                        if (parameter.enumData.isSoft()) {
                            softEnums.add(parameter.enumData);
                        } else {
                            enums.add(parameter.enumData);

                            int enumIndex = enums.indexOf(parameter.enumData);
                            parameter.enumData.getValues().forEach((enumValue, constraints) -> {
                                enumValues.add(enumValue);

                                if (!constraints.isEmpty()) {
                                    enumConstraints.add(LongObjectPair.of(((long) enumValues.indexOf(enumValue) << 32) | (enumIndex & 0xffffffffL), constraints));
                                }
                            });
                        }
                    }

                    if (parameter.postFix != null) {
                        postFixes.add(parameter.postFix);
                    }
                }
            }
        });

        enums.add(new CommandEnum("CommandName", commandNames));
        enumValues.addAll(commandNames);

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
            this.putUnsignedVarInt(values.size());
            for (String val : values) {
                int i = enumValues.indexOf(val);

                if (i < 0) {
                    throw new IllegalStateException("Enum value '" + val + "' not found");
                }

                indexWriter.accept(this, i);
            }
        });

        this.helper.putCommandData(this, this.commands, enums, postFixes, softEnums);

        this.putUnsignedVarInt(softEnums.size());
        softEnums.forEach(cmdEnum -> {
            this.putString(cmdEnum.getName());

            Set<String> values = cmdEnum.getValues().keySet();
            this.putUnsignedVarInt(values.size());
            values.forEach(this::putString);
        });

        this.putUnsignedVarInt(enumConstraints.size());
        for (LongObjectPair<Set<CommandEnumConstraint>> pair : enumConstraints) {
            this.putLInt((int) (pair.leftLong() >> 32)); // enum value index
            this.putLInt((int) pair.leftLong()); // enum index

            this.putUnsignedVarInt(pair.right().size());
            for (CommandEnumConstraint constraint : pair.right()) {
                this.putByte((byte) constraint.ordinal());
            }
        }
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
