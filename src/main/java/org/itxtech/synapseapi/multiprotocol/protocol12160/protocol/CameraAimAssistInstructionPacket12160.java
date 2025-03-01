package org.itxtech.synapseapi.multiprotocol.protocol12160.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CameraAimAssistInstructionPacket12160 extends Packet12160 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_INSTRUCTION_PACKET;

    public static final int ACTION_SET_FROM_CAMERA_PRESET = 0;
    public static final int ACTION_CLEAR = 1;

    public String cameraPresetId = "";
    public int action = ACTION_SET_FROM_CAMERA_PRESET;
    public boolean allowAimAssist;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        cameraPresetId = getString();
        action = getByte();
        allowAimAssist = getBoolean();
    }

    @Override
    public void encode() {
    }
}
