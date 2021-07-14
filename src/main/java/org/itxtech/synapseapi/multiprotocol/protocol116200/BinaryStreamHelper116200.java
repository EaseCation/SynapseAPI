package org.itxtech.synapseapi.multiprotocol.protocol116200;

import cn.nukkit.command.data.CommandParamType;
import org.itxtech.synapseapi.multiprotocol.protocol116100.BinaryStreamHelper116100;

public class BinaryStreamHelper116200 extends BinaryStreamHelper116100 {

    public static BinaryStreamHelper116200 create() {
        return new BinaryStreamHelper116200();
    }

    @Override
    public String getGameVersion() {
        return "1.16.200";
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
        int ARG_TYPE_FILE_PATH = 15;
        int ARG_TYPE_STRING = 31;
        int ARG_TYPE_BLOCK_POSITION = 39;
        int ARG_TYPE_POSITION = 40;
        int ARG_TYPE_MESSAGE = 43;
        int ARG_TYPE_RAWTEXT = 45;
        int ARG_TYPE_JSON = 49;
        int ARG_TYPE_COMMAND = 56;

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
