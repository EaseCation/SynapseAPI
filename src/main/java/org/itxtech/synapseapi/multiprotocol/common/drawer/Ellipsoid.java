package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents an ellipsoid.
 */
@ToString(callSuper = true)
public class Ellipsoid extends Shape {
    /**
     * The radii of the ellipsoid along each axis (x, y, z).
     */
    public Vector3f radii = new Vector3f(1, 1, 1);
    /**
     * The number of segments used to approximate the ellipsoid per axis. Bounds: [3, 128]
     */
    public int segmentsPerAxis = 20;

    /**
     * @param location the start location of the ellipsoid
     */
    public Ellipsoid(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.radii3 = radii;
        entry.numSegments = (byte) segmentsPerAxis;
    }

    @Override
    public Type getType() {
        return Type.ELLIPSOID;
    }
}
