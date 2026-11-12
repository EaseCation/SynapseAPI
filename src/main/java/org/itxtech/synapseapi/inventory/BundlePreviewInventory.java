package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.item.Items;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.List;
import java.util.Arrays;

/**
 * 请求内的收纳袋视图，修改始终合并到父物品槽的预览中。
 */
final class BundlePreviewInventory extends TransactionBuilderInventory {
    private final TransactionBuilderInventory root;
    private final int rootSlot;
    private final int[] path;

    BundlePreviewInventory(TransactionBuilder builder, BundleContainerRegistry.Binding binding) {
        super(binding.getInventory());
        root = builder.getInventory(binding.getInventory());
        rootSlot = binding.getRootSlot();
        path = binding.getPath();
    }

    @Override
    public int getSize() {
        return ItemBundle.MAX_STORAGE_SLOTS;
    }

    @Override
    public boolean slotExists(int slot) {
        return slot >= 0 && slot < ItemBundle.MAX_STORAGE_SLOTS;
    }

    private ItemBundle getBundle() {
        if (!root.isBundlePathCurrent(rootSlot, path)) {
            throw new ItemStackRequestProcessException("The bundle binding was replaced during the request");
        }
        Item item = root.getItem(rootSlot);
        for (int slot : path) {
            if (!(item instanceof ItemBundle bundle)) {
                throw new ItemStackRequestProcessException("The parent bundle moved during the request");
            }
            Int2ObjectMap<Item> contents = bundle.getContents();
            if (contents == null) {
                throw new ItemStackRequestProcessException("Invalid bundle contents");
            }
            item = contents.getOrDefault(slot, Items.air());
        }
        if (!(item instanceof ItemBundle bundle)) {
            throw new ItemStackRequestProcessException("The requested bundle no longer exists");
        }
        return bundle;
    }

    @Override
    public Item getItem(int index) {
        if (!slotExists(index)) {
            throw new ItemStackRequestProcessException("Invalid bundle slot");
        }
        return getContents().getOrDefault(index, Items.air()).clone();
    }

    @Override
    public Int2ObjectMap<Item> getContents() {
        Int2ObjectMap<Item> contents = getBundle().getContents();
        if (contents == null) {
            throw new ItemStackRequestProcessException("Invalid bundle contents");
        }
        return contents;
    }

    @Override
    public boolean setItem(int index, Item item, boolean send) {
        if (!slotExists(index)) {
            return false;
        }
        ItemBundle bundle = getBundle();
        Int2ObjectMap<Item> contents = bundle.getContents();
        if (contents == null) {
            return false;
        }
        Item previous = contents.getOrDefault(index, Items.air());
        if (item.isNull()) {
            contents.remove(index);
        } else {
            contents.put(index, item.clone());
        }
        if (!bundle.setContents(contents)) {
            return false;
        }
        if (previous instanceof ItemBundle || item instanceof ItemBundle) {
            int[] changedPath = Arrays.copyOf(path, path.length + 1);
            changedPath[path.length] = index;
            root.invalidateBundlePath(rootSlot, changedPath);
        }
        Item replacement = bundle;
        for (int depth = path.length - 1; depth >= 0; depth--) {
            Item parent = root.getItem(rootSlot);
            for (int indexInPath = 0; indexInPath < depth; indexInPath++) {
                if (!(parent instanceof ItemBundle parentBundle)) {
                    return false;
                }
                Int2ObjectMap<Item> parentContents = parentBundle.getContents();
                if (parentContents == null) {
                    return false;
                }
                parent = parentContents.getOrDefault(path[indexInPath], Items.air());
            }
            if (!(parent instanceof ItemBundle parentBundle)) {
                return false;
            }
            Int2ObjectMap<Item> parentContents = parentBundle.getContents();
            if (parentContents == null) {
                return false;
            }
            parentContents.put(path[depth], replacement);
            if (!parentBundle.setContents(parentContents)) {
                return false;
            }
            replacement = parentBundle;
        }
        return replacement instanceof ItemBundle changedBundle && root.setBundleItem(rootSlot, changedBundle);
    }

    @Override
    public boolean clear(int index, boolean send) {
        return setItem(index, Items.air(), send);
    }

    @Override
    public List<SlotChangeAction> generateActions() {
        return List.of();
    }
}
