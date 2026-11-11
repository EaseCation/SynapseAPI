package org.itxtech.synapseapi;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.RiderJumpPacket;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.EntityEventPacket;
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
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.RespawnPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityAbsolutePacket15;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.InteractPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.RequestAbilityPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.SetEntityMotionPacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.ClientCameraAimAssistPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.ClientMovementPredictionSyncPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.ClientMovementPredictionSyncPacket12170;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.ClientMovementPredictionSyncPacket12620;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerHotbarPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.EntityEventPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.EntityEventPacket12620;
import org.itxtech.synapseapi.multiprotocol.protocol121120.protocol.AnimatePacket121120;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.AnimatePacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.ClientCacheBlobStatusPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.SubChunkRequestPacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol11810.protocol.ScriptMessagePacket11810;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.TextPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.TextPacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.TextPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol126.protocol.TextPacket126;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.ContainerClosePacket121;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ContainerClosePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;

import java.util.BitSet;

/** 仅分类已审计的协议工作，不从客户端状态推导服务端物理或权限。 */
final class BetweenTickPackets {
    // 缓存确认暂保留原有数量门禁；其确认/回复语义另行迁移。
    static final int MAX_CACHE_ENTRIES_BETWEEN_TICKS = 64;
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
    private static final long VEHICLE_FLAGS = (1L << PlayerAuthInputFlags.IN_CLIENT_PREDICTED_IN_VEHICLE)
            | (1L << PlayerAuthInputFlags.PADDLE_LEFT)
            | (1L << PlayerAuthInputFlags.PADDLE_RIGHT);
    private static final long CONTEXT_FLAGS = (1L << PlayerAuthInputFlags.ACK_ENTITY_DATA)
            | (1L << PlayerAuthInputFlags.START_USING_ITEM)
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

    // 诊断子类型标签：类初始化时一次生成，调用时只查表，不拼接字符串。
    private static final String[] INTERACT_113 = labels("InteractPacket113",
            "NONE", "INTERACT", "DAMAGE", "VEHICLE_EXIT", "MOUSEOVER", "OPEN_NPC", "OPEN_INVENTORY");
    private static final String[] INTERACT_121130 = labels("InteractPacket121130",
            "NONE", "INTERACT", "DAMAGE", "VEHICLE_EXIT", "MOUSEOVER", "OPEN_NPC", "OPEN_INVENTORY");
    private static final String[] RESOURCE_PACK_RESPONSE = labels("ResourcePackClientResponsePacket16",
            "NONE", "REFUSED", "SEND_PACKS", "HAVE_ALL_PACKS", "COMPLETED");
    private static final String[] CONTAINER_CLOSE_116100 = labels("ContainerClosePacket116100",
            "INVENTORY", "WINDOW", "FIXED");
    private static final String[] CONTAINER_CLOSE_121 = labels("ContainerClosePacket121",
            "INVENTORY", "WINDOW", "FIXED");
    private static final String[] MOB_EQUIPMENT = labels("MobEquipmentPacket", "INVENTORY", "OFFHAND");
    private static final String[] PLAYER_HOTBAR = labels("PlayerHotbarPacket", "INVENTORY", "OFFHAND");
    private static final String[] PLAYER_HOTBAR_14 = labels("PlayerHotbarPacket14", "INVENTORY", "OFFHAND");
    private static final String[] TRANSACTION_PART = labels("InventoryTransactionPacket116",
            "CRAFT_PART", "ENCHANT_PART", "REPAIR_PART");
    private static final String[] TRANSACTION_NORMAL = labels("InventoryTransactionPacket116",
            "NORMAL_CONTAINER", "NORMAL_CREATIVE", "NORMAL_DROP", "NORMAL_SPECIAL");
    private static final String[] TRANSACTION_MISMATCH = labels("InventoryTransactionPacket116",
            "MISMATCH", "MISMATCH_ACTIONS");
    private static final String[] TRANSACTION_USE = labels("InventoryTransactionPacket116",
            "USE_CLICK_BLOCK", "USE_CLICK_AIR", "USE_BREAK_BLOCK", "USE_AS_ATTACK");
    private static final String[] TRANSACTION_ENTITY = labels("InventoryTransactionPacket116",
            "ENTITY_INTERACT", "ENTITY_ATTACK", "ENTITY_ITEM_INTERACT");
    private static final String[] TRANSACTION_RELEASE = labels("InventoryTransactionPacket116",
            "RELEASE", "CONSUME");
    private static final int NORMAL_CONTAINER = 0;
    private static final int NORMAL_CREATIVE = 1;
    private static final int NORMAL_DROP = 2;
    private static final int NORMAL_SPECIAL = 3;
    private static final int WINDOW_INVENTORY = 0;
    private static final int WINDOW_OPENED = 1;
    private static final int WINDOW_FIXED = 2;
    private static final int WINDOW_OFFHAND = 1;

