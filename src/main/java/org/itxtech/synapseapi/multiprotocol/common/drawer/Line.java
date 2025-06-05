package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a line segment.
 */
@ToString(callSuper = true)
public class Line extends Shape {
    /**
     * The end location of the line segment.
     * The final line will spawn between location and endLocation.
     */
    public final Vector3f endLocation;

    /**
     * @param location the start location of the line segment
     * @param endLocation the end location of the line segment
     */
    public Line(Vector3f location, Vector3f endLocation) {
        super(location);
        this.endLocation = endLocation;
    }

    @Override
    public void addAdditionalData(Entry entry) {
        entry.lineEndLocation = endLocation;
    }

    @Override
    public Type getType() {
        return Type.LINE;
    }
}
