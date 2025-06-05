package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a circle (2D).
 */
@ToString(callSuper = true)
public class Circle extends Shape {
    /**
     * @param location the location of the shape
     */
    public Circle(Vector3f location) {
        super(location);
    }

    @Override
    public Type getType() {
        return Type.CIRCLE;
    }
}
