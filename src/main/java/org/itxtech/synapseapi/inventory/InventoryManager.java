package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBundle;
import cn.nukkit.item.Items;
import cn.nukkit.item.NetworkItemHelper;
import cn.nukkit.item.enchantment.EnchantmentOption;
import cn.nukkit.level.Level;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.ContainerOpener;
import cn.nukkit.network.protocol.types.ContainerType;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapsePlayer116;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.PlayerEnchantOptionsPacket116;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

@Log4j2
public class InventoryManager implements InventoryListener {
    private static final Item EMPTY_ITEM_STACK = Items.air();
    private static final int UNTRACKED_ITEM_STACK_ID = 1;

    private final SynapsePlayer116 player;
    private final BundleContainerRegistry bundles;

    private final Map<Inventory, Entry> inventories = new Object2ObjectOpenHashMap<>();

    private final Int2ObjectMap<Inventory> networkIdToInventoryMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<ComplexInventoryMapEntry> complexSlotToInventoryMap = new Int2ObjectOpenHashMap<>();

    private int lastInventoryNetworkId = ContainerIds.FIRST;
    private int lastAllocatedWindowId = ContainerIds.FIRST;
    private int currentWindowType = ContainerType.CONTAINER;
    private boolean editorWindowOpen;
    @Nullable
    private BlockEditorTarget blockEditorTarget;
    private boolean clientClosingWindow;
    @Nullable
    private DataPacket currentOpenPacket;
    @Nullable
    private Level currentWindowLevel;

    private int clientSelectedHotbarSlot = -1;

    private int pendingCloseWindowId = Integer.MIN_VALUE;
    @Nullable
    private Inventory pendingCloseInventory;
    @Nullable
    private Inventory pendingOpenInventory;
    private int pendingOpenWindowId = Integer.MIN_VALUE;
    @Nullable
    private Runnable pendingOpenWindowCallback;
    @Nullable
    private Inventory preparingInventory;
    private int preparingWindowId = Integer.MIN_VALUE;
    private long windowOpenGeneration;
    private final Int2IntMap pendingWindowProperties = new Int2IntLinkedOpenHashMap();

    private int nextItemStackId = 2; // 0 empty, 1 for untraceable
    private int currentItemStackRequestId = Integer.MAX_VALUE;

    private boolean fullSyncRequested;
    private boolean committingBundleTransaction;
    private IntSet preservedBundleIds = new IntOpenHashSet();

    private final Int2IntMap enchantingTableOptions = new Int2IntOpenHashMap();

    //TODO: this should be based on the total number of crafting recipes - if there are ever 5k recipes, this will conflict with regular recipes
    private int nextDynamicRecipeId = 5000;

    public InventoryManager(SynapsePlayer116 player) {
        this.player = player;
        bundles = new BundleContainerRegistry(player, this);
        enchantingTableOptions.defaultReturnValue(-1);
    }

    public void initialize() {
        if (!player.isServerAuthoritativeInventoryEnabled()) {
            return;
        }

        add(ContainerIds.INVENTORY, player.getInventory());
        add(ContainerIds.OFFHAND, player.getOffhandInventory());
        add(ContainerIds.ARMOR, player.getArmorInventory());
        addComplex(UiContainerSlotMapping.CURSOR, player.getCursorInventory());
        addComplex(UiContainerSlotMapping.CRAFTING2X2_INPUT, player.getCraftingGrid());
    }

    private void associateIdWithInventory(int id, Inventory inventory) {
        networkIdToInventoryMap.put(id, inventory);
    }

    private int getNewWindowId() {
        do {
            lastAllocatedWindowId = Math.max(ContainerIds.FIRST, (lastAllocatedWindowId + 1) % ContainerIds.LAST);
        } while (lastAllocatedWindowId == pendingCloseWindowId || networkIdToInventoryMap.containsKey(lastAllocatedWindowId));
        return lastAllocatedWindowId;
    }

    private void add(int id, Inventory inventory) {
        if (inventories.putIfAbsent(inventory, new Entry(inventory)) != null) {
            throw new IllegalArgumentException("Inventory " + inventory.getClass().getSimpleName() + " is already tracked");
        }
        associateIdWithInventory(id, inventory);
    }

    private void addComplex(Int2IntMap slotMap, Inventory inventory) {
        ComplexInventoryMapEntry complexSlotMap = new ComplexInventoryMapEntry(inventory, slotMap);
        if (inventories.putIfAbsent(inventory, new Entry(inventory, complexSlotMap)) != null) {
            throw new IllegalArgumentException("Inventory " + inventory.getClass().getSimpleName() + " is already tracked");
        }
        for (int netSlot : complexSlotMap.slotMap.keySet()) {
            complexSlotToInventoryMap.put(netSlot, complexSlotMap);
        }
    }

    private boolean remove(int id) {
        Inventory inventory = networkIdToInventoryMap.remove(id);
        if (inventory == null) {
            return false;
        }
        if (getRegisteredWindowId(inventory) != Integer.MIN_VALUE) {
            return true;
        }
        inventories.remove(inventory);
        bundles.removeInventory(inventory);
        complexSlotToInventoryMap.values().removeIf(entry -> entry.inventory == inventory);
        return true;
    }

