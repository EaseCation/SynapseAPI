package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSerializer;
import cn.nukkit.item.*;
import cn.nukkit.item.ItemSerializer.RuntimeItemSerializer;
import cn.nukkit.nbt.tag.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.CreativeItemsPalette;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

@Log4j2
public final class LegacyItemSerializer {
    private static final Object2IntMap<String> INTERNAL_MAPPING = new Object2IntOpenHashMap<>();

    static {
        INTERNAL_MAPPING.defaultReturnValue(-1);
    }

    public static Object2IntMap<String> getInternalMapping() {
        return INTERNAL_MAPPING;
    }

    public static CompoundTag serializeItem(@Nullable Item item) {
        CompoundTag tag = new CompoundTag();

        if (item == null) {
            ItemUtil.emptyItem(tag);
            return tag;
        }

        int id = item.getId();
        if (id == ItemID.AIR) {
            ItemUtil.emptyItem(tag);
            return tag;
        }

        int count = item.getCount();
        if (count <= 0) {
            ItemUtil.emptyItem(tag);
            return tag;
        }

        int damage = item.getDamage();

        String name;
        int fullBlockId;
        if (id >= 0) {
            name = ItemUtil.ITEM_ID_TO_NAME[id];
            fullBlockId = id <= 0xff && id != ItemID.GLOW_STICK ? Block.getFullId(id, damage) : -1;

            if (name == null) {
                log.debug("Invalid item id: " + id);
                ItemUtil.emptyItem(tag);
                return tag;
            }
        } else {
            int blockId = 0xff - id;
            if (blockId >= Block.BLOCK_ID_COUNT) {
                log.debug("Invalid item block id: " + blockId);
                ItemUtil.unknownBlockItem(tag);
                return tag;
            }

            name = ItemUtil.BLOCK_ID_TO_NAME[blockId];
            if (name == null) {
                log.debug("Invalid block item id: " + id);
                ItemUtil.unknownBlockItem(tag);
                return tag;
            }

            fullBlockId = Block.getFullId(blockId, damage);
        }

        String[] metaToNewName = ItemUtil.LEGACY_TO_FLATTENED.get(name);
        if (metaToNewName != null && damage < metaToNewName.length) {
            String flattenedName = metaToNewName[damage];
            if (flattenedName != null) {
                name = flattenedName;
                damage = 0;
            }
        }

        tag.putString("Name", name);

        if (fullBlockId != -1) {
            tag.putCompound("Block", BlockSerializer.serializeRuntime(fullBlockId));

            damage = 0;
        }

        CompoundTag nbt = item.getNamedTag();

        if (damage != 0 && item instanceof ItemDurable) { // Nukkit backwards compatibility HACK
            if (nbt == null) {
                nbt = new CompoundTag();
            }

            nbt.putInt("Damage", damage);

            damage = 0;
        }

        if (nbt != null) {
            tag.putCompound("tag", nbt);
        }

        tag.putShort("Damage", damage);

        tag.putByte("Count", count);

        tag.putBoolean("WasPickedUp", item.wasPickedUp());

        if (item.getCanPlaceOnBlocks() != null) {
            Set<String> canPlaceOnBlocks = item.getCanPlaceOnBlocks();
            if (!canPlaceOnBlocks.isEmpty()) {
                tag.putList(new ListTag<>("CanPlaceOn", canPlaceOnBlocks.stream()
                        .map(blockName -> new StringTag("", blockName))
                        .collect(Collectors.toList())));
            }
        }
        if (item.getCanDestroyBlocks() != null) {
            Set<String> canDestroyBlocks = item.getCanDestroyBlocks();
            if (!canDestroyBlocks.isEmpty()) {
                tag.putList(new ListTag<>("CanDestroy", canDestroyBlocks.stream()
                        .map(blockName -> new StringTag("", blockName))
                        .collect(Collectors.toList())));
            }
        }

        return tag;
    }

