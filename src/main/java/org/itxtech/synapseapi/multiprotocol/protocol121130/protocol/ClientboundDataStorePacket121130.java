package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.ddui.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sends a list of data store properties from the server to the client.
 */
@ToString
public class ClientboundDataStorePacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_STORE_PACKET;

    public DataStoreAction[] updates;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        putArray(updates, (stream, action) -> {
            DataStoreActionType type = action.getActionType();
            stream.putUnsignedVarInt(type.ordinal());
            switch (type) {
                case UPDATE -> {
                    DataStoreUpdate update = (DataStoreUpdate) action;
                    stream.putString(update.name);
                    stream.putString(update.property);
                    stream.putString(update.path);

                    int dataType = update.dataType;
                    stream.putUnsignedVarInt(dataType);
                    switch (dataType) {
                        case DataStoreUpdate.TYPE_DOUBLE -> stream.putLDouble(update.doubleData);
                        case DataStoreUpdate.TYPE_BOOL -> stream.putBoolean(update.boolData);
                        case DataStoreUpdate.TYPE_STRING -> stream.putString(update.stringData);
                        default -> throw new IllegalArgumentException("invalid data type: " + dataType);
                    }

                    stream.putLInt(update.propertyUpdateCount);
                }
                case CHANGE -> {
                    DataStoreChange change = (DataStoreChange) action;
                    stream.putString(change.name);
                    stream.putString(change.property);
                    stream.putLInt(change.propertyUpdateCount);
                    putDynamicValue(stream, change.dynamicValue);
                }
                case REMOVAL -> {
                    DataStoreRemoval removal = (DataStoreRemoval) action;
                    stream.putString(removal.name);
                }
            }
        });
    }

    private static void putDynamicValue(BinaryStream stream, @Nullable Object value) {
        switch (value) {
            case null -> stream.putLInt(DataStoreChange.TYPE_NULL);
            case Boolean bool -> {
                stream.putLInt(DataStoreChange.TYPE_BOOLEAN);
                stream.putBoolean(bool);
            }
            case Float f -> {
                stream.putLInt(DataStoreChange.TYPE_DOUBLE);
                stream.putLDouble(f.doubleValue());
            }
            case Double d -> {
                stream.putLInt(DataStoreChange.TYPE_DOUBLE);
                stream.putLDouble(d);
            }
            case Number number -> {
                stream.putLInt(DataStoreChange.TYPE_LONG);
                stream.putLLong(number.longValue());
            }
            case String string -> {
                stream.putLInt(DataStoreChange.TYPE_STRING);
                stream.putString(string);
            }
            case List<?> list -> {
                stream.putLInt(DataStoreChange.TYPE_LIST);
                putList(stream, list);
            }
            case Map<?, ?> map -> {
                stream.putLInt(DataStoreChange.TYPE_MAP);
                putMap(stream, (Map<String, ?>) map);
            }
            default -> throw new IllegalArgumentException("unknown dynamic value: " + value.getClass().getSimpleName());
        }
    }

    private static void putList(BinaryStream stream, List<?> list) {
        stream.putUnsignedVarInt(list.size());
        for (Object element : list) {
            putDynamicValue(stream, element);
        }
    }

    private static void putMap(BinaryStream stream, Map<String, ?> map) {
        stream.putUnsignedVarInt(map.size());
        for (Entry<String, ?> entry : map.entrySet()) {
            stream.putString(entry.getKey());
            putDynamicValue(stream, entry.getValue());
        }
    }
}
