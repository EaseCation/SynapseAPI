package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.command.data.*;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.*;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class AvailableCommandsPacket16 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.AVAILABLE_COMMANDS_PACKET;

    public static final int ARG_FLAG_VALID = 0x100000;

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

    public static final int ARG_FLAG_ENUM = 0x200000;
    public static final int ARG_FLAG_POSTFIX = 0x1000000;

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
    public final List<CommandEnum> softEnums = new ArrayList<>();

    public List<String> enumValues = new ArrayList<>();
    private int enumValuesCount = 0;
    public List<String> postfixes = new ArrayList<>();
    public List<CommandEnum> enums = new ArrayList<>();
    private Map<String, Integer> enumMap = new LinkedHashMap<>();

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {

    }

    protected void putEnum(CommandEnum e) {
        this.putString(e.getName());

        this.putUnsignedVarInt(e.getValues().size());
        for (String value : e.getValues()) {
            if (this.enumValues.contains(value)) {
                int index = this.enumValues.indexOf(value);
                if (index < 0) {
                    throw new RuntimeException("Enum value " + value + " not found");
                }
                this.putEnumValueIndex(index);
            } else {
                throw new RuntimeException("Enum value " + value + " not found");
            }
        }
    }

    protected void putSoftEnum(CommandEnum e) {
        this.putString(e.getName());

        this.putUnsignedVarInt(e.getValues().size());
        for (String softEnum : e.getValues()) {
            this.putString(softEnum);
        }
    }

    protected void putEnumValueIndex(int index) {
        if (this.enumValuesCount <= 256) {
            this.putByte((byte) index);
        } else if (this.enumValuesCount <= 65536) {
            this.putLShort(index);
        } else {
            this.putLInt(index);
        }
    }

    protected void putCommandData(String name, CommandData data){
        this.putString(name);
        this.putString(data.description);
        this.putByte((byte) data.flags);
        this.putByte((byte) data.permission);
        if(data.aliases != null && this.enumMap.containsKey(data.aliases.getName())){
            this.putLInt(this.enumMap.get(data.aliases.getName()));
        }else{
            this.putLInt(-1);
        }

        this.putUnsignedVarInt(data.overloads.size());
        data.overloads.forEach((n, overload) -> {
            this.putUnsignedVarInt(overload.input.parameters.length);
            for (CommandParameter parameter : overload.input.parameters) {
                this.putString(parameter.name);
                int type;
                if(parameter.enumData != null) {
                    int t = this.enumMap.getOrDefault(parameter.enumData.getName(), -1);
                    type = ARG_FLAG_ENUM | ARG_FLAG_VALID | t;
                } else if (parameter.postFix != null){
                    int key = this.postfixes.indexOf(parameter.postFix);
                    if(key == -1) {
                        throw new RuntimeException("Postfix " + parameter.postFix + " not in postfixes array");
                    }
                    type = ARG_FLAG_POSTFIX | key;
                } else {
                    type = v12To16ArgTypeTable.getOrDefault(parameter.type, ARG_TYPE_INT);
                }
                this.putLInt(type);
                this.putBoolean(parameter.optional);
            }
        });
    }

    @Override
    public void encode() {
        this.reset();
        List<String> enumValues = new ArrayList<>();
        List<String> postfixes = new ArrayList<>();
        Map<String, CommandEnum> enumMap = new LinkedHashMap<>();
        this.commands.forEach((name, data) -> {
            for (CommandData commandData : data.versions) {
                if (commandData.aliases != null) {
                    enumMap.put(commandData.aliases.getName(), commandData.aliases);
                    enumValues.addAll(commandData.aliases.getValues());
                }
                commandData.overloads.forEach((n, overload) -> {
                    for (CommandParameter parameter : overload.input.parameters) {
                        if (parameter.enumData != null) {
                            enumMap.put(parameter.enumData.getName(), parameter.enumData);
                            enumValues.addAll(parameter.enumData.getValues());
                        }
                        if (parameter.postFix != null) {
                            postfixes.add(parameter.postFix);
                        }
                    }
                });
            }
        });

        this.enumValues = enumValues;
        this.putUnsignedVarInt(this.enumValuesCount = this.enumValues.size());
        for (String enumValue : this.enumValues) {
            this.putString(enumValue);
        }

        this.postfixes = postfixes;
        this.putUnsignedVarInt(this.postfixes.size());
        for (String postfix : this.postfixes) {
            this.putString(postfix);
        }

        this.enums = new ArrayList<>(enumMap.values());
        this.enumMap = new LinkedHashMap<>();
        List<String> enumNameList = new ArrayList<>(enumMap.keySet());
        for (int i = 0; i < enumNameList.size(); i++) {
            this.enumMap.put(enumNameList.get(i), i);
        }
        this.putUnsignedVarInt(this.enums.size());
        for (CommandEnum e : this.enums) {
            this.putEnum(e);
        }

        this.putUnsignedVarInt(this.commands.size());
        this.commands.forEach((name, data) -> {
            this.putCommandData(name, data.versions.get(0));  //TODO 有问题。。
        });

        this.putUnsignedVarInt(this.softEnums.size());
        for (CommandEnum softEnum : this.softEnums) {
            this.putSoftEnum(softEnum);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.AvailableCommandsPacket.class);

        cn.nukkit.network.protocol.AvailableCommandsPacket packet = (cn.nukkit.network.protocol.AvailableCommandsPacket) pk;

        this.commands = packet.commands;
        this.softEnums.addAll(packet.softEnums);

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AvailableCommandsPacket.class;
    }
}
