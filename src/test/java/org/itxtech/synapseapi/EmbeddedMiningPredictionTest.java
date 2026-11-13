package org.itxtech.synapseapi;

import cn.nukkit.inventory.ArmorInventory;
import cn.nukkit.inventory.CraftingGrid;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.PlayerCursorInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.ItemStackRequestAction;
import cn.nukkit.network.protocol.types.ItemStackResponse;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.request.action.MineBlockStackRequestAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthInput 夹带的挖方块请求为同帧挖掘的工具耐久登记预测。预测须保留到方块动作之后再核对（PMMP 的处理顺序）：
 * 挖掘按预测改动耐久时不产生纠正，响应带回挖掘后的物品；挖掘未发生或结果不同则照常纠正。
 */
class EmbeddedMiningPredictionTest {
    private static final int TOOL_SLOT = 0;
    private static final int DAMAGE_BEFORE = 10;

    @BeforeAll
    static void initialize() {
        LegacyMovementAdmissionTest.protocols();
    }

    @Test
    void predictionCheckedAfterTheMatchingBreakSendsNoCorrection() {
        try (Scenario scenario = new Scenario()) {
            SynapsePlayer116.PendingItemStackResponse pending = scenario.player.processItemStackRequest(scenario.request(DAMAGE_BEFORE + 1), true);
            assertNull(pending.response());
            scenario.breakBlock(DAMAGE_BEFORE + 1);
            ItemStackResponse response = scenario.player.completeItemStackRequest(pending);
            assertEquals(ItemStackResponse.RESULT_OK, response.result);
            assertEquals(DAMAGE_BEFORE + 1, response.containerInfos[0].slots[0].durabilityCorrection);
            assertEquals(0, scenario.flushedToolSyncs());
        }
    }

    @Test
    void predictionCheckedBeforeTheBreakCorrectsTheTool() {
        // 未延后（原处理顺序的效果）：预测在挖掘前被判为未兑现，挖掘的改动也没有可匹配的预测。
        try (Scenario scenario = new Scenario()) {
            SynapsePlayer116.PendingItemStackResponse pending = scenario.player.processItemStackRequest(scenario.request(DAMAGE_BEFORE + 1), false);
            assertEquals(ItemStackResponse.RESULT_OK, pending.response().result);
            scenario.breakBlock(DAMAGE_BEFORE + 1);
            assertTrue(scenario.flushedToolSyncs() > 0);
        }
    }

    @Test
    void unfulfilledPredictionIsCorrected() {
        try (Scenario scenario = new Scenario()) {
            SynapsePlayer116.PendingItemStackResponse pending = scenario.player.processItemStackRequest(scenario.request(DAMAGE_BEFORE + 1), true);
            ItemStackResponse response = scenario.player.completeItemStackRequest(pending);
            assertEquals(ItemStackResponse.RESULT_OK, response.result);
            assertEquals(DAMAGE_BEFORE, response.containerInfos[0].slots[0].durabilityCorrection);
            assertTrue(scenario.flushedToolSyncs() > 0);
        }
    }

    @Test
    void differentServerDamageIsCorrected() {
        try (Scenario scenario = new Scenario()) {
            SynapsePlayer116.PendingItemStackResponse pending = scenario.player.processItemStackRequest(scenario.request(DAMAGE_BEFORE + 1), true);
            scenario.breakBlock(DAMAGE_BEFORE + 2);
            ItemStackResponse response = scenario.player.completeItemStackRequest(pending);
            assertEquals(DAMAGE_BEFORE + 2, response.containerInfos[0].slots[0].durabilityCorrection);
            assertTrue(scenario.flushedToolSyncs() > 0);
        }
    }

    @Test
    void invalidMiningSlotIsRejectedImmediately() {
        try (Scenario scenario = new Scenario()) {
            ItemStackRequest request = scenario.request(DAMAGE_BEFORE + 1);
            ((MineBlockStackRequestAction) request.actions[0]).hotbarSlot = 20;
            SynapsePlayer116.PendingItemStackResponse pending = scenario.player.processItemStackRequest(request, true);
            assertNull(pending.predictions());
            assertEquals(ItemStackResponse.RESULT_ERROR, pending.response().result);
            // 拒绝后不残留处理中状态，同帧后续请求照常处理。
            assertNotNull(scenario.player.processItemStackRequest(scenario.request(DAMAGE_BEFORE + 1), true).predictions());
        }
    }

