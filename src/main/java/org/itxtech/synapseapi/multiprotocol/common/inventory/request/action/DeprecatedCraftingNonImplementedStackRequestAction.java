package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Tells that the current transaction involves crafting an item in a way that isn't supported by the current system.
 * At the time of writing, this includes using anvils.
 */
@ToString
public class DeprecatedCraftingNonImplementedStackRequestAction implements ItemStackRequestAction {
    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_NON_IMPLEMENTED_DEPRECATED;
    }
}
