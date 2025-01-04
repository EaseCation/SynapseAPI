package org.itxtech.synapseapi.multiprotocol.protocol12150;

import cn.nukkit.network.protocol.types.ItemStackResponseSlotInfo;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol12140.BinaryStreamHelper12140;

public class BinaryStreamHelper12150 extends BinaryStreamHelper12140 {
    public static BinaryStreamHelper12150 create() {
        return new BinaryStreamHelper12150();
    }

    @Override
    public String getGameVersion() {
        return "1.21.50";
    }

    @Override
    protected void putItemStackResponseSlotInfo(BinaryStream stream, ItemStackResponseSlotInfo info) {
        stream.putByte(info.slot);
        stream.putByte(info.hotbarSlot);
        stream.putByte(info.count);
        stream.putVarInt(info.itemStackId);
        stream.putString(info.customName);
        stream.putString(info.filteredCustomName);
        stream.putVarInt(info.durabilityCorrection);
    }
}
