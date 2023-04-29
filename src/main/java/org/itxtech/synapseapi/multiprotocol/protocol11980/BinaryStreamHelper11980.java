package org.itxtech.synapseapi.multiprotocol.protocol11980;

import cn.nukkit.command.data.CommandParamType;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol11970.BinaryStreamHelper11970;

@Log4j2
public class BinaryStreamHelper11980 extends BinaryStreamHelper11970 {
    public static BinaryStreamHelper11980 create() {
        return new BinaryStreamHelper11980();
    }

    @Override
    public String getGameVersion() {
        return "1.19.80";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 3;
        int ARG_TYPE_VALUE = 4;
        int ARG_TYPE_WILDCARD_INT = 5;
        int ARG_TYPE_OPERATOR = 6;
        int ARG_TYPE_COMPARE_OPERATOR = 7;
        int ARG_TYPE_TARGET = 8;
        int ARG_TYPE_WILDCARD_TARGET = 10;
        int ARG_TYPE_FILE_PATH = 17;
        int ARG_TYPE_INTEGER_RANGE = 23;
        int ARG_TYPE_EQUIPMENT_SLOT = 43;
        int ARG_TYPE_STRING = 44;
        int ARG_TYPE_BLOCK_POSITION = 52;
        int ARG_TYPE_POSITION = 53;
        int ARG_TYPE_MESSAGE = 55;
        int ARG_TYPE_RAWTEXT = 58;
        int ARG_TYPE_JSON = 62;
        int ARG_TYPE_BLOCK_STATES = 71;
        int ARG_TYPE_COMMAND = 74;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.COMPARE_OPERATOR, ARG_TYPE_COMPARE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.WILDCARD_TARGET, ARG_TYPE_WILDCARD_TARGET);
        this.registerCommandParameterType(CommandParamType.EQUIPMENT_SLOT, ARG_TYPE_EQUIPMENT_SLOT);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_POSITION, ARG_TYPE_BLOCK_POSITION);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
        this.registerCommandParameterType(CommandParamType.FILE_PATH, ARG_TYPE_FILE_PATH);
        this.registerCommandParameterType(CommandParamType.INTEGER_RANGE, ARG_TYPE_INTEGER_RANGE);
        this.registerCommandParameterType(CommandParamType.BLOCK_STATES, ARG_TYPE_BLOCK_STATES);
    }
}
