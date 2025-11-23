package org.itxtech.synapseapi.multiprotocol.protocol121120.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import lombok.ToString;

/**
 * Sent from the server to the client when a server script changes the rendering settings.
 */
@ToString
public class GraphicsOverrideParameterPacket121120 extends Packet121120 {
    public static final int NETWORK_ID = ProtocolInfo.GRAPHICS_OVERRIDE_PARAMETER_PACKET;

    /**
     * Sent to set the sky zenith color.
     */
    public static final int TYPE_SKY_ZENITH_COLOR = 0;
    /**
     * Sent to set the sky horizon color.
     * @since 1.21.130
     */
    public static final int TYPE_SKY_HORIZON_COLOR = 1;
    /**
     * Sent to set the horizon blend min.
     * @since 1.21.130
     */
    public static final int TYPE_HORIZON_BLEND_MIN = 2;
    /**
     * Sent to set the horizon blend max.
     * @since 1.21.130
     */
    public static final int TYPE_HORIZON_BLEND_MAX = 3;
    /**
     * Sent to set the horizon blend start.
     * @since 1.21.130
     */
    public static final int TYPE_HORIZON_BLEND_START = 4;
    /**
     * Sent to set the horizon blend mie start.
     * @since 1.21.130
     */
    public static final int TYPE_HORIZON_BLEND_MIE_START = 5;
    /**
     * Sent to set the rayleigh strength.
     * @since 1.21.130
     */
    public static final int TYPE_RAYLEIGH_STRENGTH = 6;
    /**
     * Sent to set the sun mie strength.
     * @since 1.21.130
     */
    public static final int TYPE_SUN_MIE_STRENGTH = 7;
    /**
     * Sent to set the moon mie strength.
     * @since 1.21.130
     */
    public static final int TYPE_MOON_MIE_STRENGTH = 8;
    /**
     * Sent to set the sun glare shape.
     * @since 1.21.130
     */
    public static final int TYPE_SUN_GLARE_SHAPE = 9;

    /// Pair<Time, Components>[]
    public FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[0];
    /**
     * Determines which biome the override parameter is applied to.
     */
    public String biome;
    /**
     * Identifier for the parameter that is having its value changed.
     */
    public int type;
    /**
     * If true, the specified parameter will be reset.
     * If false, the parameter will be set using the data fields.
     */
    public boolean reset;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        putUnsignedVarInt(keyframes.length);
        for (FloatObjectPair<Vector3f> keyframe : keyframes) {
            putLFloat(keyframe.leftFloat());
            putVector3f(keyframe.right());
        }
        putString(biome);
        putByte(type);
        putBoolean(reset);
    }
}
