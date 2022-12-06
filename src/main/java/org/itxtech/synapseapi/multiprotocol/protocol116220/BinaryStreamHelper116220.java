package org.itxtech.synapseapi.multiprotocol.protocol116220;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.network.LittleEndianByteBufInputStream;
import cn.nukkit.network.LittleEndianByteBufOutputStream;
import cn.nukkit.utils.BinaryStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.protocol116210.BinaryStreamHelper116210;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

@Log4j2
public class BinaryStreamHelper116220 extends BinaryStreamHelper116210 {

    public static BinaryStreamHelper116220 create() {
        return new BinaryStreamHelper116220();
    }

    @Override
    public String getGameVersion() {
        return "1.16.220";
    }

    @Override
    public final Item getSlot(BinaryStream stream) {
        return this.getSlot(stream, false);
    }

    @Override
    public final Item getItemInstance(BinaryStream stream) {
        return this.getSlot(stream, true);
    }

    protected Item getSlot(BinaryStream stream, boolean instanceItem) {
        int id = stream.getVarInt();
        if (id == 0) {
            return Item.get(Item.AIR, 0, 0);
        }

        int count = stream.getLShort();
        int damage = (int) stream.getUnsignedVarInt();

        int fullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, stream.neteaseMode, id);
        id = AdvancedRuntimeItemPalette.getId(this.protocol, stream.neteaseMode, fullId);

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

        byte[] bytes = stream.getByteArray();
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer(bytes.length);
        buf.writeBytes(bytes);

        byte[] nbt = new byte[0];
        String[] canPlace;
        String[] canBreak;

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

            if (compoundTag != null && compoundTag.getAllTags().size() > 0) {
                if (compoundTag.contains("Damage")) {
                    damage = compoundTag.getInt("Damage");
                    compoundTag.remove("Damage");
                }
                if (compoundTag.contains("__DamageConflict__")) {
                    compoundTag.put("Damage", compoundTag.removeAndGet("__DamageConflict__"));
                }
                if (!compoundTag.isEmpty()) {
                    nbt = NBTIO.write(compoundTag, ByteOrder.LITTLE_ENDIAN);
                }
            }

            canPlace = new String[in.readInt()];
            for (int i = 0; i < canPlace.length; i++) {
                canPlace[i] = in.readUTF();
            }

            canBreak = new String[in.readInt()];
            for (int i = 0; i < canBreak.length; i++) {
                canBreak[i] = in.readUTF();
            }

            if (id == ItemID.SHIELD) {
                in.readLong();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read item user data", e);
        } finally {
            buf.release();
        }

        Item item = Item.get(id, damage, count, nbt);

        if (canBreak.length > 0 || canPlace.length > 0) {
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) {
                namedTag = new CompoundTag();
            }

            if (canBreak.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanDestroy");
                for (String blockName : canBreak) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanDestroy", listTag);
            }

            if (canPlace.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanPlaceOn");
                for (String blockName : canPlace) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanPlaceOn", listTag);
            }

            item.setNamedTag(namedTag);
        }

        return item;
    }

    @Override
    public final void putSlot(BinaryStream stream, Item item) {
        this.putSlot(stream, item, false);
    }

    @Override
    public final void putItemInstance(BinaryStream stream, Item item) {
        this.putSlot(stream, item, true);
    }

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
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage"));
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

            List<String> canPlaceOn = extractStringList(stream, item, "CanPlaceOn");
            out.writeInt(canPlaceOn.size());
            for (String string : canPlaceOn) {
                out.writeUTF(string);
            }

            List<String> canDestroy = extractStringList(stream, item, "CanDestroy");
            out.writeInt(canDestroy.size());
            for (String string : canDestroy) {
                out.writeUTF(string);
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
