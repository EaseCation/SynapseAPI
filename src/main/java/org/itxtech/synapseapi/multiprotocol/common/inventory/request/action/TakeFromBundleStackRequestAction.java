package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;

/**
 * Take an item out of a bundle.
 * deprecated 1.21.20
 * @since 1.18.10
 */
@ToString
public class TakeFromBundleStackRequestAction implements ItemStackRequestAction {
    public int count;
    public ItemStackRequestSlotInfo source;
    public ItemStackRequestSlotInfo destination;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.TAKE_FROM_ITEM_CONTAINER;
    }
}
