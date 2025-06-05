package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

import java.awt.*;

/**
 * The base class for all shapes.
 * Represents an object in the world and its base properties.
 */
@ToString
public abstract class Shape {
    private static long ID = 1;

    public final long id = ID++;
    /**
     * The location of the shape.
     * For most shapes this is the centre of the shape,
     * except Line and Arrow where this represents the start point of the line.
     */
    public final Vector3f location;
    /**
     * The rotation of the shape (Euler angles - [Pitch, Yaw, Roll]).
     */
    public Vector3f rotation;
    /**
     * The scale of the shape.
     * This does not apply to Line or Arrow.
     */
    public Float scale;
    /**
     * The total initial time-span (in seconds) until this shape is automatically removed.
     * Returns 0 if the shape does not have a limited life-span.
     */
    public float totalTimeLeft;
    /**
     * The color of the shape.
     */
    public Color color;

    protected Shape(Vector3f location) {
        this.location = location;
    }

    public final Entry createPacketEntry() {
        Entry entry = new Entry();
        entry.id = id;
        entry.type = getType();
        entry.location = location;
        entry.rotation = rotation;
        entry.scale = scale;
        if (totalTimeLeft > 0) {
            entry.totalTimeLeft = totalTimeLeft;
        } else {
            entry.totalTimeLeft = Float.MAX_VALUE;
        }
        entry.color = color.getRGB();
        addAdditionalData(entry);
        return entry;
    }

    protected void addAdditionalData(Entry entry) {
    }

    public abstract Type getType();
}
