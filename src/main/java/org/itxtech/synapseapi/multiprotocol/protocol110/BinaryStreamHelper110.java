package org.itxtech.synapseapi.multiprotocol.protocol110;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol19.BinaryStreamHelper19;

public class BinaryStreamHelper110 extends BinaryStreamHelper19 {

    public static BinaryStreamHelper110 create() {
        return new BinaryStreamHelper110();
    }

    @Override
    public Item getSlot(BinaryStream stream) {
        Item item = super.getSlot(stream);

        if (item.getId() == ItemID.SHIELD) { // TODO: Shields
            stream.getVarLong();
        }

        return item;
    }

    @Override
    public void putSlot(BinaryStream stream, Item item) {
        super.putSlot(stream, item);

        if (item.getId() == ItemID.SHIELD) { // TODO: Shields
            stream.putVarLong(0);
        }
    }
}
