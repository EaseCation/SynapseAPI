package org.itxtech.synapseapi.multiprotocol.protocol11930;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.protocol.types.ItemDescriptorType;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol11921.BinaryStreamHelper11921;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;

public class BinaryStreamHelper11930 extends BinaryStreamHelper11921 {

    public static BinaryStreamHelper11930 create() {
        return new BinaryStreamHelper11930();
    }

    @Override
    public String getGameVersion() {
        return "1.19.30";
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

        stream.putByte((byte) ItemDescriptorType.INTERNAL.ordinal());

        stream.putLShort(networkId);
        stream.putLShort(damage);

        stream.putVarInt(ingredient.getCount());
    }

    @Override
    public int getItemStackRequest(BinaryStream stream) {
        int request = super.getItemStackRequest(stream);

        int filterStringCause = stream.getLInt();

        return request;
    }
}
