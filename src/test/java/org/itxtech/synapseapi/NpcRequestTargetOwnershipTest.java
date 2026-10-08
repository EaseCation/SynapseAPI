package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.dialogue.NPCDialoguePlayerHandler;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NPCRequestPacket11710;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NpcDialoguePacket11710;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.*;

class NpcRequestTargetOwnershipTest {
    @Test
    void anotherExistingEntityCannotDriveAnyCurrentDialogueAction() {
        verifyActions(true, false, false);
    }

    @Test
    void boundCurrentEntityKeepsAllThreeDialogueActions() {
        verifyActions(true, true, true);
    }

    @Test
    void disabledModeKeepsLegacyOtherEntityRouting() {
        verifyActions(false, false, true);
    }

    @Test
    void noActiveDialoguePreservesExistingHandlerRejectionAndCloseResponse() {
        Fixture fixture = new Fixture(true, true);
        when(fixture.handler.getCurrentEntity()).thenReturn(null);
        when(fixture.handler.onDialogueResponse("owned-scene", 0)).thenReturn(false);
        fixture.player.handleDataPacket(packet(NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION));
        verify(fixture.handler).onDialogueResponse("owned-scene", 0);
        verify(fixture.player).dataPacket(any(NpcDialoguePacket11710.class));
    }

    @Test
    void missingWorldEntityKeepsOriginalCloseWithoutInvokingDialogue() {
        Fixture fixture = new Fixture(true, true);
        when(fixture.level.getEntity(18)).thenReturn(null);
        fixture.player.handleDataPacket(packet(NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION));
        verify(fixture.handler, never()).onDialogueResponse(anyString(), anyInt());
        verify(fixture.player).dataPacket(any(NpcDialoguePacket11710.class));
    }

    @Test
    void receiveCancellationStillStopsBeforeTargetLookup() {
        Fixture fixture = new Fixture(true, true);
        doReturn(false).when(fixture.player).callPacketReceiveEvent(any());
        fixture.player.handleDataPacket(packet(NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION));
        verify(fixture.level, never()).getEntity(anyLong());
        verifyNoInteractions(fixture.handler);
    }

    private void verifyActions(boolean enabled, boolean sameSource, boolean expected) {
        for (int action : new int[]{NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION,
                NPCRequestPacket11710.TYPE_EXECUTE_OPENING_COMMANDS, NPCRequestPacket11710.TYPE_EXECUTE_CLOSING_COMMANDS}) {
            Fixture fixture = new Fixture(enabled, sameSource);
            fixture.player.handleDataPacket(packet(action));
            switch (action) {
                case NPCRequestPacket11710.TYPE_EXECUTE_COMMAND_ACTION -> verify(fixture.handler, times(expected ? 1 : 0)).onDialogueResponse("owned-scene", 0);
                case NPCRequestPacket11710.TYPE_EXECUTE_OPENING_COMMANDS -> verify(fixture.handler, times(expected ? 1 : 0)).onDialogueOpening("owned-scene");
                case NPCRequestPacket11710.TYPE_EXECUTE_CLOSING_COMMANDS -> verify(fixture.handler, times(expected ? 1 : 0)).onDialogueClosing("owned-scene");
                default -> throw new AssertionError("Unexpected test action");
            }
            if (!expected) verify(fixture.player, never()).dataPacket(any());
            if (!enabled) verify(fixture.handler, never()).getCurrentEntity();
        }
    }

    private NPCRequestPacket11710 packet(int action) {
        NPCRequestPacket11710 packet = new NPCRequestPacket11710();
        packet.entityRuntimeId = 18;
        packet.type = action;
        packet.actionIndex = 0;
        packet.command = "";
        packet.sceneName = "owned-scene";
        return packet;
    }

    private static final class Fixture {
        private final ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        private final Level level = mock(Level.class);
        private final NPCDialoguePlayerHandler handler = mock(NPCDialoguePlayerHandler.class);

        private Fixture(boolean enabled, boolean sameSource) {
            player.configure(mock(Server.class));
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).callPacketReceiveEvent(any(DataPacket.class));
            doReturn(level).when(player).getLevel();
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(handler).when(player).getNpcDialoguePlayerHandler();
            doReturn(true).when(player).dataPacket(any());
            Entity target = mock(Entity.class);
            when(level.getEntity(18)).thenReturn(target);
            when(handler.getCurrentEntity()).thenReturn(sameSource ? target : mock(Entity.class));
            when(handler.onDialogueResponse(anyString(), anyInt())).thenReturn(true);
            when(handler.onDialogueOpening(anyString())).thenReturn(true);
            when(handler.onDialogueClosing(anyString())).thenReturn(true);
        }
    }

    static class ProbePlayer extends SynapsePlayer116100 {
        ProbePlayer() { super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server) { this.server = server; this.isSynapseLogin = true; }
    }
}
