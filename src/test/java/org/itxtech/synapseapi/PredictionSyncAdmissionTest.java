package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.ClientMovementPredictionSyncPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.ClientMovementPredictionSyncPacket12170;
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.ClientMovementPredictionSyncPacket12620;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PredictionSyncAdmissionTest {
    @BeforeAll static void initialize() { LegacyMovementAdmissionTest.protocols(); }

    @Test void targetAndAdjacentRegisteredLayoutsDecodeExactly() {
        try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
            for (int protocol : new int[]{AbstractProtocol.PROTOCOL_121_60.getProtocolStart(), 859, 860, 898, 975,
                    AbstractProtocol.PROTOCOL_126_20.getProtocolStart()}) {
                AbstractProtocol version = AbstractProtocol.fromRealProtocol(protocol);
                BinaryStream wire = new BinaryStream();
                wire.setHelper(version.getHelper());
                wire.putUnsignedVarInt(ProtocolInfo.CLIENT_MOVEMENT_PREDICTION_SYNC_PACKET);
                wire.putByte((byte) 0); // 空VarBitSet的单字节表示。
                int floats = version.ordinal() >= AbstractProtocol.PROTOCOL_126_20.ordinal() ? 12 : 9;
                for (int i = 0; i < floats; i++) wire.putLFloat(99 + i);
                wire.putEntityUniqueId(123);
                if (version.ordinal() >= AbstractProtocol.PROTOCOL_121_70.ordinal()) wire.putBoolean(true);
                DataPacket packet = PacketRegister.getFullPacket(wire.getBuffer(), protocol, false);
                assertNotNull(packet);
                assertEquals(wire.getCount(), packet.getOffset());
                if (floats == 12) {
                    ClientMovementPredictionSyncPacket12620 sync = assertInstanceOf(ClientMovementPredictionSyncPacket12620.class, packet);
                    assertEquals(110, sync.airDragModifier);
                    assertTrue(sync.flying);
                } else if (version.ordinal() >= AbstractProtocol.PROTOCOL_121_70.ordinal()) {
                    ClientMovementPredictionSyncPacket12170 sync = assertInstanceOf(ClientMovementPredictionSyncPacket12170.class, packet);
                    assertEquals(123, sync.entityUniqueId);
                    assertTrue(sync.flying);
                } else {
                    assertInstanceOf(ClientMovementPredictionSyncPacket12160.class, packet);
                }
                assertTrue(BetweenTickPackets.supports(packet));
            }
        }
    }

    @Test void rawPredictionsDoNotChangeServerStateInEitherMode() {
        for (boolean enabled : new boolean[]{true, false}) {
            for (DataPacket packet : packets()) {
                try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(enabled, true)) {
                    f.player.setComponents(4, 5, 6);
                    long epoch = f.player.getMovementEpoch();
                    float speed = f.player.getMovementSpeed();
                    f.deliver(packet);
                    assertEquals(20, f.player.getHealth());
                    assertEquals(speed, f.player.getMovementSpeed());
                    assertEquals(4, f.player.x); assertEquals(5, f.player.y); assertEquals(6, f.player.z);
                    assertEquals(epoch, f.player.getMovementEpoch());
                    assertFalse(f.player.getAdventureSettings().get(AdventureSettings.Type.FLYING));
                    assertTrue(f.sent.isEmpty());
                    verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                    verifyNoInteractions(f.processor);
                    verify(f.player, never()).onUpdate(anyInt());
                    verify(f.player, never()).setHealth(anyFloat());
                    verify(f.player, never()).setMovementSpeed(anyFloat());
                }
            }
        }
    }

    @Test void lifecycleStatesStillOnlyReachOriginalIgnoredEvent() {
        for (String state : List.of("dead", "unspawned", "offline", "no-sequence", "no-processor")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                if (state.equals("dead")) f.player.makeDead();
                if (state.equals("unspawned")) f.player.spawned = false;
                if (state.equals("offline")) doReturn(false).when(f.player).isOnline();
                if (state.equals("no-sequence")) doReturn(false).when(f.player).supportsPacketSequences();
                if (state.equals("no-processor")) when(f.server.getPlayerInputProcessor()).thenReturn(null);
                float health = f.player.getHealth();
                f.deliver(packets().get(1));
                assertEquals(health, f.player.getHealth());
                verify(f.plugins).callEvent(any(DataPacketReceiveEvent.class));
                assertTrue(f.sent.isEmpty());
            }
        }
    }

    @Test void cancellationAndOriginalEntryGuardsRemain() {
        for (String state : List.of("cancel", "uninitialized", "retired")) {
            try (PlayerActionAdmissionTest.Fixture f = new PlayerActionAdmissionTest.Fixture(true, true)) {
                f.cancel = state.equals("cancel");
                if (state.equals("uninitialized")) doReturn(false).when(f.player).isInitialized();
                if (state.equals("retired")) doReturn(false).when(f.player).isAcceptingInputPackets();
                f.deliver(packets().get(1));
                verify(f.plugins, times(state.equals("cancel") ? 1 : 0)).callEvent(any(DataPacketReceiveEvent.class));
                assertTrue(f.sent.isEmpty());
                verifyNoInteractions(f.processor);
            }
        }
    }

    private static List<DataPacket> packets() {
        ClientMovementPredictionSyncPacket12160 old = new ClientMovementPredictionSyncPacket12160();
        old.entityDataFlags = new BitSet(); old.scale = Float.NaN; old.width = Float.POSITIVE_INFINITY;
        old.height = -10; old.movementSpeed = 100; old.health = 200; old.hunger = 100; old.entityUniqueId = -1;
        ClientMovementPredictionSyncPacket12170 current = new ClientMovementPredictionSyncPacket12170();
        current.entityDataFlags = new BitSet(); current.scale = Float.NaN; current.width = Float.POSITIVE_INFINITY;
        current.height = -10; current.movementSpeed = 100; current.health = 200; current.hunger = 100; current.flying = true;
        ClientMovementPredictionSyncPacket12620 newer = new ClientMovementPredictionSyncPacket12620();
        newer.entityDataFlags = new BitSet(); newer.scale = Float.NaN; newer.frictionModifier = -5;
        newer.airDragModifier = 100; newer.bounciness = 99; newer.movementSpeed = 100; newer.flying = true;
        return List.of(old, current, newer);
    }
}