    public static CompoundTag serializeItemStack(@Nullable Item item, int slot) {
        CompoundTag tag = serializeItem(item);

        tag.putByte("Slot", slot);

        return tag;
    }

    public static Item deserialize(CompoundTag tag) {
        String name = tag.getString("Name");
        if (name.isEmpty()) {
            return Items.air();
        }

        int count = tag.getByte("Count");
        if (count <= 0) {
            return Items.air();
        }

        int id;
        int damage;

        CompoundTag block = tag.getCompound("Block", null);
        if (block != null) {
            int fullId = BlockSerializer.deserializeRuntime(block);
            int blockId = Block.getIdFromFullId(fullId);

            if (blockId == ItemID.AIR) {
                return Items.air();
            }

            id = Block.getItemId(blockId);
            damage = Block.getDamageFromFullId(fullId);
        } else {
            ObjectIntPair<String> legacy = ItemUtil.FLATTENED_TO_LEGACY.get(name);
            if (legacy != null) {
                name = legacy.left();
                damage = legacy.rightInt();

                id = ItemUtil.ITEM_NAME_TO_ID.getInt(name);
            } else {
                id = ItemUtil.ITEM_NAME_TO_ID.getInt(name);

                if (id == ItemID.AIR) {
                    return Items.air();
                }

                damage = tag.getShort("Damage");
            }
        }

        Item item = Item.get(id, damage, count);

        CompoundTag nbt = tag.getCompound("tag", null);
        if (nbt != null) {
            if (item instanceof ItemDurable) { // Nukkit backwards compatibility HACK
                IntTag durability = nbt.removeAndGet("Damage");
                if (durability != null) {
                    item.setDamage(durability.data);
                }
            }

            item.setNamedTag(nbt);
        }

        if (tag.getBoolean("WasPickedUp")) {
            item.setPickedUp(true);
        }

        Tag canPlaceOnTag = tag.get("CanPlaceOn");
        if (canPlaceOnTag instanceof ListTag && ((ListTag<?>) canPlaceOnTag).type == Tag.TAG_String) {
            ListTag<StringTag> canPlaceOn = (ListTag<StringTag>) canPlaceOnTag;
            if (!canPlaceOn.isEmpty()) {
                item.setCanPlaceOnBlocks(canPlaceOn.getAllUnsafe().stream()
                        .map(blockName -> blockName.data)
                        .collect(Collectors.toSet()));
            }
        }
        Tag canDestroyTag = tag.get("CanDestroy");
        if (canDestroyTag instanceof ListTag && ((ListTag<?>) canDestroyTag).type == Tag.TAG_String) {
            ListTag<StringTag> canDestroy = (ListTag<StringTag>) canDestroyTag;
            if (!canDestroy.isEmpty()) {
                item.setCanDestroyBlocks(canDestroy.getAllUnsafe().stream()
                        .map(blockName -> blockName.data)
                        .collect(Collectors.toSet()));
            }
        }

        return item;
    }

    private static void registerItem(String identifier, int id, int internalId) {
        Objects.requireNonNull(identifier, "identifier");
        if (id < 0) {
            throw new IllegalArgumentException("Invalid non-block item ID: " + id);
        }

        if (ItemUtil.ITEM_ID_TO_NAME[id] != null) {
            throw new IllegalArgumentException("Attempted to register '" + identifier + "', but ID '" + id + "' already exists: " + ItemUtil.ITEM_ID_TO_NAME[id]);
        }
        if (ItemUtil.ITEM_NAME_TO_ID.containsKey(identifier)) {
            throw new IllegalArgumentException(identifier + "' already exists: " + ItemUtil.ITEM_NAME_TO_ID.getInt(identifier));
        }

        ItemUtil.ITEM_ID_TO_NAME[id] = identifier;
        ItemUtil.ITEM_NAME_TO_ID.put(identifier, id);

        INTERNAL_MAPPING.putIfAbsent(identifier, internalId);
    }

