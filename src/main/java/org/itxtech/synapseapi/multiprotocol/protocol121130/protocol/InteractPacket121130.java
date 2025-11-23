package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InteractPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class InteractPacket121130 extends Packet121130 {
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
    @Nullable
    public Vector3f position;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.action = this.getByte();
        this.target = this.getEntityRuntimeId();
        this.position = this.getOptional(BinaryStream::getVector3f);
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket toDefault() {
        InteractPacket packet = new InteractPacket();
        packet.action = this.action;
        packet.target = this.target;
        packet.position = this.position;
        return packet;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InteractPacket.class;
    }
}
