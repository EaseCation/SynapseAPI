package org.itxtech.synapseapi;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.ResourcePackChunkRequestPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerHotbarPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.AnimatePacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AnimatePacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheBlobStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;

import java.util.BitSet;

/** 仅分类已审计的协议工作，不从客户端状态推导服务端物理或权限。 */
final class BetweenTickPackets {
    // 限制补充阶段的原处理器遍历量；大包保持完整，回到普通世界阶段处理。
    static final int MAX_CHUNK_ENTRIES_BETWEEN_TICKS = 64;
    private static final long MOVEMENT_FLAGS = ((1L << PlayerAuthInputFlags.START_GLIDING) - 1)
            & ~((1L << PlayerAuthInputFlags.START_SWIMMING) | (1L << PlayerAuthInputFlags.STOP_SWIMMING))
            | (1L << PlayerAuthInputFlags.HANDLED_TELEPORT);
    private static final long PURE_POSE_FLAGS = (1L << PlayerAuthInputFlags.START_FLYING)
            | (1L << PlayerAuthInputFlags.STOP_FLYING)
            | (1L << PlayerAuthInputFlags.START_SWIMMING)
            | (1L << PlayerAuthInputFlags.STOP_SWIMMING)
            | (1L << PlayerAuthInputFlags.START_CRAWLING)
            | (1L << PlayerAuthInputFlags.STOP_CRAWLING)
            | (1L << PlayerAuthInputFlags.START_GLIDING)
            | (1L << PlayerAuthInputFlags.STOP_GLIDING);
    // 旋转标志在网易扩展位之后；只登记原攻击计时，碰撞攻击仍由世界周期执行。
    private static final long SPIN_FLAGS = (1L << PlayerAuthInputFlags.START_SPIN_ATTACK)
            | (1L << PlayerAuthInputFlags.STOP_SPIN_ATTACK);
    private static final long CONTEXT_FLAGS = (1L << PlayerAuthInputFlags.ACK_ENTITY_DATA)
            | (1L << PlayerAuthInputFlags.BLOCK_BREAKING_DELAY_ENABLED)
            | (1L << PlayerAuthInputFlags.HORIZONTAL_COLLISION)
            | (1L << PlayerAuthInputFlags.VERTICAL_COLLISION)
            | (1L << PlayerAuthInputFlags.DOWN_LEFT)
            | (1L << PlayerAuthInputFlags.DOWN_RIGHT)
            | (1L << PlayerAuthInputFlags.IS_CAMERA_RELATIVE_MOVEMENT_ENABLED)
            | (1L << PlayerAuthInputFlags.IS_ROT_CONTROLLED_BY_MOVE_DIRECTION)
            | (1L << PlayerAuthInputFlags.IS_HOTBAR_ONLY_TOUCH)
            | (1L << PlayerAuthInputFlags.JUMP_RELEASED_RAW)
            | (1L << PlayerAuthInputFlags.JUMP_PRESSED_RAW)
            | (1L << PlayerAuthInputFlags.JUMP_CURRENT_RAW)
            | (1L << PlayerAuthInputFlags.SNEAK_RELEASED_RAW)
            | (1L << PlayerAuthInputFlags.SNEAK_PRESSED_RAW);
    private static final long CONTEXT_FLAGS2 = 1L << (PlayerAuthInputFlags.SNEAK_CURRENT_RAW - Long.SIZE);

    private BetweenTickPackets() {
    }

