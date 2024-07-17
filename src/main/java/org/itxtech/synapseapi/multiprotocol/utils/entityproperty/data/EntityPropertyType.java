package org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data;

public enum EntityPropertyType {

    INT(0, EntityPropertyDataInt.class),
    FLOAT(1, EntityPropertyDataFloat.class),
    BOOL(2, EntityPropertyDataBool.class),
    ENUM(3, EntityPropertyDataEnum.class);

    private final int type;
    private final Class<? extends EntityPropertyData> dataClass;

    EntityPropertyType(int type, Class<? extends EntityPropertyData> dataClass) {
        this.type = type;
        this.dataClass = dataClass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends EntityPropertyData> getDataClass() {
        return dataClass;
    }
}
