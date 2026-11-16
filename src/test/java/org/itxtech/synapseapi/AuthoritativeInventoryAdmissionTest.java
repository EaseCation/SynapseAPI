package org.itxtech.synapseapi;

import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import org.itxtech.synapseapi.inventory.ItemStackContainerIdTranslator;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.CreativeCreateStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.MineBlockStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.TakeStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.ItemStackRequestPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 服务端权威库存（SAI）开启时的补充 tick 准入：库存请求独立成包或夹带在 AuthInput 中都按同一名单判定。
 * 只登记耐久预测的挖方块请求放行；名单外的动作仍是屏障，夹带在 AuthInput 中时整帧等世界 tick，不能借移动帧绕过。
 */
class AuthoritativeInventoryAdmissionTest {
    private static AbstractProtocol PROTOCOL;

    @BeforeAll
    static void initialize() {
        // 协议枚举初始化依赖 Server 单例，沿用已有准入测试的协议准备。
        LegacyMovementAdmissionTest.protocols();
        PROTOCOL = AbstractProtocol.PROTOCOL_126;
    }

    @Test
    void miningFrameIsAdmittedWithAuthoritativeInventory() {
        assertTrue(BetweenTickPackets.supports(frame(request(mine())), true, PROTOCOL, false));
        assertTrue(BetweenTickPackets.supports(frame(request(mine())), true, PROTOCOL, true));
        assertTrue(BetweenTickPackets.supports(packet(request(mine())), true, PROTOCOL, false));
    }

    @Test
    void miningMixedWithOrdinaryMovesIsAdmitted() {
        assertTrue(BetweenTickPackets.supports(frame(request(mine(), ordinaryTake())), true, PROTOCOL, false));
    }

    @Test
    void unlistedActionsStillHoldTheFrameAndThePacket() {
        assertFalse(BetweenTickPackets.supports(frame(request(new CreativeCreateStackRequestAction())), true, PROTOCOL, false));
        assertFalse(BetweenTickPackets.supports(frame(request(mine(), new CreativeCreateStackRequestAction())), true, PROTOCOL, false));
        assertFalse(BetweenTickPackets.supports(packet(request(mine()), request(new CreativeCreateStackRequestAction())), true, PROTOCOL, false));
    }

    @Test
    void malformedRequestsStayBarriers() {
        ItemStackRequest empty = new ItemStackRequest();
        assertFalse(BetweenTickPackets.supports(frame(empty), true, PROTOCOL, false));
        assertFalse(BetweenTickPackets.supports(frame(request((ItemStackRequestAction) null)), true, PROTOCOL, false));
        assertFalse(BetweenTickPackets.supports(new ItemStackRequestPacket116(), true, PROTOCOL, false));
    }

    @Test
    void framesWithoutRequestsAndDisabledModeKeepTheDefaultResult() {
        assertTrue(BetweenTickPackets.supports(frame(null), true, PROTOCOL, false));
        assertTrue(BetweenTickPackets.supports(frame(request(new CreativeCreateStackRequestAction())), false, PROTOCOL, false));
        assertTrue(BetweenTickPackets.supports(packet(request(new CreativeCreateStackRequestAction())), false, PROTOCOL, false));
    }

    private static MineBlockStackRequestAction mine() {
        MineBlockStackRequestAction mine = new MineBlockStackRequestAction();
        mine.hotbarSlot = 0;
        mine.predictedDurability = 1;
        mine.stackId = 2;
        return mine;
    }

    private static TakeStackRequestAction ordinaryTake() {
        int hotbar = ItemStackContainerIdTranslator.getHotbarSlotTypeId(PROTOCOL, false);
        assertTrue(ItemStackContainerIdTranslator.isOrdinaryStorageSlot(PROTOCOL, false, hotbar));
        TakeStackRequestAction take = new TakeStackRequestAction();
        take.count = 1;
        take.source = slot(hotbar, 0);
        take.destination = slot(hotbar, 1);
        return take;
    }

    private static ItemStackRequestSlotInfo slot(int container, int slot) {
        ItemStackRequestSlotInfo info = new ItemStackRequestSlotInfo();
        info.containerId = container;
        info.slotId = slot;
        return info;
    }

    private static ItemStackRequest request(ItemStackRequestAction... actions) {
        ItemStackRequest request = new ItemStackRequest();
        request.actions = actions;
        return request;
    }

    private static PlayerAuthInputPacket116220 frame(ItemStackRequest request) {
        PlayerAuthInputPacket116220 frame = new PlayerAuthInputPacket116220();
        frame.itemStackRequest = request;
        return frame;
    }

    private static ItemStackRequestPacket116 packet(ItemStackRequest... requests) {
        ItemStackRequestPacket116 packet = new ItemStackRequestPacket116();
        packet.requests = requests;
        return packet;
    }
}
