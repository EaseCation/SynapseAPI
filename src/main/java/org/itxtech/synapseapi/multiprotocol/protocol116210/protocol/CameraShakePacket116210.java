package org.itxtech.synapseapi.multiprotocol.protocol116210.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.CameraShakePacket116100;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class CameraShakePacket116210 extends Packet116210 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_SHAKE_PACKET;

    public static final int TYPE_POSITIONAL = 0;
    public static final int TYPE_ROTATIONAL = 1;

    public static final int ACTION_ADD = 0;
    public static final int ACTION_STOP = 1;

    /**
     * Intensity to shake the player's camera view.
     */
    public float intensity;
    /**
     * seconds to shake.
     */
    public float duration;
    public int type;
    public int action;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putLFloat(this.intensity);
        this.putLFloat(this.duration);
        this.putByte((byte) this.type);
        this.putByte((byte) this.action);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, CameraShakePacket116100.class);

        CameraShakePacket116100 packet = (CameraShakePacket116100) pk;
        this.intensity = packet.intensity;
        this.duration = packet.duration;
        this.type = packet.type;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return CameraShakePacket116100.class;
    }
}
