package org.itxtech.synapseapi.multiprotocol.common.inventory.request;

import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class ItemStackRequestSlotInfo {
    public int containerId;
    /**
     * @since 1.21.20
     */
    @Nullable
    public Integer dynamicContainerId;
    public int slotId;
    public int stackId;
}
