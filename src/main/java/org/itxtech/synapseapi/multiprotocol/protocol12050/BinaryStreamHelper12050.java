package org.itxtech.synapseapi.multiprotocol.protocol12050;

import cn.nukkit.block.Block;
import cn.nukkit.inventory.RecipeType;
import cn.nukkit.inventory.recipe.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.LittleEndianByteBufInputStream;
import cn.nukkit.network.LittleEndianByteBufOutputStream;
import cn.nukkit.network.protocol.types.ItemDescriptorType;
import cn.nukkit.utils.BinaryStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.protocol12040.BinaryStreamHelper12040;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.item.BlockItemFlattener;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Set;

@Log4j2
public class BinaryStreamHelper12050 extends BinaryStreamHelper12040 {
    public static BinaryStreamHelper12050 create() {
        return new BinaryStreamHelper12050();
    }

    @Override
    public String getGameVersion() {
        return "1.20.50";
    }

    @Override
    public boolean isNetEase() {
        return true;
    }

    @Override
    protected Item getSlot(BinaryStream stream, boolean instanceItem) {
        int id = stream.getVarInt();
        if (id == 0) {
            return Item.get(Item.AIR, 0, 0);
        }

        if (id < Short.MIN_VALUE || id >= Short.MAX_VALUE) {
            throw new RuntimeException("Invalid item networkID received: " + id);
        }

        int count = stream.getLShort();
        int damage = (int) stream.getUnsignedVarInt();

        int fullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, stream.neteaseMode, id);
        id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, fullId);

        int newId = id;
        id = BlockItemFlattener.downgrade(this.protocol, id);

        boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, fullId);
        if (hasData) {
            damage = AdvancedRuntimeItemPalette.getData(this.protocol, stream.neteaseMode, fullId);
        }

        if (!instanceItem && stream.getBoolean()) { // hasNetId
            stream.getVarInt(); // netId
        }

        int blockRuntimeId = stream.getVarInt();
        if (id < 256 && id != 166) { // ItemBlock
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(this.protocol, stream.neteaseMode, blockRuntimeId);
            if (legacyId != -1) {
                damage = legacyId & 0x3fff;
            }
        }

        int itemFullId = BlockItemFlattener.fixMeta(this.protocol, newId, id, damage);
        id = Item.getIdFromFullId(itemFullId);
        damage = Item.getMetaFromFullId(itemFullId);

        byte[] bytes = stream.getByteArray();
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
        buf.writeBytes(bytes);

        byte[] nbt = new byte[0];
        Set<String> canPlace;
        Set<String> canBreak;

        try (LittleEndianByteBufInputStream in = new LittleEndianByteBufInputStream(buf)) {
            int nbtSize = in.readShort();

            CompoundTag compoundTag = null;
            if (nbtSize > 0) {
                compoundTag = NBTIO.read(in, ByteOrder.LITTLE_ENDIAN);
            } else if (nbtSize == -1) {
                int tagCount = in.readUnsignedByte();
                if (tagCount != 1) throw new IllegalArgumentException("Expected 1 tag but got " + tagCount);
                compoundTag = NBTIO.read(in, ByteOrder.LITTLE_ENDIAN);
            }

            if (compoundTag != null && !compoundTag.isEmpty()) {
                if (compoundTag.contains("Damage")) {
                    damage = compoundTag.getInt("Damage");
                    compoundTag.remove("Damage");
                }
                Tag nkDamageTag = compoundTag.removeAndGet("__DamageConflict__");
                if (nkDamageTag != null) {
                    compoundTag.put("Damage", nkDamageTag);
                }
                if (!compoundTag.isEmpty()) {
                    nbt = NBTIO.write(compoundTag, ByteOrder.LITTLE_ENDIAN);
                }
            }

            int canPlaceCount = in.readInt();
            if (canPlaceCount > 0) {
                if (canPlaceCount > 4096) {
                    throw new IndexOutOfBoundsException("Too many CanPlaceOn blocks");
                }
                canPlace = new ObjectOpenHashSet<>();
                for (int i = 0; i < canPlaceCount; i++) {
                    canPlace.add(in.readUTF());
                }
            } else {
                canPlace = null;
            }

            int canBreakCount = in.readInt();
            if (canBreakCount > 0) {
                if (canBreakCount > 4096) {
                    throw new IndexOutOfBoundsException("Too many CanDestroy blocks");
                }
                canBreak = new ObjectOpenHashSet<>();
                for (int i = 0; i < canBreakCount; i++) {
                    canBreak.add(in.readUTF());
                }
            } else {
                canBreak = null;
            }

            if (id == ItemID.SHIELD) {
                in.readLong();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read item user data", e);
        } finally {
            buf.release();
        }

        if (damage < 0 || damage >= Short.MAX_VALUE) {
            throw new RuntimeException("Invalid item meta received: " + damage);
        }

        Item item = Item.get(id, damage, count, nbt);

        if (canPlace != null && !canPlace.isEmpty()) {
            item.setCanPlaceOnBlocks(canPlace);
        }

        if (canBreak != null && !canBreak.isEmpty()) {
            item.setCanDestroyBlocks(canBreak);
        }

        return item;
    }

    @Override
    protected void putSlot(BinaryStream stream, Item item, boolean instanceItem) {
        if (item == null || item.getId() == Item.AIR) {
            stream.putByte((byte) 0);
            return;
        }

        int id = item.getId();
        int meta = item.getDamage();
        boolean isBlock = id < 256 && id != 166;
        boolean isDurable = item instanceof ItemDurable;

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (id > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        stream.putVarInt(networkId);
        stream.putLShort(item.getCount());

        boolean useLegacyData = !isBlock && !isDurable && !AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId);
        stream.putUnsignedVarInt(useLegacyData ? meta : 0);

        if (!instanceItem) {
            stream.putBoolean(true); // hasNetId
            stream.putVarInt(1); // netId
        }

        Block block = isBlock ? item.getBlockUnsafe() : null;
        int runtimeId = block == null ? 0 : AdvancedGlobalBlockPalette.getOrCreateRuntimeId(this.protocol, stream.neteaseMode, block.getId(), block.getDamage());
        stream.putVarInt(runtimeId);

        if (SynapseSharedConstants.ITEM_BLOCK_DEBUG) {
            if (block == null && isBlock) {
                Block expected = Item.get(id, meta).getBlockUnsafe();
                if (expected != null) {
                    log.warn("Invalid block given: {}\nExpected block: {}", item, expected);
                }
            }
        }

        ByteBuf userDataBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        try (LittleEndianByteBufOutputStream out = new LittleEndianByteBufOutputStream(userDataBuf)) {
            if (!instanceItem && isDurable) {
                byte[] nbt = item.getCompoundTag();
                CompoundTag tag;
                if (nbt == null || nbt.length == 0) {
                    tag = new CompoundTag();
                } else {
                    tag = NBTIO.read(nbt, ByteOrder.LITTLE_ENDIAN);
                }
                Tag damageTag = tag.removeAndGet("Damage");
                if (damageTag != null) {
                    tag.put("__DamageConflict__", damageTag);
                }
                tag.putInt("Damage", meta);
                out.writeShort(-1);
                out.writeByte(1); // Hardcoded in current version
                out.write(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN));
            } else if (item.hasCompoundTag()) {
                out.writeShort(-1);
                out.writeByte(1); // Hardcoded in current version
                out.write(item.getCompoundTag());
            } else {
                userDataBuf.writeShortLE(0);
            }

            Set<String> canPlaceOn = item.getCanPlaceOnBlocks();
            if (canPlaceOn != null) {
                out.writeInt(canPlaceOn.size());
                for (String string : canPlaceOn) {
                    out.writeUTF(string);
                }
            } else {
                out.writeInt(0);
            }

            Set<String> canDestroy = item.getCanDestroyBlocks();
            if (canDestroy != null) {
                out.writeInt(canDestroy.size());
                for (String string : canDestroy) {
                    out.writeUTF(string);
                }
            } else {
                out.writeInt(0);
            }

            if (id == ItemID.SHIELD) {
                out.writeLong(0);
            }

            byte[] bytes = new byte[userDataBuf.readableBytes()];
            userDataBuf.readBytes(bytes);
            stream.putByteArray(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write item user data", e);
        } finally {
            userDataBuf.release();
        }
    }

    @Override
    public RecipeIngredient getRecipeIngredient(BinaryStream stream) {
        ItemDescriptor descriptor;
        switch (stream.getByte()) {
            default:
            case 0:
                descriptor = InvalidItemDescriptor.INSTANCE;
                break;
            case 1:
                int networkId = stream.getLShort();

                int legacyFullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, stream.neteaseMode, networkId);
                int id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, legacyFullId);
                boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, legacyFullId);

                id = BlockItemFlattener.downgrade(this.protocol, id);

                int damage = stream.getLShort();
                if (hasData) {
                    damage = AdvancedRuntimeItemPalette.getData(this.protocol, stream.neteaseMode, legacyFullId);
                } else if (damage == 0x7fff) {
                    damage = -1;
                } else {
                    damage = 0;
                }

                descriptor = new DefaultItemDescriptor(Item.get(id, damage));
                break;
            case 2:
                String molangExpression = stream.getString();
                int molangVersion = stream.getByte();

                descriptor = new MolangItemDescriptor(molangExpression, molangVersion);
                break;
            case 3:
                String tag = stream.getString();

                descriptor = new TagItemDescriptor(tag);
                break;
            case 4:
                String name = stream.getString();
                damage = stream.getLShort();

                descriptor = new DeferredItemDescriptor(name, damage);
                break;
            case 5:
                name = stream.getString();

                descriptor = new ComplexAliasItemDescriptor(name);
                break;
        }

        int count = stream.getVarInt();

        if (descriptor instanceof DefaultItemDescriptor internalDescriptor) {
            internalDescriptor.getItem().setCount(count);
        }

        return new RecipeIngredient(descriptor, count);
    }

    @Override
    public void putRecipeIngredient(BinaryStream stream, RecipeIngredient ingredient) {
        if (ingredient == null) {
            ingredient = RecipeIngredient.EMPTY;
        }
        ItemDescriptor descriptor = ingredient.getDescriptor();
        ItemDescriptorType type = descriptor.getType();

        stream.putByte(type.ordinal());
        switch (type) {
            default:
            case NONE:
                break;
            case INTERNAL:
                DefaultItemDescriptor internalDescriptor = (DefaultItemDescriptor) descriptor;
                Item item = internalDescriptor.getItem();
                if (item == null || item.isNull()) {
                    stream.setCount(stream.getCount() - 1);
                    stream.putByte(ItemDescriptorType.NONE.ordinal());
                    stream.putVarInt(0);
                    return;
                }

                int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
                int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);
                int damage = item.hasMeta() ? item.getDamage() : 0x7fff;
                if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
                    damage = 0;
                }

                if (item.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
                    networkId >>= 1;
                }

                stream.putLShort(networkId);
                stream.putLShort(damage);

                stream.putVarInt(ingredient.getCount());
                return;
            case MOLANG:
                MolangItemDescriptor molangDescriptor = (MolangItemDescriptor) descriptor;
                stream.putString(molangDescriptor.getExpression());
                stream.putByte(molangDescriptor.getVersion());
                break;
            case ITEM_TAG:
                TagItemDescriptor tagDescriptor = (TagItemDescriptor) descriptor;
                stream.putString(tagDescriptor.getTag());
                break;
            case DEFERRED:
                DeferredItemDescriptor deferredDescriptor = (DeferredItemDescriptor) descriptor;
                stream.putString(deferredDescriptor.getName());
                stream.putLShort(deferredDescriptor.getMeta());
                break;
            case COMPLEX_ALIAS:
                ComplexAliasItemDescriptor complexAliasDescriptor = (ComplexAliasItemDescriptor) descriptor;
                stream.putString(complexAliasDescriptor.getName());
                break;
        }

        stream.putVarInt(ingredient.getCount());
    }

    @Override
    public Item getCraftingRecipeIngredient(BinaryStream stream) { //TODO: ItemDescriptor
        int id;
        int damage;

        int descriptorType = stream.getByte();
        switch (descriptorType) {
            default:
            case 0:
                id = ItemID.AIR;
                damage = 0;
                break;
            case 1:
                int networkId = stream.getLShort();

                int legacyFullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, stream.neteaseMode, networkId);
                id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, legacyFullId);
                boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, legacyFullId);

                id = BlockItemFlattener.downgrade(this.protocol, id);

                damage = stream.getLShort();
                if (hasData) {
                    damage = AdvancedRuntimeItemPalette.getData(this.protocol, stream.neteaseMode, legacyFullId);
                } else if (damage == 0x7fff) {
                    damage = -1;
                } else {
                    damage = 0;
                }
                break;
            case 2:
                String molangExpression = stream.getString();
                int molangVersion = stream.getByte();

                //TODO: MolangDescriptor
                id = ItemID.AIR;
                damage = 0;
                break;
            case 3:
                String tag = stream.getString();

                //TODO: ItemTagDescriptor
                id = ItemID.AIR;
                damage = 0;
                break;
            case 4:
                String name = stream.getString();

                legacyFullId = AdvancedRuntimeItemPalette.getLegacyFullIdByName(this.protocol, stream.neteaseMode, name);
                id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, legacyFullId);
                hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, legacyFullId);

                damage = stream.getLShort();
                if (hasData) {
                    damage = AdvancedRuntimeItemPalette.getData(this.protocol, stream.neteaseMode, legacyFullId);
                } else if (damage == 0x7fff) {
                    damage = -1;
                } else {
                    damage = 0;
                }
                break;
            case 5: // 1.19.70+
                String alias = stream.getString();

                //TODO: ComplexAliasDescriptor
                id = ItemID.AIR;
                damage = 0;
                break;
        }

        int count = stream.getVarInt();
        return Item.get(id, damage, count);
    }

    @Override
    public void putCraftingRecipeIngredient(BinaryStream stream, Item ingredient) { //TODO: ItemDescriptor
        if (ingredient == null || ingredient.getId() == Item.AIR) {
            stream.putByte((byte) ItemDescriptorType.NONE.ordinal());
            stream.putVarInt(0);
            return;
        }

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, ingredient);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);
        int damage = ingredient.hasMeta() ? ingredient.getDamage() : 0x7fff;
        if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
            damage = 0;
        }

        if (ingredient.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        stream.putByte((byte) ItemDescriptorType.INTERNAL.ordinal());

        stream.putLShort(networkId);
        stream.putLShort(damage);

        stream.putVarInt(ingredient.getCount());
    }

    @Override
    public void putFurnaceRecipeIngredient(BinaryStream stream, Item ingredient, RecipeType type) {
        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, ingredient);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (ingredient.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        stream.putVarInt(networkId);

        if (type == RecipeType.FURNACE_DATA) {
            int damage = ingredient.hasMeta() ? ingredient.getDamage() : 0x7fff;
            if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
                damage = 0;
            }

            stream.putVarInt(damage);
        }
    }

    @Override
    public void putBrewingRecipeItem(BinaryStream stream, Item item) {
        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (item.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        int damage = item.getDamage();
        if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
            damage = 0;
        }

        stream.putVarInt(networkId);
        stream.putVarInt(damage);
    }

    @Override
    public void putMaterialReducerRecipeIngredient(BinaryStream stream, Item ingredient) {
        if (ingredient == null || ingredient.getId() == ItemID.AIR) {
            stream.putVarInt(ItemID.AIR);
            return;
        }

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, ingredient);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (ingredient.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        int damage = ingredient.hasMeta() ? ingredient.getDamage() : 0x7fff;
        if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
            damage = 0;
        }

        stream.putVarInt((networkId << 16) | damage);
    }

    @Override
    public int getItemNetworkId(BinaryStream stream, Item item) {
        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (item.getId() > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        return networkId;
    }
}
