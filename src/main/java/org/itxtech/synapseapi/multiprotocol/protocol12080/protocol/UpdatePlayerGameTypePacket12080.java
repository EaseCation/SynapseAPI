package org.itxtech.synapseapi.multiprotocol.protocol12080.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdatePlayerGameTypePacket12080 extends Packet12080 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_PLAYER_GAME_TYPE_PACKET;

    public int gamemode;
    public long playerEntityUniqueId;
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
        this.putVarInt(this.gamemode);
        this.putEntityUniqueId(this.playerEntityUniqueId);
        this.putUnsignedVarInt(this.tick);
    }
}
