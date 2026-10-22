package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.AdventureSettings;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.PlayerInputProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.knockback.KnockbackProfile;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.ItemAttackDamageEvent;
import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class AttackItemSourceTest {
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

    @Test void selectionDuringDamageWritesOriginalWeapon() { checkBoth("late-slot", true); }
    @Test void selectionBeforeDamageRejectsAttack() { checkBoth("early-slot", true); }
    @Test void replacementDuringDamageCannotBeOverwritten() { checkBoth("late-replace", true); }
    @Test void cancelledDamageDoesNotConsumeDurability() { checkBoth("cancel", true); }
    @Test void normalAttackConsumesOriginalWeapon() { checkBoth("normal", true); }
    @Test void disabledModeKeepsLegacyWriteback() { checkBoth("late-slot", false); }
    @Test void disconnectedAttackerCannotWriteInventoryAfterDamage() { checkBoth("late-disconnect", true); }

    @Test void poseAttackAndFollowerUseOneFifoWithoutWorldUpdate() {
        for (String pose : List.of("swim", "glide", "crawl", "fly", "spin")) {
            checkBoth("pose-" + pose, true);
        }
    }

    @Test void disabledPoseAttackKeepsOriginalHandlerBehavior() {
        for (String pose : List.of("swim", "glide", "crawl", "fly", "spin")) {
            checkBoth("pose-" + pose, false);
        }
    }

    private void checkBoth(String scenario, boolean enabled) {
        check(false, scenario, enabled);
        check(true, scenario, enabled);
    }

    private void check(boolean modern, String scenario, boolean enabled) {
        BlockPlacementInventoryResyncTest.TestPlayer player = mock(BlockPlacementInventoryResyncTest.TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(plugins);
        PlayerInventory inventory = mock(PlayerInventory.class);
        InventorySlotReference source = mock(InventorySlotReference.class);
        Level level = mock(Level.class);
        Entity target = mock(Entity.class);
        int protocol = modern ? AbstractProtocol.PROTOCOL_126_30.getProtocolStart() : AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        player.configure(server, level, inventory, protocol);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isInitialized();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isInputSessionActive();
        doReturn(true).when(player).isAlive();
        doReturn(null).when(player).getEffect(anyInt());
        doReturn(false).when(player).isClosed();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isNetEaseClient();
        doReturn(true).when(player).isSurvival();
        doReturn(protocol).when(player).getProtocol();
        doReturn(true).when(player).dataPacket(any());
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        doReturn(ItemUseHand.MAIN_HAND).when(player).getUsingItemHand();
        doReturn(true).when(player).canInteract(any(Vector3.class), nullable(AxisAlignedBB.class), anyDouble());
        doReturn(new KnockbackProfile("test")).when(player).getKnockbackProfile();
        when(level.getEntity(42)).thenReturn(target);
        when(target.getId()).thenReturn(42L);
        when(target.getLevel()).thenReturn(level);
        when(target.isAlive()).thenReturn(true);
        Item sword = Item.get(ItemID.DIAMOND_SWORD);
        when(inventory.getItemInHand()).thenAnswer(call -> sword.clone());
        when(inventory.captureHeldItem()).thenReturn(source);
        when(source.isCurrent()).thenReturn(true);
        when(source.isSelectedBy(inventory)).thenReturn(true);
        when(source.setItem(any())).thenReturn(true);
        doAnswer(call -> {
            if (scenario.equals("early-slot")) when(source.isSelectedBy(inventory)).thenReturn(false);
            return null;
        }).when(plugins).callEvent(any(ItemAttackDamageEvent.class));
        when(target.attack(any(EntityDamageEvent.class))).thenAnswer(call -> {
            if (scenario.equals("late-slot")) when(source.isSelectedBy(inventory)).thenReturn(false);
            if (scenario.equals("late-replace")) when(source.setItem(any())).thenReturn(false);
            if (scenario.equals("late-disconnect")) doReturn(false).when(player).isOnline();
            return !scenario.equals("cancel");
        });
        UseItemOnEntityData data = new UseItemOnEntityData();
        data.actionType = InventoryTransactionPacket.USE_ITEM_ON_ENTITY_ACTION_ATTACK;
        data.entityRuntimeId = 42;
        data.itemInHand = sword.clone();
        DataPacket packet;
        if (modern) {
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
        if (scenario.startsWith("pose-")) {
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).supportsPacketSequences();
            doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
            when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
            AdventureSettings settings = mock(AdventureSettings.class);
            doReturn(settings).when(player).getAdventureSettings();
            switch (scenario.substring(5)) {
                case "swim" -> doReturn(true).when(player).isSwimming();
                case "glide" -> doReturn(true).when(player).isGliding();
                case "crawl" -> doReturn(true).when(player).isCrawling();
                case "fly" -> when(settings.get(AdventureSettings.Type.FLYING)).thenReturn(true);
                case "spin" -> doReturn(true).when(player).getDataFlag(Entity.DATA_FLAG_SPIN_ATTACK);
                default -> fail("Unknown pose");
            }
            if (enabled && !modern) {
                assertTrue(BetweenTickPackets.supports(packet));
                List<String> order = new ArrayList<>();
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                        16, 4096, 16, 4096, error -> fail(error));
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                try {
                    dispatcher.offer(session, new ServerInputTask() {
                        @Override public boolean canRunBetweenTicks() {
                            return BetweenTickPackets.canRunBetweenTicks(player, packet);
                        }
                        @Override public void run(InboundContext context) {
                            assertTrue(context.betweenTicks());
                            player.handleDataPacket(packet);
                            order.add("attack");
                        }
                    }, 1, true);
                    dispatcher.offer(session, new ServerInputTask() {
                        @Override public void run(InboundContext context) { order.add("follower"); }
                        @Override public boolean canRunBetweenTicks() { return true; }
                    }, 1, true);
                    assertEquals(2, dispatcher.drain(true, 40, Long.MAX_VALUE));
                    assertEquals(List.of("attack", "follower"), order);
                    assertEquals(0, dispatcher.snapshot().queuedTasks());
                    verify(player, never()).onUpdate(anyInt());
                } finally {
                    dispatcher.close();
                }
            } else {
                // 12630不是本批目标布局，也尚未静态准入；这里只保留其原handler对照。
                if (modern) assertFalse(BetweenTickPackets.supports(packet));
                player.handleDataPacket(packet);
            }
        } else {
            player.handleDataPacket(packet);
        }
        verify(plugins).callEvent(any(ItemAttackDamageEvent.class));
        if (!enabled) {
            verify(inventory, never()).captureHeldItem();
            verify(inventory).setItemInHand(argThat(item -> item.getDamage() == 1));
        } else {
            verify(inventory, never()).setItemInHand(any());
            if (scenario.equals("early-slot")) verify(target, never()).attack(any(EntityDamageEvent.class));
            else verify(target).attack(any(EntityDamageEvent.class));
            if (scenario.equals("early-slot") || scenario.equals("cancel") || scenario.equals("late-disconnect")) {
                verify(source, never()).setItem(any());
            } else {
                verify(source).setItem(argThat(item -> item.getId() == ItemID.DIAMOND_SWORD && item.getDamage() == 1));
            }
            if (scenario.equals("early-slot") || scenario.equals("late-replace")) verify(source).sendContents(player);
        }
    }
}
