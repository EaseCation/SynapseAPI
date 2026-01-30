package org.itxtech.synapseapi.multiprotocol.common.ddui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataStoreChange implements DataStoreAction {
    public static final int TYPE_NULL = 0;
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_STRING = 4;
    public static final int TYPE_LIST = 5;
    public static final int TYPE_MAP = 6;

    public String name;
    /**
     * The name of the property in the data store that has changed
     */
    public String property;
    /**
     * The number of times this property has updated.
     * Does not increment unless the entire property updates.
     */
    public int propertyUpdateCount;
    @Nullable
    public Object dynamicValue;

    @Override
    public DataStoreActionType getActionType() {
        return DataStoreActionType.CHANGE;
    }
}
