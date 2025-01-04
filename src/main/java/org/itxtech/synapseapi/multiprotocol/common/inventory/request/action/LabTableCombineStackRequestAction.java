package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Not clear what the point of this is.
 * It's sent when the player uses a lab table, but it's not clear why this action is needed.
 */
@ToString
public class LabTableCombineStackRequestAction implements ItemStackRequestAction {
    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.LAB_TABLE_COMBINE;
    }
}
