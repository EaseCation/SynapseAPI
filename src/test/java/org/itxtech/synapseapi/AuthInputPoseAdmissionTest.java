package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerToggleSwimEvent;
import cn.nukkit.event.player.PlayerToggleCrawlEvent;
import cn.nukkit.event.Event;
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
    @Test void startsInSupplementalPhase() { check(false, "normal", true, true, true); }
    @Test void stopsInSupplementalPhase() { check(false, "normal", true, false, false); }
    @Test void cancelledStartDoesNotSwim() { check(false, "cancel", true, true, false); }
    @Test void cancelledStopRetainsPose() { check(false, "cancel", true, false, true); }
    @Test void deathDuringCallbackDoesNotWritePose() { check(false, "death", true, true, false); }
    @Test void retiredOwnerDoesNotWritePose() { check(false, "retire", true, true, false); }
    @Test void movedEpochDoesNotWritePose() { check(false, "epoch", true, true, false); }
    @Test void disabledModeKeepsOriginalCallbackContinuation() { check(false, "epoch", false, true, true); }

    @Test void startsCrawlingInSupplementalPhase() { check(true, "normal", true, true, true); }
    @Test void stopsCrawlingInSupplementalPhase() { check(true, "normal", true, false, false); }
    @Test void cancelledCrawlStartKeepsStanding() { check(true, "cancel", true, true, false); }
    @Test void cancelledCrawlStopKeepsLowPose() { check(true, "cancel", true, false, true); }
    @Test void deathDoesNotApplyCrawl() { check(true, "death", true, true, false); }
    @Test void retiredOwnerDoesNotApplyCrawl() { check(true, "retire", true, true, false); }
    @Test void movedEpochDoesNotApplyCrawl() { check(true, "epoch", true, true, false); }
    @Test void disabledCrawlKeepsOriginalContinuation() { check(true, "epoch", false, true, true); }

    private void check(boolean crawling, String variant, boolean enabled, boolean start, boolean expected) {
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
        AtomicBoolean pose = new AtomicBoolean(!start);
        if (crawling) {
            doAnswer(call -> pose.get()).when(player).isCrawling();
            doAnswer(call -> { pose.set(call.getArgument(0)); return true; }).when(player).setCrawling(anyBoolean());
        } else {
            doAnswer(call -> pose.get()).when(player).isSwimming();
            doAnswer(call -> { pose.set(call.getArgument(0)); return true; }).when(player).setSwimming(anyBoolean());
        }
        doAnswer(call -> {
            Event event = call.getArgument(0);
            if (crawling ? event instanceof PlayerToggleCrawlEvent : event instanceof PlayerToggleSwimEvent) {
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
        int flag = crawling ? (start ? PlayerAuthInputFlags.START_CRAWLING : PlayerAuthInputFlags.STOP_CRAWLING)
                : (start ? PlayerAuthInputFlags.START_SWIMMING : PlayerAuthInputFlags.STOP_SWIMMING);
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
        verify(plugins, times(1)).callEvent(crawling ? any(PlayerToggleCrawlEvent.class) : any(PlayerToggleSwimEvent.class));
        verify(player, times(1)).callPacketReceiveEvent(packet);
        verify(player, never()).onUpdate(anyInt());
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; this.spawned = true; }
    }
}
