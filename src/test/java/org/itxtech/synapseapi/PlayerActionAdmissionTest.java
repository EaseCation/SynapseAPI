package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.event.player.PlayerBedLeaveEvent;
import cn.nukkit.event.player.PlayerJumpEvent;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.ArmorInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemTrident;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentRiptide;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.MainLogger;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerActionAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void modernAuthorityKeepsOriginalRejectedActions() {
        for (int action : new int[]{0,1,2,8,9,10,11,12,15,16,18,21,22,30,31,32,33,34,35,36,37}) {
            try (Fixture f = new Fixture(true, true)) {
                f.deliver(action);assertEquals(1, f.player.violations.size());
                verify(f.processor).validateReceivedPacket(any(), any(), eq(false));
                verify(f.player, never()).setUsingItem(anyBoolean());
                verify(f.inventory, never()).getItemInHand();
                verify(f.player, never()).onUpdate(anyInt());
            }
        }
    }
    @Test void originalDisabledAuthorityRejectionsRemain() {
        try (Fixture f = new Fixture(false, true)) {f.deliver(PlayerActionPacket119.ACTION_JUMP);assertEquals(List.of("action8"), f.player.violations);verifyNoInteractions(f.processor);}
    }
    @Test void deadRespawnRequestReachesOriginalLifecycleOnce() { respawn(true); }
    @Test void disabledRespawnRequestKeepsOriginalEligibility() { respawn(false); }
    private void respawn(boolean enabled) {
        try (Fixture f = new Fixture(enabled, true)) {
            f.player.makeDead();f.deliver(PlayerActionPacket119.ACTION_RESPAWN);assertEquals(1, f.player.respawns);assertTrue(f.player.isAlive());
            f.deliver(PlayerActionPacket119.ACTION_RESPAWN);assertEquals(1, f.player.respawns);
            verify(f.player, never()).onUpdate(anyInt());
        }
    }
    @Test void cancelledOrRejectedRespawnDoesNotInvokeLifecycle() {
        for (boolean cancel : new boolean[]{true, false}) try (Fixture f = new Fixture(true, true)) {
            f.player.makeDead();f.cancel = cancel;if (!cancel) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
            f.deliver(PlayerActionPacket119.ACTION_RESPAWN);assertEquals(0, f.player.respawns);assertFalse(f.player.isAlive());
        }
    }
    @Test void uninitializedRetiredUnspawnedAndOfflineRequestsKeepGuards() {
        for (String mode : List.of("uninitialized", "retired", "unspawned", "offline")) try (Fixture f = new Fixture(true, true)) {
            f.player.makeDead();
            if (mode.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
            if (mode.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
            if (mode.equals("unspawned")) f.player.spawned = false;
            if (mode.equals("offline")) doReturn(false).when(f.player).isOnline();
            f.deliver(PlayerActionPacket119.ACTION_RESPAWN);assertEquals(0, f.player.respawns);
            if (mode.equals("retired") || mode.equals("uninitialized")) verifyNoInteractions(f.processor, f.plugins);
        }
    }
    @Test void stopSleepKeepsOriginalWakeEventAndStateWithoutWorldTick() {
        try (Fixture f = new Fixture(true, true)) {
            f.player.sleepAt(new Vector3(1, 0, 1));f.deliver(PlayerActionPacket119.ACTION_STOP_SLEEPING);
            assertFalse(f.player.isSleeping());assertFalse(f.player.getPlayerFlag(Player.DATA_PLAYER_FLAG_SLEEP));assertEquals(0, f.level.sleepTicks);
            verify(f.plugins).callEvent(any(PlayerBedLeaveEvent.class));assertTrue(f.sent.stream().anyMatch(p -> p instanceof AnimatePacket a && a.action == AnimatePacket.Action.WAKE_UP));
            f.deliver(PlayerActionPacket119.ACTION_STOP_SLEEPING);verify(f.plugins).callEvent(any(PlayerBedLeaveEvent.class));verify(f.player, never()).onUpdate(anyInt());
        }
    }
    @Test void dimensionAckAndIgnoredActionsKeepOriginalUsingTail() {
        for (int action : new int[]{3,4,5,13,14,19,20,25,26,27,1000}) try (Fixture f = new Fixture(true, true)) {
            f.deliver(action);assertTrue(f.player.violations.isEmpty());verify(f.player).setUsingItem(false);
            assertEquals(action == 14 ? 1 : 0, f.player.dimensionAcks);verifyNoInteractions(f.inventory);
        }
    }
    @Test void deniedBuildReplyKeepsItsOriginalEarlyExit() {
        try (Fixture f = new Fixture(true, true)) {f.deliver(PlayerActionPacket119.ACTION_BUILD_DENIED);assertEquals(1, f.sent.size());assertInstanceOf(LevelEventPacket.class, f.sent.getFirst());verify(f.player, never()).setUsingItem(anyBoolean());}
    }
    @Test void legacyPoseTransitionsAndCancellationKeepOriginalState() {
        for (boolean enabled : new boolean[]{true, false}) try (Fixture f = new Fixture(enabled, false)) {
            f.deliver(PlayerActionPacket119.ACTION_START_SNEAK);assertTrue(f.player.isSneaking());
            f.deliver(PlayerActionPacket119.ACTION_STOP_SNEAK);assertFalse(f.player.isSneaking());
            f.cancelToggle = true;f.deliver(PlayerActionPacket119.ACTION_START_SNEAK);assertFalse(f.player.isSneaking());
            f.deliver(PlayerActionPacket119.ACTION_JUMP);verify(f.plugins).callEvent(any(PlayerJumpEvent.class));
            verify(f.player, never()).setUsingItem(anyBoolean());
        }
    }
    @Test void legacySprintAndSwimmingDoNotAddWorldUpdates() {
        try (Fixture f = new Fixture(true, false)) {
            f.deliver(PlayerActionPacket119.ACTION_START_SPRINT);assertTrue(f.player.isSprinting());f.deliver(PlayerActionPacket119.ACTION_STOP_SPRINT);assertFalse(f.player.isSprinting());
            f.deliver(PlayerActionPacket119.ACTION_START_SWIMMING);assertTrue(f.player.isSwimming());f.deliver(PlayerActionPacket119.ACTION_STOP_SWIMMING);assertFalse(f.player.isSwimming());
            verify(f.player, never()).onUpdate(anyInt());
        }
    }
    @Test void spinStartRepeatAndStopKeepOriginalWorldCounter() {
        try (Fixture f = new Fixture(true, true)) {
            ItemTrident item = new ItemTrident();item.addEnchantment(new EnchantmentTridentRiptide());when(f.inventory.getItemInHand()).thenReturn(item);
            f.deliver(PlayerActionPacket119.ACTION_START_SPIN_ATTACK);assertTrue(f.player.getDataFlag(Entity.DATA_FLAG_SPIN_ATTACK));assertEquals(20, f.player.damageNearbyMobsTick);
            f.player.damageNearbyMobsTick = 7;f.deliver(PlayerActionPacket119.ACTION_START_SPIN_ATTACK);assertEquals(7, f.player.damageNearbyMobsTick);
            f.deliver(PlayerActionPacket119.ACTION_STOP_SPIN_ATTACK);assertFalse(f.player.getDataFlag(Entity.DATA_FLAG_SPIN_ATTACK));assertEquals(0, f.player.damageNearbyMobsTick);verify(f.player, never()).onUpdate(anyInt());
        }
    }
    @Test void legacyBreakStartDuplicateAndAbortKeepStateAndRateGuard() {
        try (Fixture f = new Fixture(true, false)) {
            doReturn(false).when(f.player).isServerAuthoritativeBlockBreakingEnabled();doReturn(true).when(f.player).isCreative();
            f.deliver(PlayerActionPacket119.ACTION_START_BREAK);assertNotNull(f.player.breaking());long first = f.player.lastBreak;
            f.deliver(PlayerActionPacket119.ACTION_START_BREAK);assertEquals(first, f.player.lastBreak);
            f.deliver(PlayerActionPacket119.ACTION_ABORT_BREAK);assertNull(f.player.breaking());verify(f.player, never()).onUpdate(anyInt());
        }
    }
    @Test void targetDecoderPreservesWholeActionPayload() {
        try (Fixture f = new Fixture(true, true)) {
            for (int protocol : new int[]{859,860,898,975}) {
                PlayerActionPacket119 p = new PlayerActionPacket119();p.action = PlayerActionPacket119.ACTION_RESPAWN;p.entityId = 1;p.x = 1;p.y = 2;p.z = 3;p.resultX = 4;p.resultY = 5;p.resultZ = 6;p.data = 1;p.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());p.encode();
                DataPacket decoded = PacketRegister.getFullPacket(p.getBuffer(), protocol, false);PlayerActionPacket119 q = assertInstanceOf(PlayerActionPacket119.class, decoded);
                assertTrue(BetweenTickPackets.supports(q));assertEquals(p.getCount(), q.getOffset());assertEquals(List.of(1,2,3,4,5,6,1), List.of(q.x,q.y,q.z,q.resultX,q.resultY,q.resultZ,q.data));
            }
        }
    }

    static class Fixture implements AutoCloseable {
        final MockedStatic<Server> global = mockStatic(Server.class);
        final Server server = mock(Server.class);
        final Level level = mock(Level.class);
        final PluginManager plugins = mock(PluginManager.class);
        final PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        final PlayerInventory inventory = mock(PlayerInventory.class);
        final Probe player;
        final boolean enabled;
        final List<DataPacket> sent = new ArrayList<>();
        boolean cancel;
        boolean cancelToggle;
        Fixture(boolean enabled, boolean authority) {
            this.enabled = enabled;when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));when(server.getDefaultLevel()).thenReturn(level);when(server.getPluginManager()).thenReturn(plugins);when(server.getLogger()).thenReturn(mock(MainLogger.class));when(server.isPrimaryThread()).thenReturn(true);when(server.getPlayerInputProcessor()).thenReturn(processor);global.when(Server::getInstance).thenReturn(server);
            try {player = spy(new Probe());} catch (RuntimeException | Error error) {global.close();throw error;}
            player.configure(inventory);doReturn(enabled).when(player).isMainThreadInputEnabled();doReturn(true).when(player).isInitialized();doReturn(true).when(player).isOnline();doReturn(true).when(player).isAcceptingInputPackets();doReturn(authority).when(player).isServerAuthoritativeMovementEnabled();doReturn(authority).when(player).isServerAuthoritativeBlockBreakingEnabled();doReturn(false).when(player).isSpectator();
            doNothing().when(player).sendData(any(Player[].class), any(EntityMetadata.class));doNothing().when(player).sendData(any(Player.class));doNothing().when(player).sendAbilities(any(), any());doNothing().when(player).setUsingItem(anyBoolean());doReturn(new AdventureSettings(player)).when(player).getAdventureSettings();
            doAnswer(call -> {sent.add(call.getArgument(0));return true;}).when(player).dataPacket(any());
            when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.ACCEPTED);
            doAnswer(call -> {if (call.getArgument(0) instanceof DataPacketReceiveEvent e && cancel) e.setCancelled();if (call.getArgument(0) instanceof PlayerToggleSneakEvent e && cancelToggle) e.setCancelled();return null;}).when(plugins).callEvent(any());
            when(inventory.getItemInHand()).thenReturn(Item.get(ItemID.STONE));doReturn(mock(ArmorInventory.class)).when(player).getArmorInventory();
            Block target = Block.get(BlockID.STONE);target.level = level;when(level.getBlock(any(BlockVector3.class))).thenReturn(target);when(level.getBlock(any(BlockVector3.class),eq(false))).thenReturn(target);when(level.getBlock(anyInt(),anyInt(),anyInt())).thenReturn(Block.get(BlockID.AIR));when(level.getBlock(any(Vector3.class))).thenReturn(target);
        }
        void deliver(int action) {
            PlayerActionPacket119 p = new PlayerActionPacket119();p.action = action;p.x = 1;p.z = 1;
            deliver(p);
        }
        void deliver(DataPacket p) {
            deliver(p, player.getMovementEpoch());
        }
        void deliver(DataPacket p, long epoch) {
            if (!enabled) {player.handleDataPacket(p);return;}
            assertTrue(BetweenTickPackets.supports(p));ServerInputDispatcher d = new ServerInputDispatcher(Thread.currentThread(),16,1024,16,1024,error -> fail(error));when(server.getInputDispatcher()).thenReturn(d);
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());List<String> order = new ArrayList<>();
            d.offer(session,new ServerInputTask(){public boolean canRunBetweenTicks(){return BetweenTickPackets.canRunBetweenTicks(player,p);}public void run(InboundContext c){assertTrue(c.betweenTicks());player.handleInputDataPacket(p,epoch);order.add("action");}},1,true);
            d.offer(session,c -> order.add("follower"),1,true);assertEquals(2,d.drain(true,1,Long.MAX_VALUE));assertEquals(List.of("action","follower"),order);assertEquals(0,d.snapshot().queuedTasks());
        }
        public void close() {global.close();}
    }
    static class Probe extends SynapsePlayer116100 {
        final List<String> violations = new ArrayList<>();int respawns;int dimensionAcks;
        Probe() {super(null,null,1L,new InetSocketAddress("127.0.0.1",19132));}
        void configure(PlayerInventory inventory) {this.inventory = inventory;this.spawned = true;this.connected = true;this.loggedIn = true;this.isSynapseLogin = true;this.protocol = 975;this.health = 20;}
        void makeDead() {this.health = 0;}
        void sleepAt(Vector3 pos) {this.sleeping = pos;this.dataProperties.putByte(DATA_PLAYER_FLAGS,1 << DATA_PLAYER_FLAG_SLEEP);}
        Block breaking() {return this.breakingBlock;}
        // 单元层检查原入口调用；完整重生体在真实客户端生命周期对照中验收。
        @Override protected void respawn() {respawns++;this.health = 20;}
        @Override protected void onDimensionChangeSuccess() {dimensionAcks++;super.onDimensionChangeSuccess();}
        @Override public void onPacketViolation(PacketViolationReason reason,String tag) {violations.add(tag);}
    }
}
