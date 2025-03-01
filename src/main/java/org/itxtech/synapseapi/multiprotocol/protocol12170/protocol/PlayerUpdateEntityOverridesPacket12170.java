package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class PlayerUpdateEntityOverridesPacket12170 extends Packet12170 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_UPDATE_ENTITY_OVERRIDES_PACKET;

    public static final int TYPE_CLEAR_OVERRIDES = 0;
    public static final int TYPE_REMOVE_OVERRIDE = 1;
    public static final int TYPE_SET_INT_OVERRIDE = 2;
    public static final int TYPE_SET_FLOAT_OVERRIDE = 3;

    public long entityUniqueId;
    public int propertyIndex;
    public int type = TYPE_CLEAR_OVERRIDES;

    public int intValue;
    public float floatValue;

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
        putEntityUniqueId(entityUniqueId);
        putUnsignedVarInt(propertyIndex);
        putByte(type);
        switch (type) {
            case TYPE_SET_INT_OVERRIDE:
                putLInt(intValue);
                break;
            case TYPE_SET_FLOAT_OVERRIDE:
                putLFloat(floatValue);
                break;
        }
    }
}
