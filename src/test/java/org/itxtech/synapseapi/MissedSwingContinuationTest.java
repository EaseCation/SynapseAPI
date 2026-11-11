package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.ClientChainData;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

class MissedSwingContinuationTest {
    @Test void liveTouchHintPublishesOnce() { check("sound", "normal", true, 1); }
    @Test void soundCallbackRetirementStopsTouchAnimation() { check("sound", "retire", true, 0); }
    @Test void soundCallbackTeleportStopsTouchAnimation() { check("sound", "teleport", true, 0); }
    @Test void soundCallbackDeathStopsTouchAnimation() { check("sound", "death", true, 0); }
    @Test void selfAnimationCallbackRetirementStopsViewerPublication() { check("self", "retire", true, 0); }
    @Test void selfAnimationCallbackTeleportStopsViewerPublication() { check("self", "teleport", true, 0); }
    @Test void cancelledSelfSendWithCurrentOwnerKeepsOriginalViewerPublication() { check("self", "cancel", true, 1); }
    @Test void disabledModeKeepsOriginalSoundContinuation() { check("sound", "teleport", false, 1); }
    @Test void disabledModeKeepsOriginalSelfContinuation() { check("self", "teleport", false, 1); }

    @Test void spectatorDoesNotPublishSwingEffects() { check("suppressed", "spectator", true, 0); }
    @Test void disabledSoundAuthorityDoesNotPublishSwingEffects() { check("suppressed", "sound-disabled", true, 0); }
    @Test void cancelledWholeInputPublishesNoSoundOrAnimation() { check("packet", "cancel", true, 0); }
    @Test void validatorRejectionPublishesNoSoundOrAnimation() { check("packet", "reject", true, 0); }

    private void check(String boundary, String mutation, boolean enabled, int publications) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(plugins);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(
                boundary.equals("packet") && mutation.equals("reject") ? InputValidationResult.REJECTED : InputValidationResult.ACCEPTED);
        doAnswer(call -> {
            if (boundary.equals("packet") && mutation.equals("cancel") && call.getArgument(0) instanceof DataPacketReceiveEvent event) event.setCancelled();
            return null;
        }).when(plugins).callEvent(any());
        Level level = mock(Level.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInHand()).thenReturn(mock(Item.class));
        player.configure(server, level, inventory);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(!mutation.equals("sound-disabled")).when(player).isServerAuthoritativeSoundEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(mutation.equals("spectator")).when(player).isSpectator();
        doReturn(4L).when(player).getMovementEpoch();
        doReturn(Map.<Long, Player>of()).when(player).getViewers();
        doReturn(true).when(player).isUsingItem();
        doReturn(true).when(player).isGliding();
        doReturn(false).when(player).commitReceivedMovement();
        doNothing().when(player).onClientTickUpdated(anyLong());
        LoginChainData chain = mock(LoginChainData.class);
        when(chain.getCurrentInputMode()).thenReturn(ClientChainData.INPUT_TOUCH);
        doReturn(chain).when(player).getLoginChainData();
        Runnable mutate = () -> {
            switch (mutation) {
                case "retire" -> doReturn(false).when(player).isAcceptingInputPackets();
                case "teleport" -> doReturn(5L).when(player).getMovementEpoch();
                case "death" -> doReturn(false).when(player).isAlive();
                default -> { }
            }
        };
        doAnswer(call -> {
            if (boundary.equals("sound")) mutate.run();
            return null;
        }).when(level).addLevelSoundEvent(any(Vector3.class), anyInt(), anyString());
        doAnswer(call -> {
            if (boundary.equals("self")) mutate.run();
            return !mutation.equals("cancel");
        }).when(player).dataPacket(any(AnimatePacket.class));
        PlayerAuthInputPacket116220 packet = mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        BitSet flags = new BitSet(); flags.set(PlayerAuthInputFlags.MISSED_SWING);
        when(packet.getInputFlags()).thenReturn(flags);
        when(packet.getInputMode()).thenReturn(ClientChainData.INPUT_TOUCH);
        when(packet.hasFlag(PlayerAuthInputFlags.MISSED_SWING)).thenReturn(true);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            if (enabled) {
                ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
                List<String> order = new ArrayList<>();
                assertTrue(BetweenTickPackets.supports(packet));
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) {
                        assertTrue(context.betweenTicks());
                        player.handleDataPacket(packet);
                        order.add("swing");
                    }
                }, 1, true);
                dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()), context -> order.add("follower"), 1, true);
                assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
                assertEquals(List.of("swing", "follower"), order);
            } else {
                player.handleDataPacket(packet);
            }
            global.verify(() -> Server.broadcastPacket(anyCollection(), any(AnimatePacket.class)), times(publications));
        }
        verify(processor, times(enabled ? 1 : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        verify(level, times(boundary.equals("packet") || boundary.equals("suppressed") ? 0 : 1)).addLevelSoundEvent(any(Vector3.class), anyInt(), anyString());
        verify(player, never()).setUsingItem(anyBoolean());
        verify(inventory, never()).setItemInHand(any(Item.class));
        verify(player, never()).onUpdate(anyInt());
        if (boundary.equals("packet") || boundary.equals("suppressed") || boundary.equals("sound") && enabled && !mutation.equals("normal")) {
            verify(player, never()).dataPacket(any(AnimatePacket.class));
        } else {
            verify(player).dataPacket(any(AnimatePacket.class));
        }
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, PlayerInventory inventory) {
            this.server = server;
            this.level = level;
            this.inventory = inventory;
            this.isSynapseLogin = true;
            this.spawned = true;
        }
        @Override protected float getBaseOffset() { return 0; }
    }
}
