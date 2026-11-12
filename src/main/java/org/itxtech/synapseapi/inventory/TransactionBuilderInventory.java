package org.itxtech.synapseapi.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.item.Items;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.*;

public class TransactionBuilderInventory extends BaseInventory {
    private final Inventory actualInventory;
    private final List<BundlePath> invalidatedBundlePaths = new ArrayList<>();
    private final IntSet bundleContentChanges = new IntOpenHashSet();

    public TransactionBuilderInventory(Inventory actualInventory) {
        super(new ActualInventory(actualInventory), actualInventory.getType(), Collections.emptyMap(), actualInventory.getSize());
        this.actualInventory = actualInventory;
    }

    public Inventory getActualInventory() {
        return actualInventory;
    }

    @Override
    protected void initSetContents(Map<Integer, Item> items) {
    }

    @Override
    public Item getItem(int index) {
        Item newItem = this.slots.get(index);
        return newItem != null ? newItem.clone() : actualInventory.getItem(index);
    }

    @Override
    public boolean setItem(int index, Item item, boolean send) {
        if (index < 0 || index >= this.size) {
            return false;
        }
        if (item.isNull()) {
            item = Items.air();
        }
        if (getItem(index) instanceof ItemBundle || item instanceof ItemBundle) {
            invalidateBundlePath(index, new int[0]);
        }
        this.slots.put(index, item.clone());
        return true;
    }

    void invalidateBundlePath(int rootSlot, int[] path) {
        invalidatedBundlePaths.add(new BundlePath(rootSlot, path.clone()));
    }

    boolean isBundlePathCurrent(int rootSlot, int[] path) {
        for (BundlePath invalid : invalidatedBundlePaths) {
            if (invalid.rootSlot() != rootSlot || invalid.path().length > path.length) {
                continue;
            }
            boolean prefix = true;
            for (int index = 0; index < invalid.path().length; index++) {
                if (invalid.path()[index] != path[index]) {
                    prefix = false;
                    break;
                }
            }
            if (prefix) {
                return false;
            }
        }
        return true;
    }

    boolean setBundleItem(int slot, ItemBundle item) {
        if (!slotExists(slot)) {
            return false;
        }
        slots.put(slot, item.clone());
        bundleContentChanges.add(slot);
        return true;
    }

    boolean hasBundleContentChange(int slot) {
        return bundleContentChanges.contains(slot);
    }

    private record BundlePath(int rootSlot, int[] path) {
    }

    @Override
    public boolean clear(int index, boolean send) {
        return setItem(index, Items.air());
    }

    @Override
    public Int2ObjectMap<Item> getContents() {
        Int2ObjectMap<Item> contents = actualInventory.getContents();
        for (Int2ObjectMap.Entry<Item> entry : contents.int2ObjectEntrySet()) {
            entry.setValue(entry.getValue().clone());
        }
        for (Int2ObjectMap.Entry<Item> entry : this.slots.int2ObjectEntrySet()) {
            int slot = entry.getIntKey();
            Item newItem = entry.getValue();
            if (newItem.isNull()) {
                contents.remove(slot);
            } else {
                contents.put(slot, newItem.clone());
            }
        }
        return contents;
    }

    @Override
    public void sendContents(Player... players) {
    }

    @Override
    public void sendSlot(int index, Player... players) {
    }

    @Override
    public Set<Player> getViewers() {
        return actualInventory.getViewers();
    }

    @Override
    public InventoryType getType() {
        return actualInventory.getType();
    }

    @Override
    public void onOpen(Player who) {
    }

    @Override
    public boolean open(Player who) {
        return false;
    }

    @Override
    public void close(Player who) {
    }

    @Override
    public void onClose(Player who) {
    }

    @Override
    public void onSlotChange(int index, Item before, Item after, boolean send) {
    }

    public List<SlotChangeAction> generateActions() {
        List<SlotChangeAction> result = new ArrayList<>();
        for (Int2ObjectMap.Entry<Item> entry : this.slots.int2ObjectEntrySet()) {
            int slot = entry.getIntKey();
            Item newItem = entry.getValue();
            Item oldItem = actualInventory.getItem(slot);
            if (!newItem.equalsExact(oldItem)) {
                result.add(new SlotChangeAction(actualInventory, slot, oldItem, newItem.clone()));
            }
        }
        return result;
    }

    private static class ActualInventory implements InventoryHolder {
        private final Inventory inventory;

        public ActualInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