    private static final class Scenario implements AutoCloseable {
        final PlayerActionAdmissionTest.Fixture fixture = new PlayerActionAdmissionTest.Fixture(true, true);
        final AtomicReference<Item> tool = new AtomicReference<>(pickaxe(DAMAGE_BEFORE));
        final MiningProbe player;

        Scenario() {
            PlayerInventory inventory = fixture.inventory;
            when(inventory.getSize()).thenReturn(1);
            when(inventory.isHotbarSlot(anyInt())).thenAnswer(call -> (int) call.getArgument(0) < 9);
            when(inventory.getItem(TOOL_SLOT)).thenAnswer(call -> tool.get().clone());
            when(inventory.isHolderValid()).thenReturn(true);
            when(inventory.slotExists(anyInt())).thenAnswer(call -> (int) call.getArgument(0) == TOOL_SLOT);
            when(inventory.getContents()).thenAnswer(call -> {
                Int2ObjectMap<Item> contents = new Int2ObjectOpenHashMap<>();
                contents.put(TOOL_SLOT, tool.get());
                return contents;
            });
            player = new MiningProbe(inventory);
            player.inventoryManager.initialize();
            // 进服时的整包同步为每格分配物品编号，客户端请求引用的是这个编号。
            player.inventoryManager.syncSlot(inventory, TOOL_SLOT);
            player.sent.clear();
        }

        ItemStackRequest request(int predictedDamage) {
            MineBlockStackRequestAction mine = new MineBlockStackRequestAction();
            mine.hotbarSlot = TOOL_SLOT;
            mine.predictedDurability = predictedDamage;
            mine.stackId = player.inventoryManager.getItemStackId(player.getInventory(), TOOL_SLOT);
            ItemStackRequest request = new ItemStackRequest();
            request.requestId = -3;
            request.actions = new ItemStackRequestAction[]{mine};
            return request;
        }

        /**
         * 模拟同帧挖掘由服务端逻辑扣除耐久后的格子变更通知。
         */
        void breakBlock(int damage) {
            tool.set(pickaxe(damage));
            player.inventoryManager.onSlotChange(player.getInventory(), TOOL_SLOT);
        }

        long flushedToolSyncs() {
            player.sent.clear();
            player.inventoryManager.flushPendingUpdates();
            return player.sent.stream()
                    .filter(packet -> packet instanceof InventorySlotPacket || packet instanceof InventoryContentPacket)
                    .count();
        }

        private static Item pickaxe(int damage) {
            Item item = Item.get(ItemID.IRON_PICKAXE);
            item.setDamage(damage);
            return item;
        }

        @Override
        public void close() {
            fixture.close();
        }
    }

    private static final class MiningProbe extends SynapsePlayer116 {
        private final ArmorInventory armor = mock(ArmorInventory.class);
        private final PlayerOffhandInventory offhand = mock(PlayerOffhandInventory.class);
        private final PlayerCursorInventory cursor = mock(PlayerCursorInventory.class);
        private final CraftingGrid crafting = mock(CraftingGrid.class);
        private final List<DataPacket> sent = new ArrayList<>();

        private MiningProbe(PlayerInventory inventory) {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
            this.inventory = inventory;
            this.spawned = true;
            this.health = 20;
            bindInputSession(new ServerInputDispatcher.Session(UUID.randomUUID()));
        }

        @Override public boolean isServerAuthoritativeInventoryEnabled() { return true; }
        @Override public boolean isMainThreadInputEnabled() { return true; }
        @Override public boolean isOnline() { return true; }
        @Override public AbstractProtocol getAbstractProtocol() { return AbstractProtocol.PROTOCOL_126; }
        @Override public boolean isNetEaseClient() { return false; }
        @Override @Nullable public Inventory getCurrentWindow() { return null; }
        @Override public ArmorInventory getArmorInventory() { return armor; }
        @Override public PlayerOffhandInventory getOffhandInventory() { return offhand; }
        @Override public PlayerCursorInventory getCursorInventory() { return cursor; }
        @Override public CraftingGrid getCraftingGrid() { return crafting; }
        @Override public boolean dataPacket(DataPacket packet) { sent.add(packet); return true; }
    }
}
