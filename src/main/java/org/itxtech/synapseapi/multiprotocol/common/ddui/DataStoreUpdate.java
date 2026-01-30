package org.itxtech.synapseapi.multiprotocol.common.ddui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataStoreUpdate implements DataStoreAction {
    public static final int TYPE_DOUBLE = 0;
    public static final int TYPE_BOOL = 1;
    public static final int TYPE_STRING = 2;

    public String name;
    /**
     * The name of the property in the data store that has changed
     */
    public String property;
    /**
     * The path through the property that needs updating.
     */
    public String path;

    public int dataType;
    public double doubleData;
    public boolean boolData;
    public String stringData = "";

    /**
     * The number of times this property has updated.
     * Does not increment unless the entire property updates.
     */
    public int propertyUpdateCount;
    /**
     * The number of times this path has updated.
     * Resets if the entire property has changed.
     * @since 1.26.0
     */
    public int pathUpdateCount;

    @Override
    public DataStoreActionType getActionType() {
        return DataStoreActionType.UPDATE;
    }

    public static DataStoreUpdate create(String name, String property, String path, double data, int updateCount) {
        return create(name, property, path, data, updateCount, updateCount);
    }

    public static DataStoreUpdate create(String name, String property, String path, double data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreUpdate(name, property, path, TYPE_DOUBLE, data, false, "", propertyUpdateCount, pathUpdateCount);
    }

    public static DataStoreUpdate create(String name, String property, String path, boolean data, int updateCount) {
        return create(name, property, path, data, updateCount, updateCount);
    }

    public static DataStoreUpdate create(String name, String property, String path, boolean data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreUpdate(name, property, path, TYPE_BOOL, 0, data, "", propertyUpdateCount, pathUpdateCount);
    }

    public static DataStoreUpdate create(String name, String property, String path, String data, int updateCount) {
        return create(name, property, path, data, updateCount, updateCount);
    }

    public static DataStoreUpdate create(String name, String property, String path, String data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreUpdate(name, property, path, TYPE_STRING, 0, false, data, propertyUpdateCount, pathUpdateCount);
    }
}
