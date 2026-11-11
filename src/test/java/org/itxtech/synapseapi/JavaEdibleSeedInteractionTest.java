package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.event.Event;
import cn.nukkit.event.entity.EntityInventoryChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.ItemUseHandState;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.HeightRange;
import cn.nukkit.level.Level;
import cn.nukkit.level.sound.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaEdibleSeedInteractionTest {
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
    void clickingStoneStartsEatingWithoutPlantingOrConsuming() throws Exception {
        for (int protocol : new int[]{975, 1001}) for (int item : new int[]{ItemID.CARROT, ItemID.POTATO}) {
            for (boolean inputMode : new boolean[]{false, true}) try (Fixture f = new Fixture(protocol, item, inputMode, false)) {
                f.request();
                assertEquals(List.of(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, PlayerInteractEvent.Action.RIGHT_CLICK_AIR), f.interactions, f.label);
                assertTrue(f.player.isUsingItem(), f.label);
                assertEquals(10, f.inventory.getItem(0).getCount(), f.label);
                assertEquals(BlockID.AIR, f.blockAt(0, 2, 0).getId(), f.label);
                assertEquals(0, f.placements, f.label);
            }
        }
    }

    @Test
    void cancelledBlockInteractionCannotFallBackToEating() throws Exception {
        for (int protocol : new int[]{975, 1001}) for (int item : new int[]{ItemID.CARROT, ItemID.POTATO}) {
            for (boolean inputMode : new boolean[]{false, true}) try (Fixture f = new Fixture(protocol, item, inputMode, false)) {
                f.onInteract = PlayerInteractEvent::setCancelled;
                f.request();
                assertEquals(List.of(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK), f.interactions, f.label);
                assertFalse(f.player.isUsingItem(), f.label);
                assertEquals(10, f.inventory.getItem(0).getCount(), f.label);
                assertEquals(0, f.inventoryChanges, f.label);
                assertEquals(0, f.placements, f.label);
            }
        }
    }

    @Test
    void plantingOnFarmlandConsumesExactlyOneSeedWithoutStartingEating() throws Exception {
        for (int protocol : new int[]{975, 1001}) for (int item : new int[]{ItemID.CARROT, ItemID.POTATO}) {
            for (boolean inputMode : new boolean[]{false, true}) try (Fixture f = new Fixture(protocol, item, inputMode, true)) {
                f.request();
                f.assertSinglePlantAndDebit(item);
            }
        }
    }

    @Test
    void invalidatedBlockInteractionCannotStartEatingOrOverwriteItsSource() throws Exception {
        for (boolean inputMode : new boolean[]{false, true}) {
            for (String change : List.of("slot", "replacement", "epoch", "offline")) {
                try (Fixture f = new Fixture(1001, ItemID.CARROT, inputMode, false)) {
                    f.onInteract = event -> {
                        switch (change) {
                            case "slot" -> f.inventory.setHeldItemIndex(1, false);
                            case "replacement" -> f.inventory.slots.put(0, Item.get(ItemID.POTATO, 0, 6));
                            case "epoch" -> f.advanceEpoch();
                            case "offline" -> doReturn(false).when(f.player).isOnline();
                        }
                    };
                    f.request();
                    assertEquals(List.of(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK), f.interactions, change);
                    assertFalse(f.player.isUsingItem(), change);
                    assertEquals(change.equals("replacement") ? ItemID.POTATO : ItemID.CARROT, f.inventory.getItem(0).getId(), change);
                    assertEquals(change.equals("replacement") ? 6 : 10, f.inventory.getItem(0).getCount(), change);
                    assertEquals(0, f.inventoryChanges, change);
                    assertEquals(0, f.placements, change);
                }
            }
        }
    }

    @Test
    void completedPlantStillChargesOriginalSlotAfterEpochAndSelectionChange() throws Exception {
        for (int protocol : new int[]{975, 1001}) for (int item : new int[]{ItemID.CARROT, ItemID.POTATO}) {
            for (boolean inputMode : new boolean[]{false, true}) try (Fixture f = new Fixture(protocol, item, inputMode, true)) {
                f.afterPlacement = () -> {
                    f.advanceEpoch();
                    f.inventory.setHeldItemIndex(1, false);
                };
                f.request();
                f.assertSinglePlantAndDebit(item);
                assertEquals(1, f.inventory.getHeldItemIndex(), f.label);
                assertEquals(ItemID.DIAMOND, f.inventory.getItem(1).getId(), f.label);
                assertEquals(3, f.inventory.getItem(1).getCount(), f.label);
            }
        }
    }

    /** World storage, event dispatch and transport are external; packet handling, item use and inventory execute real code. */
    private static final class Fixture implements AutoCloseable {
        final TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        final PlayerInventory inventory = new PlayerInventory(player);
        final Level level = mock(Level.class, CALLS_REAL_METHODS);
        final Map<String, Block> world = new HashMap<>();
        final List<PlayerInteractEvent.Action> interactions = new ArrayList<>();
        final String label;
        final int protocol;
        final int item;
        final MockedStatic<Server> servers;
        Consumer<PlayerInteractEvent> onInteract = event -> {};
        Runnable afterPlacement = () -> {};
        int placements;
        int inventoryChanges;

        Fixture(int protocol, int item, boolean inputMode, boolean farmland) throws Exception {
            this.protocol = protocol;
            this.item = item;
            label = protocol + "/" + item + "/input=" + inputMode;
            Server server = mock(Server.class);
            PluginManager plugins = mock(PluginManager.class);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getTick()).thenReturn(40);
            doReturn(server).when(level).getServer();
            doReturn(HeightRange.MINIMUM).when(level).getHeightRange();
            putBlock(0, 1, 0, Block.get(farmland ? BlockID.FARMLAND : BlockID.STONE));
            doAnswer(call -> {
                Vector3 position = call.getArgument(0);
                return blockAt(position.getFloorX(), position.getFloorY(), position.getFloorZ());
            }).when(level).getBlock(any(Vector3.class));
            doAnswer(call -> blockAt(call.getArgument(0), call.getArgument(1), call.getArgument(2)))
                    .when(level).getBlock(anyInt(), anyInt(), anyInt());
            doReturn(Block.get(BlockID.AIR)).when(level).getExtraBlock(any(Vector3.class));
            doAnswer(call -> {
                Vector3 position = call.getArgument(0);
                Block block = call.getArgument(1);
                putBlock(position.getFloorX(), position.getFloorY(), position.getFloorZ(), block.clone());
                placements++;
                afterPlacement.run();
                return true;
            }).when(level).setBlock(any(Vector3.class), any(Block.class), eq(true));
            doNothing().when(level).sendBlocks(any(Player[].class), any(Block[].class), anyInt());
            doNothing().when(level).sendBlocks(any(Player[].class), any(Block[].class), anyInt(), anyInt());
            doNothing().when(level).addSound(any(Sound.class), anyCollection());
            doNothing().when(level).addLevelSoundEvent(any(Vector3.class), anyInt(), anyInt());

            player.configure(server, level, inventory, protocol);
            setField(Entity.class, player, "dataProperties", new EntityMetadata());
            setField(Player.class, player, "itemUseHandState", new ItemUseHandState());
            doReturn(true).when(player).isInitialized();
            doReturn(inputMode).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(false).when(player).isClosed();
            doReturn(true).when(player).isJavaClient();
            doReturn(false).when(player).isNetEaseClient();
            doReturn(protocol).when(player).getProtocol();
            doReturn(level).when(player).getLevel();
            doReturn(Map.of()).when(player).getViewers();
            doReturn(ContainerIds.INVENTORY).when(player).getWindowId(inventory);
            doReturn(new AdventureSettings(player)).when(player).getAdventureSettings();
            doReturn(new PlayerFood(player, 10, 0)).when(player).getFoodData();
            doReturn(true).when(player).canInteract(any(Vector3.class), anyDouble());
            doReturn(true).when(player).dataPacket(any(DataPacket.class));
            doNothing().when(player).setButtonText(anyString());
            doNothing().when(player).sendData(any(Player[].class), any(EntityMetadata.class));
            inventory.slots.put(0, Item.get(item, 0, 10));
            inventory.slots.put(1, Item.get(ItemID.DIAMOND, 0, 3));
            inventory.getViewers().add(player);
            doAnswer(call -> {
                Event event = call.getArgument(0);
                if (event instanceof PlayerInteractEvent interaction) {
                    interactions.add(interaction.getAction());
                    onInteract.accept(interaction);
                } else if (event instanceof EntityInventoryChangeEvent) {
                    inventoryChanges++;
                }
                return null;
            }).when(plugins).callEvent(any(Event.class));
            servers = mockStatic(Server.class);
            servers.when(Server::getInstance).thenReturn(server);
        }

        void request() {
            UseItemData data = new UseItemData();
            data.actionType = InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK;
            data.blockPos = new BlockVector3(0, 1, 0);
            data.face = BlockFace.UP;
            data.playerPos = new Vector3(0, 1, 0);
            data.clickPos = new Vector3f(0.5f, 1, 0.5f);
            data.itemInHand = Item.get(item, 0, 10);
            data.clientInteractPrediction = true;
            if (protocol == 1001) {
                InventoryTransactionPacket12630 packet = new InventoryTransactionPacket12630();
                packet.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
                packet.transactionData = data;
                packet.actions = new NetworkInventoryAction[0];
                player.handleDataPacket(packet);
            } else {
                InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
                packet.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
                packet.transactionData = data;
                packet.actions = new NetworkInventoryAction[0];
                player.handleDataPacket(packet);
            }
        }

        void assertSinglePlantAndDebit(int item) {
            assertEquals(List.of(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK), interactions, label);
            assertEquals(item == ItemID.CARROT ? BlockID.CARROTS : BlockID.POTATOES, blockAt(0, 2, 0).getId(), label);
            assertEquals(1, placements, label);
            assertEquals(9, inventory.getItem(0).getCount(), label);
            assertEquals(1, inventoryChanges, label);
            assertFalse(player.isUsingItem(), label);
        }

        Block blockAt(int x, int y, int z) {
            Block block = world.get(x + ":" + y + ":" + z);
            if (block != null) return block;
            Block air = Block.get(BlockID.AIR);
            air.position(new BlockVector3(x, y, z), level);
            return air;
        }

        void putBlock(int x, int y, int z, Block block) {
            block.position(new BlockVector3(x, y, z), level);
            world.put(x + ":" + y + ":" + z, block);
        }

        void advanceEpoch() {
            try {
                setField(Player.class, player, "movementEpoch", player.getMovementEpoch() + 1);
            } catch (ReflectiveOperationException error) {
                throw new AssertionError(error);
            }
        }

        @Override
        public void close() { servers.close(); }
    }

    private static void setField(Class<?> owner, Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
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
            this.isSynapseLogin = true;
            this.spawned = true;
            this.startAction = -1;
        }

        @Override
        protected boolean callPacketReceiveEvent(DataPacket packet) { return true; }
    }
}
