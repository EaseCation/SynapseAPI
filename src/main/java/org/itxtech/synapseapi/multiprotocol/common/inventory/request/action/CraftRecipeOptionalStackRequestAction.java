package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Renames an item in an anvil, or map on a cartography table.
 * @since 1.16.200
 */
@ToString
public class CraftRecipeOptionalStackRequestAction implements ItemStackRequestAction {
    public int recipeId;
    public int filterStringIndex;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE_OPTIONAL;
    }
}
