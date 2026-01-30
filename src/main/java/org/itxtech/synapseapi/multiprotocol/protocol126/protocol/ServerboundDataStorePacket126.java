package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.ddui.DataStoreUpdate;

/**
 * Applies a single update to the server data store from the client.
 */
@ToString
public class ServerboundDataStorePacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_DATA_STORE_PACKET;

    public DataStoreUpdate update;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        String name = getString();
        if (name.length() > 1000) {
            throw new IndexOutOfBoundsException("string too long");
        }
        String property = getString();
        if (property.length() > 1000) {
            throw new IndexOutOfBoundsException("string too long");
        }
        String path = getString();
        if (path.length() > 1000) {
            throw new IndexOutOfBoundsException("string too long");
        }

        double doubleData = 0;
        boolean boolData = false;
        String stringData = "";
        int dataType = getLInt();
        switch (dataType) {
            case DataStoreUpdate.TYPE_DOUBLE -> doubleData = getLDouble();
            case DataStoreUpdate.TYPE_BOOL -> boolData = getBoolean();
            case DataStoreUpdate.TYPE_STRING -> {
                stringData = getString();
                if (stringData.length() > 5000) {
                    throw new IndexOutOfBoundsException("string too long");
                }
            }
            default -> throw new IllegalArgumentException("invalid data type: " + dataType);
        }

        int propertyUpdateCount = getLInt();
        int pathUpdateCount = getLInt();

        update = switch (dataType) {
            case DataStoreUpdate.TYPE_DOUBLE -> DataStoreUpdate.create(name, property, path, doubleData, propertyUpdateCount, pathUpdateCount);
            case DataStoreUpdate.TYPE_BOOL -> DataStoreUpdate.create(name, property, path, boolData, propertyUpdateCount, pathUpdateCount);
            case DataStoreUpdate.TYPE_STRING -> DataStoreUpdate.create(name, property, path, stringData, propertyUpdateCount, pathUpdateCount);
            default -> null;
        };
    }

    @Override
    public void encode() {
    }
}