    public int getWindowId(Inventory inventory) {
        int windowId = getRegisteredWindowId(inventory);
        if (windowId != Integer.MIN_VALUE) {
            return windowId;
        }
        if (inventory != null && preparingInventory == inventory) {
            return preparingWindowId;
        }
        return inventory != null && pendingOpenInventory == inventory ? pendingOpenWindowId : Integer.MIN_VALUE;
    }

    public boolean isWindowOpen(Inventory inventory) {
        return inventory != null && getRegisteredWindowId(inventory) != Integer.MIN_VALUE;
    }

    public boolean isAwaitingWindowClose(Inventory inventory) {
        return inventory != null && pendingCloseInventory == inventory && pendingCloseWindowId != Integer.MIN_VALUE;
    }

    private int getRegisteredWindowId(Inventory inventory) {
        for (Int2ObjectMap.Entry<Inventory> entry : networkIdToInventoryMap.int2ObjectEntrySet()) {
            if (entry.getValue() != inventory) {
                continue;
            }
            return entry.getIntKey();
        }
        return Integer.MIN_VALUE;
    }

    @Override
    @Nullable
    public Inventory getWindowById(int id) {
        return networkIdToInventoryMap.get(id);
    }

    public int getCurrentWindowId() {
        return lastInventoryNetworkId;
    }

    public boolean isInventoryWindowAccessible(Inventory inventory) {
        return !player.isInputTransferCommitted()
                && (inventory == player.getInventory() || inventory != networkIdToInventoryMap.get(lastInventoryNetworkId)
                || currentWindowLevel == player.getLevel());
    }

    public BundleContainerRegistry getBundleRegistry() {
        return bundles;
    }

    public void beginBundleTransaction(IntSet preservedIds) {
        committingBundleTransaction = true;
        preservedBundleIds = new IntOpenHashSet(preservedIds);
    }

    public void endBundleTransaction() {
        committingBundleTransaction = false;
        preservedBundleIds.clear();
    }

    boolean preservesBundle(int id) {
        return committingBundleTransaction && preservedBundleIds.contains(id);
    }

    int getPredictionRequestId() {
        return currentItemStackRequestId < 0 ? currentItemStackRequestId : -1;
    }

    @Nullable
    public ObjectIntPair<Inventory> locateWindowAndSlot(int windowId, int netSlotId) {
        if (windowId == ContainerIds.UI) {
            ComplexInventoryMapEntry entry = complexSlotToInventoryMap.get(netSlotId);
            if (entry == null) {
                return null;
            }
            Inventory inventory = entry.inventory;
            int coreSlotId = entry.mapNetToCore(netSlotId);
            if (coreSlotId == -1 || !inventory.isHolderValid() || !isInventoryWindowAccessible(inventory)
                    || !inventory.slotExists(coreSlotId)) {
                return null;
            }
            return ObjectIntPair.of(inventory, coreSlotId);
        }
        Inventory inventory = networkIdToInventoryMap.get(windowId);
        if (inventory == null || !inventory.isHolderValid() || !isInventoryWindowAccessible(inventory)
                || !inventory.slotExists(netSlotId)) {
            return null;
        }
        return ObjectIntPair.of(inventory, netSlotId);
    }

    public void addPredictedSlotChange(Inventory inventory, int slot, Item item) {
        Entry entry = inventories.get(inventory);
        if (entry == null || !inventory.slotExists(slot)) {
            throw new IllegalArgumentException("Cannot predict an untracked inventory slot");
        }
        entry.predictions.put(slot, item.clone());
        entry.networkPredictions.remove(slot);
    }

    public void addTransactionPredictedSlotChanges(InventoryTransaction tx) {
        LongSet knownHashes = new LongOpenHashSet();
        LongSet pendingHashes = new LongOpenHashSet();
        for (InventoryAction action : tx.getActions()) {
            if (action instanceof SlotChangeAction slotChange) {
                Item original = NetworkItemHelper.project(action.getSourceItemUnsafe());
                if (original.hasCompoundTag() && original.getNamedTag().contains(NetworkItemHelper.SERVER_DATA_HASH)) {
                    long hash = original.getNamedTag().getLong(NetworkItemHelper.SERVER_DATA_HASH);
                    Entry entry = inventories.get(slotChange.getInventory());
                    if (entry != null && entry.pendingSyncs.containsKey(slotChange.getSlot())) {
                        pendingHashes.add(hash);
                    } else {
                        knownHashes.add(hash);
                    }
                }
            }
        }
        for (InventoryAction action : tx.getActions()) {
            if (action instanceof SlotChangeAction slotChange) {
                Inventory inventory = slotChange.getInventory();
                int slot = slotChange.getSlot();
                Item target = slotChange.getTargetItem();
                addPredictedSlotChange(inventory, slot, target);
                Item projected = NetworkItemHelper.project(target);
                if (projected.hasCompoundTag() && projected.getNamedTag().contains(NetworkItemHelper.SERVER_DATA_HASH)
                        && (pendingHashes.contains(projected.getNamedTag().getLong(NetworkItemHelper.SERVER_DATA_HASH))
                        || !knownHashes.contains(projected.getNamedTag().getLong(NetworkItemHelper.SERVER_DATA_HASH)))) {
                    // 尚未下发的摘要须跟随移动或拆分继续纠正；只有客户端已知的摘要可省略 NBT 同步。
                    inventories.get(inventory).pendingSyncs.put(slot, target);
                }
            }
        }
    }

