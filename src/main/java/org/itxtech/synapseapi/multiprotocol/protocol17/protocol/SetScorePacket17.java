package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.data.ScorePacketInfo;

import java.util.ArrayList;
import java.util.List;

@ToString
public class SetScorePacket17 extends Packet17 {

    public static final int TYPE_CHANGE_SCORE = 0;
    public static final int TYPE_REMOVE_REMOVE = 1;

    public byte type;
    public List<ScorePacketInfo> entries = new ArrayList<>();

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
        this.putByte(type);
        this.putScorePacketInfos(entries);
    }

    public void putScorePacketInfos(List<ScorePacketInfo> info) {
        this.putUnsignedVarInt(info.size());
        for(ScorePacketInfo entry : info) {
            this.putVarLong(entry.scoreboardId);
            this.putString(entry.objectiveName);
            this.putLInt(entry.score);
            if (this.type == TYPE_CHANGE_SCORE) {
                this.putByte(entry.addType);
                switch (entry.addType) {
                    case ScorePacketInfo.TYPE_ENTITY:
                    case ScorePacketInfo.TYPE_PLAYER:
                        this.putEntityUniqueId(entry.entityId);
                        break;
                    case ScorePacketInfo.TYPE_DUMMY:
                        this.putString(entry.fakePlayer);
                        break;
                }
            }
        }
    }

    public List<ScorePacketInfo> getScorePacketInfos() {
        List<ScorePacketInfo> info = new ArrayList<>();
        int length = (int) this.getUnsignedVarInt();
        for(int i = 0; i < length; i++) {
            ScorePacketInfo entry = new ScorePacketInfo();
            entry.scoreboardId = this.getVarLong();
            entry.objectiveName = this.getString();
            entry.score = this.getLInt();
            if(this.type == 0) {
                entry.addType = (byte) this.getByte();
                switch(entry.addType) {
                    case 1:
                    case 2:
                        entry.entityId = this.getEntityUniqueId();
                        break;
                    case 3:
                        entry.fakePlayer = this.getString();
                }
            }
            info.add(entry);
        }
        return info;
    }
}
