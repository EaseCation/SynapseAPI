package org.itxtech.synapseapi.multiprotocol.protocol119.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class RequestPermissionsPacket119 extends Packet119 {
    public static final int NETWORK_ID = ProtocolInfo.REQUEST_PERMISSIONS_PACKET;

    public static final int FLAG_BUILD = 1;
    public static final int FLAG_MINE = 1 << 1;
    public static final int FLAG_DOORS_AND_SWITCHES = 1 << 2;
    public static final int FLAG_OPEN_CONTAINERS = 1 << 3;
    public static final int FLAG_ATTACK_PLAYERS = 1 << 4;
    public static final int FLAG_ATTACK_MOBS = 1 << 5;
    public static final int FLAG_OPERATOR = 1 << 6;
    public static final int FLAG_TELEPORT = 1 << 7;

    public long entityUniqueId;
    public int flags;
    public int customFlags;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        entityUniqueId = getLLong();
        flags = getVarInt();
        customFlags = getLShort();
    }

    @Override
    public void encode() {
    }
}
