package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

/**
 * A shape class that a text label.
 * The text label automatically faces the screen.
 */
@ToString(callSuper = true)
public class Text extends Shape {
    /**
     * The text of the shape to display.
     */
    public String text;

    /**
     * @param location the location of the shape
     * @param text the text of the shape to display
     */
    public Text(Vector3f location, String text) {
        super(location);
        this.text = text;
    }

    @Override
    public void addAdditionalData(Entry entry) {
        entry.text = text;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
