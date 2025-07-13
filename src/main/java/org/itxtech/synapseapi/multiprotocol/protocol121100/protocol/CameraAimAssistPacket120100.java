package org.itxtech.synapseapi.multiprotocol.protocol121100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CameraAimAssistPacket120100 extends Packet121100 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_PACKET;

    public static final int MODE_ANGLE = 0;
    public static final int MODE_DISTANCE = 1;

    public static final int ACTION_SET = 0;
    public static final int ACTION_CLEAR = 1;

    public String presetId = "minecraft:aim_assist_default";

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
    public boolean showDebugRender;

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
        putString(presetId);

        putVector2f(pitch, yaw);

        putLFloat(distance);

        putByte(mode);
        putByte(action);
        putBoolean(showDebugRender);
    }
}
