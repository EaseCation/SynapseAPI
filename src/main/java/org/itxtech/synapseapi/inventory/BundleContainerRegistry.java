package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.DoubleChestInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.item.Items;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.itxtech.synapseapi.SynapsePlayer116;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12130.protocol.ContainerRegistryCleanupPacket12030;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * 每玩家的收纳袋动态容器绑定，动态编号不写回服务端物品。
 */
public final class BundleContainerRegistry {
    private final SynapsePlayer116 player;
    private final InventoryManager manager;
    private final Int2ObjectMap<Binding> bindings = new Int2ObjectOpenHashMap<>();
    private final IntSet pendingSyncs = new IntOpenHashSet();
    private int nextId = 1;

    BundleContainerRegistry(SynapsePlayer116 player, InventoryManager manager) {
        this.player = player;
        this.manager = manager;
    }

    public boolean isSupported() {
        AbstractProtocol protocol = player.getAbstractProtocol();
        return protocol != null && protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart();
    }

    public int getContainerType() {
        return player.isNetEaseClient() ? ItemStackContainerIdTranslator.N12120_DYNAMIC : ItemStackContainerIdTranslator.V12120_DYNAMIC;
    }

    public int getNextContainerId() {
        return nextId;
    }

    public void setNextContainerId(int nextId) {
        if (nextId <= 0 || nextId == Integer.MAX_VALUE || nextId < this.nextId) {
            throw new IllegalArgumentException("Invalid dynamic container sequence");
        }
        this.nextId = nextId;
    }

    public void restoreContainerSequence(int previousNextId) {
        if (previousNextId <= 0 || previousNextId >= Integer.MAX_VALUE - 50) {
            throw new IllegalArgumentException("Invalid transferred dynamic container sequence");
        }
        setNextContainerId(previousNextId + 50);
    }

    public boolean hasBinding(Inventory inventory, int slot) {
        for (Binding binding : bindings.values()) {
            if (binding.inventory == inventory && binding.rootSlot == slot) {
                return true;
            }
        }
        return false;
    }

    public Item prepare(Inventory inventory, int slot, Item item) {
        if (!isSupported()) {
            return item;
        }
        Item identity = getRootIdentity(inventory, slot);
        IntList replaced = new IntArrayList();
        for (Binding binding : bindings.values()) {
            if (binding.inventory == inventory && binding.rootSlot == slot && binding.rootIdentity != identity
                    && (!manager.preservesBundle(binding.id) || !isPublishing(inventory, slot))) {
                replaced.add(binding.id);
            }
        }
        remove(replaced);
        IntSet visited = new IntOpenHashSet();
        Item result = prepare(inventory, slot, new int[0], item, visited);
        IntList removed = new IntArrayList();
        for (Binding binding : bindings.values()) {
            if (binding.inventory == inventory && binding.rootSlot == slot && !visited.contains(binding.id)) {
                removed.add(binding.id);
            }
        }
        remove(removed);
        return result;
    }

    private static Item getRootIdentity(Inventory inventory, int slot) {
        if (inventory instanceof DoubleChestInventory chest) {
            int leftSize = chest.getLeftSide().getSize();
            return slot < leftSize ? getRootIdentity(chest.getLeftSide(), slot)
                    : getRootIdentity(chest.getRightSide(), slot - leftSize);
        }
        if (inventory instanceof BaseInventory base && base.slots != null) {
            return base.slots.get(slot);
        }
        return inventory.getContents().get(slot);
    }

    private static boolean isPublishing(Inventory inventory, int slot) {
        if (inventory instanceof DoubleChestInventory chest) {
            int leftSize = chest.getLeftSide().getSize();
            return slot < leftSize ? isPublishing(chest.getLeftSide(), slot)
                    : isPublishing(chest.getRightSide(), slot - leftSize);
        }
        return inventory instanceof BaseInventory base && base.isPublishingAuthoritativeSlotChange(slot);
    }

    IntSet getPreservedIds(TransactionBuilder builder) {
        IntSet preserved = new IntOpenHashSet();
        for (Binding binding : bindings.values()) {
            if (builder.isBundlePathCurrent(binding.inventory, binding.rootSlot, binding.path)) {
                preserved.add(binding.id);
            }
        }
        return preserved;
    }

