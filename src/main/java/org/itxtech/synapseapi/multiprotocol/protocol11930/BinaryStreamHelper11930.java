package org.itxtech.synapseapi.multiprotocol.protocol11930;

import cn.nukkit.item.Item;
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
    public void putCraftingRecipeIngredient(BinaryStream stream, Item ingredient) {
        if (ingredient == null || ingredient.getId() == Item.AIR) {
            stream.putBoolean(false);
            stream.putVarInt(0);
            return;
        }

        int networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(this.protocol, stream.neteaseMode, ingredient);
        int networkId = AdvancedRuntimeItemPalette.getNetworkId(this.protocol, stream.neteaseMode, networkFullId);
        int damage = ingredient.hasMeta() ? ingredient.getDamage() : 0x7fff;
        if (AdvancedRuntimeItemPalette.hasData(this.protocol, stream.neteaseMode, networkFullId)) {
            damage = 0;
        }

        stream.putBoolean(true);
        stream.putLShort(networkId);
        stream.putLShort(damage);
        stream.putVarInt(ingredient.getCount());
    }
}
