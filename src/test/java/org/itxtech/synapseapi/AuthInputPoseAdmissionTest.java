package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleSwimEvent;
import cn.nukkit.event.player.PlayerToggleCrawlEvent;
import cn.nukkit.event.Event;
import cn.nukkit.event.player.PlayerToggleGlideEvent;
import cn.nukkit.inventory.ArmorInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemElytra;
import cn.nukkit.item.ItemID;
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

class AuthInputPoseAdmissionTest {
    @Test void startsInSupplementalPhase() { check(Pose.SWIM, "normal", true, true, true); }
    @Test void stopsInSupplementalPhase() { check(Pose.SWIM, "normal", true, false, false); }
    @Test void cancelledStartDoesNotSwim() { check(Pose.SWIM, "cancel", true, true, false); }
    @Test void cancelledStopRetainsPose() { check(Pose.SWIM, "cancel", true, false, true); }
    @Test void deathDuringCallbackDoesNotWritePose() { check(Pose.SWIM, "death", true, true, false); }
    @Test void retiredOwnerDoesNotWritePose() { check(Pose.SWIM, "retire", true, true, false); }
    @Test void movedEpochDoesNotWritePose() { check(Pose.SWIM, "epoch", true, true, false); }
    @Test void disabledModeKeepsOriginalCallbackContinuation() { check(Pose.SWIM, "epoch", false, true, true); }

    @Test void startsCrawlingInSupplementalPhase() { check(Pose.CRAWL, "normal", true, true, true); }
    @Test void stopsCrawlingInSupplementalPhase() { check(Pose.CRAWL, "normal", true, false, false); }
    @Test void cancelledCrawlStartKeepsStanding() { check(Pose.CRAWL, "cancel", true, true, false); }
    @Test void cancelledCrawlStopKeepsLowPose() { check(Pose.CRAWL, "cancel", true, false, true); }
    @Test void deathDoesNotApplyCrawl() { check(Pose.CRAWL, "death", true, true, false); }
    @Test void retiredOwnerDoesNotApplyCrawl() { check(Pose.CRAWL, "retire", true, true, false); }
    @Test void movedEpochDoesNotApplyCrawl() { check(Pose.CRAWL, "epoch", true, true, false); }
    @Test void disabledCrawlKeepsOriginalContinuation() { check(Pose.CRAWL, "epoch", false, true, true); }

    private enum Pose {
        SWIM,
        CRAWL,
        GLIDE
    }

    @Test void startsGlidingInSupplementalPhase() { check(Pose.GLIDE, "normal", true, true, true); }
    @Test void stopsGlidingInSupplementalPhase() { check(Pose.GLIDE, "normal", true, false, false); }
    @Test void cancelledGlideStartKeepsStanding() { check(Pose.GLIDE, "cancel", true, true, false); }
    @Test void cancelledGlideStopKeepsPose() { check(Pose.GLIDE, "cancel", true, false, true); }
    @Test void missingElytraRejectsStart() { check(Pose.GLIDE, "missing", true, true, false); }
    @Test void brokenElytraRejectsStart() { check(Pose.GLIDE, "broken", true, true, false); }
    @Test void deathDoesNotApplyGlide() { check(Pose.GLIDE, "death", true, true, false); }
    @Test void retiredOwnerDoesNotApplyGlide() { check(Pose.GLIDE, "retire", true, true, false); }
    @Test void movedEpochDoesNotApplyGlide() { check(Pose.GLIDE, "epoch", true, true, false); }
    @Test void disabledGlideKeepsOriginalContinuation() { check(Pose.GLIDE, "epoch", false, true, true); }

