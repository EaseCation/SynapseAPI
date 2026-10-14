package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AnimatePacket;
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

    private void check(String boundary, String mutation, boolean enabled, int publications) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        Level level = mock(Level.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInHand()).thenReturn(mock(Item.class));
        player.configure(server, level, inventory);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).isServerAuthoritativeSoundEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(false).when(player).isSpectator();
        doReturn(4L).when(player).getMovementEpoch();
        doReturn(Map.<Long, Player>of()).when(player).getViewers();
        doReturn(true).when(player).callPacketReceiveEvent(any());
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
        when(packet.getInputMode()).thenReturn(ClientChainData.INPUT_TOUCH);
        when(packet.hasFlag(PlayerAuthInputFlags.MISSED_SWING)).thenReturn(true);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            player.handleDataPacket(packet);
            global.verify(() -> Server.broadcastPacket(anyCollection(), any(AnimatePacket.class)), times(publications));
        }
        verify(level).addLevelSoundEvent(any(Vector3.class), anyInt(), anyString());
        if (boundary.equals("sound") && enabled && !mutation.equals("normal")) {
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
