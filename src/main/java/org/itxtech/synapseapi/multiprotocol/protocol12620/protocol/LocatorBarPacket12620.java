package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12610.protocol.LocatorBarPacket12610.Waypoint;

import java.awt.*;

/**
 * Syncs LocatorBar changes on the server with the client.
 */
@ToString
public class LocatorBarPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.LOCATOR_BAR_PACKET;

    public Waypoint[] waypoints;

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
        putArray(waypoints, (stream, waypoint) -> {
            stream.putUUID(waypoint.groupHandle);

            stream.putLInt(waypoint.updateFlag);
            stream.putOptional(waypoint.visible, BinaryStream::putBoolean);
            stream.putOptional(waypoint.dimensionLocation, (bs, location) -> {
                bs.putVector3f(location.x, location.y, location.z);
                bs.putVarInt(location.dimension);
            });
            stream.putOptional(waypoint.texturePath, BinaryStream::putString);
            stream.putOptional(waypoint.iconSize, BinaryStream::putVector2f);
            stream.putOptional(waypoint.color, (bs, color) -> bs.putLInt(color.getRGB()));
            stream.putOptional(waypoint.clientPositionAuthority, BinaryStream::putBoolean);
            stream.putOptional(waypoint.entityUniqueId, BinaryStream::putEntityUniqueId);

            stream.putByte(waypoint.action);
        });
    }
}