    public void addRawPredictedSlotChanges(NetworkInventoryAction[] networkInventoryActions) {
        for (NetworkInventoryAction action : networkInventoryActions) {
            if (action.sourceType != NetworkInventoryAction.SOURCE_CONTAINER) {
                continue;
            }

            int windowId = action.windowId;
            if (windowId != ContainerIds.INVENTORY && windowId != ContainerIds.OFFHAND && windowId != ContainerIds.ARMOR) {
                // legacy transactions should not modify or predict anything other than these inventories,
                // since these are the only ones accessible when not in-game (ItemStackRequest is used for everything else)
                throw new IllegalArgumentException("Legacy transactions cannot predict changes to inventory with ID " + windowId);
            }

            ObjectIntPair<Inventory> info = locateWindowAndSlot(windowId, action.inventorySlot);
            if (info == null) {
                continue;
            }

            Inventory inventory = info.left();
            int slot = info.rightInt();
            Item fullPrediction = inventory.getItem(slot);
            fullPrediction.setCount(action.newItem.getCount());
            fullPrediction.setDamage(action.newItem.getDamage());
            if (action.newItem.isNull()) {
                addPredictedSlotChange(inventory, slot, Items.air());
            } else if (NetworkItemHelper.matches(fullPrediction, action.newItem, true)) {
                addPredictedSlotChange(inventory, slot, fullPrediction);
            } else {
                Entry entry = inventories.get(inventory);
                entry.predictions.put(slot, action.newItem.clone());
                entry.networkPredictions.add(slot);
            }
        }
    }

    public void setCurrentItemStackRequestId(int id) {
        currentItemStackRequestId = id;
    }

    public void removeCurrentItemStackRequestId() {
        setCurrentItemStackRequestId(Integer.MAX_VALUE);
    }

    /**
     * When the server initiates a window close, it does so by sending a ContainerClose to the client, which causes the
     * client to behave as if it initiated the close itself. It responds by sending a ContainerClose back to the server,
     * which the server is then expected to respond to.
     *
     * Sending the client a new window before sending this final response creates buggy behaviour on the client, which
     * is problematic when switching windows. Therefore, we defer sending any new windows until after the client
     * responds to our window close instruction, so that we can complete the window handshake correctly.
     *
     * This is a pile of complicated garbage that only exists because Mojang overengineered the process of opening and
     * closing inventory windows.
     */
    private void openWindowDeferred(@Nullable Inventory inventory, int windowId, Runnable action) {
        if (pendingCloseWindowId != Integer.MIN_VALUE) {
            log.debug("{} deferring opening of new window, waiting for close ack of window {}", player.getName(), pendingCloseWindowId);
            pendingOpenInventory = inventory;
            pendingOpenWindowId = windowId;
            pendingOpenWindowCallback = action;
        } else {
            action.run();
        }
    }

    private void clearPendingOpenWindow() {
        pendingOpenInventory = null;
        pendingOpenWindowId = Integer.MIN_VALUE;
        pendingOpenWindowCallback = null;
    }

    private Int2IntMap createComplexSlotMapping(Inventory inventory) {
        if (inventory instanceof CraftingInventory) {
            return UiContainerSlotMapping.CRAFTING3X3_INPUT ;
        }
        if (inventory instanceof EnchantInventory) {
            return UiContainerSlotMapping.ENCHANTING_TABLE;
        }
        if (inventory instanceof AnvilInventory) {
            return UiContainerSlotMapping.ANVIL;
        }
        if (inventory instanceof StonecutterInventory) {
            return UiContainerSlotMapping.STONE_CUTTER_INPUT;
        }
        if (inventory instanceof SmithingTableInventory) {
            return UiContainerSlotMapping.SMITHING_TABLE;
        }
        if (inventory instanceof GrindstoneInventory) {
            return UiContainerSlotMapping.GRINDSTONE;
        }
        if (inventory instanceof LoomInventory) {
            return UiContainerSlotMapping.LOOM;
        }
        if (inventory instanceof CartographyTableInventory) {
            return UiContainerSlotMapping.CARTOGRAPHY_TABLE;
        }
        if (inventory instanceof BeaconInventory) {
            return UiContainerSlotMapping.BEACON_PAYMENT;
        }
//        if (inventory instanceof TradeInventory) {
//            return UiContainerSlotMapping.TRADE2_INGREDIENT;
//        }
//        if (inventory instanceof TradeV1Inventory) {
//            return UiContainerSlotMapping.TRADE_INGREDIENT;
//        }
        return null;
    }

