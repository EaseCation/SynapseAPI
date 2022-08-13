package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CameraShakePacket116100 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_SHAKE_PACKET;

    public static final int TYPE_POSITIONAL = 0;
    public static final int TYPE_ROTATIONAL = 1;

    /**
     * Intensity to shake the player's camera view.
     */
    public float intensity;
    /**
     * seconds to shake.
     */
    public float duration;
    public int type;

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
    }
}
