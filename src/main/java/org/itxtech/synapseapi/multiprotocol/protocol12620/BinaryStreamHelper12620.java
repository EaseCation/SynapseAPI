package org.itxtech.synapseapi.multiprotocol.protocol12620;

import cn.nukkit.block.Block;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.item.*;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.network.LittleEndianByteBufInputStream;
import cn.nukkit.network.LittleEndianByteBufOutputStream;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.protocol12610.BinaryStreamHelper12610;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Set;

@Log4j2
public class BinaryStreamHelper12620 extends BinaryStreamHelper12610 {
    public static BinaryStreamHelper12620 create() {
        return new BinaryStreamHelper12620();
    }

    @Override
    public String getGameVersion() {
        return "1.26.20";
    }

    @Override
    protected void registerCommandParameterTypes() {
        int ARG_TYPE_INT = 1;
        int ARG_TYPE_FLOAT = 3;
        int ARG_TYPE_VALUE = 4;
        int ARG_TYPE_WILDCARD_INT = 5;
        int ARG_TYPE_OPERATOR = 6;
        int ARG_TYPE_COMPARE_OPERATOR = 7;
        int ARG_TYPE_TARGET = 8;
        int ARG_TYPE_WILDCARD_TARGET = 10;
        int ARG_TYPE_FILE_PATH = 17;
        int ARG_TYPE_INTEGER_RANGE = 23;
        int ARG_TYPE_EQUIPMENT_SLOT = 47;
        int ARG_TYPE_STRING = 56;
        int ARG_TYPE_BLOCK_POSITION = 64;
        int ARG_TYPE_POSITION = 65;
        int ARG_TYPE_MESSAGE = 68;
        int ARG_TYPE_RAWTEXT = 70;
        int ARG_TYPE_JSON = 74;
        int ARG_TYPE_BLOCK_STATES = 84;
        int ARG_TYPE_COMMAND = 88;

        this.registerCommandParameterType(CommandParamType.INT, ARG_TYPE_INT);
        this.registerCommandParameterType(CommandParamType.FLOAT, ARG_TYPE_FLOAT);
        this.registerCommandParameterType(CommandParamType.VALUE, ARG_TYPE_VALUE);
        this.registerCommandParameterType(CommandParamType.WILDCARD_INT, ARG_TYPE_WILDCARD_INT);
        this.registerCommandParameterType(CommandParamType.COMPARE_OPERATOR, ARG_TYPE_COMPARE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.OPERATOR, ARG_TYPE_OPERATOR);
        this.registerCommandParameterType(CommandParamType.TARGET, ARG_TYPE_TARGET);
        this.registerCommandParameterType(CommandParamType.WILDCARD_TARGET, ARG_TYPE_WILDCARD_TARGET);
        this.registerCommandParameterType(CommandParamType.EQUIPMENT_SLOT, ARG_TYPE_EQUIPMENT_SLOT);
        this.registerCommandParameterType(CommandParamType.STRING, ARG_TYPE_STRING);
        this.registerCommandParameterType(CommandParamType.BLOCK_POSITION, ARG_TYPE_BLOCK_POSITION);
        this.registerCommandParameterType(CommandParamType.POSITION, ARG_TYPE_POSITION);
        this.registerCommandParameterType(CommandParamType.MESSAGE, ARG_TYPE_MESSAGE);
        this.registerCommandParameterType(CommandParamType.RAWTEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.JSON, ARG_TYPE_JSON);
        this.registerCommandParameterType(CommandParamType.TEXT, ARG_TYPE_RAWTEXT);
        this.registerCommandParameterType(CommandParamType.COMMAND, ARG_TYPE_COMMAND);
        this.registerCommandParameterType(CommandParamType.FILE_PATH, ARG_TYPE_FILE_PATH);
        this.registerCommandParameterType(CommandParamType.INTEGER_RANGE, ARG_TYPE_INTEGER_RANGE);
        this.registerCommandParameterType(CommandParamType.BLOCK_STATES, ARG_TYPE_BLOCK_STATES);
    }

