package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import lombok.ToString;

/**
 * Sent from the server to the client when a server script changes the rendering settings.
 */
@ToString
public class GraphicsOverrideParameterPacket126 extends Packet126 {
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
    /**
     * Sent to set the water chlorophyll value.
     * @since 1.26.0
     */
    public static final int TYPE_CHLOROPHYLL = 10;
    /**
     * Sent to set the water CDOM value.
     * @since 1.26.0
     */
    public static final int TYPE_CDOM = 11;
    /**
     * Sent to set the water suspended sediment value.
     * @since 1.26.0
     */
    public static final int TYPE_SUSPENDED_SEDIMENT = 12;
    /**
     * Sent to set the water waves depth value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_DEPTH = 13;
    /**
     * Sent to set the water waves frequency value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_FREQUENCY = 14;
    /**
     * Sent to set the water waves frequency scaling value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_FREQUENCY_SCALING = 15;
    /**
     * Sent to set the water waves speed value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_SPEED = 16;
    /**
     * Sent to set the water waves speed scaling value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_SPEED_SCALING = 17;
    /**
     * Sent to set the water waves shape value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_SHAPE = 18;
    /**
     * Sent to set the water waves octaves value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_OCTAVES = 19;
    /**
     * Sent to set the water waves mix value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_MIX = 20;
    /**
     * Sent to set the water waves pull value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_PULL = 21;
    /**
     * Sent to set the water waves direction increment value.
     * @since 1.26.0
     */
    public static final int TYPE_WAVES_DIRECTION_INCREMENT = 22;
    /**
     * Sent to set the color grading midtones contrast value.
     * @since 1.26.0
     */
    public static final int TYPE_MIDTONES_CONTRAST = 23;
    /**
     * Sent to set the color grading highlights contrast value.
     * @since 1.26.0
     */
    public static final int TYPE_HIGHLIGHTS_CONTRAST = 24;
    /**
     * Sent to set the color grading shadows contrast value.
     * @since 1.26.0
     */
    public static final int TYPE_SHADOWS_CONTRAST = 25;

    /// Pair<Time, Components>[]
    public FloatObjectPair<Vector3f>[] keyframes = new FloatObjectPair[0];
    /**
     * If set, contains a single float graphics parameter to be overridden.
     */
    public Float floatValue;
    /**
     * If set, contains a single Vec3 graphics parameter to be overridden.
     */
    public Vector3f vecValue;
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

        putArray(keyframes, (stream, keyframe) -> {
            stream.putLFloat(keyframe.leftFloat());
            stream.putVector3f(keyframe.right());
        });
        putOptional(floatValue, BinaryStream::putLFloat);
        putOptional(vecValue, BinaryStream::putVector3f);
        putString(biome);
        putByte(type);
        putBoolean(reset);
    }
}