    public void onCurrentWindowChange(Inventory inventory) {
        onCurrentWindowRemove();

        // 先为插件预留窗口编号，收到关闭确认前不注册可被客户端请求访问的库存。
        int windowId = getNewWindowId();
        long generation = windowOpenGeneration;
        Level origin = player.getLevel();
        currentWindowLevel = origin;
        openWindowDeferred(inventory, windowId, () -> {
            if (generation != windowOpenGeneration || player.getLevel() != origin || player.isInputTransferCommitted()) {
                return;
            }
            preparingInventory = inventory;
            preparingWindowId = windowId;
            if (!inventory.prepareAuthoritativeOpen(player, () -> completeWindowOpen(inventory, windowId, generation, origin))) {
                completeWindowOpen(inventory, windowId, generation, origin);
            }
        });
    }

    private void completeWindowOpen(Inventory inventory, int windowId, long generation, Level origin) {
        if (generation != windowOpenGeneration || preparingInventory != inventory || preparingWindowId != windowId
                || player.isClosed() || player.getLevel() != origin || player.isInputTransferCommitted()) {
            return;
        }
        preparingInventory = null;
        preparingWindowId = Integer.MIN_VALUE;
        DataPacket packet = createContainerOpenPacket(windowId, inventory);
        if (packet == null) {
            pendingWindowProperties.clear();
            return;
        }
        Int2IntMap slotMap = createComplexSlotMapping(inventory);
        if (slotMap != null) {
            addComplex(slotMap, inventory);
            associateIdWithInventory(windowId, inventory);
        } else {
            add(windowId, inventory);
        }
        lastInventoryNetworkId = windowId;
        // workaround useless bullshit in 1.21 - ContainerClose requires a type now for some reason
        currentWindowType = packet instanceof ContainerOpener opener ? opener.getWindowType() : ContainerType.CONTAINER;
        currentOpenPacket = packet;
        player.dataPacket(packet);

        if (generation != windowOpenGeneration || getRegisteredWindowId(inventory) != windowId
                || player.getLevel() != origin || player.isInputTransferCommitted()) {
            return;
        }

        syncContents(inventory);
        flushPendingWindowProperties(inventory, windowId);
        if (generation == windowOpenGeneration && getRegisteredWindowId(inventory) == windowId
                && player.getLevel() == origin && !player.isInputTransferCommitted()) {
            player.onAuthoritativeInventoryOpened(inventory);
        }
    }

    @Nullable
    protected DataPacket createContainerOpenPacket(int windowId, Inventory inventory) {
        return inventory.createContainerOpenPacket(player, windowId);
    }

    public void onClientOpenMainInventory() {
        if (!player.tryRemoveCurrentWindow()) {
            return;
        }
        player.restoreTemporaryInventoryItems();
        onCurrentWindowRemove();

        int windowId = getNewWindowId();
        Level origin = player.getLevel();
        currentWindowLevel = origin;
        openWindowDeferred(player.getInventory(), windowId, () -> {
            if (player.getLevel() != origin || player.isInputTransferCommitted()) {
                return;
            }
            associateIdWithInventory(windowId, player.getInventory());
            lastInventoryNetworkId = windowId;
            currentWindowType = ContainerType.INVENTORY;

            ContainerOpenPacket packet = new ContainerOpenPacket();
            packet.windowId = windowId;
            packet.type = currentWindowType;
            packet.x = player.getFloorX();
            packet.y = player.getFloorY();
            packet.z = player.getFloorZ();
//            packet.entityId = player.getId();
            currentOpenPacket = packet;
            player.dataPacket(packet);
        });
    }

    public boolean isCurrentBlockEditor(int x, int y, int z, int type) {
        BlockEditorTarget target = blockEditorTarget;
        return target != null && target.level() == player.getLevel()
                && target.x() == x && target.y() == y && target.z() == z && target.type() == type;
    }

    public void openBlockEditor(int x, int y, int z, int type) {
        if (isCurrentBlockEditor(x, y, z, type)) {
            return;
        }
        onCurrentWindowRemove();
        // 等待旧窗口关闭时保留目标去重，发送后是否继续跟踪由界面类型决定。
        blockEditorTarget = new BlockEditorTarget(player.getLevel(), x, y, z, type);
        int windowId = getNewWindowId();
        Level origin = player.getLevel();
        currentWindowLevel = origin;
        openWindowDeferred(null, windowId, () -> {
            if (player.getLevel() != origin || player.isInputTransferCommitted()) {
                return;
            }
            lastInventoryNetworkId = windowId;
            currentWindowType = type;
            // 讲台打开的是书本界面，关闭时不发送 ContainerClose，不能等待它的关闭确认。
            editorWindowOpen = type != ContainerType.LECTERN;
            ContainerOpenPacket packet = new ContainerOpenPacket();
            packet.windowId = windowId;
            packet.type = type;
            packet.x = x;
            packet.y = y;
            packet.z = z;
            currentOpenPacket = editorWindowOpen ? packet : null;
            if (!editorWindowOpen) {
                blockEditorTarget = null;
            }
            player.dataPacket(packet);
        });
    }

