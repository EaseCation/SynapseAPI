package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.Event;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.level.HeightRange;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.InventoryContentPacket;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockPlacementInventoryResyncTest {
    @BeforeAll
    static void initializeItems() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_30.getProtocolStart();
        }
    }

    @Test
    void modernUnplacedBlockWithMatchingUnchangedInventorySendsNoInventoryCorrection() {
        verifyUnchangedInventory(true, true);
    }

    @Test
    void legacyProtocolUnplacedBlockWithMatchingUnchangedInventorySendsNoInventoryCorrection() {
        verifyUnchangedInventory(false, true);
    }

    @Test
    void disabledInputModeAlsoOmitsMatchingUnchangedInventoryCorrection() {
        verifyUnchangedInventory(true, false);
    }

    @Test
    void failedClientPredictionWithDifferentClaimedCountStillReceivesAuthoritativeInventory() {
        try (InventorySyncFixture fixture = new InventorySyncFixture(true, true)) {
            fixture.request(false, 9);

            assertEquals(1, fixture.interactions.get());
            assertEquals(10, fixture.inventory.getItem(0).getCount());
            fixture.assertCorrectionsContain(10);
        }
    }

    @Test
    void serverCancellingSuccessfulClientPredictionStillRestoresAuthoritativeInventory() {
        try (InventorySyncFixture fixture = new InventorySyncFixture(true, true)) {
            fixture.onInteract = event -> event.setCancelled();
            fixture.request(true, 10);

            assertEquals(1, fixture.interactions.get());
            assertEquals(10, fixture.inventory.getItem(0).getCount());
            fixture.assertCorrectionsContain(10);
        }
    }

    @Test
    void inventoryChangedDuringInteractionStillReceivesActualInventoryCorrection() {
        try (InventorySyncFixture fixture = new InventorySyncFixture(true, true)) {
            fixture.onInteract = event -> fixture.inventory.setItem(0, wool(6), false);
            fixture.request(false, 10);

            assertEquals(1, fixture.interactions.get());
            assertEquals(6, fixture.inventory.getItem(0).getCount());
            fixture.assertCorrectionsContain(6);
        }
    }

    @Test
    void rejectedModernSlotStillCorrectsPredictedBlockAndInventory() {
        verifyFailureResync(true, true);
    }

    @Test
    void rejectedLegacySlotStillCorrectsPredictedBlockAndInventory() {
        verifyFailureResync(false, true);
    }

    private void verifyUnchangedInventory(boolean modern, boolean inputMode) {
        try (InventorySyncFixture fixture = new InventorySyncFixture(modern, inputMode)) {
            fixture.request(false, 10);

            assertEquals(1, fixture.interactions.get());
            assertEquals(ItemID.WHITE_WOOL, fixture.inventory.getItem(0).getId());
            assertEquals(10, fixture.inventory.getItem(0).getCount());
            assertTrue(fixture.inventoryPackets().isEmpty(), "Unchanged inventory must not be sent back");
        }
    }

    private void verifyFailureResync(boolean modern, boolean cancelledSlot) {
        TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Level level = mock(Level.class);
        int protocol = modern ? AbstractProtocol.PROTOCOL_126_30.getProtocolStart()
                : AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        player.configure(mock(Server.class), level, inventory, protocol);
        doReturn(true).when(player).isInitialized();
        doReturn(cancelledSlot).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isNetEaseClient();
        doReturn(protocol).when(player).getProtocol();
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        doReturn(false).when(player).setDataFlag(anyInt(), anyBoolean());
        doReturn(true).when(player).canInteract(any(Vector3.class), anyDouble());
        Item held = Item.get(ItemID.STONE, 0, 2);
        when(inventory.getItemInHand()).thenReturn(held.clone());
        if (cancelledSlot) {
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(false).when(player).isClosed();
            doReturn(true).when(player).dataPacket(any());
            when(inventory.getHotbarSize()).thenReturn(9);
            when(inventory.getItem(0)).thenReturn(held.clone());
            when(inventory.equipItem(0)).thenReturn(false);
            Block target = mock(Block.class);
            when(level.getBlock(any(Vector3.class))).thenReturn(target);
            when(target.getSide(BlockFace.UP)).thenReturn(mock(Block.class));
        }
        UseItemData data = new UseItemData();
        data.actionType = InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK;
        data.blockPos = new BlockVector3(0, 1, 0);
        data.face = BlockFace.UP;
        data.playerPos = new Vector3(0, 1, 0);
        data.clickPos = new Vector3f(0.5f, 1, 0.5f);
        data.itemInHand = held.clone();
        data.clientInteractPrediction = false;
        DataPacket packet;
        if (modern) {
            InventoryTransactionPacket12630 transaction = new InventoryTransactionPacket12630();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        } else {
            InventoryTransactionPacket116 transaction = new InventoryTransactionPacket116();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        }
        player.handleDataPacket(packet);
        verify(inventory).sendHeldItem(player);
        verify(inventory, never()).setItemInHand(any());
        assertEquals(2, held.getCount());
        if (cancelledSlot) {
            verify(inventory).sendContents(player);
            verify(level, times(1)).sendBlocks(any(Player[].class), any(Block[].class), anyInt());
            verify(level, times(1)).sendBlocks(any(Player[].class), any(Block[].class), anyInt(), eq(1));
            verify(player, never()).canInteract(any(Vector3.class), anyDouble());
        }
    }

    private static Item wool(int count) {
        return Item.get(ItemID.WHITE_WOOL, 0, count);
    }

    /**
     * 提供构造的请求、玩家状态、世界查询及外部环境；库存、方块使用和同步判定执行真实代码。
     */
    private static final class InventorySyncFixture implements AutoCloseable {
        private final TestPlayer player;
        private final PlayerInventory inventory;
        private final boolean modern;
        private final List<DataPacket> packets = new ArrayList<>();
        private final AtomicInteger interactions = new AtomicInteger();
        private Consumer<PlayerInteractEvent> onInteract = event -> {};
        private final MockedStatic<Server> servers;

        private InventorySyncFixture(boolean modern, boolean inputMode) {
            this.modern = modern;
            Server server = mock(Server.class);
            PluginManager plugins = mock(PluginManager.class);
            when(server.getPluginManager()).thenReturn(plugins);
            Level level = mock(Level.class, CALLS_REAL_METHODS);
            doReturn(server).when(level).getServer();
            doReturn(HeightRange.MINIMUM).when(level).getHeightRange();
            Block support = Block.get(BlockID.STONE);
            support.position(new BlockVector3(0, 1, 0), level);
            Block air = Block.get(BlockID.AIR);
            air.position(new BlockVector3(0, 2, 0), level);
            doReturn(support).when(level).getBlock(any(Vector3.class));
            doReturn(air).when(level).getBlock(0, 2, 0);
            doReturn(air).when(level).getExtraBlock(any(Vector3.class));
            doNothing().when(level).sendBlocks(any(Player[].class), any(Block[].class), anyInt());
            doNothing().when(level).sendBlocks(any(Player[].class), any(Block[].class), anyInt(), anyInt());

            player = mock(TestPlayer.class, CALLS_REAL_METHODS);
            inventory = new PlayerInventory(player);
            inventory.slots.put(0, wool(10));
            inventory.getViewers().add(player);
            int protocol = modern ? AbstractProtocol.PROTOCOL_126_30.getProtocolStart()
                    : AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
            player.configure(server, level, inventory, protocol);
            doReturn(true).when(player).isInitialized();
            doReturn(inputMode).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(false).when(player).isClosed();
            doReturn(false).when(player).isJavaClient();
            doReturn(false).when(player).isNetEaseClient();
            doReturn(false).when(player).isSneaking();
            doReturn(false).when(player).isOffhandItemInteraction();
            doReturn(protocol).when(player).getProtocol();
            doReturn(level).when(player).getLevel();
            doReturn(Map.of()).when(player).getViewers();
            doReturn(ContainerIds.INVENTORY).when(player).getWindowId(inventory);
            doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
            doReturn(false).when(player).setDataFlag(anyInt(), anyBoolean());
            doReturn(true).when(player).canInteract(any(Vector3.class), anyDouble());
            doNothing().when(player).setButtonText(anyString());
            doAnswer(call -> {
                packets.add(call.getArgument(0));
                return true;
            }).when(player).dataPacket(any(DataPacket.class));
            doAnswer(call -> {
                Event event = call.getArgument(0);
                if (event instanceof PlayerInteractEvent interaction) {
                    interactions.incrementAndGet();
                    onInteract.accept(interaction);
                }
                return null;
            }).when(plugins).callEvent(any(Event.class));
            servers = mockStatic(Server.class);
            servers.when(Server::getInstance).thenReturn(server);
        }

        private void request(boolean predictedSuccess, int claimedCount) {
            UseItemData data = new UseItemData();
            data.actionType = InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK;
            data.blockPos = new BlockVector3(0, 1, 0);
            data.face = BlockFace.UP;
            data.playerPos = new Vector3(0, 1, 0);
            data.clickPos = new Vector3f(0.5f, 1, 0.5f);
            data.itemInHand = wool(claimedCount);
            data.clientInteractPrediction = predictedSuccess;
            if (modern) {
                InventoryTransactionPacket12630 transaction = new InventoryTransactionPacket12630();
                transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
                transaction.transactionData = data;
                transaction.actions = new NetworkInventoryAction[0];
                player.handleDataPacket(transaction);
            } else {
                InventoryTransactionPacket116 transaction = new InventoryTransactionPacket116();
                transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
                transaction.transactionData = data;
                transaction.actions = new NetworkInventoryAction[0];
                player.handleDataPacket(transaction);
            }
        }

        private List<DataPacket> inventoryPackets() {
            return packets.stream().filter(packet ->
                    packet instanceof InventorySlotPacket slot && slot.inventoryId == ContainerIds.INVENTORY
                    || packet instanceof InventoryContentPacket content && content.inventoryId == ContainerIds.INVENTORY
            ).toList();
        }

        private void assertCorrectionsContain(int count) {
            List<DataPacket> corrections = inventoryPackets();
            assertFalse(corrections.isEmpty(), "Authoritative inventory correction is required");
            for (DataPacket packet : corrections) {
                Item item;
                if (packet instanceof InventorySlotPacket slot) {
                    assertEquals(0, slot.slot);
                    item = slot.item;
                } else {
                    item = ((InventoryContentPacket) packet).slots[0];
                }
                assertEquals(ItemID.WHITE_WOOL, item.getId());
                assertEquals(count, item.getCount());
            }
        }

        @Override
        public void close() {
            servers.close();
        }
    }

    static class TestPlayer extends SynapsePlayer116100 {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void configure(Server server, Level level, PlayerInventory inventory, int protocol) {
            this.server = server;
            this.level = level;
            this.inventory = inventory;
            this.protocol = protocol;
            isSynapseLogin = true;
            spawned = true;
        }

        @Override
        protected boolean callPacketReceiveEvent(DataPacket packet) { return true; }
    }
}
