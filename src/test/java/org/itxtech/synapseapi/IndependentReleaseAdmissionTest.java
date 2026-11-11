package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBow;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IndependentReleaseAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void releaseAndFollowerDoNotWaitForPoseStage() {
        for (String state : List.of("normal", "swim", "crawl", "glide", "fly", "sleep", "riding", "spin")) {
            check(state, true);
            check(state, false);
        }
    }

    @Test void originalCancellationLifecycleAndUnexpectedConsumeRemainIntact() {
        for (String state : List.of("cancel", "not-using", "spectator", "consume", "retired", "uninitialized", "rejected", "release-false")) {
            check(state, true);
            if (!List.of("retired", "rejected").contains(state)) check(state, false);
        }
    }

    @Test void pendingTransactionsKeepTheirOriginalDisposition() {
        for (String state : List.of("craft", "enchant", "repair")) {
            check(state, true);
            check(state, false);
        }
    }

    private void check(String state, boolean enabled) {
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        EmbeddedUseLifecycleAdmissionTest.ProbePlayer player = mock(EmbeddedUseLifecycleAdmissionTest.ProbePlayer.class, CALLS_REAL_METHODS);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getPlayerInputProcessor()).thenReturn(processor);
            when(server.getLogger()).thenReturn(mock(MainLogger.class));
            when(server.isPrimaryThread()).thenReturn(true);
            player.configure(server, mock(Level.class), inventory, mock(PlayerOffhandInventory.class), 1500);
            doReturn(975).when(player).getProtocol();
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(!state.equals("uninitialized")).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            doReturn(!state.equals("retired")).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isInputSessionActive();
            doReturn(true).when(player).isCurrentInputPosition();
            doReturn(true).when(player).supportsPacketSequences();
            doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
            doReturn(state.equals("spectator")).when(player).isSpectator();
            doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
            doReturn(ItemUseHand.MAIN_HAND).when(player).getUsingItemHand();
            doReturn(false).when(player).isJavaClient();
            doReturn(false).when(player).isUsingOffhandItem();
            doNothing().when(player).sendAllInventories();
            doNothing().when(player).removeAllWindows(anyBoolean());
            AdventureSettings settings = mock(AdventureSettings.class);
            doReturn(settings).when(player).getAdventureSettings();
            doReturn(state.equals("swim")).when(player).isSwimming();
            doReturn(state.equals("crawl")).when(player).isCrawling();
            doReturn(state.equals("glide")).when(player).isGliding();
            doReturn(state.equals("sleep")).when(player).isSleeping();
            when(settings.get(AdventureSettings.Type.FLYING)).thenReturn(state.equals("fly"));
            doReturn(state.equals("spin")).when(player).getDataFlag(Entity.DATA_FLAG_SPIN_ATTACK);
            if (state.equals("riding")) player.riding = mock(Entity.class);
            AtomicBoolean using = new AtomicBoolean(!state.equals("not-using"));
            doAnswer(call -> using.get()).when(player).isUsingItem();
            doAnswer(call -> { using.set(call.getArgument(0)); return null; }).when(player).setUsingItem(anyBoolean());
            CraftingTransaction craft = state.equals("craft") ? mock(CraftingTransaction.class) : null;
            EnchantTransaction enchant = state.equals("enchant") ? mock(EnchantTransaction.class) : null;
            RepairItemTransaction repair = state.equals("repair") ? mock(RepairItemTransaction.class) : null;
            player.setPendingTransactions(craft, enchant, repair);
            List<String> order = new ArrayList<>();
            doAnswer(call -> {
                if (call.getArgument(0) instanceof DataPacketReceiveEvent event) {
                    order.add("event"); if (state.equals("cancel")) event.setCancelled();
                }
                return null;
            }).when(plugins).callEvent(any());
            when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
                assertEquals("event", order.getLast()); order.add("validate");
                return state.equals("rejected") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED;
            });
            Item item = spy(new ItemBow());
            when(inventory.getItemInHand()).thenReturn(item);
            long clock = System.currentTimeMillis() - 1500;
            player.setInitialElapsed(1500);
            doAnswer(call -> {
                int elapsed = call.getArgument(1);
                assertTrue(elapsed >= 29 && elapsed <= (System.currentTimeMillis() - clock) / 50 + 1);
                order.add("release"); return !state.equals("release-false");
            }).when(item).onRelease(eq(player), anyInt(), any(Vector3.class));
            ReleaseItemData data = new ReleaseItemData();
            data.actionType = state.equals("consume") ? InventoryTransactionPacket116.RELEASE_ITEM_ACTION_CONSUME : InventoryTransactionPacket116.RELEASE_ITEM_ACTION_RELEASE;
            data.itemInHand = item.clone();data.headRot = new Vector3();
            InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
            packet.transactionType = InventoryTransactionPacket116.TYPE_RELEASE_ITEM;
            packet.transactionData = data;packet.actions = new NetworkInventoryAction[0];
            assertTrue(BetweenTickPackets.supports(packet));
            if (enabled) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096, error -> fail(error));
                when(server.getInputDispatcher()).thenReturn(dispatcher);
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                dispatcher.offer(session, new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) { assertTrue(context.betweenTicks());player.handleInputDataPacket(packet, player.getMovementEpoch()); }
                }, 1, true);
                dispatcher.offer(session, context -> order.add("follower"), 1, true);
                assertEquals(2, dispatcher.drain(true, 40, Long.MAX_VALUE), state);
                assertEquals("follower", order.getLast());
                assertEquals(0, dispatcher.snapshot().queuedTasks());dispatcher.close();
            } else player.handleDataPacket(packet);
            boolean releases = !List.of("cancel", "not-using", "spectator", "consume", "retired", "uninitialized", "rejected").contains(state);
            verify(item, times(releases ? 1 : 0)).onRelease(eq(player), anyInt(), any(Vector3.class));
            if (releases || state.equals("consume") || state.equals("not-using")) assertFalse(using.get());
            if (state.equals("release-false") || state.equals("not-using")) verify(inventory).sendContents(player);
            assertTrue(player.pendingTransactionsAre(craft, null, repair));
            if (craft != null) { verify(craft, never()).execute();verify(craft, never()).addAction(any()); }
            if (enchant != null) { verify(enchant, never()).execute();verify(player).removeAllWindows(false);verify(player).sendAllInventories(); }
            if (repair != null) { verify(repair, never()).execute();verify(repair, never()).addAction(any()); }
            int entries = state.equals("uninitialized") || enabled && state.equals("retired") ? 0 : 1;
            int packetEvents = !enabled && entries != 0 && !state.equals("cancel") ? 2 : entries;
            verify(plugins, times(packetEvents)).callEvent(any(DataPacketReceiveEvent.class));
            verify(processor, times(enabled ? entries : 0)).validateReceivedPacket(eq(player), eq(packet), anyBoolean());
            verify(player, never()).onUpdate(anyInt());
        }
    }
}
