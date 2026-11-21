package org.itxtech.synapseapi;

import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全包准入 M9 默认放行；M10 起资源包完成回复（登录收尾）也放行，按包类型没有静态屏障。
 * 按字段残缺、未审子类型等旧条件不再设屏障：两处执行位置的结果相同。
 */
class BetweenTickPacketsTest {
    @Test
    void everyResourcePackResponseIncludingCompletionIsAdmitted() {
        ResourcePackClientResponsePacket16 modern = new ResourcePackClientResponsePacket16();
        ResourcePackClientResponsePacket vanilla = new ResourcePackClientResponsePacket();
        for (byte status = 0; status <= ResourcePackClientResponsePacket16.STATUS_COMPLETED + 1; status++) {
            modern.responseStatus = status;
            vanilla.responseStatus = status;
            assertTrue(BetweenTickPackets.supports(modern));
            assertTrue(BetweenTickPackets.supports(vanilla));
        }
    }

    @Test
    void formerFieldConditionsNoLongerHoldTheQueue() {
        AnimatePacket wakeUp = new AnimatePacket();
        wakeUp.action = AnimatePacket.Action.WAKE_UP;
        assertTrue(BetweenTickPackets.supports(wakeUp));
        assertTrue(BetweenTickPackets.supports(new MobEquipmentPacket()));
        InventoryTransactionPacket116 incomplete = new InventoryTransactionPacket116();
        incomplete.actions = null;
        assertTrue(BetweenTickPackets.supports(incomplete));
    }
}
