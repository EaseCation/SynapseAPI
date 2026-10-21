package org.itxtech.synapseapi;

import cn.nukkit.PlayerFood;
import cn.nukkit.AdventureSettings;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
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
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbeddedUseLifecycleAdmissionTest {
    @BeforeAll static void initialize() { Block.init(); Item.init(); Effect.init(); }
    @Test void matureUseConsumesOnceAndImmediateSecondCompletionKeepsDurationCheck() { check("mature", true); }
    @Test void earlyCompletionCannotConsume() { check("early", true); }
    @Test void wholePacketCancellationStopsCommitAndUse() { check("packet-cancel", true); }
    @Test void moveRejectionStopsUse() { check("move-reject", true); }
    @Test void interactCancellationKeepsOriginalStack() { check("interact-cancel", true); }
    @Test void interactSlotChangeCannotConsumeOldHand() { check("slot", true); }
    @Test void interactEqualReplacementCannotConsumeOldSource() { check("replace", true); }
    @Test void consumptionCancellationDoesNotConsume() { check("consume-cancel", true); }
    @Test void startFlagWithoutUsePayloadDoesNotStartOrConsume() { check("edge-only", true); }
    @Test void initialAirUseStartsWithoutCompleting() { check("start", true); }
    @Test void disabledModeKeepsOriginalCompletion() { check("mature", false); }

    private void check(String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        Level level = mock(Level.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        PlayerFood food = mock(PlayerFood.class);
        AtomicBoolean using = new AtomicBoolean(!variant.equals("start") && !variant.equals("edge-only"));
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
            doAnswer(call -> using.get()).when(player).isUsingItem();
            doAnswer(call -> { boolean value = call.getArgument(0); using.set(value); player.setUseClock(value); return null; }).when(player).setUsingItem(anyBoolean());
            doAnswer(call -> { order.add("commit"); return !variant.equals("move-reject"); }).when(player).commitReceivedMovement();
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
            PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
            when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
            BitSet flags = new BitSet(); flags.set(PlayerAuthInputFlags.START_USING_ITEM);
            if (!variant.equals("edge-only")) flags.set(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION);
            when(packet.getInputFlags()).thenReturn(flags);
            when(packet.hasFlag(anyInt())).thenCallRealMethod();
            when(packet.getInventoryActions()).thenReturn(new NetworkInventoryAction[0]);
            if (!variant.equals("edge-only")) {
                UseItemData data = new UseItemData(); data.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_CLICK_AIR;
                data.hotbarSlot = 0; data.itemInHand = new ItemBread(0, 8);
                when(packet.getUseItemData()).thenReturn(data);
            }
            int count = variant.equals("mature") ? 2 : 1;
            player.setInitialElapsed(variant.equals("early") ? 0 : 1700);
            if (enabled) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                assertTrue(BetweenTickPackets.supports(packet));
                for (int i = 0; i < count; i++) dispatcher.offer(session, new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) { assertTrue(context.betweenTicks()); player.handleDataPacket(packet); }
                }, 1, true);
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> order.add("follower"), 1, true);
                assertEquals(count + 1, dispatcher.drain(true, 40, Long.MAX_VALUE));
                assertEquals("follower", order.getLast());
            } else for (int i = 0; i < count; i++) player.handleDataPacket(packet);
            boolean consumed = variant.equals("mature");
            assertEquals(consumed ? 7 : 8, inventory.getItem(0).getCount());
            verify(food, times(consumed ? 1 : 0)).addFoodLevel(any(Food.class));
            verify(processor, times(enabled ? count : 0)).validateReceivedPacket(any(), any(), anyBoolean());
            verify(plugins, times(count)).callEvent(any(DataPacketReceiveEvent.class));
            if (variant.equals("edge-only")) { assertFalse(using.get()); verify(player, never()).setUsingItem(anyBoolean()); }
            if (variant.equals("start")) { assertTrue(using.get()); verify(plugins, never()).callEvent(any(PlayerItemConsumeEvent.class)); }
            if (order.contains("interact") && enabled) assertTrue(order.indexOf("commit") < order.indexOf("interact"));
            if (variant.equals("packet-cancel")) verify(player, never()).commitReceivedMovement();
            if (variant.equals("move-reject")) verify(plugins, never()).callEvent(any(PlayerInteractEvent.class));
            verify(player, never()).onUpdate(anyInt());
        }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, PlayerInventory inventory, PlayerOffhandInventory offhand, int elapsed) {
            this.server = server; this.level = level; this.inventory = inventory; this.offhandInventory = offhand;
            this.isSynapseLogin = true; this.spawned = true; this.startAction = 1;
            this.startActionTimestamp = System.currentTimeMillis() - elapsed;
        }
        void setInitialElapsed(int elapsed) { this.startActionTimestamp = System.currentTimeMillis() - elapsed; }
        void setUseClock(boolean value) { this.startAction = value ? 40 : -1; this.startActionTimestamp = value ? System.currentTimeMillis() : -1; }
    }
}