    private static void registerItemAux(String identifier, int id, int internalId) {
        Objects.requireNonNull(identifier, "identifier");
        if (id < 0) {
            throw new IllegalArgumentException("Invalid non-block item ID: " + id);
        }

        if (ItemUtil.ITEM_ID_TO_NAME[id] == null) {
            throw new IllegalArgumentException("Attempted to register '" + identifier + "', but ID '" + id + "' doesn't exists: " + ItemUtil.ITEM_ID_TO_NAME[id]);
        }
        if (ItemUtil.ITEM_NAME_TO_ID.containsKey(identifier)) {
            throw new IllegalArgumentException(identifier + "' already exists: " + ItemUtil.ITEM_NAME_TO_ID.getInt(identifier));
        }

        ItemUtil.ITEM_NAME_TO_ID.put(identifier, id);

        INTERNAL_MAPPING.putIfAbsent(identifier, internalId);
    }

    private static void registerCustomBlockItem(String fullName, int itemId) {
        Objects.requireNonNull(fullName, "fullName");
        if (itemId >= 0) {
            throw new IllegalArgumentException("Invalid custom block item ID: " + itemId);
        }
        int blockId = Block.itemIdToBlockId(itemId);

        if (ItemUtil.BLOCK_ID_TO_NAME[blockId] != null) {
            throw new IllegalArgumentException("Attempted to register '" + fullName + "', but ID '" + itemId + "' already exists: " + ItemUtil.BLOCK_ID_TO_NAME[blockId]);
        }
        if (ItemUtil.ITEM_NAME_TO_ID.containsKey(fullName)) {
            throw new IllegalArgumentException(fullName + "' already exists: " + ItemUtil.ITEM_NAME_TO_ID.getInt(fullName));
        }

        ItemUtil.BLOCK_ID_TO_NAME[blockId] = fullName;
        ItemUtil.ITEM_NAME_TO_ID.put(fullName, itemId);
        ItemUtil.ITEM_TO_BLOCK.put(fullName, fullName);
    }

    public static void initialize() {
        log.debug("Loading item serializer...");

        VanillaItemUpgrader.initialize();

        ItemSerializer.setSerializer(new RuntimeItemSerializer() {
            @Override
            public CompoundTag serializeItem(@Nullable Item item) {
                return LegacyItemSerializer.serializeItem(item);
            }

            @Override
            public CompoundTag serializeItemStack(@Nullable Item item, int slot) {
                return LegacyItemSerializer.serializeItemStack(item, slot);
            }

            @Override
            public Item deserialize(CompoundTag tag) {
                return LegacyItemSerializer.deserialize(tag);
            }

            @Override
            public void registerItem(String identifier, int id) {
                LegacyItemSerializer.registerItem(identifier, id, id << 16);
            }

            @Override
            public void registerItem(String identifier, int id, int maxAuxVal) {
                LegacyItemSerializer.registerItem(identifier, id, (id << 16) | 0xffff);
            }

            @Override
            public void registerItemAux(String identifier, int id, int meta) {
                LegacyItemSerializer.registerItemAux(identifier, id, (id << 16) | meta);
            }

            @Override
            public void registerCustomItem(String fullName, int id, @Nullable IntFunction<CompoundTag> componentsSupplier) {
                registerItem(fullName, id);

                AdvancedRuntimeItemPalette.registerCustomItem(fullName, id, componentsSupplier);
            }

            @Override
            public void registerCustomBlockItem(String fullName, int itemId) {
                LegacyItemSerializer.registerCustomBlockItem(fullName, itemId);
                AdvancedRuntimeItemPalette.registerCustomItem(fullName, itemId, itemId);

                CreativeItemsPalette.registerCustomItem(Item.get(itemId));
            }

            @Override
            public void rebuildRuntimeMapping() {
                AdvancedRuntimeItemPalette.rebuildNetworkCache();
            }
        });

        Items.registerVanillaNewItems();
    }

    private LegacyItemSerializer() {
        throw new IllegalStateException();
    }
}
