package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AnimateEntityPacket116100 extends Packet116100 {

    public String animation = "";
    public String nextState = "default";
    public String stopExpression = ""; // "query.any_animation_finished"
    public String controller = "__runtime_controller";
    public float blendOutTime = 0;
    public long[] entityRuntimeIds = new long[0];

    @Override
    public int pid() {
        return ProtocolInfo.ANIMATE_ENTITY_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        super.reset();
        this.putString(animation);
        this.putString(nextState);
        this.putString(stopExpression);
        this.putString(controller);
        this.putLFloat(blendOutTime);
        this.putUnsignedVarInt(entityRuntimeIds.length);
        for (long entityRuntimeId : entityRuntimeIds) {
            this.putEntityRuntimeId(entityRuntimeId);
        }
    }
}
