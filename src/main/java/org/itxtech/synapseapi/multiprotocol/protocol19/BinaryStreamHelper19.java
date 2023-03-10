package org.itxtech.synapseapi.multiprotocol.protocol19;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.utils.BinaryStream;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import org.itxtech.synapseapi.multiprotocol.protocol18.BinaryStreamHelper18;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

public class BinaryStreamHelper19 extends BinaryStreamHelper18 {

    public static BinaryStreamHelper19 create() {
        return new BinaryStreamHelper19();
    }

    @Override
    public Item getSlot(BinaryStream stream) {
        int id = stream.getVarInt();
        if (id == ItemID.AIR) {
            return Item.get(ItemID.AIR, 0, 0);
        }

        if (id < Short.MIN_VALUE || id >= Short.MAX_VALUE) {
            throw new RuntimeException("Invalid item ID received: " + id);
        }

        int auxValue = stream.getVarInt();
        int data = auxValue >> 8;
        if (data == Short.MAX_VALUE) {
            data = -1;
        }
        int cnt = auxValue & 0xff;

        int nbtLen = stream.getLShort();
        byte[] nbt = new byte[0];
        if (nbtLen < Short.MAX_VALUE) {
            nbt = stream.get(nbtLen);
        } else if (nbtLen == 65535) {
            int nbtTagCount = (int) stream.getUnsignedVarInt();
            int offset = stream.getOffset();
            FastByteArrayInputStream inputStream = new FastByteArrayInputStream(stream.get());
            for (int i = 0; i < nbtTagCount; i++) {
                try {
                    // TODO: 05/02/2019 This hack is necessary because we keep the raw NBT tag. Try to remove it.
                    CompoundTag tag = NBTIO.read(inputStream, ByteOrder.LITTLE_ENDIAN, true);
                    // tool damage hack
                    if (tag.contains("Damage")) {
                        data = tag.getInt("Damage");
                        tag.remove("Damage");
                    }
                    if (tag.contains("__DamageConflict__")) {
                        tag.put("Damage", tag.removeAndGet("__DamageConflict__"));
                    }
                    if (tag.getAllTags().size() > 0) {
                        nbt = NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, false);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            stream.setOffset(offset + (int) inputStream.position());
        }

        if (data < 0 || data >= Short.MAX_VALUE) {
            throw new RuntimeException("Invalid item meta received: " + data);
        }

        ListTag<StringTag> canPlace;
        ListTag<StringTag> canBreak;

        int canPlaceCount = stream.getVarInt();
        if (canPlaceCount > 0) {
            if (canPlaceCount > 4096) {
                throw new IndexOutOfBoundsException("Too many CanPlaceOn blocks");
            }
            canPlace = new ListTag<>("CanPlaceOn");
            for (int i = 0; i < canPlaceCount; i++) {
                canPlace.add(new StringTag("", stream.getString()));
            }
        } else {
            canPlace = null;
        }

        int canBreakCount = stream.getVarInt();
        if (canBreakCount > 0) {
            if (canBreakCount > 4096) {
                throw new IndexOutOfBoundsException("Too many CanDestroy blocks");
            }
            canBreak = new ListTag<>("CanDestroy");
            for (int i = 0; i < canBreakCount; i++) {
                canBreak.add(new StringTag("", stream.getString()));
            }
        } else {
            canBreak = null;
        }

        Item item = Item.get(
                id, data, cnt, nbt
        );

        if (canPlace != null || canBreak != null) {
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) {
                namedTag = new CompoundTag();
            }

            if (canPlace != null) {
                namedTag.putList(canPlace);
            }
            if (canBreak != null) {
                namedTag.putList(canBreak);
            }

            item.setNamedTag(namedTag);
        }

        return item;
    }

    @Override
    public void putSlot(BinaryStream stream, Item item) {
        if (item == null || item.getId() == ItemID.AIR) {
            stream.putVarInt(0);
            return;
        }

        boolean isDurable = item instanceof ItemDurable;

        stream.putVarInt(item.getId());

        int auxValue = item.getCount();
        if (!isDurable) {
            auxValue |= (((item.hasMeta() ? item.getDamage() : -1) & 0x7fff) << 8);
        }
        stream.putVarInt(auxValue);

        if (item.hasCompoundTag() || isDurable) {
            try {
                // hack for tool damage
                byte[] nbt = item.getCompoundTag();
                CompoundTag tag;
                if (nbt == null || nbt.length == 0) {
                    tag = new CompoundTag();
                } else {
                    tag = NBTIO.read(nbt, ByteOrder.LITTLE_ENDIAN, false);
                }
                if (tag.contains("Damage")) {
                    tag.put("__DamageConflict__", tag.removeAndGet("Damage"));
                }
                if (isDurable) {
                    tag.putInt("Damage", item.getDamage());
                }

                stream.putLShort(0xffff);
                stream.putByte((byte) 1);
                stream.put(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            stream.putLShort(0);
        }
        List<String> canPlaceOn = extractStringList(stream, item, "CanPlaceOn");
        List<String> canDestroy = extractStringList(stream, item, "CanDestroy");
        stream.putVarInt(canPlaceOn.size());
        for (String block : canPlaceOn) {
            stream.putString(block);
        }
        stream.putVarInt(canDestroy.size());
        for (String block : canDestroy) {
            stream.putString(block);
        }
    }
}
