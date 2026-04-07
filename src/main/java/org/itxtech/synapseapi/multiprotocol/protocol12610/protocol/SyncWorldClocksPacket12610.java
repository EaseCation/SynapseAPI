package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.clock.*;

/**
 * Initializes and syncs world clocks from the server to clients.
 * Sent from the server when a client joins to initialize all world clocks for the client and periodically to all clients to keep them in sync.
 * It is also sent to all clients when a world clock's paused state changes or when time markers are added or removed.
 */
@ToString
public class SyncWorldClocksPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.SYNC_WORLD_CLOCKS_PACKET;

    public SyncClockAction action;

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

        SyncClockActionType type = action.getType();
        putUnsignedVarInt(type.ordinal());
        switch (type) {
            case SYNC_STATE -> {
                putArray(((SyncStateAction) action).data, (stream, entry) -> {
                    stream.putUnsignedVarLong(entry.id);
                    stream.putVarInt(entry.time);
                    stream.putBoolean(entry.paused);
                });
            }
            case INITIALIZE_REGISTRY -> {
                putArray(((InitializeRegistryAction) action).data, (stream, entry) -> {
                    stream.putUnsignedVarLong(entry.id);
                    stream.putString(entry.name);
                    stream.putVarInt(entry.time);
                    stream.putBoolean(entry.paused);
                    stream.putArray(entry.markers, SyncWorldClocksPacket12610::putTimeMarker);
                });
            }
            case ADD_TIME_MARKER -> {
                AddTimeMarkerAction add = (AddTimeMarkerAction) action;
                putUnsignedVarLong(add.id);
                putArray(add.markers, SyncWorldClocksPacket12610::putTimeMarker);
            }
            case REMOVE_TIME_MARKER -> {
                RemoveTimeMarkerAction remove = (RemoveTimeMarkerAction) action;
                putUnsignedVarLong(remove.id);

                putUnsignedVarInt(remove.markerIds.length);
                for (long id : remove.markerIds) {
                    putUnsignedVarLong(id);
                }
            }
        }
    }

    private static void putTimeMarker(BinaryStream stream, TimeMarker marker) {
        stream.putUnsignedVarLong(marker.id);
        stream.putString(marker.name);
        stream.putVarInt(marker.time);
        stream.putOptional(marker.period, BinaryStream::putLInt);
    }
}
