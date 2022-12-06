package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdatePlayerGameTypePacket116 extends Packet116 {
    public final static int NETWORK_ID = ProtocolInfo.UPDATE_PLAYER_GAME_TYPE_PACKET;

    public int gamemode;
    public long playerEntityUniqueId;

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
    }
}
