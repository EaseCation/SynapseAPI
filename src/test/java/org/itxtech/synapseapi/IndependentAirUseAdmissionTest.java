package org.itxtech.synapseapi;

import cn.nukkit.PlayerFood;
import cn.nukkit.AdventureSettings;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBread;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.food.Food;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.potion.Effect;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndependentAirUseAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); Effect.init(); }

    @Test void originalStartCompletionAndCancellationContracts() {
        for (String variant : List.of("mature", "early", "start", "packet-cancel", "interact-cancel", "slot", "replace", "consume-cancel", "event-epoch", "dead", "unspawned", "offline", "retired")) check(variant, true);
    }

    @Test void postureAndPendingTransactionsDoNotBlockFollowingMovement() {
        for (String variant : List.of("gliding", "swimming", "crawling", "flying", "riding", "sleep", "teleport", "craft", "enchant", "repair")) check(variant, true);
    }

    @Test void disabledModePreservesOriginalUseAndInventoryAccounting() {
        for (String variant : List.of("mature", "early", "start", "packet-cancel", "interact-cancel", "consume-cancel", "swimming", "flying", "craft", "enchant", "repair")) check(variant, false);
    }

    private void check(String variant, boolean enabled) {
        EmbeddedUseLifecycleAdmissionTest.ProbePlayer player = mock(EmbeddedUseLifecycleAdmissionTest.ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        Level level = mock(Level.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        PlayerFood food = mock(PlayerFood.class);
        AtomicBoolean using = new AtomicBoolean(!variant.equals("start"));
        List<String> order = new ArrayList<>();
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        when(server.getTick()).thenReturn(40);
        when(server.isPrimaryThread()).thenReturn(true);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            PlayerInventory inventory = spy(new PlayerInventory(player));
            PlayerOffhandInventory offhand = spy(new PlayerOffhandInventory(player));
            player.configure(server, level, inventory, offhand, variant.equals("early") ? 10 : 1700);
            doReturn(new EntityMetadata()).when(player).getDataProperties();
            doReturn(mock(AdventureSettings.class)).when(player).getAdventureSettings();
            doReturn(975).when(player).getProtocol();
            doReturn(true).when(player).isInitialized();
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(true).when(player).isCurrentInputPosition();
            doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
            doReturn(true).when(player).supportsPacketSequences();
            doReturn(true).when(player).isSurvivalLike();
            doReturn(false).when(player).isClosed();
            doReturn(false).when(player).isCreative();
            doReturn(false).when(player).isSpectator();
            doReturn(false).when(player).isSleeping();
            doReturn(false).when(player).isOffhandItemInteraction();
            doReturn(ItemUseHand.MAIN_HAND).when(player).getUsingItemHand();
            doReturn(false).when(player).isJavaClient();
            doNothing().when(player).removeAllWindows(anyBoolean());
            doNothing().when(player).sendAllInventories();
            when(server.getLogger()).thenReturn(mock(MainLogger.class));
            doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
            doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
            doReturn(food).when(player).getFoodData();
            when(food.getLevel()).thenReturn(10);
            when(food.getMaxLevel()).thenReturn(20);
            doReturn(Map.of()).when(player).getViewers();
            doReturn(ContainerIds.INVENTORY).when(player).getWindowId(any());
            doReturn(true).when(player).dataPacket(any());
            doNothing().when(player).setButtonText(anyString());
            doNothing().when(player).onClientTickUpdated(anyLong());
            doReturn(Vector3.ZERO).when(player).getDirectionVector();
            doNothing().when(player).sendData(player);
            doNothing().when(player).sendAbilities(eq(player), any());
            doAnswer(call -> using.get()).when(player).isUsingItem();
            doAnswer(call -> { boolean value = call.getArgument(0); using.set(value); player.setUseClock(value); return null; }).when(player).setUsingItem(anyBoolean());
            doNothing().when(inventory).sendContents(player);
            doNothing().when(offhand).sendContents(player);
            inventory.setItem(0, new ItemBread(0, 8));
            inventory.setItem(1, new Item(ItemID.DIAMOND_SWORD));
            inventory.setHeldItemIndex(0, false);
            clearInvocations(player, inventory, food);
            doAnswer(call -> {
                Object event = call.getArgument(0);
                if (event instanceof DataPacketReceiveEvent received) {
                    order.add("event"); if (variant.equals("packet-cancel")) received.setCancelled();
                    if (variant.equals("event-epoch")) doReturn(false).when(player).isCurrentInputPosition();
                } else if (event instanceof PlayerInteractEvent interact) {
                    order.add("interact");
                    if (variant.equals("interact-cancel")) interact.setCancelled();
                    if (variant.equals("slot")) inventory.setHeldItemIndex(1, false);
                    if (variant.equals("replace")) inventory.setItem(0, new ItemBread(0, 8));
                } else if (event instanceof PlayerItemConsumeEvent consume) {
                    order.add("consume"); if (variant.equals("consume-cancel")) consume.setCancelled();
                }
                return null;
            }).when(plugins).callEvent(any());
            when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
                assertEquals("event", order.getLast()); order.add("validate"); return InputValidationResult.ACCEPTED;
            });
            UseItemData data = new UseItemData();
            data.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR;
            data.hotbarSlot = 0; data.itemInHand = new ItemBread(0, 8);
            InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
            packet.transactionType = InventoryTransactionPacket116.TYPE_USE_ITEM;
            packet.transactionData = data;packet.actions = new NetworkInventoryAction[0];
            if (variant.equals("sleep")) doReturn(true).when(player).isSleeping();
            if (variant.equals("teleport")) player.awaitTeleport();
            if (variant.equals("unspawned")) player.spawned = false;
            if (variant.equals("dead")) doReturn(false).when(player).isAlive();
            if (variant.equals("offline")) doReturn(false).when(player).isOnline();
            if (variant.equals("retired")) doReturn(false).when(player).isAcceptingInputPackets();
            if (variant.equals("gliding")) doReturn(true).when(player).isGliding();
            if (variant.equals("swimming")) doReturn(true).when(player).isSwimming();
            if (variant.equals("crawling")) doReturn(true).when(player).isCrawling();
            if (variant.equals("flying")) when(player.getAdventureSettings().get(AdventureSettings.Type.FLYING)).thenReturn(true);
            if (variant.equals("riding")) player.riding = mock(Entity.class);
            CraftingTransaction craft = mock(CraftingTransaction.class);
            EnchantTransaction enchant = mock(EnchantTransaction.class);
            RepairItemTransaction repair = mock(RepairItemTransaction.class);
            player.setPendingTransactions(variant.equals("craft") ? craft : null, variant.equals("enchant") ? enchant : null, variant.equals("repair") ? repair : null);
            int count = variant.equals("mature") ? 2 : 1;
            player.setInitialElapsed(variant.equals("early") ? 0 : 1700);
            if (enabled) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                assertTrue(BetweenTickPackets.supports(packet));
                for (int i = 0; i < count; i++) dispatcher.offer(session, new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) { assertTrue(context.betweenTicks()); player.handleInputDataPacket(packet, player.getMovementEpoch()); }
                }, 1, true);
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> order.add("follower"), 1, true);
                assertEquals(count + 1, dispatcher.drain(true, 40, Long.MAX_VALUE), variant);
                assertEquals("follower", order.getLast());
            } else for (int i = 0; i < count; i++) player.handleDataPacket(packet);
            boolean consumed = List.of("mature", "gliding", "swimming", "crawling", "flying", "riding", "sleep", "teleport", "craft", "enchant", "repair").contains(variant);
            assertEquals(consumed ? 7 : 8, inventory.getItem(0).getCount());
            verify(food, times(consumed ? 1 : 0)).addFoodLevel(any(Food.class));
            verify(processor, times(enabled && !variant.equals("retired") ? count : 0)).validateReceivedPacket(any(), any(), anyBoolean());
            verify(plugins, times(enabled && variant.equals("retired") ? 0 : count)).callEvent(any(DataPacketReceiveEvent.class));
            if (variant.equals("start")) { assertTrue(using.get()); verify(plugins, never()).callEvent(any(PlayerItemConsumeEvent.class)); }
            if (List.of("unspawned", "dead", "offline", "retired", "event-epoch").contains(variant)) verify(plugins, never()).callEvent(any(PlayerInteractEvent.class));
            verify(player, never()).commitReceivedMovement();
            assertTrue(player.pendingTransactionsAre(variant.equals("craft") ? craft : null, null, variant.equals("repair") ? repair : null));
            verify(craft, never()).execute();verify(craft, never()).addAction(any());
            verify(repair, never()).execute();verify(repair, never()).addAction(any());
            verify(enchant, never()).execute();verify(enchant, never()).addAction(any());
            if (variant.equals("enchant")) { verify(player).removeAllWindows(false);verify(player).sendAllInventories(); }
            verify(player, never()).onUpdate(anyInt());
        }
    }

}
