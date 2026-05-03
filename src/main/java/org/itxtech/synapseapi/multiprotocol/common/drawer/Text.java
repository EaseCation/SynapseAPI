package org.itxtech.synapseapi.multiprotocol.common.drawer;

import cn.nukkit.math.Vector3f;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Entry;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.ServerScriptDebugDrawerPacket12190.Type;

import java.awt.*;

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
     * The color of the background plate of the text.
     * If set to undefined, it will use the default color.
     * @since 1.26.20
     */
    public Color backgroundColorOverride;
    /**
     * If set to true, the text will be hidden behind blocks or entities.
     * By default this is set to false (will always render).
     * @since 1.26.20
     */
    public boolean depthTest;
    /**
     * If set to true, the text will not face the camera and instead will use the rotation from the shape.
     * @since 1.26.20
     */
    public boolean useRotation;
    /**
     * If set to true, the text primitive will render the back-face of the background.
     * Defaults to true but will always be false if 'useRotation' is set to false.
     * @since 1.26.20
     */
    public boolean backfaceVisible = true;
    /**
     * If set to true, the text primitive will render the back-face of the text.
     * Defaults to true but will always be false if 'useRotation' is set to false.
     * @since 1.26.20
     */
    public boolean textBackfaceVisible = true;

    /**
     * @param location the location of the shape
     * @param text the text of the shape to display
     */
    public Text(Vector3f location, String text) {
        super(location);
        this.text = text;
    }

    @Override
    public void addAdditionalData(Entry entry, AbstractProtocol protocol) {
        entry.text = text;
        entry.textRotation = useRotation;
        if (backgroundColorOverride != null) {
            entry.backgroundColor = backgroundColorOverride.getRGB();
        }
        entry.depthTest = depthTest;
        entry.showBackface = backfaceVisible;
        entry.showTextBackface = textBackfaceVisible;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