    public void onCurrentWindowRemove() {
        windowOpenGeneration++;
        currentWindowLevel = null;
        preparingInventory = null;
        preparingWindowId = Integer.MIN_VALUE;
        blockEditorTarget = null;
        pendingWindowProperties.clear();
        clearPendingOpenWindow();
        Inventory closingInventory = networkIdToInventoryMap.get(lastInventoryNetworkId);
        if (!remove(lastInventoryNetworkId) && !editorWindowOpen) {
            return;
        }
        editorWindowOpen = false;
        currentOpenPacket = null;
        enchantingTableOptions.clear();
        if (clientClosingWindow) {
            return;
        }

        ContainerClosePacket packet = new ContainerClosePacket();
        packet.windowId = lastInventoryNetworkId;
        packet.windowType = currentWindowType;
        packet.wasServerInitiated = true;
        player.dataPacket(packet);

        if (pendingCloseWindowId != Integer.MIN_VALUE) {
            throw new IllegalStateException("We should not have opened a new window while a window was waiting to be closed");
        }
        pendingCloseWindowId = lastInventoryNetworkId;
        pendingCloseInventory = closingInventory;

    }

    public void onClientRemoveWindow(int id) {
        if (id == -1) {
            id = lastInventoryNetworkId;
        }

        Level origin = player.getLevel();
        long movementEpoch = player.getMovementEpoch();
        boolean rejected = false;
        if (id == lastInventoryNetworkId) {
            if (id != pendingCloseWindowId && (networkIdToInventoryMap.containsKey(id) || editorWindowOpen)) {
                clientClosingWindow = true;
                try {
                    rejected = !player.tryRemoveCurrentWindow();
                } finally {
                    clientClosingWindow = false;
                }
            }
        } else {
            log.debug("{} attempted to close inventory with network ID {}, but current is {}", player.getName(), id, lastInventoryNetworkId);
        }

        // Always send this, even if no window matches. If we told the client to close a window,
        // it will behave as if it initiated the close and expect an ack.
        ContainerClosePacket packet = new ContainerClosePacket();
        packet.windowId = id;
        packet.windowType = currentWindowType;
        packet.wasServerInitiated = false;
        player.dataPacket(packet);
        if (rejected) {
            if (player.isConnected() && !player.isClosed() && player.isAlive() && !player.isInputTransferCommitted()
                    && player.getLevel() == origin && player.getMovementEpoch() == movementEpoch
                    && (currentWindowLevel == null || currentWindowLevel == player.getLevel())) {
                if (currentOpenPacket != null) {
                    player.dataPacket(currentOpenPacket.clone());
                }
                syncAll();
            }
            return;
        }

        if (pendingCloseWindowId == id) {
            pendingCloseWindowId = Integer.MIN_VALUE;
            Inventory closedInventory = pendingCloseInventory;
            pendingCloseInventory = null;

            Runnable callback = this.pendingOpenWindowCallback;
            clearPendingOpenWindow();
            if (closedInventory != null) {
                player.onAuthoritativeInventoryCloseAcknowledged(closedInventory);
            }
            if (callback != null) {
                log.debug("Opening deferred window after close ack of window {}", id);
                callback.run();
            }
        }
    }

    @Override
    public void onSlotChange(Inventory inventory, int slot) {
        Entry inventoryEntry = inventories.get(inventory);
        if (inventoryEntry == null) {
            // this can happen when an inventory changed during InventoryCloseEvent,
            // or when a temporary inventory is cleared before removal.
            return;
        }

        Item currentItem = inventory.getItem(slot);
        Item clientSideItem = inventoryEntry.predictions.remove(slot);
        boolean networkPrediction = inventoryEntry.networkPredictions.remove(slot);
        boolean matches = clientSideItem != null && (networkPrediction ? NetworkItemHelper.matches(currentItem, clientSideItem, true) : clientSideItem.equalsExact(currentItem));
        if (!matches) {
            // no prediction or incorrect - do not associate this with the currently active itemstack request
            trackItemStack(inventoryEntry, slot, currentItem, -1);
            inventoryEntry.pendingSyncs.put(slot, currentItem);
        } else {
            // correctly predicted - associate the change with the currently active itemstack request
            trackItemStack(inventoryEntry, slot, currentItem, currentItemStackRequestId);
            // 已排队的纠正仍需发送，但内容必须跟随本 tick 后续成功预测的变更。
            if (inventoryEntry.pendingSyncs.containsKey(slot)) {
                inventoryEntry.pendingSyncs.put(slot, currentItem);
            }
        }
        boolean hadBundle = bundles.hasBinding(inventory, slot);
        bundles.prepare(inventory, slot, currentItem);
        if (hadBundle || currentItem instanceof ItemBundle) {
            inventoryEntry.pendingSyncs.put(slot, currentItem);
        }
    }

    private record BlockEditorTarget(Level level, int x, int y, int z, int type) {
    }

