package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class CorrectPlayerMovePredictionPacket116100 extends Packet116100 {
    public static final int NETWORK_ID = ProtocolInfo.CORRECT_PLAYER_MOVE_PREDICTION_PACKET;

    public float x;
    public float y;
    public float z;
    public float deltaX;
    public float deltaY;
    public float deltaZ;
    public boolean onGround;
    public long tick;

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
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.deltaX, this.deltaY, this.deltaZ);
        this.putBoolean(this.onGround);
        this.putUnsignedVarLong(this.tick);
    }
}
