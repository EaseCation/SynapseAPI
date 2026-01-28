package org.itxtech.synapseapi.multiprotocol.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataStoreEntry {
    public static final int ACTION_UPDATE = 0;
    public static final int ACTION_CHANGE = 1;
    public static final int ACTION_REMOVAL = 2;

    public static final int TYPE_DOUBLE = 0;
    public static final int TYPE_BOOL = 1;
    public static final int TYPE_STRING = 2;

    public int action = ACTION_UPDATE;
    public String name;
    /**
     * The name of the property in the data store that has changed
     */
    public String property = "";
    /**
     * The path through the property that needs updating.
     */
    public String path = "";
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
     */
    public int pathUpdateCount;

    public DataStoreEntry(String name) {
        this.action = ACTION_REMOVAL;
        this.name = name;
    }

    public static DataStoreEntry update(String name, String property, String path, double data, int updateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_DOUBLE, data, false, "", updateCount, updateCount);
    }

    public static DataStoreEntry update(String name, String property, String path, boolean data, int updateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_BOOL, 0, data, "", updateCount, updateCount);
    }

    public static DataStoreEntry update(String name, String property, String path, String data, int updateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_STRING, 0, false, data, updateCount, updateCount);
    }

    public static DataStoreEntry update(String name, String property, String path, double data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_DOUBLE, data, false, "", propertyUpdateCount, pathUpdateCount);
    }

    public static DataStoreEntry update(String name, String property, String path, boolean data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_BOOL, 0, data, "", propertyUpdateCount, pathUpdateCount);
    }

    public static DataStoreEntry update(String name, String property, String path, String data, int propertyUpdateCount, int pathUpdateCount) {
        return new DataStoreEntry(ACTION_UPDATE, name, property, path, TYPE_STRING, 0, false, data, propertyUpdateCount, pathUpdateCount);
    }

    public static DataStoreEntry change(String name, String property, double data, int updateCount) {
        return new DataStoreEntry(ACTION_CHANGE, name, property, "", TYPE_DOUBLE, data, false, "", updateCount, 0);
    }

    public static DataStoreEntry change(String name, String property, boolean data, int updateCount) {
        return new DataStoreEntry(ACTION_CHANGE, name, property, "", TYPE_BOOL, 0, data, "", updateCount, 0);
    }

    public static DataStoreEntry change(String name, String property, String data, int updateCount) {
        return new DataStoreEntry(ACTION_CHANGE, name, property, "", TYPE_STRING, 0, false, data, updateCount, 0);
    }

    public static DataStoreEntry removal(String name) {
        return new DataStoreEntry(name);
    }
}
