package org.itxtech.synapseapi.inventory;

import cn.nukkit.inventory.*;
import cn.nukkit.inventory.transaction.*;
import cn.nukkit.inventory.transaction.action.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.ItemBanner;
import cn.nukkit.item.ItemBookWritten;
import cn.nukkit.inventory.recipe.BookCloningRecipe;
import cn.nukkit.inventory.recipe.BannerDuplicateRecipe;
import cn.nukkit.item.enchantment.EnchantmentOption;
import cn.nukkit.network.protocol.types.*;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.itxtech.synapseapi.SynapsePlayer116;
import org.itxtech.synapseapi.inventory.InventoryManager.ItemStackInfo;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.*;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemStackRequestExecutor {
    private final SynapsePlayer116 player;
    private final InventoryManager inventoryManager;
    private final ItemStackRequest request;

    private final TransactionBuilder builder = new TransactionBuilder();

    private final List<ItemStackRequestSlotInfo> requestSlotInfos = new ArrayList<>();

    private InventoryTransaction specialTransaction;

    private final List<Item> craftingResults = new ArrayList<>(1 + 9);

    @Nullable
    private Item nextCreatedItem;
    @Nullable
    private MultiRecipe dynamicRecipe;
    @Nullable
    private ShapelessUserDataRecipe userDataRecipe;
    @Nullable
    private MultiRecipe multiRecipe;
    private List<Item> dynamicRecipeInputs = List.of();
    private List<Item> dynamicRecipeResults = List.of();
    @Nullable
    private Item[] dynamicRecipeGrid;
    @Nullable
    private Inventory dynamicRecipeGridInventory;
    private int dynamicRecipeRepetitions;
    private final List<ConsumedInput> consumedInputs = new ArrayList<>();
    private boolean generated;
    private boolean usesBundle;
    private boolean createdItemFromCreativeInventory;
    private int createdItemsTakenCount;

    public ItemStackRequestExecutor(SynapsePlayer116 player, ItemStackRequest request) {
        this.player = player;
        this.inventoryManager = player.getInventoryManager();
        this.request = request;
    }

    private String prettyInventoryAndSlot(Inventory inventory, int slot) {
        if (inventory instanceof TransactionBuilderInventory inv) {
            inventory = inv.getActualInventory();
        }
        return inventory.getClass().getSimpleName() + "@" + System.identityHashCode(inventory) + ", slot: " + slot;
    }

    private void matchItemStack(Inventory inventory, int slotId, int clientItemStackId) throws ItemStackRequestProcessException {
        ItemStackInfo info = inventoryManager.getItemStackInfo(inventory, slotId);
        if (info == null) {
            throw new IllegalArgumentException("The inventory is tracked and the slot is valid, so this should not be null");
        }

        if (!(clientItemStackId < 0 ? info.requestId == clientItemStackId : info.stackId == clientItemStackId)) {
            throw new ItemStackRequestProcessException(prettyInventoryAndSlot(inventory, slotId) +
                    ": Mismatched expected itemstack, client expected: " + clientItemStackId + ", server actual: " + info.stackId + ", last modified by request: " + info.requestId);
        }
    }

    protected ObjectIntPair<TransactionBuilderInventory> getBuilderInventoryAndSlot(ItemStackRequestSlotInfo info) throws ItemStackRequestProcessException {
        if (info == null) {
            throw new ItemStackRequestProcessException("Missing item stack request slot");
        }
        int slotId = info.slotId;
        int windowId = ItemStackContainerIdTranslator.translate(player.getAbstractProtocol(), player.isNetEaseClient(), info.containerId, inventoryManager.getCurrentWindowId());
        if (windowId == ContainerIds.REGISTRY) {
            BundleContainerRegistry bundles = inventoryManager.getBundleRegistry();
            if (!bundles.isSupported() || info.dynamicContainerId == null || info.containerId != bundles.getContainerType()) {
                throw new ItemStackRequestProcessException("Invalid dynamic bundle container");
            }
            BundleContainerRegistry.Binding binding = bundles.get(info.dynamicContainerId);
            if (binding != null && player.getInventorySlotDisplay(binding.getInventory(), binding.getRootSlot()) != null) {
                throw new ItemStackRequestProcessException("Cannot modify a read-only display slot");
            }
            InventoryManager.ItemStackInfo stack = binding == null ? null : binding.getInfo(slotId);
            if (stack == null || info.stackId != request.requestId
                    && !(info.stackId < 0 ? stack.requestId == info.stackId : stack.stackId == info.stackId)) {
                throw new ItemStackRequestProcessException("Unknown bundle container or mismatched bundle item stack");
            }
            usesBundle = true;
            return ObjectIntPair.of(new BundlePreviewInventory(builder, binding), slotId);
        }
        if (info.dynamicContainerId != null) {
            throw new ItemStackRequestProcessException("Unexpected dynamic container ID for a normal inventory");
        }
        if (windowId == ContainerIds.OFFHAND) {
            //TODO: HACK! The client sends an incorrect slot ID for the offhand as of 1.19.70 (though this doesn't really matter since the offhand has only 1 slot anyway)
            slotId = 0;
        }
        ObjectIntPair<Inventory> windowAndSlot = inventoryManager.locateWindowAndSlot(windowId, slotId);
        if (windowAndSlot == null) {
            throw new ItemStackRequestProcessException("No open inventory matches container UI ID: " + info.containerId + ", slot ID: " + info.slotId);
        }
        Inventory inventory = windowAndSlot.left();
        int slot = windowAndSlot.rightInt();
        if (!inventory.slotExists(slot)) {
            throw new ItemStackRequestProcessException("No such inventory slot :" + prettyInventoryAndSlot(inventory, slot));
        }

        if (player.getInventorySlotDisplay(inventory, slot) != null) {
            throw new ItemStackRequestProcessException("Cannot modify a read-only display slot");
        }

        if (info.stackId != request.requestId) { // the itemstack may have been modified by the current request
            matchItemStack(inventory, slot, info.stackId);
        }

        return ObjectIntPair.of(builder.getInventory(inventory), slot);
    }

    protected void transferItems(ItemStackRequestSlotInfo source, ItemStackRequestSlotInfo destination, int count) throws ItemStackRequestProcessException {
        Item removed = removeItemFromSlot(source, count);
        addItemToSlot(destination, removed, count);
    }

    /**
     * Deducts items from an inventory slot, returning a stack containing the removed items.
     */
    protected Item removeItemFromSlot(ItemStackRequestSlotInfo slotInfo, int count) throws ItemStackRequestProcessException {
        int createdOutputTypeId = ItemStackContainerIdTranslator.getCreatedOutputSlotTypeId(player.getAbstractProtocol(), player.isNetEaseClient());
        if (slotInfo == null) {
            throw new ItemStackRequestProcessException("Missing item stack request slot");
        }
        if (slotInfo.dynamicContainerId == null && slotInfo.containerId == createdOutputTypeId
                && slotInfo.slotId == UiContainerSlots.CREATED_ITEM_OUTPUT) {
            // special case for the "created item" output slot
            //TODO: do we need to send a response for this slot info?
            return takeCreatedItem(count);
        }
        requestSlotInfos.add(slotInfo);
        ObjectIntPair<TransactionBuilderInventory> pair = getBuilderInventoryAndSlot(slotInfo);
        Inventory inventory = pair.left();
        int slot = pair.rightInt();
        if (count < 1) {
            // this should be impossible at the protocol level, but in case of buggy core code this will prevent exploits
            throw new ItemStackRequestProcessException(prettyInventoryAndSlot(inventory, slot) + ": Cannot take less than 1 items from a stack");
        }

        Item existingItem = inventory.getItem(slot);
        if (existingItem.getCount() < count) {
            throw new ItemStackRequestProcessException(prettyInventoryAndSlot(inventory, slot) + ": Cannot take count items from a stack of " + existingItem.getCount());
        }

        Item removed = existingItem.split(count);
        if (!inventory.setItem(slot, existingItem)) {
            throw new ItemStackRequestProcessException("The item could not be removed from its container");
        }

        return removed;
    }

    /**
     * Adds items to the target slot, if they are stackable.
     */
    protected void addItemToSlot(ItemStackRequestSlotInfo slotInfo, Item item, int count) throws ItemStackRequestProcessException {
        requestSlotInfos.add(slotInfo);
        ObjectIntPair<TransactionBuilderInventory> pair = getBuilderInventoryAndSlot(slotInfo);
        Inventory inventory = pair.left();
        int slot = pair.rightInt();
        if (count < 1) {
            // this should be impossible at the protocol level, but in case of buggy core code this will prevent exploits
            throw new ItemStackRequestProcessException(prettyInventoryAndSlot(inventory, slot) + ": Cannot take less than 1 items from a stack");
        }

        Item existingItem = inventory.getItem(slot);
        if (!existingItem.isNull() && !existingItem.equals(item)) {
            throw new ItemStackRequestProcessException(prettyInventoryAndSlot(inventory, slot) + ": Can only add items to an empty slot, or a slot containing the same item");
        }

        // we can't use the existing item here; it may be an empty stack
        Item newItem = item.clone();
        long total = (long) existingItem.getCount() + count;
        if (total > Math.min(newItem.getMaxStackSize(), inventory.getMaxStackSize())) {
            throw new ItemStackRequestProcessException("Item transfer exceeds the maximum stack size");
        }
        newItem.setCount((int) total);
        if (!inventory.setItem(slot, newItem)) {
            throw new ItemStackRequestProcessException("The item does not fit in its destination container");
        }
    }

    protected void dropItem(Item item, int count) {
        if (count < 1) {
            throw new ItemStackRequestProcessException("Cannot drop less than 1 of an item");
        }
        Item drop = item.clone();
        drop.setCount(count);
        builder.addAction(new DropItemAction(Items.air(), drop));
    }

    protected void setNextCreatedItem(@Nullable Item item) throws ItemStackRequestProcessException {
        setNextCreatedItem(item, false);
    }

    protected void setNextCreatedItem(@Nullable Item item, boolean creative) throws ItemStackRequestProcessException {
        if (item != null && item.isNull()) {
            item = null;
        }
        if (nextCreatedItem != null) {
            // while this is more complicated than simply adding the action when the item is taken, this ensures that
            // plugins can tell the difference between 1 item that got split into 2 slots, vs 2 separate items.
            if (createdItemFromCreativeInventory && createdItemsTakenCount > 0) {
                nextCreatedItem.setCount(createdItemsTakenCount);
                builder.addAction(new CreateItemAction(nextCreatedItem));
            } else if (createdItemsTakenCount < nextCreatedItem.getCount()) {
                throw new ItemStackRequestProcessException("Not all of the previous created item was taken");
            }
        }
        nextCreatedItem = item == null ? null : item.clone();
        createdItemFromCreativeInventory = creative;
        createdItemsTakenCount = 0;
    }

    protected void beginCrafting(int recipeId, int repetitions) throws ItemStackRequestProcessException {
        requireNoSpecialTransaction();
        validateRepetitions(repetitions);
        Recipe recipe = getRecipe(recipeId);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            throw new ItemStackRequestProcessException("No such crafting recipe index: " + recipeId);
        }
        Inventory window = player.getCurrentWindow();
        RecipeTag tag = window instanceof PlayerUIComponent component ? component.getRecipeTag() : RecipeTag.CRAFTING_TABLE;
        if (craftingRecipe.getTag() != tag
                || craftingRecipe.requiresCraftingTable() && !(window instanceof CraftingInventory)) {
            throw new ItemStackRequestProcessException("The recipe is not available in the current inventory");
        }
        specialTransaction = new CraftingTransaction(player, craftingRecipe, repetitions);
        if (craftingRecipe instanceof MultiRecipe multi) {
            dynamicRecipe = multi;
            multiRecipe = multi;
            dynamicRecipeRepetitions = repetitions;
            dynamicRecipeGrid = captureCraftingGrid(window);
            return;
        }
        if (craftingRecipe instanceof ShapelessUserDataRecipe userData) {
            userDataRecipe = userData;
            dynamicRecipeRepetitions = repetitions;
            return;
        }
        for (Item result : craftingRecipe.getAllResults()) {
            Item output = result.clone();
            long count = (long) output.getCount() * repetitions;
            if (count <= 0 || count > Integer.MAX_VALUE) {
                throw new ItemStackRequestProcessException("Invalid crafting output count");
            }
            output.setCount((int) count);
            craftingResults.add(output);
        }
        if (craftingResults.size() == 1) {
            selectCraftingResult(0);
        }
        //for multi-output recipes, later actions will tell us which result to create and when
    }

    @Nullable
    private Item[] captureCraftingGrid(@Nullable Inventory window) {
        Inventory grid = window instanceof CraftingInventory ? window : player.getCraftingGrid();
        if (grid == null || grid.getSize() != 4 && grid.getSize() < 9) {
            return null;
        }
        dynamicRecipeGridInventory = grid;
        Item[] snapshot = new Item[9];
        for (int slot = 0; slot < snapshot.length; slot++) {
            snapshot[slot] = Items.air();
        }
        TransactionBuilderInventory preview = builder.getInventory(grid);
        if (grid.getSize() == 4) {
            // 玩家四格按二维位置映射到九格，保留空位而不是压平材料顺序。
            for (int slot = 0; slot < 4; slot++) {
                snapshot[slot / 2 * 3 + slot % 2] = preview.getItem(slot);
            }
        } else {
            for (int slot = 0; slot < 9; slot++) {
                snapshot[slot] = preview.getItem(slot);
            }
        }
        return snapshot;
    }

    private Recipe getRecipe(int recipeId) {
        Recipe recipe = player.getServer().getCraftingManager().getRecipe(recipeId);
        if (recipe == null || recipe instanceof MultiRecipe multi && !multi.isSupported()) {
            throw new ItemStackRequestProcessException("No supported recipe index: " + recipeId);
        }
        return recipe;
    }

    private void validateRepetitions(int repetitions) {
        if (repetitions < 1 || repetitions > 256) {
            throw new ItemStackRequestProcessException("Crafting repetitions must be between 1 and 256");
        }
    }

    private void requireNoSpecialTransaction() {
        if (specialTransaction != null || nextCreatedItem != null || !craftingResults.isEmpty()) {
            throw new ItemStackRequestProcessException("Another special transaction is already in progress");
        }
    }

    private void selectCraftingResult(int resultIndex) {
        resolveDynamicResult();
        if (resultIndex < 0 || resultIndex >= craftingResults.size() || craftingResults.get(resultIndex) == null) {
            throw new ItemStackRequestProcessException("No unclaimed crafting result index: " + resultIndex);
        }
        setNextCreatedItem(craftingResults.get(resultIndex));
        craftingResults.set(resultIndex, null);
    }

    private void resolveDynamicResult() {
        if (dynamicRecipe == null && userDataRecipe == null) {
            return;
        }
        // 工作台与玩家四格均按核心槽位排序，保留动态配方所需的输入顺序。
        consumedInputs.sort(Comparator.comparingInt(input -> input.inventory() == dynamicRecipeGridInventory
                || input.inventory() instanceof CraftingInventory ? input.slot() : Integer.MAX_VALUE));
        List<Item> inputs = new ArrayList<>(consumedInputs.size());
        ConsumedInput previous = null;
        for (ConsumedInput input : consumedInputs) {
            Item item = input.item();
            if (previous != null && previous.inventory() == input.inventory() && previous.slot() == input.slot()) {
                Item previousItem = inputs.getLast();
                previousItem.setCount(previousItem.getCount() + item.getCount());
            } else {
                inputs.add(item.clone());
            }
            previous = input;
        }
        List<Item> results;
        Item retainedTemplate = findRetainedTemplate(inputs);
        if (retainedTemplate != null) {
            inputs.add(retainedTemplate.clone());
        }
        if (dynamicRecipe != null) {
            results = dynamicRecipe.getResults(inputs, dynamicRecipeRepetitions, dynamicRecipeGrid, player.isNetEaseClient());
        } else {
            Item output = userDataRecipe.getResult(inputs, dynamicRecipeRepetitions);
            results = output == null ? List.of() : List.of(output);
        }
        if (results.isEmpty()) {
            throw new ItemStackRequestProcessException("The dynamic recipe has no valid output");
        }
        int retainedCount = retainedTemplate == null ? 0 : retainedTemplate.getCount();
        for (Item result : results) {
            if (result == null || result.isNull() || result.getCount() <= 0) {
                throw new ItemStackRequestProcessException("The dynamic recipe has an invalid output");
            }
            Item claimable = result.clone();
            if (retainedCount > 0 && claimable.equals(retainedTemplate, true, true, true)) {
                int returned = Math.min(retainedCount, claimable.getCount());
                claimable.setCount(claimable.getCount() - returned);
                retainedCount -= returned;
            }
            craftingResults.add(claimable.isNull() ? null : claimable);
        }
        if (retainedCount != 0) {
            throw new ItemStackRequestProcessException("The recipe did not preserve its retained template");
        }
        dynamicRecipeInputs = inputs;
        dynamicRecipeResults = results.stream().map(Item::clone).toList();
        dynamicRecipe = null;
        userDataRecipe = null;
    }

    @Nullable
    private Item findRetainedTemplate(List<Item> consumed) {
        boolean book = dynamicRecipe instanceof BookCloningRecipe;
        boolean banner = dynamicRecipe instanceof BannerDuplicateRecipe;
        if ((!book && !banner) || dynamicRecipeGrid == null || dynamicRecipeGridInventory == null) {
            return null;
        }
        for (Item input : consumed) {
            if (isTemplate(input, book)) {
                return null;
            }
        }
        Item retained = null;
        TransactionBuilderInventory preview = builder.getInventory(dynamicRecipeGridInventory);
        for (int slot = 0; slot < dynamicRecipeGrid.length; slot++) {
            Item candidate = dynamicRecipeGrid[slot];
            if (!isTemplate(candidate, book)) {
                continue;
            }
            int coreSlot = dynamicRecipeGridInventory.getSize() == 4 ? slot / 3 * 2 + slot % 3 : slot;
            Item current = preview.getItem(coreSlot);
            if (!current.equalsExact(candidate) || retained != null) {
                throw new ItemStackRequestProcessException("The retained crafting template is missing or ambiguous");
            }
            // 模板未消费时，语义投入和返还使用相同份数抵消，不创造第二份可领取模板。
            retained = candidate.clone(dynamicRecipeRepetitions);
        }
        return retained;
    }

    private static boolean isTemplate(Item item, boolean book) {
        return !item.isNull() && (book ? item instanceof ItemBookWritten
                : item instanceof ItemBanner banner && banner.getPatternsSize() > 0);
    }

    protected Item takeCreatedItem(int count) throws ItemStackRequestProcessException {
        if (count < 1){
            //this should be impossible at the protocol level, but in case of buggy core code this will prevent exploits
            throw new ItemStackRequestProcessException("Cannot take less than 1 created item");
        }

        if (nextCreatedItem == null && (dynamicRecipe != null || userDataRecipe != null)) {
            resolveDynamicResult();
            int singleResult = -1;
            for (int index = 0; index < craftingResults.size(); index++) {
                if (craftingResults.get(index) != null) {
                    if (singleResult != -1) {
                        throw new ItemStackRequestProcessException("A multi-output recipe requires an explicit result index");
                    }
                    singleResult = index;
                }
            }
            if (singleResult == -1) {
                throw new ItemStackRequestProcessException("A multi-output recipe requires an explicit result index");
            }
            selectCraftingResult(singleResult);
        }
        Item createdItem = nextCreatedItem;
        if (createdItem == null) {
            throw new ItemStackRequestProcessException("No created item is waiting to be taken");
        }

        if (!createdItemFromCreativeInventory) {
            int availableCount = createdItem.getCount() - createdItemsTakenCount;
            if (count > availableCount) {
                throw new ItemStackRequestProcessException("Not enough created items available to be taken (have " + availableCount + ", tried to take " + count + ")");
            }
        }

        createdItemsTakenCount += count;
        Item takenItem = createdItem.clone();
        takenItem.setCount(count);
        if (!createdItemFromCreativeInventory && createdItemsTakenCount >= createdItem.getCount()) {
            setNextCreatedItem(null);
        }
        return takenItem;
    }

    private void assertDoingCrafting() throws ItemStackRequestProcessException {
        if (!(specialTransaction instanceof CraftingTransaction)
                && !(specialTransaction instanceof CartographyTransaction)
                && !(specialTransaction instanceof AnvilTransaction)
                && !(specialTransaction instanceof GrindstoneTransaction)
                && !(specialTransaction instanceof EnchantTransaction)
                && !(specialTransaction instanceof LoomTransaction)
                && !(specialTransaction instanceof SmithingTransaction)
        ) {
            if (specialTransaction == null) {
                throw new ItemStackRequestProcessException("Expected CraftRecipe or CraftRecipeAuto action to precede this action");
            } else {
                throw new ItemStackRequestProcessException("A different special transaction is already in progress");
            }
        }
    }

    protected void processItemStackRequestAction(ItemStackRequestAction action) throws ItemStackRequestProcessException {
        switch (action.getType()) {
            case TAKE -> {
                TakeStackRequestAction take = (TakeStackRequestAction) action;
                transferItems(take.source, take.destination, take.count);
            }
            case PLACE -> {
                PlaceStackRequestAction place = (PlaceStackRequestAction) action;
                transferItems(place.source, place.destination, place.count);
            }
            case SWAP -> {
                SwapStackRequestAction swap = (SwapStackRequestAction) action;
                requestSlotInfos.add(swap.source);
                requestSlotInfos.add(swap.destination);

                ObjectIntPair<TransactionBuilderInventory> source = getBuilderInventoryAndSlot(swap.source);
                ObjectIntPair<TransactionBuilderInventory> destination = getBuilderInventoryAndSlot(swap.destination);
                Inventory sourceInventory = source.left();
                Inventory destinationInventory = destination.left();
                int sourceSlot = source.rightInt();
                int destinationSlot = destination.rightInt();

                Item sourceItem = sourceInventory.getItem(sourceSlot);
                Item destinationItem = destinationInventory.getItem(destinationSlot);
                if (!sourceInventory.setItem(sourceSlot, destinationItem)
                        || !destinationInventory.setItem(destinationSlot, sourceItem)) {
                    throw new ItemStackRequestProcessException("The swapped items do not fit in their containers");
                }
            }
            case DROP -> {
                //TODO: this action has a "randomly" field, I have no idea what it's used for
                DropStackRequestAction drop = (DropStackRequestAction) action;
                Item dropped = removeItemFromSlot(drop.source, drop.count);
                builder.addAction(new DropItemAction(Items.air(), dropped));
            }
            case DESTROY -> {
                DestroyStackRequestAction destroy = (DestroyStackRequestAction) action;
                if (specialTransaction instanceof BeaconTransaction) {
                    ObjectIntPair<TransactionBuilderInventory> payment = getBuilderInventoryAndSlot(destroy.source);
                    if (!(payment.left().getActualInventory() instanceof BeaconInventory)
                            || payment.left().getActualInventory() != player.getCurrentWindow()
                            || payment.rightInt() != BeaconInventory.INPUT_SLOT) {
                        throw new ItemStackRequestProcessException("Beacon payment must use the current beacon slot");
                    }
                }
                Item destroyed = removeItemFromSlot(destroy.source, destroy.count);
                if (specialTransaction instanceof BeaconTransaction) {
                    builder.addAction(new PayItemAction(destroyed));
                } else {
                    builder.addAction(new DestroyItemAction(destroyed));
                }
            }
            case CONSUME -> {
                assertDoingCrafting();
                CraftingConsumeInputStackRequestAction consume = (CraftingConsumeInputStackRequestAction) action;
                ObjectIntPair<TransactionBuilderInventory> source = getBuilderInventoryAndSlot(consume.source);
                Item consumed = removeItemFromSlot(consume.source, consume.count);
                consumedInputs.add(new ConsumedInput(source.left().getActualInventory(), source.rightInt(), consumed));
            }
            case CREATE -> {
                assertDoingCrafting();
                CraftingCreateSpecificResultStackRequestAction create = (CraftingCreateSpecificResultStackRequestAction) action;
                selectCraftingResult(create.resultIndex);
            }
            //TODO: lab table (edu chem)
            case LAB_TABLE_COMBINE, PLACE_IN_ITEM_CONTAINER, TAKE_FROM_ITEM_CONTAINER ->
                    throw new ItemStackRequestProcessException("This item container operation is not supported");
            case MINE_BLOCK -> handleMineBlock((MineBlockStackRequestAction) action);
            case BEACON_PAYMENT -> {
                requireNoSpecialTransaction();
                BeaconPaymentStackRequestAction beacon = (BeaconPaymentStackRequestAction) action;
                if (!(player.getCurrentWindow() instanceof BeaconInventory inventory)) {
                    throw new ItemStackRequestProcessException("A beacon inventory must be open");
                }
                specialTransaction = new BeaconTransaction(player, beacon.primaryEffectId, beacon.secondaryEffectId, inventory.getHolder());
            }
            case CRAFT_RECIPE -> {
                CraftRecipeStackRequestAction craft = (CraftRecipeStackRequestAction) action;
                handleCraftRecipe(craft.recipeId, craft.repetitions);
            }
            case CRAFT_RECIPE_AUTO -> {
                CraftRecipeAutoStackRequestAction auto = (CraftRecipeAutoStackRequestAction) action;
                handleCraftRecipe(auto.recipeId, auto.repetitions);
            }
            case CRAFT_CREATIVE -> {
                if (!player.isCreative() || specialTransaction != null) {
                    throw new ItemStackRequestProcessException("Creative item creation is not allowed");
                }
                CreativeCreateStackRequestAction creative = (CreativeCreateStackRequestAction) action;
                validateRepetitions(creative.repetitions);
                Item item = CreativeItemsPalette.getItem(creative.creativeItemId - 1, player.getAbstractProtocol(), player.isNetEaseClient());
                if (item == null || item.isNull() || item.isBundle() && !inventoryManager.getBundleRegistry().isSupported()) {
                    throw new ItemStackRequestProcessException("No supported creative item index: " + creative.creativeItemId);
                }
                setNextCreatedItem(item, true);
            }
            case CRAFT_RECIPE_OPTIONAL -> handleOptionalRecipe((CraftRecipeOptionalStackRequestAction) action);
            case CRAFT_REPAIR_AND_DISENCHANT -> handleGrindstone((GrindstoneStackRequestAction) action);
            case CRAFT_LOOM -> handleLoom((LoomStackRequestAction) action);
            case CRAFT_NON_IMPLEMENTED_DEPRECATED ->
                    throw new ItemStackRequestProcessException("The requested recipe is not implemented");
            case CRAFT_RESULTS_DEPRECATED -> {
                // no obvious use
            }
            default -> throw new ItemStackRequestProcessException("Unhandled item stack request action");
        }
    }

    private void handleCraftRecipe(int recipeId, int repetitions) {
        requireNoSpecialTransaction();
        validateRepetitions(repetitions);
        Inventory window = player.getCurrentWindow();
        if (window instanceof EnchantInventory inventory) {
            int optionIndex = inventoryManager.getEnchantingTableOptionIndex(recipeId);
            EnchantmentOption option = optionIndex >= 0 ? inventory.getOption(optionIndex) : null;
            if (repetitions != 1 || option == null) {
                throw new ItemStackRequestProcessException("No current enchanting option matches the request");
            }
            TransactionBuilderInventory preview = builder.getInventory(inventory);
            Item input = preview.getItem(EnchantInventory.INPUT_SLOT);
            if (!input.equalsExact(inventory.getInputSlot())) {
                throw new ItemStackRequestProcessException("The enchanting input changed before its options were refreshed");
            }
            Item output = EnchantInventory.getOutput(input, option);
            if (output == null || output.isNull()) {
                throw new ItemStackRequestProcessException("The enchanting option has no output");
            }
            specialTransaction = new EnchantTransaction(player, option, optionIndex + 1);
            setNextCreatedItem(output);
        } else if (window instanceof SmithingTableInventory inventory) {
            Recipe recipe = getRecipe(recipeId);
            TransactionBuilderInventory preview = builder.getInventory(inventory);
            Item base = preview.getItem(SmithingTableInventory.INPUT_SLOT);
            Item material = preview.getItem(SmithingTableInventory.INGREDIENT_SLOT);
            Item template = preview.getItem(SmithingTableInventory.TEMPLATE_SLOT);
            Item output;
            if (recipe instanceof SmithingTransformRecipe transform) {
                output = SmithingTableInventory.getOutput(transform, base);
                specialTransaction = new TransformTransaction(player, transform, repetitions, base, material, template);
            } else if (recipe instanceof SmithingTrimRecipe trim) {
                output = SmithingTableInventory.getOutput(trim, base, material, template);
                specialTransaction = new TrimTransaction(player, trim, repetitions, base, material, template);
            } else {
                throw new ItemStackRequestProcessException("The recipe is not a smithing recipe");
            }
            if (output == null || output.isNull()) {
                throw new ItemStackRequestProcessException("The smithing recipe has no valid output");
            }
            long count = (long) output.getCount() * repetitions;
            if (count <= 0 || count > Integer.MAX_VALUE) {
                throw new ItemStackRequestProcessException("Invalid smithing output count");
            }
            output.setCount((int) count);
            setNextCreatedItem(output);
        } else {
            beginCrafting(recipeId, repetitions);
        }
    }

    @Nullable
    private String getFilterString(int index) {
        if (index == -1) {
            return null;
        }
        String[] strings = request.filterStrings;
        if (index == 0 && strings != null && strings.length == 0) {
            return null;
        }
        if (strings == null || index < 0 || index >= strings.length || strings[index] == null) {
            throw new ItemStackRequestProcessException("Invalid filter string index: " + index);
        }
        return strings[index];
    }

    private void handleOptionalRecipe(CraftRecipeOptionalStackRequestAction action) {
        requireNoSpecialTransaction();
        String name = getFilterString(action.filterStringIndex);
        Inventory window = player.getCurrentWindow();
        FakeBlockRecipeResult result;
        if (window instanceof AnvilInventory inventory) {
            TransactionBuilderInventory preview = builder.getInventory(inventory);
            Item input = preview.getItem(AnvilInventory.INPUT_SLOT);
            Item material = preview.getItem(AnvilInventory.INGREDIENT_SLOT);
            result = AnvilInventory.calculateResult(input, material, name, player.isCreative());
            specialTransaction = new AnvilTransaction(player, name, input, material);
        } else if (window instanceof CartographyTableInventory inventory) {
            String cartographyName = name == null ? "" : name;
            TransactionBuilderInventory preview = builder.getInventory(inventory);
            Item input = preview.getItem(CartographyTableInventory.INPUT_SLOT);
            Item material = preview.getItem(CartographyTableInventory.INGREDIENT_SLOT);
            // 单地图可以直接改名或取出，客户端保留的配方 ID 不参与这条路径的匹配。
            Recipe recipe = CartographyTableInventory.isSingleMapInput(input, material) || action.recipeId == 0
                    ? null : getRecipe(action.recipeId);
            if (recipe != null && (!(recipe instanceof CraftingRecipe craftingRecipe)
                    || craftingRecipe.getTag() != RecipeTag.CARTOGRAPHY_TABLE)) {
                throw new ItemStackRequestProcessException("The recipe is not a cartography recipe");
            }
            result = CartographyTableInventory.getResult(input, material, recipe, cartographyName);
            if (result == null) {
                throw new ItemStackRequestProcessException("The cartography operation has no valid output");
            }
            specialTransaction = new CartographyTransaction(player, recipe, cartographyName, input, material, result);
        } else {
            throw new ItemStackRequestProcessException("An anvil or cartography inventory must be open");
        }
        if (result == null || result.output().isNull()) {
            throw new ItemStackRequestProcessException("The requested workstation operation has no valid output");
        }
        setNextCreatedItem(result.output());
    }

    private void handleGrindstone(GrindstoneStackRequestAction action) {
        requireNoSpecialTransaction();
        validateRepetitions(action.repetitions);
        if (action.repetitions != 1 || !(player.getCurrentWindow() instanceof GrindstoneInventory inventory)) {
            throw new ItemStackRequestProcessException("The grindstone operation requires one open workstation");
        }
        TransactionBuilderInventory preview = builder.getInventory(inventory);
        Item input = preview.getItem(GrindstoneInventory.INPUT_SLOT);
        Item material = preview.getItem(GrindstoneInventory.INGREDIENT_SLOT);
        FakeBlockRecipeResult result = GrindstoneInventory.calculateResult(input, material, player.isNetEaseClient());
        if (result == null || result.output().isNull()) {
            throw new ItemStackRequestProcessException("The grindstone operation has no valid output");
        }
        specialTransaction = new GrindstoneTransaction(player, input, material);
        setNextCreatedItem(result.output());
    }

    private void handleLoom(LoomStackRequestAction action) {
        requireNoSpecialTransaction();
        validateRepetitions(action.repetitions);
        if (!(player.getCurrentWindow() instanceof LoomInventory inventory)) {
            throw new ItemStackRequestProcessException("A loom inventory must be open");
        }
        TransactionBuilderInventory preview = builder.getInventory(inventory);
        Item banner = preview.getItem(LoomInventory.BANNER_SLOT);
        Item dye = preview.getItem(LoomInventory.DYE_SLOT);
        Item pattern = preview.getItem(LoomInventory.PATTERN_SLOT);
        Item output = LoomInventory.getOutput(action.patternId, banner, dye, pattern);
        if (output == null || output.isNull()) {
            throw new ItemStackRequestProcessException("The loom pattern has no valid output");
        }
        output.setCount(action.repetitions);
        specialTransaction = new LoomTransaction(player, action.patternId, action.repetitions, banner, dye, pattern);
        setNextCreatedItem(output);
    }

    private void handleMineBlock(MineBlockStackRequestAction action) {
        PlayerInventory inventory = player.getInventory();
        if (!inventory.isHotbarSlot(action.hotbarSlot)) {
            throw new ItemStackRequestProcessException("Invalid mining hotbar slot");
        }
        if (player.getInventorySlotDisplay(inventory, action.hotbarSlot) != null) {
            throw new ItemStackRequestProcessException("Cannot modify a read-only display slot");
        }
        Item item = inventory.getItem(action.hotbarSlot);
        if (action.predictedDurability < 0 || item instanceof ItemDurable
                && action.predictedDurability > item.getMaxDurability()) {
            throw new ItemStackRequestProcessException("Invalid predicted item durability");
        }
        ItemStackRequestSlotInfo slot = new ItemStackRequestSlotInfo();
        slot.containerId = ItemStackContainerIdTranslator.getHotbarSlotTypeId(player.getAbstractProtocol(), player.isNetEaseClient());
        slot.slotId = action.hotbarSlot;
        slot.stackId = action.stackId;
        requestSlotInfos.add(slot);
        if (item instanceof ItemDurable) {
            // 只记录客户端预测，实际耐久仍由服务端挖掘逻辑修改。
            item.setDamage(action.predictedDurability);
            inventoryManager.addPredictedSlotChange(inventory, action.hotbarSlot, item);
        }
    }

    @Nullable
    public InventoryTransaction generateInventoryTransaction() throws ItemStackRequestProcessException {
        if (generated || request.actions == null || request.actions.length == 0) {
            throw new ItemStackRequestProcessException("Invalid or already processed item stack request");
        }
        generated = true;
        ItemStackRequestAction[] actions = request.actions;
        for (int i = 0; i < actions.length; i++) {
            ItemStackRequestAction action = actions[i];
            if (action == null) {
                throw new ItemStackRequestProcessException("Missing item stack request action");
            }
            try {
                processItemStackRequestAction(action);
            } catch (ItemStackRequestProcessException e) {
                throw new ItemStackRequestProcessException("Error processing action " + i + " (" + action.getClass().getSimpleName() + "): " + e.getMessage(), e);
            }
        }
        if (dynamicRecipe != null || userDataRecipe != null) {
            throw new ItemStackRequestProcessException("The dynamic crafting result was not taken");
        }
        for (Item result : craftingResults) {
            if (result != null) {
                throw new ItemStackRequestProcessException("A crafting result was not taken");
            }
        }
        setNextCreatedItem(null);
        List<InventoryAction> inventoryActions = builder.generateActions();

        if (inventoryActions.isEmpty()) {
            if (specialTransaction != null) {
                throw new ItemStackRequestProcessException("The special transaction has no inventory changes");
            }
            return null;
        }
        InventoryTransaction transaction = specialTransaction != null ? specialTransaction : new InventoryTransaction(player, new ArrayList<>());
        if (usesBundle) {
            if (specialTransaction != null) {
                throw new ItemStackRequestProcessException("Crafting directly from a dynamic bundle container is not supported");
            }
            transaction = new BundleTransaction(player);
        }
        if (multiRecipe != null) {
            transaction = new MultiRecipeTransaction(player, multiRecipe, dynamicRecipeRepetitions, dynamicRecipeInputs, dynamicRecipeResults, dynamicRecipeGrid, dynamicRecipeGridInventory);
        }
        for (InventoryAction inventoryAction : inventoryActions) {
            transaction.addAction(inventoryAction);
        }
        return transaction;
    }

    public ItemStackResponse buildItemStackResponse() {
        ItemStackResponseBuilder builder = new ItemStackResponseBuilder(player, request.requestId);
        for (ItemStackRequestSlotInfo requestInfo : requestSlotInfos) {
            builder.addSlot(requestInfo.containerId, requestInfo.dynamicContainerId, requestInfo.slotId);
        }
        return builder.build();
    }

    public void beginBundleCommit() {
        if (usesBundle) {
            inventoryManager.beginBundleTransaction(inventoryManager.getBundleRegistry().getPreservedIds(builder));
        }
    }

    private record ConsumedInput(Inventory inventory, int slot, Item item) {
    }
}
