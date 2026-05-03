package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a circle (2D).
 */
@ToString(callSuper = true)
public class Circle extends Shape {
    public int segments = 20;

    /**
     * @param location the location of the shape
     */
    public Circle(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        super.addAdditionalData(entry, protocol);
        entry.numSegments = (byte) segments;
    }

    @Override
    public Type getType() {
        return Type.CIRCLE;
    }
}
