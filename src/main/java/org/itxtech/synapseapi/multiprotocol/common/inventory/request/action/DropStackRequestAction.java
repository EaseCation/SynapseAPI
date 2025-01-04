package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;

/**
 * Drops some (or all) items from the source slot into the world as an item entity.
 */
@ToString
public class DropStackRequestAction implements ItemStackRequestAction {
    public int count;
    public ItemStackRequestSlotInfo source;
    public boolean randomly;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.DROP;
    }
}