    private BetweenTickPackets() {
    }

    /**
     * 屏障诊断的类型标签；只对放行与否依赖包内字段的包细分子类型，未知取值归 OTHER。
     * 只读包字段，不访问玩家；调用位于 dispatcher 锁内。
     */
    static String diagnosticType(DataPacket packet) {
        if (packet instanceof InventoryTransactionPacket116 transaction) {
            return transactionType(transaction);
        }
        if (packet instanceof InteractPacket121130 interact) {
            return pick(INTERACT_121130, interact.action);
        }
        if (packet instanceof InteractPacket113 interact) {
            return pick(INTERACT_113, interact.action);
        }
        if (packet instanceof ContainerClosePacket121 close) {
            return pick(CONTAINER_CLOSE_121, closeWindow(close.windowId));
        }
        if (packet instanceof ContainerClosePacket116100 close) {
            return pick(CONTAINER_CLOSE_116100, closeWindow(close.windowId));
        }
        if (packet instanceof MobEquipmentPacket equipment) {
            return pick(MOB_EQUIPMENT, equipmentWindow(equipment.windowId));
        }
        if (packet instanceof PlayerHotbarPacket hotbar) {
            return pick(PLAYER_HOTBAR, equipmentWindow(hotbar.windowId));
        }
        if (packet instanceof PlayerHotbarPacket14 hotbar) {
            return pick(PLAYER_HOTBAR_14, equipmentWindow(hotbar.windowId));
        }
        if (packet instanceof ResourcePackClientResponsePacket16 response) {
            return pick(RESOURCE_PACK_RESPONSE, response.responseStatus);
        }
        return packet.getClass().getSimpleName();
    }

    private static String transactionType(InventoryTransactionPacket116 packet) {
        if (packet.isCraftingPart) return TRANSACTION_PART[0];
        if (packet.isEnchantingPart) return TRANSACTION_PART[1];
        if (packet.isRepairItemPart) return TRANSACTION_PART[2];
        return switch (packet.transactionType) {
            case InventoryTransactionPacket116.TYPE_NORMAL -> pick(TRANSACTION_NORMAL, normalSource(packet.actions));
            case InventoryTransactionPacket116.TYPE_MISMATCH ->
                    pick(TRANSACTION_MISMATCH, packet.actions == null || packet.actions.length == 0 ? 0 : 1);
            case InventoryTransactionPacket116.TYPE_USE_ITEM -> pick(TRANSACTION_USE,
                    packet.transactionData instanceof UseItemData data ? data.actionType : -1);
            case InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY -> pick(TRANSACTION_ENTITY,
                    packet.transactionData instanceof UseItemOnEntityData data ? data.actionType : -1);
            case InventoryTransactionPacket116.TYPE_RELEASE_ITEM -> pick(TRANSACTION_RELEASE,
                    packet.transactionData instanceof ReleaseItemData data ? data.actionType : -1);
            default -> TRANSACTION_NORMAL[TRANSACTION_NORMAL.length - 1];
        };
    }

    /**
     * 丢弃优先；合成格、关窗溢出等虚拟来源归 SPECIAL；只有容器与创造栏时归 CREATIVE。
     */
    private static int normalSource(NetworkInventoryAction[] actions) {
        if (actions == null) return -1;
        boolean creative = false;
        boolean special = false;
        for (NetworkInventoryAction action : actions) {
            if (action == null) return -1;
            switch (action.sourceType) {
                case NetworkInventoryAction.SOURCE_WORLD -> {
                    return NORMAL_DROP;
                }
                case NetworkInventoryAction.SOURCE_CREATIVE -> creative = true;
                case NetworkInventoryAction.SOURCE_CONTAINER -> {
                }
                default -> special = true;
            }
        }
        return special ? NORMAL_SPECIAL : creative ? NORMAL_CREATIVE : NORMAL_CONTAINER;
    }

    private static int closeWindow(int windowId) {
        if (windowId == ContainerIds.INVENTORY) return WINDOW_INVENTORY;
        if (windowId >= ContainerIds.FIRST && windowId <= ContainerIds.LAST) return WINDOW_OPENED;
        if (windowId >= ContainerIds.OFFHAND && windowId <= ContainerIds.NETEASE_UI) return WINDOW_FIXED;
        return -1;
    }

