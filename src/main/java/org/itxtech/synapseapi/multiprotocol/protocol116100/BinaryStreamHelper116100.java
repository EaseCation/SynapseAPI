package org.itxtech.synapseapi.multiprotocol.protocol116100;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.utils.*;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.BinaryStreamHelper116100NE;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BinaryStreamHelper116100 extends BinaryStreamHelper116100NE {

    public static BinaryStreamHelper116100 create() {
        return new BinaryStreamHelper116100();
    }

    @Override
    public Item getSlot(BinaryStream stream) {
        int networkId = stream.getVarInt();
        if (networkId == 0) {
            return Item.get(0, 0, 0);
        }

        int legacyFullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, networkId);
        boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, legacyFullId);
        int id = AdvancedRuntimeItemPalette.getId(this.protocol, legacyFullId);

        int auxValue = stream.getVarInt();
        int data = auxValue >> 8;
        if (hasData) {
            // Swap data using legacy full id
            data = AdvancedRuntimeItemPalette.getData(this.protocol, legacyFullId);
        } else if (data == Short.MAX_VALUE) {
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

        String[] canPlaceOn = new String[stream.getVarInt()];
        for (int i = 0; i < canPlaceOn.length; ++i) {
            canPlaceOn[i] = stream.getString();
        }

        String[] canDestroy = new String[stream.getVarInt()];
        for (int i = 0; i < canDestroy.length; ++i) {
            canDestroy[i] = stream.getString();
        }

        Item item = Item.get(
                id, data, cnt, nbt
        );

        if (canDestroy.length > 0 || canPlaceOn.length > 0) {
            CompoundTag namedTag = item.getNamedTag();
            if (namedTag == null) {
                namedTag = new CompoundTag();
            }

            if (canDestroy.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanDestroy");
                for (String blockName : canDestroy) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanDestroy", listTag);
            }

            if (canPlaceOn.length > 0) {
                ListTag<StringTag> listTag = new ListTag<>("CanPlaceOn");
                for (String blockName : canPlaceOn) {
                    listTag.add(new StringTag("", blockName));
                }
                namedTag.put("CanPlaceOn", listTag);
            }
            item.setNamedTag(namedTag);
        }

        if (item.getId() == 513) { // TODO: Shields
            stream.getVarLong();
        }

        return item;
    }

    @Override
    public void putSlot(BinaryStream stream, Item item) {
        if (item == null || item.getId() == 0) {
            stream.putVarInt(0);
            return;
        }

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, item);
        boolean clearData = AdvancedRuntimeItemPalette.hasData(this.protocol, networkFullId);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, networkFullId);
        stream.putVarInt(networkId);

        int auxValue = item.getCount();
        boolean isDurable = item instanceof ItemDurable;
        if (!isDurable) {
            int meta = clearData ? 0 : item.hasMeta() ? item.getDamage() : -1;
            auxValue |= ((meta & 0x7fff) << 8);
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

        if (item.getId() == 513) { // TODO: Shields
            stream.putVarLong(0);
        }
    }

    @Override
    public Item getRecipeIngredient(BinaryStream stream) {
        int networkId = stream.getVarInt();
        if (networkId == 0) {
            return Item.get(0, 0, 0);
        }

        int legacyFullId = AdvancedRuntimeItemPalette.getLegacyFullId(this.protocol, networkId);
        int id = AdvancedRuntimeItemPalette.getId(this.protocol, legacyFullId);
        boolean hasData = AdvancedRuntimeItemPalette.hasData(this.protocol, legacyFullId);

        int damage = stream.getVarInt();
        if (hasData) {
            damage = AdvancedRuntimeItemPalette.getData(this.protocol, legacyFullId);
        } else if (damage == 0x7fff) {
            damage = -1;
        }

        int count = stream.getVarInt();
        return Item.get(id, damage, count);
    }

    @Override
    public void putRecipeIngredient(BinaryStream stream, Item ingredient) {
        if (ingredient == null || ingredient.getId() == 0) {
            stream.putVarInt(0);
            return;
        }

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, ingredient);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, networkFullId);
        int damage = ingredient.hasMeta() ? ingredient.getDamage() : 0x7fff;
        if (AdvancedRuntimeItemPalette.hasData(this.protocol, networkFullId)) {
            damage = 0;
        }

        stream.putVarInt(networkId);
        stream.putVarInt(damage);
        stream.putVarInt(ingredient.getCount());
    }

    @Override
    public void putSkin(BinaryStream stream, Skin skin) {
        stream.putString(skin.getSkinId());
        stream.putString(skin.getSkinResourcePatch());
        stream.putImage(skin.getSkinData());

        List<SkinAnimation> animations = skin.getAnimations();
        stream.putLInt(animations.size());
        for (SkinAnimation animation : animations) {
            stream.putImage(animation.image);
            stream.putLInt(animation.type);
            stream.putLFloat(animation.frames);
            stream.putLInt(animation.expression);
        }

        stream.putImage(skin.getCapeData());
        stream.putString(skin.getGeometryData());
        stream.putString(skin.getAnimationData());
        stream.putBoolean(skin.isPremium());
        stream.putBoolean(skin.isPersona());
        stream.putBoolean(skin.isCapeOnClassic());
        stream.putString(skin.getCapeId());
        stream.putString(skin.getFullSkinId());
        stream.putString(skin.getArmSize());
        stream.putString(skin.getSkinColor());
        List<PersonaPiece> pieces = skin.getPersonaPieces();
        stream.putLInt(pieces.size());
        for (PersonaPiece piece : pieces) {
            stream.putString(piece.id);
            stream.putString(piece.type);
            stream.putString(piece.packId);
            stream.putBoolean(piece.isDefault);
            stream.putString(piece.productId);
        }

        List<PersonaPieceTint> tints = skin.getTintColors();
        stream.putLInt(tints.size());
        for (PersonaPieceTint tint : tints) {
            stream.putString(tint.pieceType);
            List<String> colors = tint.colors;
            stream.putLInt(colors.size());
            for (String color : colors) {
                stream.putString(color);
            }
        }
    }

    @Override
    public Skin getSkin(BinaryStream stream) {
        Skin skin = new Skin();
        skin.setSkinId(stream.getString());
        skin.setSkinResourcePatch(stream.getString());
        skin.setSkinData(stream.getImage());

        int animationCount = stream.getLInt();
        for (int i = 0; i < animationCount; i++) {
            SerializedImage image = stream.getImage();
            int type = stream.getLInt();
            float frames = stream.getLFloat();
            int expression = stream.getLInt();
            skin.getAnimations().add(new SkinAnimation(image, type, frames, expression));
        }

        skin.setCapeData(stream.getImage());
        skin.setGeometryData(stream.getString());
        skin.setAnimationData(stream.getString());
        skin.setPremium(stream.getBoolean());
        skin.setPersona(stream.getBoolean());
        skin.setCapeOnClassic(stream.getBoolean());
        skin.setCapeId(stream.getString());
        stream.getString(); // TODO: Full skin id
        skin.setArmSize(stream.getString());
        skin.setSkinColor(stream.getString());

        int piecesLength = stream.getLInt();
        for (int i = 0; i < piecesLength; i++) {
            String pieceId = stream.getString();
            String pieceType = stream.getString();
            String packId = stream.getString();
            boolean isDefault = stream.getBoolean();
            String productId = stream.getString();
            skin.getPersonaPieces().add(new PersonaPiece(pieceId, pieceType, packId, isDefault, productId));
        }

        int tintsLength = stream.getLInt();
        for (int i = 0; i < tintsLength; i++) {
            String pieceType = stream.getString();
            List<String> colors = new ArrayList<>();
            int colorsLength = stream.getLInt();
            for (int i2 = 0; i2 < colorsLength; i2++) {
                colors.add(stream.getString());
            }
            skin.getTintColors().add(new PersonaPieceTint(pieceType, colors));
        }
        return skin;
    }

    @Override
    public int getItemNetworkId(Item item) {
        return AdvancedRuntimeItemPalette.getNetworkId(this.protocol, AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol,item));
    }

}
