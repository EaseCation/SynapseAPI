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
    /**
     * Sent to set the color grading highlights gain value.
     * @since 1.26.10
     */
    public static final int TYPE_HIGHLIGHTS_GAIN = 26;
    /**
     * Sent to set the color grading highlights gamma value.
     * @since 1.26.10
     */
    public static final int TYPE_HIGHLIGHTS_GAMMA = 27;
    /**
     * Sent to set the color grading highlights offset value.
     * @since 1.26.10
     */
    public static final int TYPE_HIGHLIGHTS_OFFSET = 28;
    /**
     * Sent to set the color grading highlights saturation value.
     * @since 1.26.10
     */
    public static final int TYPE_HIGHLIGHTS_SATURATION = 29;
    /**
     * Sent to set the color grading midtones gain value.
     * @since 1.26.10
     */
    public static final int TYPE_MIDTONES_GAIN = 30;
    /**
     * Sent to set the color grading midtones gamma value.
     * @since 1.26.10
     */
    public static final int TYPE_MIDTONES_GAMMA = 31;
    /**
     * Sent to set the color grading midtones offset value.
     * @since 1.26.10
     */
    public static final int TYPE_MIDTONES_OFFSET = 32;
    /**
     * Sent to set the color grading midtones saturation value.
     * @since 1.26.10
     */
    public static final int TYPE_MIDTONES_SATURATION = 33;
    /**
     * Sent to set the color grading shadows gain value.
     * @since 1.26.10
     */
    public static final int TYPE_SHADOWS_GAIN = 34;
    /**
     * Sent to set the color grading shadows gamma value.
     * @since 1.26.10
     */
    public static final int TYPE_SHADOWS_GAMMA = 35;
    /**
     * Sent to set the color grading shadows offset value.
     * @since 1.26.10
     */
    public static final int TYPE_SHADOWS_OFFSET = 36;
    /**
     * Sent to set the color grading shadows saturation value.
     * @since 1.26.10
     */
    public static final int TYPE_SHADOWS_SATURATION = 37;
    /**
     * Sent to set the color grading highlights min value.
     * @since 1.26.10
     */
    public static final int TYPE_HIGHLIGHTS_MIN = 38;
    /**
     * Sent to set the color grading shadows max value.
     * @since 1.26.10
     */
    public static final int TYPE_SHADOWS_MAX = 39;
    /**
     * Sent to set the color grading temperature value.
     * @since 1.26.10
     */
    public static final int TYPE_TEMPERATURE = 40;
    /**
     * Sent to set the lighting sun color value.
     * @since 1.26.10
     */
    public static final int TYPE_SUN_COLOR = 41;
    /**
     * Sent to set the lighting sun illuminance value.
     * @since 1.26.10
     */
    public static final int TYPE_SUN_ILLUMINANCE = 42;
    /**
     * Sent to set the lighting moon color value.
     * @since 1.26.10
     */
    public static final int TYPE_MOON_COLOR = 43;
    /**
     * Sent to set the lighting moon illuminance value.
     * @since 1.26.10
     */
    public static final int TYPE_MOON_ILLUMINANCE = 44;
    /**
     * Sent to set the lighting flash color value.
     * @since 1.26.10
     */
    public static final int TYPE_FLASH_COLOR = 45;
    /**
     * Sent to set the lighting flash illuminance value.
     * @since 1.26.10
     */
    public static final int TYPE_FLASH_ILLUMINANCE = 46;
    /**
     * Sent to set the lighting ambient color value.
     * @since 1.26.10
     */
    public static final int TYPE_AMBIENT_COLOR = 47;
    /**
     * Sent to set the lighting ambient illuminance value.
     * @since 1.26.10
     */
    public static final int TYPE_AMBIENT_ILLUMINANCE = 48;
    /**
     * Sent to set the lighting emissive desaturation value.
     * @since 1.26.20
     */
    public static final int TYPE_EMISSIVE_DESATURATION = 49;
    /**
     * Sent to set the lighting sky intensity value.
     * @since 1.26.20
     */
    public static final int TYPE_SKY_INTENSITY = 50;
    /**
     * Sent to set the lighting orbital offset degrees value.
     * @since 1.26.20
     */
    public static final int TYPE_ORBITAL_OFFSET_DEGREES = 51;

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
