package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import java.util.UUID;

@ToString
public class EmoteListPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.EMOTE_LIST_PACKET;

    public long runtimeId;
    public UUID[] pieceIds = new UUID[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.runtimeId = this.getEntityRuntimeId();
        this.pieceIds = this.getArray(UUID.class, BinaryStream::getUUID);
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(runtimeId);
        this.putUnsignedVarInt(pieceIds.length);
        for (UUID id : pieceIds) {
            this.putUUID(id);
        }
    }
}
