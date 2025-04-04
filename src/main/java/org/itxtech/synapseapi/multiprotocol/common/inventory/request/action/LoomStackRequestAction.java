package org.itxtech.synapseapi.multiprotocol.common.inventory.request.action;

import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackRequestActionType;
import lombok.ToString;

/**
 * Apply a pattern to a banner using a loom.
 * @since 1.17.40
 */
@ToString
public class LoomStackRequestAction implements ItemStackRequestAction {
    public String patternId;
    /**
     * @since 1.21.20
     */
    public int repetitions = 1;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_LOOM;
    }
}
