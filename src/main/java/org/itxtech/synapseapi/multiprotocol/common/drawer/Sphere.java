package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a sphere.
 */
@ToString(callSuper = true)
public class Sphere extends Shape {
    /**
     * @param location the location of the shape
     */
    public Sphere(Vector3f location) {
        super(location);
    }

    @Override
    public Type getType() {
        return Type.SPHERE;
    }
}
