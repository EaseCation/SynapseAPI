package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistEntityPriority;

/**
 * Camera aim-assist actor priority data sent from the server to clients.
 */
@ToString
public class CameraAimAssistEntityPriorityPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_ENTITY_PRIORITY_PACKET;

    public CameraAimAssistEntityPriority[] priorities = new CameraAimAssistEntityPriority[0];

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
        putArray(priorities, (stream, priority) -> {
            stream.putLInt(priority.presetIndex);
            stream.putLInt(priority.categoryIndex);
            stream.putLInt(priority.entityIndex);
            stream.putLInt(priority.priorityValue);
        });
    }
}
