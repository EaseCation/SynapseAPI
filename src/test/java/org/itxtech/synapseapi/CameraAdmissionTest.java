package org.itxtech.synapseapi;

import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol121100.protocol.CameraAimAssistPacket120100;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.ClientCameraAimAssistPacket12160;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CameraAdmissionTest {
    @BeforeAll
    static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test
    void clearRestoresOnlyTheServerPreset() { checkServerPreset(true); }

    @Test
    void disabledModeKeepsOriginalServerPresetReply() { checkServerPreset(false); }

    private void checkServerPreset(boolean enabled) {
        for (int protocol : new int[]{860, 975}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(enabled, true)) {
                doReturn(protocol).when(f.player).getProtocol();
                prepare(f);
                ClientCameraAimAssistPacket12160 request = packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, true);
                f.deliver(request);
                assertTrue(f.player.allowAimAssist);
                assertEquals("server:known", f.player.aimAssistPresetId);
                assertEquals(1, f.sent.size());
                CameraAimAssistPacket120100 reply = assertInstanceOf(CameraAimAssistPacket120100.class, f.sent.getFirst());
                assertEquals(CameraAimAssistPacket120100.ACTION_SET, reply.action);
                assertEquals(CameraAimAssistPacket120100.MODE_DISTANCE, reply.mode);
                assertEquals("server:known", reply.presetId);
                assertEquals(11, reply.pitch);
                assertEquals(22, reply.yaw);
                assertEquals(6, reply.distance);
                verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                if (enabled) verify(f.processor).validateReceivedPacket(f.player, request, false);
                else verifyNoInteractions(f.processor);
                verify(f.player, never()).onUpdate(anyInt());
            }
        }
    }

    @Test
    void deniedPreferenceOrMissingServerPresetKeepsClearReply() {
        for (boolean allow : new boolean[]{true, false}) {
            for (boolean configured : new boolean[]{true, false}) {
                if (allow && configured) continue;
                try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                    if (configured) prepare(f);
                    f.deliver(packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, allow));
                    assertEquals(allow, f.player.allowAimAssist);
                    CameraAimAssistPacket120100 reply = assertInstanceOf(CameraAimAssistPacket120100.class, f.sent.getFirst());
                    assertEquals(CameraAimAssistPacket120100.ACTION_CLEAR, reply.action);
                    assertEquals(CameraAimAssistPacket120100.MODE_ANGLE, reply.mode);
                    assertEquals("", reply.presetId);
                    assertEquals(30, reply.pitch);
                    assertEquals(45, reply.yaw);
                    assertEquals(5.7f, reply.distance);
                }
            }
        }
    }

    @Test
    void clientPresetAndUnknownActionDoNotImplementUnfinishedBehavior() {
        for (int action : new int[]{ClientCameraAimAssistPacket12160.ACTION_SET_FROM_CAMERA_PRESET, 255}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                prepare(f);
                f.deliver(packet(action, true));
                assertTrue(f.player.allowAimAssist);
                assertEquals("server:known", f.player.aimAssistPresetId);
                assertTrue(f.sent.isEmpty());
            }
        }
    }

    @Test
    void cancelledAndRejectedRequestsDoNotChangePreference() {
        for (boolean cancel : new boolean[]{true, false}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                prepare(f);
                f.cancel = cancel;
                if (!cancel) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                f.deliver(packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, true));
                assertFalse(f.player.allowAimAssist);
                assertTrue(f.sent.isEmpty());
                assertEquals("server:known", f.player.aimAssistPresetId);
            }
        }
    }

    @Test
    void initializationAndRetirementKeepOriginalEntryRejection() {
        for (boolean retire : new boolean[]{true, false}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                if (retire) doReturn(false).when(f.player).isAcceptingInputPackets();
                else doReturn(false).when(f.player).isInitialized();
                f.deliver(packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, true));
                assertFalse(f.player.allowAimAssist);
                assertTrue(f.sent.isEmpty());
                verifyNoInteractions(f.plugins, f.processor);
            }
        }
    }

    @Test
    void preferenceDoesNotDependOnPhysicalInputReadiness() {
        for (String state : List.of("dead", "unspawned", "offline", "no-sequence", "no-processor")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                if (state.equals("dead")) f.player.makeDead();
                if (state.equals("unspawned")) f.player.spawned = false;
                if (state.equals("offline")) doReturn(false).when(f.player).isOnline();
                if (state.equals("no-sequence")) doReturn(false).when(f.player).supportsPacketSequences();
                if (state.equals("no-processor")) when(f.server.getPlayerInputProcessor()).thenReturn(null);
                f.deliver(packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, true));
                assertTrue(f.player.allowAimAssist);
                assertEquals(1, f.sent.size());
                assertEquals(0, f.player.respawns);
                verify(f.player, never()).onUpdate(anyInt());
            }
        }
    }

    @Test
    void callbackRetirementStopsPreferenceChange() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            doAnswer(call -> { doReturn(false).when(f.player).isAcceptingInputPackets(); return null; })
                    .when(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
            f.deliver(packet(ClientCameraAimAssistPacket12160.ACTION_CLEAR, true));
            assertFalse(f.player.allowAimAssist);
            assertTrue(f.sent.isEmpty());
            verifyNoInteractions(f.processor);
        }
    }

    @Test
    void targetProtocolsDecodeWholeClientPreference() {
        for (int protocol : new int[]{859, 860, 898, 975}) {
            BinaryStream wire = new BinaryStream();
            wire.putUnsignedVarInt(ProtocolInfo.CLIENT_CAMERA_AIM_ASSIST_PACKET);
            wire.putString("client:untrusted");
            wire.putByte((byte) ClientCameraAimAssistPacket12160.ACTION_CLEAR);
            wire.putBoolean(true);
            DataPacket decoded = PacketRegister.getFullPacket(wire.getBuffer(), protocol, false);
            ClientCameraAimAssistPacket12160 packet = assertInstanceOf(ClientCameraAimAssistPacket12160.class, decoded);
            assertTrue(BetweenTickPackets.supports(packet));
            assertEquals(wire.getCount(), packet.getOffset());
            assertEquals("client:untrusted", packet.cameraPresetId);
            assertEquals(ClientCameraAimAssistPacket12160.ACTION_CLEAR, packet.action);
            assertTrue(packet.allowAimAssist);
        }
    }

    private static void prepare(PlayerActionAdmissionTest.Fixture f) {
        f.player.setAimAssist(CameraAimAssistPacket120100.MODE_DISTANCE, 11, 22, 6, "server:known");
        f.sent.clear();
    }

    private static ClientCameraAimAssistPacket12160 packet(int action, boolean allow) {
        ClientCameraAimAssistPacket12160 packet = new ClientCameraAimAssistPacket12160();
        packet.action = action;
        packet.allowAimAssist = allow;
        packet.cameraPresetId = "client:untrusted";
        return packet;
    }
}
