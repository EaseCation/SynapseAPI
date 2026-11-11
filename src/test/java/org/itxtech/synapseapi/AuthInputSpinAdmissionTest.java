package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTrident;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentRiptide;
import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthInputSpinAdmissionTest {
    @Test void startArmsOriginalWorldCounter() { check("normal", true, true, true, 20); }
    @Test void simultaneousStartStopEndsBeforeWorldScan() { check("combined", true, true, false, 0); }
    @Test void stopClearsCounter() { check("normal", true, false, false, 0); }
    @Test void repeatedStartDoesNotExtendWindow() { check("repeat", true, true, true, 7); }
    @Test void missingItemDoesNotArm() { check("missing", true, true, false, 0); }
    @Test void missingEnchantmentDoesNotArm() { check("plain", true, true, false, 0); }
    @Test void spectatorDoesNotArm() { check("spectator", true, true, false, 0); }
    @Test void packetCancellationDoesNotArm() { check("cancel", true, true, false, 0); }
    @Test void metadataRetirementStopsCounterWrite() { check("retire", true, true, true, 0); }
    @Test void metadataPositionChangeStopsCounterWrite() { check("epoch", true, true, true, 0); }
    @Test void disabledModeKeepsOriginalContinuation() { check("epoch", false, true, true, 20); }

    private void check(String variant, boolean enabled, boolean start, boolean expectedSpin, int expectedTicks) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        Level level = mock(Level.class);
        player.configure(server, level);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isUsingItem();
        doReturn(variant.equals("spectator")).when(player).isSpectator();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doReturn(!variant.equals("cancel")).when(player).callPacketReceiveEvent(any());
        doNothing().when(player).onClientTickUpdated(anyLong());
        doReturn(false).when(player).commitReceivedMovement();
        PlayerInventory inventory = mock(PlayerInventory.class);
        doReturn(inventory).when(player).getInventory();
        Item trident = variant.equals("missing") ? mock(Item.class) : new ItemTrident();
        if (!variant.equals("plain") && !variant.equals("missing")) trident.addEnchantment(new EnchantmentTridentRiptide());
        when(inventory.getItemInHand()).thenReturn(trident);
        AtomicBoolean spin = new AtomicBoolean(!start || variant.equals("repeat"));
        // 已有窗口的剩余周期由原world更新产生；重复输入不能把它续回20。
        player.damageNearbyMobsTick = spin.get() ? 7 : 0;
        doAnswer(call -> call.<Integer>getArgument(0) == Entity.DATA_FLAG_SPIN_ATTACK && spin.get()).when(player).getDataFlag(anyInt());
        doAnswer(call -> {
            boolean value = call.getArgument(1);
            boolean changed = spin.getAndSet(value) != value;
            if (variant.equals("retire")) doReturn(false).when(player).isAcceptingInputPackets();
            if (variant.equals("epoch")) doReturn(false).when(player).isCurrentInputPosition();
            return changed;
        }).when(player).setDataFlag(eq(Entity.DATA_FLAG_SPIN_ATTACK), anyBoolean());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        int flag = start ? PlayerAuthInputFlags.START_SPIN_ATTACK : PlayerAuthInputFlags.STOP_SPIN_ATTACK;
        BitSet flags = new BitSet();
        flags.set(flag);
        if (variant.equals("combined")) {
            flags.set(PlayerAuthInputFlags.STOP_SPIN_ATTACK);
            when(packet.hasFlag(PlayerAuthInputFlags.STOP_SPIN_ATTACK)).thenReturn(true);
        }
        when(packet.getInputFlags()).thenReturn(flags);
        when(packet.hasFlag(flag)).thenReturn(true);
        if (enabled) {
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            List<String> order = new ArrayList<>();
            assertTrue(BetweenTickPackets.supports(packet));
            dispatcher.offer(session, new ServerInputTask() {
                @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                @Override public void run(InboundContext context) {
                    assertTrue(context.betweenTicks());
                    player.handleDataPacket(packet);
                    order.add("spin");
                }
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
            assertEquals(List.of("spin", "follower"), order);
        } else {
            player.handleDataPacket(packet);
        }
        assertEquals(expectedSpin, spin.get());
        assertEquals(expectedTicks, player.damageNearbyMobsTick);
        verify(player, times(1)).callPacketReceiveEvent(packet);
        verify(player, never()).onUpdate(anyInt());
        verify(level, never()).getNearbyEntities(any(AxisAlignedBB.class), same(player));
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level) { this.server = server; this.level = level; this.spawned = true; this.isSynapseLogin = true; }
    }
}