    private Item prepare(Inventory inventory, int rootSlot, int[] path, Item item, IntSet visited) {
        if (!(item instanceof ItemBundle bundle)) {
            return item;
        }
        Int2ObjectMap<Item> contents = bundle.getContents();
        if (contents == null) {
            return item;
        }
        Binding binding = null;
        for (Binding candidate : bindings.values()) {
            if (candidate.inventory == inventory && candidate.rootSlot == rootSlot && Arrays.equals(candidate.path, path)) {
                binding = candidate;
                break;
            }
        }
        if (binding == null) {
            if (nextId == Integer.MAX_VALUE) {
                throw new IllegalStateException("Dynamic container IDs have been exhausted");
            }
            binding = new Binding(nextId++, inventory, rootSlot, path);
            bindings.put(binding.id, binding);
        }
        visited.add(binding.id);
        boolean changed = binding.storage == null || !binding.storage.equalsExact(bundle);
        Item[] networkContents = new Item[ItemBundle.MAX_STORAGE_SLOTS];
        for (int slot = 0; slot < networkContents.length; slot++) {
            Item content = contents.getOrDefault(slot, Items.air());
            Item previous = binding.contents.get(slot);
            if (previous == null || !previous.equalsExact(content)) {
                binding.infos.put(slot, new InventoryManager.ItemStackInfo(manager.getPredictionRequestId(),
                        content.isNull() ? 0 : manager.newItemStackId()));
            }
            int[] childPath = Arrays.copyOf(path, path.length + 1);
            childPath[path.length] = slot;
            Item networkItem = prepare(inventory, rootSlot, childPath, content, visited).clone();
            networkItem.setItemStackId(binding.infos.get(slot).stackId);
            networkContents[slot] = networkItem;
        }
        binding.storage = (ItemBundle) bundle.clone();
        binding.rootIdentity = getRootIdentity(inventory, rootSlot);
        binding.contents = contents;
        binding.networkContents = networkContents;
        if (changed) {
            pendingSyncs.add(binding.id);
        }
        ItemBundle networkBundle = (ItemBundle) bundle.clone();
        networkBundle.setDynamicId(binding.id);
        return networkBundle;
    }

    @Nullable
    public Binding get(int id) {
        Binding binding = bindings.get(id);
        if (binding == null || !manager.isInventoryWindowAccessible(binding.inventory)
                || manager.getItemStackInfo(binding.inventory, binding.rootSlot) == null) {
            return null;
        }
        if (binding.rootIdentity != getRootIdentity(binding.inventory, binding.rootSlot)) {
            return null;
        }
        Item current = binding.inventory.getItem(binding.rootSlot);
        for (int slot : binding.path) {
            if (!(current instanceof ItemBundle bundle)) {
                return null;
            }
            Int2ObjectMap<Item> contents = bundle.getContents();
            if (contents == null) {
                return null;
            }
            current = contents.getOrDefault(slot, Items.air());
        }
        return current instanceof ItemBundle && binding.storage != null && current.equalsExact(binding.storage) ? binding : null;
    }

    public void removeInventory(Inventory inventory) {
        IntList removed = new IntArrayList();
        for (Binding binding : bindings.values()) {
            if (binding.inventory == inventory) {
                removed.add(binding.id);
            }
        }
        remove(removed);
    }

    private void remove(IntList ids) {
        if (ids.isEmpty()) {
            return;
        }
        ContainerRegistryCleanupPacket12030 packet = new ContainerRegistryCleanupPacket12030();
        @SuppressWarnings("unchecked")
        IntObjectPair<Integer>[] removed = new IntObjectPair[ids.size()];
        for (int index = 0; index < ids.size(); index++) {
            int id = ids.getInt(index);
            bindings.remove(id);
            pendingSyncs.remove(id);
            removed[index] = IntObjectPair.of(getContainerType(), id);
        }
        packet.removedContainers = removed;
        player.dataPacket(packet);
    }

    public void flush() {
        IntList ready = new IntArrayList(pendingSyncs);
        pendingSyncs.clear();
        for (int id : ready) {
            Binding binding = get(id);
            if (binding == null) {
                continue;
            }
            InventoryContentPacket packet = new InventoryContentPacket();
            packet.inventoryId = ContainerIds.REGISTRY;
            packet.containerSlotType = getContainerType();
            packet.dynamicContainerId = id;
            packet.storageItem = binding.storage.getShell();
            packet.slots = binding.networkContents;
            player.dataPacket(packet);
        }
    }

    public static final class Binding {
        private final int id;
        private final Inventory inventory;
        private final int rootSlot;
        private final int[] path;
        @Nullable
        private ItemBundle storage;
        @Nullable
        private Item rootIdentity;
        private Int2ObjectMap<Item> contents = new Int2ObjectOpenHashMap<>();
        private final Int2ObjectMap<InventoryManager.ItemStackInfo> infos = new Int2ObjectOpenHashMap<>();
        private Item[] networkContents = new Item[0];

        private Binding(int id, Inventory inventory, int rootSlot, int[] path) {
            this.id = id;
            this.inventory = inventory;
            this.rootSlot = rootSlot;
            this.path = path;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public int getRootSlot() {
            return rootSlot;
        }

        public int[] getPath() {
            return path.clone();
        }

        public Item getItem(int slot) {
            return contents.getOrDefault(slot, Items.air()).clone();
        }

        @Nullable
        public InventoryManager.ItemStackInfo getInfo(int slot) {
            return infos.get(slot);
        }
    }
}
