package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * @since 1.16.210
 */
@ToString
public class MineBlockStackRequestAction implements ItemStackRequestAction {
    public int hotbarSlot;
    public int predictedDurability;
    public int stackId;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.MINE_BLOCK;
    }
}
