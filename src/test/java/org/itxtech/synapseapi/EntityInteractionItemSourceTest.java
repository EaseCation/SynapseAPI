package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemDyeRed;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class EntityInteractionItemSourceTest {
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

    @Test void earlySlotCannotStartEffect() { checkTargets("early-slot", true); }
    @Test void equalReplacementCannotStartEffect() { checkTargets("early-replace", true); }
    @Test void earlyTeleportCannotStartEffect() { checkTargets("early-epoch", true); }
    @Test void earlyCloseCannotStartEffect() { checkTargets("early-close", true); }
    @Test void removedTargetCannotStartEffect() { checkTargets("target-remove", true); }
    @Test void deadTargetCannotStartEffect() { checkTargets("target-dead", true); }
    @Test void replacedTargetCannotStartEffect() { checkTargets("target-replace", true); }
    @Test void targetInAnotherWorldCannotStartEffect() { checkTargets("target-world", true); }
    @Test void teleportedTargetCannotStartEffect() { checkTargets("target-epoch", true); }
    @Test void targetOutOfRangeAfterEventCannotStartEffect() { checkTargets("target-distance", true); }
    @Test void cancelledEventResyncsOriginalInventory() { checkTargets("cancel", true); }
    @Test void lateSlotConsumesOriginalSource() { checkTargets("late-slot", true); }
    @Test void lateReplacementIsNotOverwritten() { checkTargets("late-replace", true); }
    @Test void disconnectedPlayerCannotWriteInventory() { checkTargets("late-close", true); }
    @Test void normalInteractionConsumesOnce() { checkTargets("normal", true); }
    @Test void lastItemClearsOriginalSource() { checkTargets("last", true); }
    @Test void durableToolWritesOnlyOriginalSource() { checkTargets("tool", true); }
    @Test void brokenToolClearsOnlyOriginalSource() { checkTargets("broken-tool", true); }
    @Test void declinedInteractionDoesNotConsume() { checkTargets("decline", true); }
    @Test void disabledModeKeepsLegacyCallbackWriteback() { checkTargets("early-slot", false); }

    private void checkTargets(String scenario, boolean enabled) {
        // 三个实际目标与另一个交易分支分别执行；这不代表1001真人兼容验收。
        for (AbstractProtocol protocol : new AbstractProtocol[]{AbstractProtocol.PROTOCOL_121_124,
                AbstractProtocol.PROTOCOL_121_130, AbstractProtocol.PROTOCOL_126_20,
                AbstractProtocol.PROTOCOL_126_30}) {
            check(protocol, scenario, enabled);
        }
    }

    private void check(AbstractProtocol protocol, String scenario, boolean enabled) {
        BlockPlacementInventoryResyncTest.TestPlayer player = mock(BlockPlacementInventoryResyncTest.TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(plugins);
        PlayerInventory inventory = mock(PlayerInventory.class);
        InventorySlotReference source = mock(InventorySlotReference.class);
        Level level = mock(Level.class);
        Entity target = scenario.equals("target-epoch") ? mock(Player.class) : mock(Entity.class);
        player.configure(server, level, inventory, protocol.getProtocolStart());
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isInitialized();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isInputSessionActive();
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isNetEaseClient();
        doReturn(true).when(player).isSurvival();
        doReturn(true).when(player).dataPacket(any());
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        doReturn(true).when(player).canInteract(any(Vector3.class), nullable(AxisAlignedBB.class), anyDouble());
        when(level.getEntity(42)).thenReturn(target);
        when(target.getId()).thenReturn(42L);
        when(target.getLevel()).thenReturn(level);
        when(target.isAlive()).thenReturn(true);
        Item held = scenario.contains("tool") ? Item.get(ItemID.DIAMOND_SWORD)
                : new ItemDyeRed(0, scenario.equals("last") ? 1 : 8);
        if (scenario.equals("broken-tool")) held.setDamage(held.getMaxDurability());
        when(inventory.getItemInHand()).thenAnswer(call -> held.clone());
        when(inventory.captureHeldItem()).thenReturn(source);
        when(source.isCurrent()).thenReturn(true);
        when(source.isSelectedBy(inventory)).thenReturn(true);
        when(source.setItem(any())).thenReturn(true);
        doAnswer(call -> {
            switch (scenario) {
                case "early-slot" -> when(source.isSelectedBy(inventory)).thenReturn(false);
                case "early-replace" -> when(source.isCurrent()).thenReturn(false);
                case "early-epoch" -> doReturn(1L).when(player).getMovementEpoch();
                case "early-close" -> doReturn(false).when(player).isOnline();
                case "target-remove" -> when(target.isClosed()).thenReturn(true);
                case "target-dead" -> when(target.isAlive()).thenReturn(false);
                case "target-replace" -> when(level.getEntity(42)).thenReturn(null);
                case "target-world" -> when(target.getLevel()).thenReturn(mock(Level.class));
                case "target-epoch" -> when(((Player) target).getMovementEpoch()).thenReturn(1L);
                case "target-distance" -> doReturn(false).when(player).canInteract(any(Vector3.class), nullable(AxisAlignedBB.class), anyDouble());
                case "cancel" -> ((PlayerInteractEntityEvent) call.getArgument(0)).setCancelled();
                default -> { }
            }
            return null;
        }).when(plugins).callEvent(any(PlayerInteractEntityEvent.class));
        when(target.onInteract(eq(player), any(Item.class), nullable(Vector3.class))).thenAnswer(call -> {
            if (scenario.equals("late-slot")) when(source.isSelectedBy(inventory)).thenReturn(false);
            if (scenario.equals("late-replace")) when(source.setItem(any())).thenReturn(false);
            if (scenario.equals("late-close")) doReturn(false).when(player).isOnline();
            return !scenario.equals("decline");
        });
        UseItemOnEntityData data = new UseItemOnEntityData();
        data.actionType = InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_INTERACT;
        data.entityRuntimeId = 42;
        data.itemInHand = held.clone();
        DataPacket packet;
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_126_30.getProtocolStart()) {
            InventoryTransactionPacket12630 transaction = new InventoryTransactionPacket12630();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        } else {
            InventoryTransactionPacket116 transaction = new InventoryTransactionPacket116();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        }
        player.handleDataPacket(packet);
        verify(plugins).callEvent(any(PlayerInteractEntityEvent.class));
        if (!enabled) {
            verify(inventory, never()).captureHeldItem();
            verify(target).onInteract(eq(player), any(Item.class), nullable(Vector3.class));
            verify(inventory).setItemInHand(argThat(item -> item.getCount() == 7));
            return;
        }
        verify(inventory, never()).setItemInHand(any());
        boolean rejectBeforeEffect = scenario.startsWith("early") || scenario.startsWith("target") || scenario.equals("cancel");
        if (rejectBeforeEffect) {
            verify(target, never()).onInteract(eq(player), any(Item.class), nullable(Vector3.class));
            verify(source, never()).setItem(any());
            if (!scenario.equals("early-close")) {
                verify(source).sendContents(player);
                verify(player).dataPacket(isA(PlayerHotbarPacket.class));
            }
        } else {
            verify(target).onInteract(eq(player), any(Item.class), nullable(Vector3.class));
            if (scenario.equals("decline") || scenario.equals("late-close")) {
                verify(source, never()).setItem(any());
            } else if (scenario.equals("last") || scenario.equals("broken-tool")) {
                verify(source).setItem(argThat(Item::isNull));
            } else if (scenario.equals("tool")) {
                verify(source).setItem(argThat(item -> item.getId() == ItemID.DIAMOND_SWORD && item.getDamage() == 1));
            } else {
                verify(source).setItem(argThat(item -> item.getId() == ItemID.RED_DYE && item.getCount() == 7));
            }
            if (scenario.equals("late-replace")) verify(source).sendContents(player);
        }
    }
}