    private void sendInventorySlotPackets(int windowId, int netSlot, Item itemStack) {
        /*
         * TODO: HACK!
         * As of 1.20.12, the client ignores change of itemstackID in some cases when the old item == the new item.
         * Notably, this happens with armor, offhand and enchanting tables, but not with main inventory.
         * While we could track the items previously sent to the client, that's a waste of memory and would
         * cost performance. Instead, clear the slot(s) first, then send the new item(s).
         * The network cost of doing this is fortunately minimal, as an air itemstack is only 1 byte.
         */
        if (itemStack.getItemStackId() != 0) {
            InventorySlotPacket packet = new InventorySlotPacket();
            packet.inventoryId = windowId;
            packet.slot = netSlot;
            packet.item = EMPTY_ITEM_STACK;
            player.dataPacket(packet);
        }

        // now send the real contents
        InventorySlotPacket packet = new InventorySlotPacket();
        packet.inventoryId = windowId;
        packet.slot = netSlot;
        packet.item = itemStack;
        player.dataPacket(packet);
    }

    private void sendInventoryContentPackets(int windowId, Item... itemStacks) {
        /*
         * TODO: HACK!
         * As of 1.20.12, the client ignores change of itemstackID in some cases when the old item == the new item.
         * Notably, this happens with armor, offhand and enchanting tables, but not with main inventory.
         * While we could track the items previously sent to the client, that's a waste of memory and would
         * cost performance. Instead, clear the slot(s) first, then send the new item(s).
         * The network cost of doing this is fortunately minimal, as an air itemstack is only 1 byte.
         */
        {
            InventoryContentPacket packet = new InventoryContentPacket();
            packet.inventoryId = windowId;
            Item[] items = new Item[itemStacks.length];
            Arrays.fill(items, EMPTY_ITEM_STACK);
            packet.slots = items;
            player.dataPacket(packet);
        }

        // now send the real contents
        InventoryContentPacket packet = new InventoryContentPacket();
        packet.inventoryId = windowId;
        packet.slots = itemStacks;
        player.dataPacket(packet);
    }

    @Override
    public void syncSlot(Inventory inventory, int slot) {
        Entry entry = inventories.get(inventory);
        if (entry == null || !isInventoryWindowAccessible(inventory) || !inventory.slotExists(slot)
                || entry.complexSlotMap == null && getRegisteredWindowId(inventory) == Integer.MIN_VALUE
                || entry.complexSlotMap != null && entry.complexSlotMap.mapCoreToNet(slot) == -1) {
            return;
        }
        Item item = inventory.getItem(slot);
        trackItemStack(entry, slot, item, -1);
        syncSlot(inventory, slot, item);
    }

    public void syncSlot(Inventory inventory, int slot, Item itemStack) {
        if (!isInventoryWindowAccessible(inventory)) {
            return;
        }
        Entry entry = inventories.get(inventory);
        if (entry == null) {
            throw new IllegalArgumentException("Cannot sync an untracked inventory");
        }
        ItemStackInfo itemStackInfo = entry.itemStackInfos.get(slot);
        if (itemStackInfo == null) {
            throw new IllegalArgumentException("Cannot sync an untracked inventory slot");
        }
        int windowId;
        int netSlot;
        ComplexInventoryMapEntry complexSlotMap = entry.complexSlotMap;
        if (complexSlotMap != null) {
            windowId = ContainerIds.UI;
            netSlot = complexSlotMap.mapCoreToNet(slot);
            if (netSlot == -1) {
                throw new IllegalArgumentException("We already have an ItemStackInfo, so this should not be null");
            }
        } else {
            int id = getRegisteredWindowId(inventory);
            if (id == Integer.MIN_VALUE) {
                throw new IllegalArgumentException("We already have an ItemStackInfo, so this should not be null");
            }
            windowId = id;
            netSlot = slot;
        }

        itemStack = prepareSlotDisplay(inventory, slot, itemStack, itemStackInfo);
        if (windowId == ContainerIds.OFFHAND) {
            //TODO: HACK!
            //The client may sometimes ignore the InventorySlotPacket for the offhand slot.
            //This can cause a lot of problems (totems, arrows, and more...).
            //The workaround is to send an InventoryContentPacket instead
            //BDS (Bedrock Dedicated Server) also seems to work this way.
            sendInventoryContentPackets(windowId, itemStack);
        } else {
            sendInventorySlotPackets(windowId, netSlot, itemStack);
        }
        entry.predictions.remove(slot);
        entry.networkPredictions.remove(slot);
        entry.pendingSyncs.remove(slot);
        bundles.flush();
    }

