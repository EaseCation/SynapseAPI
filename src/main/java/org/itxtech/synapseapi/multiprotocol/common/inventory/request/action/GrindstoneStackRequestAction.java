package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Repair and/ or remove enchantments from an item in a grindstone.
 * @since 1.17.40
 */
@ToString
public class GrindstoneStackRequestAction implements ItemStackRequestAction {
    public int recipeId;
    public int repairCost;
    /**
     * @since 1.21.20
     */
    public int repetitions = 1;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_REPAIR_AND_DISENCHANT;
    }
}
