package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
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
     * If defined, this distance will be used to determine how far away this primitive will be rendered for each client.
     * By default the distance will match the client's render distance setting.
     * @since 1.26.20
     */
    public Float maximumRenderDistance;
    /**
     * The color of the shape.
     */
    public Color color;
    /**
     * The entity this shape is attached to.
     * When set, this shape will copy the root location of the attached entity and the shape's position will be used as an offset.
     * @since 1.26.0
     */
    public Long attachedEntityRuntimeId;

    protected Shape(Vector3f location) {
        this.location = location;
    }

    public final Entry createPacketEntry(int dimension, AbstractProtocol protocol) {
        Entry entry = new Entry();
        entry.id = id;
        entry.dimension = dimension;
        entry.type = getType();
        entry.location = location;
        entry.rotation = rotation;
        entry.scale = scale;
        if (totalTimeLeft > 0) {
            entry.totalTimeLeft = totalTimeLeft;
        } else {
            entry.totalTimeLeft = Float.MAX_VALUE;
        }
        entry.maximumRenderDistance = maximumRenderDistance;
        if (color != null) {
            entry.color = color.getRGB();
        }
        entry.attachedEntityRuntimeId = attachedEntityRuntimeId;
        addAdditionalData(entry, protocol);
        return entry;
    }

    protected void addAdditionalData(Entry entry, AbstractProtocol protocol) {
    }

    public abstract Type getType();
}
