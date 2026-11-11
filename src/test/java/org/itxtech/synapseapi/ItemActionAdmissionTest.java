package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.ItemBow;
import cn.nukkit.item.ItemBread;
import cn.nukkit.item.ItemCrossbow;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemActionAdmissionTest {
    @Test
    void sustainedUseDoesNotBlockAlreadyAdmittedAirUse() {
        TestPlayer player = player();
        PlayerInventory inventory = mock(PlayerInventory.class);
        player.useInventory(inventory);
        doReturn(true).when(player).isUsingItem();
        when(inventory.getItemInHand()).thenReturn(new ItemBow());
        doReturn(true).when(player).isJavaClient();
        doReturn(false).when(player).isJavaClient();
        doReturn(true).when(player).isUsingOffhandItem();
        doReturn(false).when(player).isUsingOffhandItem();
        when(inventory.getItemInHand()).thenReturn(new ItemBread());
        when(inventory.getItemInHand()).thenReturn(new ItemCrossbow());
        when(inventory.getItemInHand()).thenReturn(new ItemBow() { });
        when(inventory.getItemInHand()).thenReturn(new ItemBow());

        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                16, 1024, 16, 1024, exception -> fail(exception));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        PlayerAuthInputPacket116220 movement = new PlayerAuthInputPacket116220();
        movement.inputFlags = new BitSet();
        dispatcher.offer(session, task(player, movement, () -> order.add("bow-movement")), 1, true);
        dispatcher.offer(session, task(player, new InventoryTransactionPacket116(), () -> order.add("release")), 1, true);
        assertEquals(2, dispatcher.drain(true, 40, Long.MAX_VALUE));
        assertEquals(List.of("bow-movement", "release"), order);
        verify(player, never()).onUpdate(anyInt());
        verify(player, never()).setUsingItem(anyBoolean());
    }

    @Test
    void ordinaryMovementAndReleaseConsumeOneFifoWithoutAdvancingTheWorldTick() {
        TestPlayer player = player();
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                16, 1024, 16, 1024, exception -> fail(exception));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        PlayerAuthInputPacket116220 movement = new PlayerAuthInputPacket116220();
        movement.inputFlags = new BitSet();
        InventoryTransactionPacket116 release = new InventoryTransactionPacket116();
        dispatcher.offer(session, task(player, movement, () -> {
            assertTrue(dispatcher.getCurrentContext().betweenTicks());
            assertEquals(40, dispatcher.getCurrentContext().serverTick());
            order.add("movement");
        }), 1, true);
        dispatcher.offer(session, task(player, release, () -> {
            assertTrue(dispatcher.getCurrentContext().betweenTicks());
            assertEquals(40, dispatcher.getCurrentContext().serverTick());
            order.add("release");
        }), 1, true);

        dispatcher.drain(true, 40, Long.MAX_VALUE);

        assertEquals(List.of("movement", "release"), order);
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        // 输入唤醒不调用世界周期的持续使用回调，也不重启使用计时。
        verify(player, never()).onUpdate(anyInt());
        verify(player, never()).setUsingItem(anyBoolean());
    }

    /**
     * 删除动态门禁后，睡眠、待确认传送、飞行、骑乘、待完成合成等状态不再把已静态放行的物品动作推迟到世界周期；
     * 这些状态在下一世界 tick 开始前不会改变，handler 在补充阶段看到的状态与原世界阶段相同。
     */
    @Test
    void formerDynamicGateStatesNoLongerHoldAdmittedWork() {
        List<java.util.function.Consumer<TestPlayer>> states = List.of(
                p -> doReturn(true).when(p).isSleeping(),
                p -> doReturn(true).when(p).isSwimming(),
                p -> doReturn(true).when(p).isGliding(),
                p -> doReturn(true).when(p).isCrawling(),
                p -> doReturn(true).when(p).getDataFlag(anyInt()),
                p -> doReturn(false).when(p).isServerAuthoritativeMovementEnabled(),
                p -> doReturn(false).when(p).supportsPacketSequences(),
                p -> doReturn(false).when(p).isAlive(),
                p -> p.spawned(false),
                p -> p.ride(mock(Entity.class)),
                TestPlayer::awaitTeleport,
                p -> p.pendingCraft(mock(CraftingTransaction.class)),
                p -> p.pendingEnchant(mock(EnchantTransaction.class)),
                p -> p.pendingRepair(mock(RepairItemTransaction.class)));
        for (java.util.function.Consumer<TestPlayer> state : states) {
            TestPlayer player = player();
            doReturn(true).when(player).isUsingItem();
            state.accept(player);
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                    16, 1024, 16, 1024, exception -> fail(exception));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            List<String> order = new ArrayList<>();
            PlayerAuthInputPacket116220 movement = new PlayerAuthInputPacket116220();
            movement.inputFlags = new BitSet();
            dispatcher.offer(session, task(player, movement, () -> order.add("movement")), 1, true);
            dispatcher.offer(session, task(player, new InventoryTransactionPacket116(), () -> order.add("release")), 1, true);

            assertEquals(2, dispatcher.drain(true, 40, Long.MAX_VALUE));
            assertEquals(List.of("movement", "release"), order);
            assertEquals(0, dispatcher.snapshot().betweenTickBarriers());
        }
    }

    private static ServerInputTask task(TestPlayer player, DataPacket packet, Runnable action) {
        return new ServerInputTask() {
            @Override public void run(InboundContext context) { action.run(); }
        };
    }

    static TestPlayer player() {
        TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        player.prepare(server);
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).isUsingOffhandItem();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isSwimming();
        doReturn(false).when(player).isGliding();
        doReturn(false).when(player).isCrawling();
        doReturn(false).when(player).getDataFlag(anyInt());
        doReturn(mock(AdventureSettings.class)).when(player).getAdventureSettings();
        return player;
    }

    static class TestPlayer extends SynapsePlayer116100 {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void prepare(Server server) {
            this.server = server;
            this.spawned = true;
        }

        void spawned(boolean value) { this.spawned = value; }
        void ride(Entity value) { this.riding = value; }
        Server testServer() { return this.server; }
        void pendingCraft(CraftingTransaction value) { this.craftingTransaction = value; }
        void pendingEnchant(EnchantTransaction value) { this.enchantTransaction = value; }
        void pendingRepair(RepairItemTransaction value) { this.repairItemTransaction = value; }
        void useInventory(PlayerInventory inventory) { this.inventory = inventory; }

        void awaitTeleport() {
            this.teleportPosition = new Vector3();
        }

        void clearTeleport() {
            this.teleportPosition = null;
        }
    }
}
