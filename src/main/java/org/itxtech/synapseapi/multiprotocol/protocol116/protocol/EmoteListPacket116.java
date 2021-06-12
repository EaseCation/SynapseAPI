package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.UUID;

public class EmoteListPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.EMOTE_LIST_PACKET;

    public long runtimeId;
    public final List<UUID> pieceIds = new ObjectArrayList<>();

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.runtimeId = this.getEntityRuntimeId();
        int size = (int) this.getUnsignedVarInt();
        for (int i = 0; i < size; i++) {
            UUID id = this.getUUID();
            pieceIds.add(id);
        }
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(runtimeId);
        this.putUnsignedVarInt(pieceIds.size());
        for (UUID id : pieceIds) {
            this.putUUID(id);
        }
    }
}