    private void check(Pose target, String variant, boolean enabled, boolean start, boolean expected) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        player.configure(server);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(mock(PlayerInputProcessor.class));
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).supportsPacketSequences();
        doReturn(false).when(player).isSleeping();
        doReturn(false).when(player).isGliding();
        doReturn(false).when(player).isCrawling();
        doReturn(false).when(player).isSwimming();
        doReturn(false).when(player).isSneaking();
        doReturn(false).when(player).isUsingItem();
        doReturn(false).when(player).getDataFlag(anyInt());
        doReturn(mock(AdventureSettings.class)).when(player).getAdventureSettings();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doNothing().when(player).onClientTickUpdated(anyLong());
        doReturn(false).when(player).commitReceivedMovement();
        doNothing().when(player).sendData(player);
        ArmorInventory armor = mock(ArmorInventory.class);
        doReturn(armor).when(player).getArmorInventory();
        Item chestplate = new ItemElytra();
        if (variant.equals("missing")) {
            chestplate = mock(Item.class);
            when(chestplate.getId()).thenReturn(ItemID.AIR);
        } else if (variant.equals("broken")) {
            chestplate.setDamage(chestplate.getMaxDurability() - 1);
        }
        when(armor.getChestplate()).thenReturn(chestplate);
        AtomicBoolean pose = new AtomicBoolean(!start);
        if (target == Pose.GLIDE) {
            doAnswer(call -> pose.get()).when(player).isGliding();
            doAnswer(call -> { pose.set(call.getArgument(0)); return true; }).when(player).setGliding(anyBoolean());
        } else if (target == Pose.CRAWL) {
            doAnswer(call -> pose.get()).when(player).isCrawling();
            doAnswer(call -> { pose.set(call.getArgument(0)); return true; }).when(player).setCrawling(anyBoolean());
        } else {
            doAnswer(call -> pose.get()).when(player).isSwimming();
            doAnswer(call -> { pose.set(call.getArgument(0)); return true; }).when(player).setSwimming(anyBoolean());
        }
        doAnswer(call -> {
            Event event = call.getArgument(0);
            if (target == Pose.CRAWL && event instanceof PlayerToggleCrawlEvent
                    || target == Pose.SWIM && event instanceof PlayerToggleSwimEvent
                    || target == Pose.GLIDE && event instanceof PlayerToggleGlideEvent) {
                switch (variant) {
                    case "cancel" -> event.setCancelled();
                    case "death" -> doReturn(false).when(player).isAlive();
                    case "retire" -> doReturn(false).when(player).isAcceptingInputPackets();
                    case "epoch" -> doReturn(false).when(player).isCurrentInputPosition();
                    default -> { }
                }
            }
            return null;
        }).when(plugins).callEvent(any());
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        int flag = switch (target) {
            case SWIM -> start ? PlayerAuthInputFlags.START_SWIMMING : PlayerAuthInputFlags.STOP_SWIMMING;
            case CRAWL -> start ? PlayerAuthInputFlags.START_CRAWLING : PlayerAuthInputFlags.STOP_CRAWLING;
            case GLIDE -> start ? PlayerAuthInputFlags.START_GLIDING : PlayerAuthInputFlags.STOP_GLIDING;
        };
        BitSet flags = new BitSet();
        flags.set(flag);
        when(packet.getInputFlags()).thenReturn(flags);
        when(packet.hasFlag(flag)).thenReturn(true);
        if (enabled) {
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(),
                    16, 1024, 16, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            List<String> order = new ArrayList<>();
            assertTrue(BetweenTickPackets.supports(packet));
            dispatcher.offer(session, new ServerInputTask() {
                @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                @Override public void run(InboundContext context) {
                    assertTrue(context.betweenTicks());
                    player.handleDataPacket(packet);
                    order.add("pose");
                }
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
            assertEquals(List.of("pose", "follower"), order);
        } else {
            player.handleDataPacket(packet);
        }
        assertEquals(expected, pose.get());
        // 使用原身高getter验证状态含义；真实包围盒和水体碰撞另由实机验证。
        assertEquals(expected ? 0.6f : 1.8f, player.getHeight());
        switch (target) {
            case SWIM -> verify(plugins, times(1)).callEvent(any(PlayerToggleSwimEvent.class));
            case CRAWL -> verify(plugins, times(1)).callEvent(any(PlayerToggleCrawlEvent.class));
            case GLIDE -> verify(plugins, times(1)).callEvent(any(PlayerToggleGlideEvent.class));
        }
        if (variant.equals("missing") || variant.equals("broken")) {
            verify(plugins).callEvent(argThat(event -> event instanceof PlayerToggleGlideEvent && event.isCancelled()));
        }
        verify(armor, never()).setChestplate(any(), anyBoolean());
        verify(player, times(1)).callPacketReceiveEvent(packet);
        verify(player, never()).onUpdate(anyInt());
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; this.spawned = true; }
    }
}
