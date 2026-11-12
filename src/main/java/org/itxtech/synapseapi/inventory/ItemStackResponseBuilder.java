package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.network.protocol.types.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.itxtech.synapseapi.SynapsePlayer116;
import org.itxtech.synapseapi.inventory.InventoryManager.ItemStackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemStackResponseBuilder {
    private final SynapsePlayer116 player;
    private final int requestId;
    private final InventoryManager inventoryManager;

    private final Map<ContainerKey, IntSet> changedSlots = new LinkedHashMap<>();

    public ItemStackResponseBuilder(SynapsePlayer116 player, int requestId) {
        this.player = player;
        this.requestId = requestId;
        this.inventoryManager = player.getInventoryManager();
    }

    public void addSlot(int containerSlotType, int slotId) {
        addSlot(containerSlotType, null, slotId);
    }

    public void addSlot(int containerSlotType, @Nullable Integer dynamicContainerId, int slotId) {
        changedSlots.computeIfAbsent(new ContainerKey(containerSlotType, dynamicContainerId), key -> new IntOpenHashSet())
                .add(slotId);
    }

    @Nullable
    public ObjectIntPair<Inventory> getInventoryAndSlot(int containerSlotType, int slotId) {
        int windowId = ItemStackContainerIdTranslator.translate(player.getAbstractProtocol(), player.isNetEaseClient(), containerSlotType, inventoryManager.getCurrentWindowId());
        if (windowId == ContainerIds.OFFHAND) {
            //TODO: HACK! The client sends an incorrect slot ID for the offhand as of 1.19.70 (though this doesn't really matter since the offhand has only 1 slot anyway)
            slotId = 0;
        }
        ObjectIntPair<Inventory> windowAndSlot = inventoryManager.locateWindowAndSlot(windowId, slotId);
        if (windowAndSlot == null) {
            return null;
        }
        if (!windowAndSlot.left().slotExists(windowAndSlot.rightInt())) {
            return null;
        }
        return windowAndSlot;
    }

    public ItemStackResponse build() {
        int createdOutputTypeId = ItemStackContainerIdTranslator.getCreatedOutputSlotTypeId(player.getAbstractProtocol(), player.isNetEaseClient());

        List<ItemStackResponseContainerInfo> responseContainerInfos = new ArrayList<>();
        for (Map.Entry<ContainerKey, IntSet> entry : changedSlots.entrySet()) {
            ContainerKey container = entry.getKey();
            if (container.type() == createdOutputTypeId) {
                continue;
            }
            List<ItemStackResponseSlotInfo> slots = new ArrayList<>();
            for (int slotId : entry.getValue()) {
                Item item;
                ItemStackInfo stackInfo;
                if (container.dynamicId() != null) {
                    BundleContainerRegistry.Binding binding = inventoryManager.getBundleRegistry().get(container.dynamicId());
                    if (binding == null) {
                        continue;
                    }
                    stackInfo = binding.getInfo(slotId);
                    item = binding.getItem(slotId);
                } else {
                    ObjectIntPair<Inventory> inventoryAndSlot = getInventoryAndSlot(container.type(), slotId);
                    if (inventoryAndSlot == null) {
                        // a plugin may have closed the inventory during an event, or the slot may have been invalid
                        continue;
                    }
                    Inventory inventory = inventoryAndSlot.left();
                    int slot = inventoryAndSlot.rightInt();
                    stackInfo = inventoryManager.getItemStackInfo(inventory, slot);
                    item = inventory.getItem(slot);
                }
                if (stackInfo == null) {
                    throw new IllegalStateException("ItemStackInfo should never be null for an open inventory");
                }
                ItemStackResponseSlotInfo info = new ItemStackResponseSlotInfo();
                info.slot = slotId;
                info.hotbarSlot = slotId;
                info.count = item.getCount();
                info.itemStackId = stackInfo.stackId;
                info.customName = item.getCustomName();
                info.filteredCustomName = item.getFilteredCustomName();
                info.durabilityCorrection = item instanceof ItemDurable ? item.getDamage() : 0;
                slots.add(info);
            }
            if (!slots.isEmpty()) {
                ItemStackResponseContainerInfo info = new ItemStackResponseContainerInfo();
                info.containerId = container.type();
                info.dynamicContainerId = container.dynamicId();
                info.slots = slots.toArray(new ItemStackResponseSlotInfo[0]);
                responseContainerInfos.add(info);
            }
        }

        ItemStackResponse response = new ItemStackResponse();
        response.result = ItemStackResponse.RESULT_OK;
        response.requestId = requestId;
        response.containerInfos = responseContainerInfos.toArray(new ItemStackResponseContainerInfo[0]);
        return response;
    }

    private record ContainerKey(int type, @Nullable Integer dynamicId) {
    }
}