    @Override
    public Item getItemStack(BinaryStream stream) {
        int id = stream.getLSignedShort();
        int count = stream.getLShort();
        int damage = (int) stream.getUnsignedVarInt();
        boolean hasStackId = stream.getBoolean();
        int stackIdType;
        int stackId;
        if (hasStackId) {
            stackIdType = (int) stream.getUnsignedVarInt();
            stackId = stream.getVarInt();
        }
        int blockRuntimeId = (int) stream.getUnsignedVarInt();
        byte[] userData = stream.getByteArray();

        if (id == Item.AIR || count == 0) {
            return Items.air();
        }

        int fullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, stream.neteaseMode, id);
        id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, fullId);

        boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, fullId);
        if (hasData) {
            damage = AdvancedRuntimeItemPalette.getData(this.protocol, stream.neteaseMode, fullId);
        }

        if (id == ItemBlockID.IRON_CHAIN) { //TODO: special item block rename
            id = Item.CHAIN;
        }

        if (id < 256 && id != Item.GLOW_STICK) { // ItemBlock
            int legacyId = AdvancedGlobalBlockPalette.getLegacyId(this.protocol, stream.neteaseMode, blockRuntimeId);
            if (legacyId != -1) {
                damage = legacyId & Block.getUnsafe(Block.itemIdToBlockId(id)).getItemKeepMetaMask();
            }
        }

        byte[] nbt = new byte[0];
        Set<String> canPlace = null;
        Set<String> canBreak = null;
        long blockingTicks = 0;

        int length = userData.length;
        if (length > 0) {
            ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer(length);
            buf.writeBytes(userData);

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
                    if (id != Item.GLOW_STICK && id != Item.SPARKLER) {
                        if (compoundTag.contains("Damage")) {
                            damage = compoundTag.getInt("Damage");
                            compoundTag.remove("Damage");
                        }
                        Tag nkDamageTag = compoundTag.removeAndGet("__DamageConflict__");
                        if (nkDamageTag != null) {
                            compoundTag.put("Damage", nkDamageTag);
                        }
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
                }

                if (id == ItemID.SHIELD) {
                    blockingTicks = in.readLong();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read item user data", e);
            } finally {
                buf.release();
            }
        }

        if (damage < 0 || damage >= Short.MAX_VALUE) {
            throw new RuntimeException("Invalid item meta received: " + damage);
        }

        Item item = Item.get(id, damage, count, nbt);

        if (item.isItemBlock()) {
            item.setDamage(0);
        }

        if (canPlace != null && !canPlace.isEmpty()) {
            item.setCanPlaceOnBlocks(canPlace);
        }

        if (canBreak != null && !canBreak.isEmpty()) {
            item.setCanDestroyBlocks(canBreak);
        }

        return item;
    }

    protected static final byte[] SERIALIZED_EMPTY_ITEM_STACK = Utils.make(() -> {
        BinaryStream stream = new BinaryStream();
        stream.putLShort(0); // itemId
        stream.putLShort(0); // count
        stream.putUnsignedVarInt(0); // auxVal

        final boolean hasStackId = false;
        stream.putBoolean(hasStackId);
        if (hasStackId) {
            stream.putUnsignedVarInt(0); // stackIdType
            stream.putVarInt(0); // stackId
        }

        stream.putVarInt(0); // blockRuntimeId
        stream.putUnsignedVarInt(0); // userData.length = stream.putByteArray(new byte[0]);
        return stream.getBuffer(); // 8 bytes = 2 + 2 + 1 + 1 + 1 + 1
    });

    @Override
    public void putItemStack(BinaryStream stream, Item item) {
        if (item.isNull()) {
            stream.put(SERIALIZED_EMPTY_ITEM_STACK);
            return;
        }

        if (item.getCount() > 64) {
            throw new IllegalArgumentException("Item stack size must be less than or equal to 64");
        }

        int id = item.getId();
        int meta = item.getDamage();
        boolean isBlock = id < 256 && id != Item.GLOW_STICK;
        boolean isDurable = item instanceof ItemDurable;

        int networkFullId;
        if (id == Item.CHAIN) { //TODO: special item block rename
            id = ItemBlockID.IRON_CHAIN;
            networkFullId = id;
        } else {
            networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, item);
        }
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);

        if (id > 0 && networkId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
            networkId >>= 1;
        }

        stream.putLShort(networkId);
        stream.putLShort(item.getCount());

        boolean useLegacyData = !isBlock && !isDurable && !AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId);
        stream.putUnsignedVarInt(useLegacyData ? meta : 0);

        boolean hasStackId = true;
        stream.putBoolean(hasStackId);
        if (hasStackId) {
            stream.putUnsignedVarInt(0); // stackIdType: 0 ItemStackNetId, 1 ItemStackRequestId, 2 ItemStackLegacyRequestId
            stream.putVarInt(1); // stackId
        }

        Block block = isBlock ? item.getBlockUnsafe() : null;
        int runtimeId = block == null || block.isBlockItem() ? 0 : AdvancedGlobalBlockPalette.getOrCreateRuntimeId(this.protocol, stream.neteaseMode, block.getId(), block.getItemSerializationMeta());
        stream.putUnsignedVarInt(runtimeId);

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
            if (isDurable) {
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
}
