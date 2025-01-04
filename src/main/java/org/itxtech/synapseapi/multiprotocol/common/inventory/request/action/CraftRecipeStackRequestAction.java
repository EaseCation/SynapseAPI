package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Tells that the current transaction crafted the specified recipe.
 */
@ToString
public class CraftRecipeStackRequestAction implements ItemStackRequestAction {
    public int recipeId;
    /**
     * @since 1.21.20
     */
    public int repetitions = 1;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE;
    }
}
