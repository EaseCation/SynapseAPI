package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class InteractPacket113 extends Packet113 {
    public static final int NETWORK_ID = ProtocolInfo.INTERACT_PACKET;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_INTERACT = 1;
    public static final int ACTION_DAMAGE = 2;
    public static final int ACTION_VEHICLE_EXIT = 3;
    public static final int ACTION_MOUSEOVER = 4;
    public static final int ACTION_OPEN_NPC = 5;
    public static final int ACTION_OPEN_INVENTORY = 6;

    public int action;
    public long target;

    public float x;
    public float y;
    public float z;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = this.getByte();
        this.target = this.getEntityRuntimeId();

        if (this.action == ACTION_MOUSEOVER || this.action == ACTION_VEHICLE_EXIT) {
            this.x = this.getLFloat();
            this.y = this.getLFloat();
            this.z = this.getLFloat();
        }
    }

    @Override
    public void encode() {
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InteractPacket.class;
    }

    @Override
    public DataPacket toDefault() {
        InteractPacket packet = new InteractPacket();
        packet.action = this.action;
        packet.target = this.target;
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        return packet;
    }
}