    private static int equipmentWindow(int windowId) {
        if (windowId == ContainerIds.INVENTORY) return WINDOW_INVENTORY;
        if (windowId == ContainerIds.OFFHAND) return WINDOW_OFFHAND;
        return -1;
    }

    private static String[] labels(String type, String... names) {
        String[] labels = new String[names.length + 1];
        for (int i = 0; i < names.length; i++) {
            labels[i] = type + '/' + names[i];
        }
        labels[names.length] = type + "/OTHER";
        return labels;
    }

    /**
     * 越界取值落到最后一项 OTHER。
     */
    private static String pick(String[] labels, int index) {
        return index >= 0 && index < labels.length - 1 ? labels[index] : labels[labels.length - 1];
    }

    private static boolean isIndependentMovementInput(DataPacket packet) {
        return packet instanceof MovePlayerPacket116100NE || packet instanceof MoveEntityAbsolutePacket15
                || packet instanceof PlayerInputPacket || packet instanceof RequestAbilityPacket119
                || packet instanceof RiderJumpPacket || packet instanceof AdventureSettingsPacket
                || packet instanceof PlayerActionPacket119 || packet instanceof RespawnPacket113
                || packet instanceof ClientCameraAimAssistPacket12160
                || packet instanceof ClientMovementPredictionSyncPacket12160
                || packet instanceof ClientMovementPredictionSyncPacket12170
                || packet instanceof ClientMovementPredictionSyncPacket12620 || packet instanceof SetEntityMotionPacket12070;
    }

    /**
     * 仅限经 SynapsePlayer116100 原 handler 投递滤词队列的文本；聊天事件仍由世界周期的滤词出口执行。
     * 原版 TextPacket 由 Nukkit 同步执行聊天，保持队头屏障。
     */
    private static boolean isFilteredText(DataPacket packet) {
        return packet instanceof TextPacket126 || packet instanceof TextPacket121130 || packet instanceof TextPacket121
                || packet instanceof TextPacket116100 || packet instanceof TextPacket116100NE;
    }

