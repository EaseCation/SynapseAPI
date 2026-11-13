package org.itxtech.synapseapi;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import org.itxtech.synapseapi.inventory.ItemStackContainerIdTranslator;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.ItemStackRequestSlotInfo;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.DropStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.MineBlockStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.PlaceStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.SwapStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.TakeStackRequestAction;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.ItemStackRequestPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

/**
 * 输入任务能否在两个世界 tick 之间执行（全包准入 M9：默认放行 + 显式屏障）。
 * 补充阶段与下一 tick 开头的 drain 之间没有调度器、Level 或实体更新，调度器 tick 相同；
 * 可观察差异只有 getTick() 少 1、墙钟提前与回包更早。只把结果会因此改变的包列为屏障。
 */
final class BetweenTickPackets {
    private BetweenTickPackets() {
    }

    /**
     * 服务端权威库存开启时，库存请求只有普通存储格之间的取、放、交换、丢弃可以放行，
     * 其余请求（合成、附魔、动态容器等）作为屏障；关闭时与 {@link #supports(DataPacket)} 相同。
     */
    static boolean supports(DataPacket packet, boolean authoritativeInventory, AbstractProtocol protocol, boolean netease) {
        if (authoritativeInventory) {
            if (packet instanceof ItemStackRequestPacket116 requests) {
                if (requests.requests == null) return false;
                for (ItemStackRequest request : requests.requests) {
                    if (!supportsInventoryRequest(request, protocol, netease)) return false;
                }
            } else if (packet instanceof IPlayerAuthInputPacket input && input.getItemStackRequest() != null
                    && !supportsInventoryRequest(input.getItemStackRequest(), protocol, netease)) {
                return false;
            }
        }
        return supports(packet);
    }

    private static boolean supportsInventoryRequest(ItemStackRequest request, AbstractProtocol protocol, boolean netease) {
        if (request == null || request.actions == null) return false;
        for (ItemStackRequestAction action : request.actions) {
            boolean ordinary = switch (action) {
                case TakeStackRequestAction take -> ordinarySlot(take.source, protocol, netease)
                        && ordinarySlot(take.destination, protocol, netease);
                case PlaceStackRequestAction place -> ordinarySlot(place.source, protocol, netease)
                        && ordinarySlot(place.destination, protocol, netease);
                case SwapStackRequestAction swap -> ordinarySlot(swap.source, protocol, netease)
                        && ordinarySlot(swap.destination, protocol, netease);
                case DropStackRequestAction drop -> ordinarySlot(drop.source, protocol, netease);
                // 挖方块请求只登记客户端预测的工具耐久：不生成库存事务、不读 tick，实际耐久仍由同帧挖掘逻辑修改。
                // 客户端随每次用耐久工具挖碎方块的 AuthInput 帧发送；若保留屏障，整帧移动连同其后全部玩家的输入都要等下一世界 tick。
                case MineBlockStackRequestAction ignored -> true;
                case null, default -> false;
            };
            if (!ordinary) return false;
        }
        return true;
    }

    private static boolean ordinarySlot(ItemStackRequestSlotInfo slot, AbstractProtocol protocol, boolean netease) {
        return slot != null && slot.dynamicContainerId == null
                && ItemStackContainerIdTranslator.isOrdinaryStorageSlot(protocol, netease, slot.containerId);
    }

    /**
     * 唯一屏障：资源包完成回复进入登录收尾（PlayerLoginEvent、房间加入与重连），
     * 已有业务按 getTick() 差值计算（如重连镜头分段），提前执行会少算 1 tick。
     */
    static boolean supports(DataPacket packet) {
        if (packet instanceof ResourcePackClientResponsePacket16 response) {
            return response.responseStatus != ResourcePackClientResponsePacket16.STATUS_COMPLETED;
        }
        if (packet instanceof ResourcePackClientResponsePacket response) {
            return response.responseStatus != ResourcePackClientResponsePacket.STATUS_COMPLETED;
        }
        return true;
    }
}