    @Override
    public void syncContents(Inventory inventory) {
        if (!isInventoryWindowAccessible(inventory)) {
            return;
        }
        Entry entry = inventories.get(inventory);
        if (entry == null) {
            // this can happen when an inventory changed during InventoryCloseEvent,
            // or when a temporary inventory is cleared before removal.
            return;
        }

        int windowId;
        ComplexInventoryMapEntry complexSlotMap = entry.complexSlotMap;
        if (complexSlotMap != null) {
            windowId = ContainerIds.UI;
        } else {
            int id = getRegisteredWindowId(inventory);
            if (id == Integer.MIN_VALUE) {
                return;
            }
            windowId = id;
        }

        entry.predictions.clear();
        entry.networkPredictions.clear();
        entry.pendingSyncs.clear();

        Item[] contents = new Item[inventory.getSize()];
        for (int slot = 0; slot < contents.length; slot++) {
            Item itemStack = inventory.getItem(slot);
            ItemStackInfo info = trackItemStack(entry, slot, itemStack, -1);
            itemStack = prepareSlotDisplay(inventory, slot, itemStack, info);
            contents[slot] = itemStack;
        }

        if (complexSlotMap != null) {
            for (int slot = 0; slot < contents.length; slot++) {
                int packetSlot = complexSlotMap.mapCoreToNet(slot);
                if (packetSlot == -1) {
                    continue;
                }
                sendInventorySlotPackets(windowId, packetSlot, contents[slot]);
            }
        } else {
            sendInventoryContentPackets(windowId, contents);
        }
        bundles.flush();
    }

    private Item prepareSlotDisplay(Inventory inventory, int slot, Item item, ItemStackInfo info) {
        Item actual = bundles.prepare(inventory, slot, item);
        Item display = player.getInventorySlotDisplay(inventory, slot);
        if (display != null) {
            Item projected = NetworkItemHelper.project(display).clone();
            CompoundTag tag = projected.getNamedTag();
            if (tag != null && removeDisplayContainerIds(tag)) {
                projected.setNamedTag(tag);
            }
            projected.setItemStackId(projected.isNull() ? 0 : UNTRACKED_ITEM_STACK_ID);
            return projected;
        }
        actual.setItemStackId(info.stackId);
        return actual;
    }

    private static boolean removeDisplayContainerIds(Tag tag) {
        boolean removed = false;
        if (tag instanceof CompoundTag compound) {
            removed = compound.removeAndGet(ItemBundle.DYNAMIC_ID_TAG) != null;
            for (Tag child : compound.getAllTags()) {
                removed |= removeDisplayContainerIds(child);
            }
        } else if (tag instanceof ListTag<?> list) {
            for (Tag child : list.getAllUnsafe()) {
                removed |= removeDisplayContainerIds(child);
            }
        }
        return removed;
    }

    public void syncAll() {
        for (Inventory inventory : inventories.keySet()) {
            syncContents(inventory);
        }
    }

    public void requestSyncAll() {
        fullSyncRequested = true;
    }

    public void syncMismatchedPredictedSlotChanges() {
        for (Entry entry : inventories.values()) {
            Inventory inventory = entry.inventory;
            for (int slot : entry.predictions.keySet()) {
                if (!inventory.slotExists(slot) || !entry.itemStackInfos.containsKey(slot)) {
                    continue; //TODO: size desync ???
                }

                // any prediction that still exists at this point is a slot that was predicted to change but didn't
                log.debug("{} detected prediction mismatch in inventory {}@{} slot {}", player.getName(), inventory.getClass().getSimpleName(), System.identityHashCode(inventory), slot);
                entry.pendingSyncs.put(slot, inventory.getItem(slot));
            }

            entry.predictions.clear();
            entry.networkPredictions.clear();
        }
    }

    public void flushPendingUpdates() {
        if (fullSyncRequested) {
            fullSyncRequested = false;
            log.debug("{} full inventory sync requested, sending contents of {} inventories", player.getName(), inventories.size());
            syncAll();
            return;
        }

        for (Entry entry : inventories.values()) {
            if (entry.pendingSyncs.isEmpty()) {
                continue;
            }

            Inventory inventory = entry.inventory;
            log.debug("{} syncing slots {} in inventory {}@{}", player.getName(), entry.pendingSyncs, inventory.getClass().getSimpleName(), System.identityHashCode(inventory));
            ObjectIterator<Int2ObjectMap.Entry<Item>> iterator = entry.pendingSyncs.int2ObjectEntrySet().iterator();
            while (iterator.hasNext()) {
                Int2ObjectMap.Entry<Item> pending = iterator.next();
                int slot = pending.getIntKey();
                Item item = pending.getValue();
                iterator.remove();
                syncSlot(inventory, slot, item);
            }
            entry.pendingSyncs.clear();
        }
    }

    public void syncData(Inventory inventory, int propertyId, int value) {
        if (!isInventoryWindowAccessible(inventory)) {
            return;
        }
        int windowId = getRegisteredWindowId(inventory);
        if (windowId == Integer.MIN_VALUE) {
            if (inventory != null && (inventory == pendingOpenInventory || inventory == preparingInventory)) {
                // 初始属性可能来自提前执行的 onOpen，等待窗口实际打开后再发送最新值。
                pendingWindowProperties.put(propertyId, value);
            }
            return;
        }
        ContainerSetDataPacket packet = new ContainerSetDataPacket();
        packet.windowId = windowId;
        packet.property = propertyId;
        packet.value = value;
        player.dataPacket(packet);
    }

