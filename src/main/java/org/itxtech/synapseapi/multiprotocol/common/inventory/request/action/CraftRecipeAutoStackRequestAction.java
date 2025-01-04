package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.inventory.recipe.RecipeIngredient;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Tells that the current transaction crafted the specified recipe, using the recipe book.
 * This is effectively the same as the regular crafting result action.
 */
@ToString
public class CraftRecipeAutoStackRequestAction implements ItemStackRequestAction {
    public int recipeId;
    /**
     * @since 1.17.10
     */
    public int repetitions = 1;
    /**
     * @since 1.21.20
     */
    public int repetitions2; //repetitions property is sent twice, mojang...
    /**
     * @since 1.19.40
     */
    public RecipeIngredient[] ingredients;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE_AUTO;
    }
}
