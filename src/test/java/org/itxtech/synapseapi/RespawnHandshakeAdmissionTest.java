package org.itxtech.synapseapi;

import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.RespawnPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.RespawnPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RespawnHandshakeAdmissionTest {
    @BeforeAll
    static void initialize() {
        LegacyMovementAdmissionTest.protocols();
    }

    @Test
    void deadHandshakeRepliesFromServerWithoutRespawning() {
        checkDeadReply(true);
    }

    @Test
    void disabledHandshakeKeepsOriginalReply() {
        checkDeadReply(false);
    }

    private void checkDeadReply(boolean enabled) {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(enabled, true)) {
            f.player.makeDead();
            f.player.setComponents(17, 29, -11);
            f.player.noDamageTicks = 13;
            long epoch = f.player.getMovementEpoch();
            RespawnPacket113 packet = ready();
            packet.x = Float.NaN;
            packet.y = Float.POSITIVE_INFINITY;
            packet.z = -123456;
            packet.runtimeEntityId = 987654;
            f.deliver(packet);
            assertEquals(1, f.sent.size());
            RespawnPacket113 reply = assertInstanceOf(RespawnPacket113.class, f.sent.getFirst());
            assertEquals(RespawnPacket.STATE_READY_TO_SPAWN, reply.respawnState);
            assertEquals(17, reply.x);
            assertEquals(29, reply.y);
            assertEquals(-11, reply.z);
            assertEquals(0, reply.runtimeEntityId);
            assertEquals(0, f.player.respawns);
            assertFalse(f.player.isAlive());
            assertEquals(epoch, f.player.getMovementEpoch());
            assertEquals(13, f.player.noDamageTicks);
            verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
            verify(f.player, never()).onUpdate(anyInt());
            if (enabled) verify(f.processor).validateReceivedPacket(f.player, packet, false);
            else verifyNoInteractions(f.processor);
        }
    }

    @Test
    void duplicateRepliesDoNotDuplicateActualRespawn() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            f.player.makeDead();
            f.deliver(ready());
            f.deliver(ready());
            assertEquals(2, f.sent.size());
            assertEquals(0, f.player.respawns);
            f.deliver(PlayerActionPacket119.ACTION_RESPAWN);
            assertEquals(1, f.player.respawns);
            f.deliver(ready());
            assertEquals(2, f.sent.size());
            f.deliver(PlayerActionPacket119.ACTION_RESPAWN);
            assertEquals(1, f.player.respawns);
        }
    }

    @Test
    void aliveAndNonReadyStatesKeepOriginalIgnore() {
        for (boolean alive : new boolean[]{true, false}) {
            for (int state : new int[]{0, 1, 2, 3, 255}) {
                if (!alive && state == RespawnPacket.STATE_CLIENT_READY_TO_SPAWN) continue;
                try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                    if (!alive) f.player.makeDead();
                    RespawnPacket113 packet = ready();
                    packet.respawnState = state;
                    f.deliver(packet);
                    assertTrue(f.sent.isEmpty());
                    assertEquals(0, f.player.respawns);
                }
            }
        }
    }

    @Test
    void cancellationAndValidationRejectionCannotReopenHandshake() {
        for (boolean cancel : new boolean[]{true, false}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                f.player.makeDead();
                f.cancel = cancel;
                if (!cancel) when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                RespawnPacket113 packet = ready();
                f.deliver(packet);
                assertTrue(f.sent.isEmpty());
                assertFalse(f.player.isAlive());
                verify(f.processor).validateReceivedPacket(f.player, packet, cancel);
            }
        }
    }

    @Test
    void retiredAndUninitializedDoNotReachOriginalHandler() {
        for (boolean retired : new boolean[]{true, false}) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                f.player.makeDead();
                if (retired) doReturn(false).when(f.player).isAcceptingInputPackets();
                else doReturn(false).when(f.player).isInitialized();
                f.deliver(ready());
                assertTrue(f.sent.isEmpty());
                verifyNoInteractions(f.plugins, f.processor);
            }
        }
    }

    @Test
    void eventRetirementPreventsReplyAndFurtherValidation() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            f.player.makeDead();
            doAnswer(call -> {
                doReturn(false).when(f.player).isAcceptingInputPackets();
                return null;
            }).when(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
            f.deliver(ready());
            assertTrue(f.sent.isEmpty());
            verifyNoInteractions(f.processor);
        }
    }

    @Test
    void nonPositionHandshakeKeepsCurrentServerReplyForOldEpoch() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            f.player.makeDead();
            f.player.setComponents(3, 4, 5);
            f.deliver(ready(), f.player.getMovementEpoch() - 1);
            RespawnPacket113 reply = assertInstanceOf(RespawnPacket113.class, f.sent.getFirst());
            assertEquals(3, reply.x);
            assertEquals(4, reply.y);
            assertEquals(5, reply.z);
            assertEquals(0, f.player.respawns);
        }
    }

    @Test
    void targetProtocolsDecodeWholeHandshakeBeforeAdmission() {
        for (int protocol : new int[]{859, 860, 898, 975}) {
            RespawnPacket113 packet = ready();
            packet.x = 1.25f;
            packet.y = -60;
            packet.z = -2.5f;
            packet.runtimeEntityId = 1234;
            packet.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());
            packet.encode();
            DataPacket decoded = PacketRegister.getFullPacket(packet.getBuffer(), protocol, false);
            RespawnPacket113 actual = assertInstanceOf(RespawnPacket113.class, decoded);
            assertTrue(BetweenTickPackets.supports(actual));
            assertEquals(packet.getCount(), actual.getOffset());
            assertEquals(packet.x, actual.x);
            assertEquals(packet.y, actual.y);
            assertEquals(packet.z, actual.z);
            assertEquals(packet.respawnState, actual.respawnState);
            assertEquals(packet.runtimeEntityId, actual.runtimeEntityId);
        }
    }

    private static RespawnPacket113 ready() {
        RespawnPacket113 packet = new RespawnPacket113();
        packet.respawnState = RespawnPacket.STATE_CLIENT_READY_TO_SPAWN;
        return packet;
    }
}
