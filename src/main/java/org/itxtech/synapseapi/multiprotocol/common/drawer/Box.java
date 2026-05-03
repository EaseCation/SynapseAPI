package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
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
    public final Vector3f bound = new Vector3f(1, 1, 1);

    /**
     * @param location the location of the shape
     */
    public Box(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.boxBound = bound;

        if (AbstractProtocol.PROTOCOL_126_10.isOlderThanOrEqual(protocol)) {
            // backward compatibility: currently in the center
            float scale = entry.scale != null ? entry.scale : 1;
            entry.location.setComponents(entry.location.add(bound.multiply(scale * 0.5f)));
        }
    }

    @Override
    public Type getType() {
        return Type.BOX;
    }
}
