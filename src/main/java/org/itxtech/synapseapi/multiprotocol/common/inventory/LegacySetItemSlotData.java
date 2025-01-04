package org.itxtech.synapseapi.multiprotocol.common.inventory;

public class LegacySetItemSlotData {
    private static final byte[] EMPTY_SLOTS = new byte[0];

    public int containerId;
    public byte[] changedSlotIndexes = EMPTY_SLOTS;
}
