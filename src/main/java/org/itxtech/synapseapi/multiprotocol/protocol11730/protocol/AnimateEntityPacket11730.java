package org.itxtech.synapseapi.multiprotocol.protocol11730.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.AnimateEntityPacket116100;
import org.itxtech.synapseapi.utils.ClassUtils;

public class AnimateEntityPacket11730 extends Packet11730 {

    public static final int NETWORK_ID = ProtocolInfo.ANIMATE_ENTITY_PACKET;

    public String animation = "";
    public String nextState = "default";
    public String stopExpression = ""; // "query.any_animation_finished"
    public int stopExpressionVersion = 16777216;
    public String controller = "__runtime_controller";
    public float blendOutTime = 0;
    public long[] entityRuntimeIds = new long[0];

    @Override
    public int pid() {
        return NETWORK_ID;
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
        this.putLInt(stopExpressionVersion);
        this.putString(controller);
        this.putLFloat(blendOutTime);
        this.putUnsignedVarInt(entityRuntimeIds.length);
        for (long entityRuntimeId : entityRuntimeIds) {
            this.putEntityRuntimeId(entityRuntimeId);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, AnimateEntityPacket116100.class);

        AnimateEntityPacket116100 packet = (AnimateEntityPacket116100) pk;
        this.animation = packet.animation;
        this.nextState = packet.nextState;
        this.stopExpression = packet.stopExpression;
        this.controller = packet.controller;
        this.blendOutTime = packet.blendOutTime;
        this.entityRuntimeIds = packet.entityRuntimeIds;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return AnimateEntityPacket116100.class;
    }
}
