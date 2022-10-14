package org.itxtech.synapseapi.multiprotocol.protocol119.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.PlayerAbility;
import lombok.ToString;

@ToString
public class RequestAbilityPacket119 extends Packet119 {
    public static final int NETWORK_ID = ProtocolInfo.REQUEST_ABILITY_PACKET;

    public static final int TYPE_BOOL = 1;
    public static final int TYPE_FLOAT = 2;

    public PlayerAbility ability;
    public int type;
    public boolean boolValue;
    public float floatValue;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        ability = PlayerAbility.byId(getVarInt());
        type = getByte();
        boolValue = getBoolean();
        floatValue = getLFloat();
    }

    @Override
    public void encode() {
    }
}
