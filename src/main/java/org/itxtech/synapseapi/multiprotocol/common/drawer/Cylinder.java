package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a cylinder.
 */
@ToString(callSuper = true)
public class Cylinder extends Shape {
    /**
     * Radius along the X axis of the cylinder (x: bottom radius, y: top radius).
     */
    public Vector2f radiiX = new Vector2f(1, 1);
    /**
     * Radius along the Z axis of the cylinder (x: bottom radius, y: top radius).
     */
    public Vector2f radiiZ = new Vector2f(1, 1);
    /**
     * The height of the cylinder.
     */
    public float height = 1;
    /**
     * The number of segments used to approximate the circular cross-section of the cylinder. Bounds: [3, 128]
     */
    public int numSegments = 20;

    /**
     * @param location the start location of the cylinder
     */
    public Cylinder(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.radii = radiiX;
        entry.radii2 = radiiZ;
        entry.height = height;
        entry.numSegments = (byte) numSegments;
    }

    @Override
    public Type getType() {
        return Type.CYLINDER;
    }
}
