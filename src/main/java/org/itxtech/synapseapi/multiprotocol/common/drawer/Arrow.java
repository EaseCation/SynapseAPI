package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents an arrow.
 */
@ToString(callSuper = true)
public class Arrow extends Line {
    /**
     * The length of the arrow's head/tip.
     */
    public Float headLength;
    /**
     * The radius of the arrow's head/tip.
     */
    public Float headRadius;
    /**
     * The number of segments for the base circle of the arrow's head/tip (default: 4).
     */
    public Integer headSegments;

    /**
     * @param location the start location of the line segment
     * @param endLocation the end location of the line segment
     */
    public Arrow(Vector3f location, Vector3f endLocation) {
        super(location, endLocation);
    }

    @Override
    public void addAdditionalData(Entry entry) {
        super.addAdditionalData(entry);
        entry.arrowHeadLength = headLength;
        entry.arrowHeadRadius = headRadius;
        entry.numSegments = headSegments == null ? null : headSegments.byteValue();
    }

    @Override
    public Type getType() {
        return Type.ARROW;
    }
}