    static boolean supports(DataPacket packet) {
        if (packet instanceof NetworkStackLatencyPacket19 || packet instanceof ResourcePackChunkRequestPacket) {
            return true;
        }
        if (packet instanceof SubChunkRequestPacket11810 request) {
            // 只登记原子区块队列；构建和正常发送仍由 checkNetwork 的原节流驱动。
            return request.positionOffsets != null
                    && request.positionOffsets.length <= MAX_CHUNK_ENTRIES_BETWEEN_TICKS;
        }
        if (packet instanceof ClientCacheBlobStatusPacket112 status) {
            // 确认记账保持输入原序；回复沿原出站队列异步编码，不重放或延后确认本身。
            return status.hitSet != null && status.missSet != null
                    && status.hitSet.size() + status.missSet.size() <= MAX_CHUNK_ENTRIES_BETWEEN_TICKS;
        }
        if (packet instanceof InteractPacket113 interact) {
            // 看向实体沿原事件与提示更新处理；开库存、下车等动作仍保持世界阶段屏障。
            return interact.action == InteractPacket113.ACTION_MOUSEOVER;
        }
        if (packet instanceof NEPyRpcPacket16 rpc) {
            // JE 的输入扩展先于普通交互包；解码限额及每个回调后的会话复检沿用原实现。
            return rpc.subPackets != null;
        }
        if (packet instanceof MobEquipmentPacket equipment) {
            return equipment.windowId == ContainerIds.INVENTORY && equipment.item != null;
        }
        if (packet instanceof PlayerHotbarPacket hotbar) {
            return hotbar.windowId == ContainerIds.INVENTORY;
        }
        if (packet instanceof PlayerHotbarPacket14 hotbar) {
            return hotbar.windowId == ContainerIds.INVENTORY;
        }
        if (packet instanceof AnimatePacket animation) {
            return animation.action == AnimatePacket.Action.SWING_ARM;
        }
        if (packet instanceof AnimatePacket121120 animation) {
            return animation.action == AnimatePacket.Action.SWING_ARM && Float.isFinite(animation.data);
        }
        if (packet instanceof AnimatePacket121130 animation) {
            return animation.action == AnimatePacket.Action.SWING_ARM && Float.isFinite(animation.data);
        }
        if (packet instanceof InventoryTransactionPacket116 transaction) {
            return supportsItemAction(transaction);
        }
        if (!(packet instanceof IPlayerAuthInputPacket input)) {
            return false;
        }
        UseItemData use = input.getUseItemData();
        boolean embeddedUse = use != null;
        if (input.hasFlag(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION) != embeddedUse
                || input.getItemStackRequest() != null || input.isCraftingPart()
                || input.isEnchantingPart() || input.isRepairItemPart()
                || input.getBlockActions() != null && input.getBlockActions().length != 0) {
            return false;
        }
        if (embeddedUse) {
            // 仍由原 AuthInput handler 在有效移动提交后完整处理，不拆分内嵌动作。
            if (use.actionType != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR
                    || use.itemInHand == null || !supportsInventoryActions(input.getInventoryActions())) {
                return false;
            }
        } else if (input.getLegacyRequestId() != 0
                || input.getInventoryActions() != null && input.getInventoryActions().length != 0
                || input.getRequestChangedSlots() != null && input.getRequestChangedSlots().length != 0) {
            return false;
        }
        int shift = input.getNeteaseFlagsVersion();
        // 当前解码器只实现原版、一个和两个附加位的三种布局。
        if (shift < 0 || shift > 2) {
            return false;
        }
        long formatFlags = ((1L << shift) - 1) << PlayerAuthInputFlags.ACK_ENTITY_DATA;
        long allowed = MOVEMENT_FLAGS | (1L << PlayerAuthInputFlags.MISSED_SWING) | formatFlags | (CONTEXT_FLAGS << shift)
                | (embeddedUse ? 1L << PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION : 0)
                // 独立姿态切换沿原handler；混合世界动作仍保留尚未迁移的阶段边界。
                | (!embeddedUse && !input.hasFlag(PlayerAuthInputFlags.MISSED_SWING) ? PURE_POSE_FLAGS | (SPIN_FLAGS << shift) : 0);
        long allowed2 = (CONTEXT_FLAGS2 << shift)
                | (shift == 0 ? 0 : CONTEXT_FLAGS >>> (Long.SIZE - shift));
        BitSet flags = input.getInputFlags();
        if (flags == null) return false;
        for (int bit = flags.nextSetBit(0); bit >= 0; bit = flags.nextSetBit(bit + 1)) {
            if (bit >= Long.SIZE * 2 || ((bit < Long.SIZE ? allowed : allowed2) & (1L << bit)) == 0) {
                return false;
            }
        }
        return true;
    }

