package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * This action precedes a "take" or "place" action involving the "created item" magic slot.
 * It indicates that the "created item" output slot now contains output N of a previously specified crafting recipe.
 * This is only used with crafting recipes that have multiple outputs.
 * For recipes with single outputs, it's assumed that the content of the "created item" slot is the only output.
 */
@ToString
public class CraftingCreateSpecificResultStackRequestAction implements ItemStackRequestAction {
    public int resultIndex;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CREATE;
    }
}
