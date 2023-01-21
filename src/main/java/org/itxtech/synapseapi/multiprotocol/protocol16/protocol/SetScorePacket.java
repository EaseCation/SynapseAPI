package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.data.ScorePacketEntry;

@ToString
public class SetScorePacket extends Packet16 {

    public static final int TYPE_CHANGE_SCORE = 0;
    public static final int TYPE_REMOVE_REMOVE = 1;

    public int type;
    public ScorePacketEntry[] entries = new ScorePacketEntry[0];

    @Override
    public int pid() {
        return ProtocolInfo.SET_SCORE_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putByte((byte) this.type);
        this.putUnsignedVarInt(this.entries.length);
        for (ScorePacketEntry entry : this.entries) {
            this.putUUID(entry.uuid);
            this.putString(entry.objectiveName);
            this.putLInt(entry.score);
        }
    }
}
