package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import cn.nukkit.network.protocol.types.ItemStackResponse;

@ToString
public class ItemStackResponsePacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.ITEM_STACK_RESPONSE_PACKET;

    public ItemStackResponse[] responses;

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
        putUnsignedVarInt(responses.length);
        for (ItemStackResponse response : responses) {
            helper.putItemStackResponse(this, response);
        }
    }
}
