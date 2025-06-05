package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a box or cuboid.
 */
@ToString(callSuper = true)
public class Box extends Shape {
    /**
     * The bounding box of the shape.
     * The final box will be this bound multiplied by the shape's scale.
     */
    public Vector3f bound;

    /**
     * @param location the location of the shape
     */
    public Box(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry) {
        entry.boxBound = bound;
    }

    @Override
    public Type getType() {
        return Type.BOX;
    }
}
