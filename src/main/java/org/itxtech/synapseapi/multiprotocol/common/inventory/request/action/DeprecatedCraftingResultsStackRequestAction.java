package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Not clear what this is needed for, but it is very clearly marked as deprecated,
 * so hopefully it'll go away before I have to write a proper description for it.
 */
@ToString
public class DeprecatedCraftingResultsStackRequestAction implements ItemStackRequestAction {
    public Item[] results;
    public int iterations;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RESULTS_DEPRECATED;
    }
}
