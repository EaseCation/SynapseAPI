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
