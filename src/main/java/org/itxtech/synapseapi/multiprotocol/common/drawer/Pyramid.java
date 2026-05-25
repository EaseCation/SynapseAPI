package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that represents a pyramid.
 */
@ToString(callSuper = true)
public class Pyramid extends Shape {
    /**
     * The width of the pyramid's base.
     */
    public float width = 1;
    /**
     * The depth of the pyramid's base.
     */
    public Float depth;
    /**
     * The height of the pyramid.
     */
    public float height = 1;

    /**
     * @param location the start location of the pyramid
     */
    public Pyramid(Vector3f location) {
        super(location);
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.width = width;
        entry.depth = depth;
        entry.height = height;
    }

    @Override
    public Type getType() {
        return Type.PYRAMID;
    }
}
