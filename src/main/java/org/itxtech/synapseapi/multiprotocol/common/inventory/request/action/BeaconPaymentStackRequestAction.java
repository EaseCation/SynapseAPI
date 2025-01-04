package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Completes a transaction involving a beacon consuming input to produce effects.
 */
@ToString
public class BeaconPaymentStackRequestAction implements ItemStackRequestAction {
    public int primaryEffectId;
    public int secondaryEffectId;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.BEACON_PAYMENT;
    }
}
