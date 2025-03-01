package org.itxtech.synapseapi.multiprotocol.protocol12130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.Packet12120;

@ToString
public class CameraAimAssistPacket12030 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_PACKET;

    public static final int MODE_ANGLE = 0;
    public static final int MODE_DISTANCE = 1;

    public static final int ACTION_SET = 0;
    public static final int ACTION_CLEAR = 1;

    /**
     * 10~90
     */
    public float pitch = 50;
    /**
     * 10~90
     */
    public float yaw = 50;

    /**
     * 1~16
     */
    public float distance = 8.5f;

    public int mode = MODE_ANGLE;
    public int action = ACTION_SET;

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

        putVector2f(pitch, yaw);

        putLFloat(distance);

        putByte(mode);
        putByte(action);
    }
}
