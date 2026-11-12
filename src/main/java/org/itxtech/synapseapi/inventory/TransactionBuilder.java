package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.action.InventoryAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionBuilder {
    private final Map<Inventory, TransactionBuilderInventory> inventories = new HashMap<>();
    private final List<InventoryAction> extraActions = new ArrayList<>();

    public void addAction(InventoryAction action) {
        extraActions.add(action);
    }

    public TransactionBuilderInventory getInventory(Inventory inventory) {
        return inventories.computeIfAbsent(inventory, TransactionBuilderInventory::new);
    }

    boolean isBundlePathCurrent(Inventory inventory, int slot, int[] path) {
        TransactionBuilderInventory preview = inventories.get(inventory);
        return preview != null && preview.hasBundleContentChange(slot) && preview.isBundlePathCurrent(slot, path);
    }

    public List<InventoryAction> generateActions() {
        List<InventoryAction> actions = new ArrayList<>(extraActions);
        for (TransactionBuilderInventory inventory : inventories.values()) {
            actions.addAll(inventory.generateActions());
        }
        return actions;
    }
}