    static boolean supports(DataPacket packet) {
        // 独立移动与状态请求仍沿原权限/事件/忽略或兼容handler；位置和实体周期不额外推进。
        if (isIndependentMovementInput(packet)) return true;
        if (packet instanceof EntityEventPacket116100 event) {
            return event.event == EntityEventPacket.EATING_ITEM;
        }
        if (packet instanceof EntityEventPacket12620 event) {
            return event.event == EntityEventPacket.EATING_ITEM;
        }
        if (packet instanceof NetworkStackLatencyPacket19 || packet instanceof ResourcePackChunkRequestPacket
                || packet instanceof ScriptMessagePacket11810 || isFilteredText(packet)) {
            return true;
        }
        if (packet instanceof SubChunkRequestPacket11810 request) {
            // 只登记原子区块队列；构建和正常发送仍由 checkNetwork 的原节流驱动。
            return request.positionOffsets != null;
        }
        if (packet instanceof ClientCacheBlobStatusPacket112 status) {
            // 确认记账保持输入原序；回复沿原出站队列异步编码，不重放或延后确认本身。
            return status.hitSet != null && status.missSet != null
                    && status.hitSet.size() + status.missSet.size() <= MAX_CACHE_ENTRIES_BETWEEN_TICKS;
        }
        if (packet instanceof InteractPacket113 interact) {
            // 看向实体沿原事件与提示更新处理；开库存、下车等动作仍保持世界阶段屏障。
            return interact.action == InteractPacket113.ACTION_MOUSEOVER;
        }
        if (packet instanceof InteractPacket121130 interact) {
            return interact.action == InteractPacket121130.ACTION_MOUSEOVER;
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
        if (packet instanceof PlayerAuthInputPacket116220) {
            // 目标现代协议完整移动沿原解码、事件、验证和handler执行，不再重复按载荷设阶段门禁。
            return true;
        }
        if (!(packet instanceof IPlayerAuthInputPacket input)) {
            return false;
        }
        UseItemData use = input.getUseItemData();
        boolean embeddedUse = use != null;
        if (input.hasFlag(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION) != embeddedUse
                || input.getItemStackRequest() != null || input.isCraftingPart()
                || input.isEnchantingPart() || input.isRepairItemPart()) {
            return false;
        }
        if (embeddedUse) {
            // 仍由原 AuthInput handler 在有效移动提交后完整处理，不拆分内嵌动作。
            if (use.actionType != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR
                    && use.actionType != InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK
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
        long allowed = MOVEMENT_FLAGS | (1L << PlayerAuthInputFlags.MISSED_SWING)
                | (1L << PlayerAuthInputFlags.PERFORM_BLOCK_ACTIONS) | formatFlags | (CONTEXT_FLAGS << shift)
                | (embeddedUse ? 1L << PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION : 0)
                // 同包姿态与已支持的物品使用保持原handler顺序，不拆分执行。
                | PURE_POSE_FLAGS | ((SPIN_FLAGS | VEHICLE_FLAGS) << shift);
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
        return blockReason(player, packet) == InputBlockReason.NONE;
    }

    /**
     * 原准入与诊断共用一次判定，返回首次失败原因，不额外读取玩家状态。
     */
    static InputBlockReason blockReason(SynapsePlayer player, DataPacket packet) {
        // 保留原Entry对高频移动的直接判断，不扫描其他工作类型。
        if (packet instanceof IPlayerAuthInputPacket input) {
            return movementBlockReason(player, input);
        }
        if (isIndependentMovementInput(packet) || packet instanceof NEPyRpcPacket16) return player.movementBlockReason();
        if (packet instanceof MobEquipmentPacket || packet instanceof PlayerHotbarPacket) return player.movementBlockReason();
        // 悬停沿原生命周期拒绝与目标提示处理，不能借此放行开库存或下车。
        if (packet instanceof InteractPacket113 interact && interact.action == InteractPacket113.ACTION_MOUSEOVER
                || packet instanceof InteractPacket121130 modern && modern.action == InteractPacket121130.ACTION_MOUSEOVER) {
            return player.movementBlockReason();
        }
        if (packet instanceof NetworkStackLatencyPacket19 || packet instanceof ResourcePackChunkRequestPacket) {
            // 资源下载早于出生，只登记原请求；有效连接由外层校验，发送仍遵守 checkNetwork 节流。
            return InputBlockReason.NONE;
        }
        if (packet instanceof ScriptMessagePacket11810) {
            // JE 频道与相机请求沿原主线程事件/handler；连接归属由外层检查，不依赖物理输入就绪。
            return InputBlockReason.NONE;
        }
        if (isFilteredText(packet)) {
            // 原handler只触发收包事件并投递滤词；出生、存活与接入资格由原handler及聊天出口复检。
            return InputBlockReason.NONE;
        }
        if (packet instanceof SubChunkRequestPacket11810 || packet instanceof ClientCacheBlobStatusPacket112) {
            // 地形下载和缓存确认早于出生；连接归属由原任务检查，不能依赖物理输入就绪。
            return InputBlockReason.NONE;
        }
        if (packet instanceof InventoryTransactionPacket116 transaction) {
            // 独立攻击沿原次数/距离/来源与取消校验，不以物品姿态门禁阻塞后继移动。
            if (transaction.transactionType == InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY
                    && transaction.transactionData instanceof UseItemOnEntityData attack
                    && attack.actionType == InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK) {
                return player.movementBlockReason();
            }
            // 释放与对空使用沿原来源、时长及待事务处理，不以姿态门禁延后整条FIFO。
            return transaction.transactionType == InventoryTransactionPacket116.TYPE_MISMATCH
                    || transaction.transactionType == InventoryTransactionPacket116.TYPE_RELEASE_ITEM
                    || transaction.transactionType == InventoryTransactionPacket116.TYPE_USE_ITEM
                    && transaction.transactionData instanceof UseItemData use
                    && use.actionType == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR
                    ? player.movementBlockReason() : player.itemActionBlockReason();
        }
        return player.inputStateBlockReason();
    }

    /**
     * 一次消费判定交出首次失败原因；不在诊断阶段重新读取玩家状态。
     */
    static InputBlockReason movementBlockReason(SynapsePlayer player, IPlayerAuthInputPacket input) {
        return player.movementBlockReason();
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
            // 空同步仍完整经过原事件和待事务处理，不单独摘出库存回复。
            case InventoryTransactionPacket116.TYPE_MISMATCH -> packet.actions.length == 0;
            case InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY ->
                    packet.transactionData instanceof UseItemOnEntityData data
                            && data.actionType == InventoryTransactionPacket116.USE_ITEM_ON_ENTITY_ACTION_ATTACK
                            && data.itemInHand != null;
            case InventoryTransactionPacket116.TYPE_USE_ITEM ->
                    packet.transactionData instanceof UseItemData data
                            && (data.actionType == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR
                            || data.actionType == InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_BLOCK)
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
