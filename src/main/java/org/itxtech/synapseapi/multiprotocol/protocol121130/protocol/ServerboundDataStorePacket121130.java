package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.DataStoreEntry;

/**
 * Applies a single update to the server data store from the client.
 */
@ToString
public class ServerboundDataStorePacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_DATA_STORE_PACKET;

    public DataStoreEntry update;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        String name = getString();
        String property = getString();
        String path = getString();
        double doubleData = 0;
        boolean boolData = false;
        String stringData = "";
        int dataType = (int) getUnsignedVarInt();
        switch (dataType) {
            case DataStoreEntry.TYPE_DOUBLE -> doubleData = getLDouble();
            case DataStoreEntry.TYPE_BOOL -> boolData = getBoolean();
            case DataStoreEntry.TYPE_STRING -> stringData = getString();
        }
        int updateCount = (int) getUnsignedVarInt();

        update = switch (dataType) {
            case DataStoreEntry.TYPE_DOUBLE -> DataStoreEntry.update(name, property, path, doubleData, updateCount);
            case DataStoreEntry.TYPE_BOOL -> DataStoreEntry.update(name, property, path, boolData, updateCount);
            case DataStoreEntry.TYPE_STRING -> DataStoreEntry.update(name, property, path, stringData, updateCount);
            default -> null;
        };
    }

    @Override
    public void encode() {
    }
}
