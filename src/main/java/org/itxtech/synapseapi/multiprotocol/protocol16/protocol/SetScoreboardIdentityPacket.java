package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.data.ScoreboardIdentityPacketEntry;

public class SetScoreboardIdentityPacket extends Packet16 {

    public static final int TYPE_UPDATE_IDENTITY = 0;
    public static final int TYPE_REMOVE_IDENTITY = 1;

    public int type;
    public ScoreboardIdentityPacketEntry[] entries = new ScoreboardIdentityPacketEntry[0];

    @Override
    public int pid() {
        return ProtocolInfo.SET_SCOREBOARD_IDENTITY_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) this.type);
        this.putUnsignedVarInt(this.entries.length);
        for (ScoreboardIdentityPacketEntry entry : this.entries) {
            this.putVarLong(entry.scoreboardId);
            if (this.type == TYPE_UPDATE_IDENTITY) {
                this.putUUID(entry.uuid);
            }
        }
    }
}
