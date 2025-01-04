package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;

/**
 * Swaps two stacks.
 * These don't have to be in the same inventory.
 * This action does not modify the stacks themselves.
 */
@ToString
public class SwapStackRequestAction implements ItemStackRequestAction {
    public ItemStackRequestSlotInfo source;
    public ItemStackRequestSlotInfo destination;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.SWAP;
    }
}
