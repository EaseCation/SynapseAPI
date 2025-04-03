package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import cn.nukkit.network.protocol.types.ItemStackRequest;

@ToString
public class ItemStackRequestPacket116 extends Packet116 {
    public static final int NETWORK_ID = ProtocolInfo.ITEM_STACK_REQUEST_PACKET;

    public static final int ACTION_TAKE = 0;
    public static final int ACTION_PLACE = 1;
    public static final int ACTION_SWAP = 2;
    public static final int ACTION_DROP = 3;
    public static final int ACTION_DESTROY = 4;
    public static final int ACTION_CRAFTING_CONSUME_INPUT = 5;
    public static final int ACTION_CRAFTING_MARK_SECONDARY_RESULT_SLOT = 6;
    public static final int ACTION_LAB_TABLE_COMBINE = 7;
    public static final int ACTION_BEACON_PAYMENT = 8;
    public static final int ACTION_CRAFTING_RECIPE = 9;
    public static final int ACTION_CRAFTING_RECIPE_AUTO = 10;
    public static final int ACTION_CREATIVE_CREATE = 11;
    public static final int ACTION_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING = 12;
    public static final int ACTION_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING = 13;

    public ItemStackRequest[] requests;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        int count = (int) getUnsignedVarInt();
        if (count > 80) {
            //TODO: we can probably lower this limit, but this will do for now
            throw new IndexOutOfBoundsException("Too many requests in ItemStackRequestPacket");
        }
        requests = new ItemStackRequest[count];
        for (int i = 0; i < count; i++) {
            requests[i] = helper.getItemStackRequest(this);
        }
    }

    @Override
    public void encode() {
    }
}
