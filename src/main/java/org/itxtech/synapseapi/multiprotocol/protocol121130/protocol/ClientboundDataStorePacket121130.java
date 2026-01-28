package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.DataStoreEntry;

/**
 * Sends a list of data store properties from the server to the client.
 */
@ToString
public class ClientboundDataStorePacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_STORE_PACKET;

    public DataStoreEntry[] updates;

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

        putArray(updates, (stream, entry) -> {
            int action = entry.action;
            stream.putByte(action);
            switch (action) {
                case DataStoreEntry.ACTION_UPDATE -> {
                    stream.putString(entry.name);
                    stream.putString(entry.property);
                    stream.putString(entry.path);
                    putDynamicValue(stream, entry);
                    stream.putUnsignedVarInt(entry.propertyUpdateCount);
                }
                case DataStoreEntry.ACTION_CHANGE -> {
                    stream.putString(entry.name);
                    stream.putString(entry.property);
                    putDynamicValue(stream, entry);
                    stream.putUnsignedVarInt(entry.propertyUpdateCount);
                }
                case DataStoreEntry.ACTION_REMOVAL -> stream.putString(entry.name);
            }
        });
    }

    private static void putDynamicValue(BinaryStream stream, DataStoreEntry entry) {
        int type = entry.dataType;
        stream.putUnsignedVarInt(type);
        switch (type) {
            case DataStoreEntry.TYPE_DOUBLE -> stream.putLDouble(entry.doubleData);
            case DataStoreEntry.TYPE_BOOL -> stream.putBoolean(entry.boolData);
            case DataStoreEntry.TYPE_STRING -> stream.putString(entry.stringData);
        }
    }
}
