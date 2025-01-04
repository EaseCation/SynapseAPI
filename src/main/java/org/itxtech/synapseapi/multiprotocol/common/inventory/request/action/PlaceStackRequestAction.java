package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;

@ToString
public class PlaceStackRequestAction implements ItemStackRequestAction {
    public int count;
    public ItemStackRequestSlotInfo source;
    public ItemStackRequestSlotInfo destination;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.PLACE;
    }
}
