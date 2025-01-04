package org.itxtech.synapseapi.multiprotocol.protocol12130;

import cn.nukkit.network.protocol.types.ItemStackResponseContainerInfo;
import cn.nukkit.network.protocol.types.ItemStackResponseSlotInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;
import org.itxtech.synapseapi.multiprotocol.protocol12120.BinaryStreamHelper12120;

@Log4j2
public class BinaryStreamHelper12130 extends BinaryStreamHelper12120 {
    public static BinaryStreamHelper12130 create() {
        return new BinaryStreamHelper12130();
    }

    @Override
    public String getGameVersion() {
        return "1.21.30";
    }

    @Override
    protected void putItemStackResponseContainerInfo(BinaryStream stream, ItemStackResponseContainerInfo container) {
        stream.putByte(container.containerId);
        stream.putOptional(container.dynamicContainerId, BinaryStream::putLInt);
        for (ItemStackResponseSlotInfo slot : container.slots) {
            putItemStackResponseSlotInfo(stream, slot);
        }
    }

    @Override
    protected ItemStackRequestSlotInfo getItemStackRequestSlotInfo(BinaryStream stream) {
        ItemStackRequestSlotInfo info = new ItemStackRequestSlotInfo();
        info.containerId = stream.getByte();
        info.dynamicContainerId = stream.getBoolean() ? stream.getLInt() : null;
        info.slotId = stream.getByte();
        info.stackId = stream.getVarInt();
        return info;
    }
}
