package org.itxtech.synapseapi.multiprotocol.protocol11830;

import cn.nukkit.command.data.CommandParamType;
import org.itxtech.synapseapi.multiprotocol.protocol11810.BinaryStreamHelper11810;

public class BinaryStreamHelper11830 extends BinaryStreamHelper11810 {

    public static BinaryStreamHelper11830 create() {
        return new BinaryStreamHelper11830();
    }

    @Override
    public String getGameVersion() {
        return "1.18.30";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 3;
        int ARG_TYPE_VALUE = 4;
        int ARG_TYPE_WILDCARD_INT = 5;
        int ARG_TYPE_OPERATOR = 6;
        int ARG_TYPE_TARGET = 7;
        int ARG_TYPE_WILDCARD_TARGET = 9;
        int ARG_TYPE_FILE_PATH = 16;
        int ARG_TYPE_EQUIPMENT_SLOT = 37;
        int ARG_TYPE_STRING = 38;
        int ARG_TYPE_BLOCK_POSITION = 46;
        int ARG_TYPE_POSITION = 47;
        int ARG_TYPE_MESSAGE = 50;
        int ARG_TYPE_RAWTEXT = 52;
        int ARG_TYPE_JSON = 56;
        int ARG_TYPE_COMMAND = 69;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
    }
}