    private void flushPendingWindowProperties(Inventory inventory, int windowId) {
        if (pendingWindowProperties.isEmpty() || getRegisteredWindowId(inventory) != windowId) {
            return;
        }
        Int2IntMap properties = new Int2IntLinkedOpenHashMap(pendingWindowProperties);
        pendingWindowProperties.clear();
        for (Int2IntMap.Entry property : properties.int2IntEntrySet()) {
            if (getRegisteredWindowId(inventory) != windowId) {
                break;
            }
            syncData(inventory, property.getIntKey(), property.getIntValue());
        }
    }

    public void onClientSelectHotbarSlot(int slot) {
        clientSelectedHotbarSlot = slot;
    }

    public void syncSelectedHotbarSlot() {
        int selected = player.getInventory().getHeldItemIndex();
        if (selected == clientSelectedHotbarSlot) {
            return;
        }

        PlayerHotbarPacket packet = new PlayerHotbarPacket();
        packet.selectedHotbarSlot = selected;
        packet.windowId = ContainerIds.INVENTORY;
        packet.selectHotbarSlot = true;
        player.dataPacket(packet);

        clientSelectedHotbarSlot = selected;
    }

    public void syncEnchantingTableOptions(EnchantmentOption... options) {
        enchantingTableOptions.clear();
        nextDynamicRecipeId = Math.max(nextDynamicRecipeId, player.getServer().getCraftingManager().getNetworkRecipeCount());
        for (int index = 0; index < options.length; index++) {
            if (nextDynamicRecipeId == Integer.MAX_VALUE) {
                throw new IllegalStateException("Dynamic recipe IDs have been exhausted");
            }
            EnchantmentOption option = options[index];
            int optionId = nextDynamicRecipeId++;
            option.optionId = optionId;
            enchantingTableOptions.put(optionId, index);
        }

        PlayerEnchantOptionsPacket116 packet = new PlayerEnchantOptionsPacket116();
        packet.options = options;
        packet.neteaseMode = player.isNetEaseClient();
        player.dataPacket(packet);
    }

    public int getEnchantingTableOptionIndex(int recipeId) {
        return enchantingTableOptions.get(recipeId);
    }

    public int getNextDynamicRecipeId() {
        return nextDynamicRecipeId;
    }

    public void setNextDynamicRecipeId(int dynamicRecipeId) {
        if (dynamicRecipeId < 0) {
            throw new IllegalArgumentException("Dynamic recipe ID must be nonnegative");
        }
        this.nextDynamicRecipeId = dynamicRecipeId;
    }

    public int getNextItemStackId() {
        return nextItemStackId;
    }

    public void setNextItemStackId(int itemStackId) {
        this.nextItemStackId = itemStackId;
    }

    int newItemStackId() {
        return nextItemStackId++;
    }

    @Nullable
    public ItemStackInfo getItemStackInfo(Inventory inventory, int slot) {
        Entry entry = inventories.get(inventory);
        if (entry == null) {
            return null;
        }
        return entry.itemStackInfos.get(slot);
    }

    public int getItemStackId(Inventory inventory, int slot) {
        ItemStackInfo info = getItemStackInfo(inventory, slot);
        return info == null ? -1 : info.stackId;
    }

    private ItemStackInfo trackItemStack(Entry entry, int slotId, Item itemStack, int itemStackRequestId) {
        ItemStackInfo info = new ItemStackInfo(itemStackRequestId, itemStack.isNull() ? 0 : newItemStackId());
        entry.itemStackInfos.put(slotId, info);
        return info;
    }

    private static class Entry {
        Inventory inventory;
        ComplexInventoryMapEntry complexSlotMap;

        final Int2ObjectMap<Item> predictions = new Int2ObjectOpenHashMap<>();
        final IntSet networkPredictions = new IntOpenHashSet();
        final Int2ObjectMap<ItemStackInfo> itemStackInfos = new Int2ObjectOpenHashMap<>();
        final Int2ObjectMap<Item> pendingSyncs = new Int2ObjectOpenHashMap<>();

        Entry(Inventory inventory) {
            this(inventory, null);
        }

        Entry(Inventory inventory, ComplexInventoryMapEntry complexSlotMap) {
            this.inventory = inventory;
            this.complexSlotMap = complexSlotMap;
        }
    }

    @AllArgsConstructor
    public static class ItemStackInfo {
        int requestId;
        int stackId;
    }

    private static class ComplexInventoryMapEntry {
        Inventory inventory;

        final Int2IntMap slotMap;
        final Int2IntMap reverseSlotMap;

        public ComplexInventoryMapEntry(Inventory inventory, Int2IntMap slotMap) {
            this.inventory = inventory;
            this.slotMap = slotMap;

            reverseSlotMap = new Int2IntOpenHashMap();
            reverseSlotMap.defaultReturnValue(-1);
            for (Int2IntMap.Entry entry : slotMap.int2IntEntrySet()) {
                reverseSlotMap.put(entry.getIntValue(), entry.getIntKey());
            }
        }

        public int mapNetToCore(int slot) {
            return slotMap.get(slot);
        }

        public int mapCoreToNet(int slot) {
            return reverseSlotMap.get(slot);
        }
    }
}
