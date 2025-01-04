package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Creates an item by copying it from the creative inventory.
 * This is treated as a crafting action by vanilla.
 */
@ToString
public class CreativeCreateStackRequestAction implements ItemStackRequestAction {
    public int creativeItemId;
    /**
     * @since 1.21.20
     */
    public int repetitions = 1;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_CREATIVE;
    }
}