    /** 按工作职责读取当前状态；切槽和释放不能被同一玩家的持续使用状态锁住。 */
    static boolean canRunBetweenTicks(SynapsePlayer player, DataPacket packet) {
        if (packet instanceof NetworkStackLatencyPacket19 || packet instanceof ResourcePackChunkRequestPacket) {
            // 资源下载早于出生，只登记原请求；有效连接由外层校验，发送仍遵守 checkNetwork 节流。
            return true;
        }
        if (packet instanceof SubChunkRequestPacket11810 || packet instanceof ClientCacheBlobStatusPacket112) {
            // 地形下载和缓存确认早于出生；连接归属由原任务检查，不能依赖物理输入就绪。
            return true;
        }
        if (packet instanceof InventoryTransactionPacket116) {
            return player.canProcessItemActionBetweenTicks();
        }
        if (packet instanceof IPlayerAuthInputPacket input) {
            return movementBlockReason(player, input) == InputBlockReason.NONE;
        }
        return player.canProcessInputStateBetweenTicks();
    }

    /**
     * 一次消费判定交出首次失败原因；不在诊断阶段重新读取玩家状态。
     */
    static InputBlockReason movementBlockReason(SynapsePlayer player, IPlayerAuthInputPacket input) {
        InputBlockReason reason = player.movementBlockReason();
        if (reason != InputBlockReason.NONE) return reason;
        if (player.isUsingItem() && (input.getUseItemData() != null
                || input.hasFlag(PlayerAuthInputFlags.MISSED_SWING))) {
            return InputBlockReason.USING_WITH_EMBEDDED_ACTION;
        }
        if (input.getUseItemData() == null
                && (!input.hasFlag(PlayerAuthInputFlags.MISSED_SWING) || !player.isServerAuthoritativeSoundEnabled())) {
            return InputBlockReason.NONE;
        }
        return player.itemActionBlockReason();
    }

    private static boolean supportsItemAction(InventoryTransactionPacket116 packet) {
        if (packet.isCraftingPart || packet.isEnchantingPart || packet.isRepairItemPart
                || packet.actions == null) {
            return false;
        }
        // 旧版槽位同步元数据不参与原处理器的库存结算，其编号也不是窗口编号。
        // 保留完整元数据，准入只依据下方真正的库存动作；不能用同步声明替代动作校验。
        // 完整消费原包，普通容器、合成与创造动作仍保留为队头屏障。
        if (!supportsInventoryActions(packet.actions)) {
            return false;
        }
        return switch (packet.transactionType) {
            case InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY ->
                    packet.transactionData instanceof UseItemOnEntityData data
                            && data.actionType == InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK
                            && data.itemInHand != null;
            case InventoryTransactionPacket116.TYPE_USE_ITEM ->
                    packet.transactionData instanceof UseItemData data
                            && data.actionType == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR
                            && data.itemInHand != null;
            case InventoryTransactionPacket116.TYPE_RELEASE_ITEM ->
                    packet.transactionData instanceof ReleaseItemData data
                            && (data.actionType == InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE
                            || data.actionType == InventoryTransactionPacket116.RELEASE_ITEM_ACTION_CONSUME)
                            && data.itemInHand != null;
            default -> false;
        };
    }

    private static boolean supportsInventoryActions(NetworkInventoryAction[] actions) {
        if (actions == null) {
            return false;
        }
        for (NetworkInventoryAction action : actions) {
            if (action == null || action.sourceType != NetworkInventoryAction.SOURCE_CONTAINER
                    || action.windowId != ContainerIds.INVENTORY && action.windowId != ContainerIds.OFFHAND) {
                return false;
            }
        }
        return true;
    }
}
