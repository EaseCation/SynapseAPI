package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector2f;
import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a cone.
 */
@ToString(callSuper = true)
public class Cone extends Shape {
    /**
     * The radii of the cone's circular base (x: bottom radius, y: top radius).
     */
    public Vector2f radii = new Vector2f(1, 1);
    /**
     * The height of the cone.
     */
    public float height = 1;
    /**
     * The number of segments used to approximate the circular base of the cone. Bounds: [3, 128]
     */
    public int numSegments = 20;

    /**
     * @param location the start location of the cone
     */
    public Cone(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.radii = radii;
        entry.height = height;
        entry.numSegments = (byte) numSegments;
    }

    @Override
    public Type getType() {
        return Type.CONE;
    }
}
