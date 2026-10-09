package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.Level;
import cn.nukkit.item.Item;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.LevelChunkPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.network.protocol.SubChunkPacket;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol11920.protocol.NetworkChunkPublisherUpdatePacket11920;
import org.itxtech.synapseapi.network.protocol.PacketSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MovementSendAdmissionTest {
    @BeforeAll
    static void initializeProtocol() {
        Block.init();
        Item.init();
        Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            assertTrue(AbstractProtocol.PROTOCOL_121_20.getProtocolStart() > 0);
        }
    }

    @Test
    void nestedHideQueuesRemovalButRejectsTheOuterPosition() {
        Fixture fixture = new Fixture(true, true);
        fixture.onMove(event -> fixture.recipient.hidePlayer(fixture.actor));

        assertFalse(fixture.recipient.dataPacket(fixture.movement()));

        List<DataPacket> packets = fixture.sent(1);
        assertInstanceOf(RemoveEntityPacket.class, packets.getFirst());
        assertEquals(fixture.actor.getId(), ((RemoveEntityPacket) packets.getFirst()).eid);
        verify(fixture.plugins, times(2)).callEvent(any(DataPacketSendEvent.class));
    }

    @Test
    void aNestedReplacementDropsEachOwnedSequenceOnce() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger droppedInner = new AtomicInteger();
        AtomicInteger droppedOuter = new AtomicInteger();
        AtomicInteger appended = new AtomicInteger();
        fixture.onMove(event -> {
            fixture.recipient.hidePlayer(fixture.actor);
            PacketSequence inner = new PacketSequence(List.of(event.getPacket()), appended::incrementAndGet, droppedInner::incrementAndGet);
            event.setPacket(new PacketSequence(List.of(inner), appended::incrementAndGet, droppedOuter::incrementAndGet));
        });

        assertFalse(fixture.recipient.dataPacket(fixture.movement()));
        assertInstanceOf(RemoveEntityPacket.class, fixture.sent(1).getFirst());
        assertEquals(1, droppedInner.get());
        assertEquals(1, droppedOuter.get());
        assertEquals(0, appended.get());
    }

    @Test
    void aCurrentReplacementIsAdmittedWithoutEarlyLifecycleCallbacks() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger dropped = new AtomicInteger();
        AtomicInteger appended = new AtomicInteger();
        fixture.onMove(event -> event.setPacket(new PacketSequence(List.of(event.getPacket()),
                appended::incrementAndGet, dropped::incrementAndGet)));

        assertTrue(fixture.recipient.dataPacket(fixture.movement()));
        assertInstanceOf(PacketSequence.class, fixture.sent(1).getFirst());
        assertEquals(0, dropped.get());
        assertEquals(0, appended.get());
        verify(fixture.plugins, times(1)).callEvent(any(DataPacketSendEvent.class));
    }

    @Test
    void independentRemovalReplacementRemainsAnExplicitCommand() {
        Fixture fixture = new Fixture(true, true);
        fixture.onMove(event -> {
            fixture.recipient.hidePlayer(fixture.actor);
            RemoveEntityPacket removal = new RemoveEntityPacket();
            removal.eid = fixture.actor.getId();
            event.setPacket(removal);
        });

        assertTrue(fixture.recipient.dataPacket(fixture.movement()));
        for (DataPacket packet : fixture.sent(2)) assertInstanceOf(RemoveEntityPacket.class, packet);
    }

    @Test
    void independentSelfCorrectionIsNotDroppedWithTheHiddenActor() {
        Fixture fixture = new Fixture(true, true);
        fixture.onMove(event -> {
            fixture.recipient.hidePlayer(fixture.actor);
            MovePlayerPacket correction = new MovePlayerPacket();
            correction.eid = SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID;
            correction.mode = MovePlayerPacket.MODE_TELEPORT;
            event.setPacket(correction);
        });

        assertTrue(fixture.recipient.dataPacket(fixture.movement()));
        List<DataPacket> packets = fixture.sent(2);
        assertInstanceOf(RemoveEntityPacket.class, packets.getFirst());
        MovePlayerPacket correction = assertInstanceOf(MovePlayerPacket.class, packets.getLast());
        assertEquals(SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID, correction.eid);
        assertEquals(MovePlayerPacket.MODE_TELEPORT, correction.mode);
    }

    @Test
    void eitherDisabledSideKeepsTheOriginalSendOrdering() {
        for (boolean[] modes : new boolean[][]{{false, false}, {false, true}, {true, false}}) {
            Fixture fixture = new Fixture(modes[0], modes[1]);
            fixture.onMove(event -> fixture.recipient.hidePlayer(fixture.actor));
            assertTrue(fixture.recipient.dataPacket(fixture.movement()));
            List<DataPacket> packets = fixture.sent(2);
            assertInstanceOf(RemoveEntityPacket.class, packets.getFirst());
            assertInstanceOf(MovePlayerPacket.class, packets.getLast());
        }
    }

    @Test
    void cancellingAReplacementStillDiscardsWithoutEnqueueing() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger dropped = new AtomicInteger();
        fixture.onMove(event -> {
            event.setPacket(new PacketSequence(List.of(event.getPacket()), () -> { }, dropped::incrementAndGet));
            event.setCancelled();
        });

        assertFalse(fixture.recipient.dataPacket(fixture.movement()));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertEquals(1, dropped.get());
    }

    @Test
    void nestedTeleportRetainsNewViewAndDropsOldRemoval() {
        Fixture fixture = new Fixture(true, true);
        fixture.onRemove(event -> fixture.recipient.observedEpoch++);
        fixture.actor.despawnFrom(fixture.recipient);
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertSame(fixture.recipient, fixture.actor.getViewers().get(fixture.recipient.getLoaderId()));
    }

    @Test
    void changedRecipientWorldDropsOldRemovalWithoutUsingPositionData() {
        Fixture fixture = new Fixture(true, true);
        Level replacement = mock(Level.class);
        fixture.onRemove(event -> fixture.recipient.setLevel(replacement));
        fixture.actor.despawnFrom(fixture.recipient);
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void removalWrappedByCallbackIsDiscardedExactlyOnce() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger dropped = new AtomicInteger();
        fixture.onRemove(event -> {
            fixture.recipient.observedEpoch++;
            event.setPacket(new PacketSequence(List.of(event.getPacket()), () -> {}, dropped::incrementAndGet));
        });
        fixture.actor.despawnFrom(fixture.recipient);
        assertEquals(1, dropped.get());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertSame(fixture.recipient, fixture.actor.getViewers().get(fixture.recipient.getLoaderId()));
    }

    @Test
    void callbackRetirementInvalidatesOuterRemoval() {
        Fixture fixture = new Fixture(true, true);
        fixture.onRemove(event -> fixture.recipient.bindInputSession(null));
        fixture.actor.despawnFrom(fixture.recipient);
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertTrue(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void cleanupStartedAfterRetirementStillSendsRemoval() {
        Fixture fixture = new Fixture(true, true);
        fixture.recipient.bindInputSession(null);
        fixture.actor.despawnFrom(fixture.recipient);
        assertInstanceOf(RemoveEntityPacket.class, fixture.sent(1).getFirst());
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void disabledRemovalRetainsLegacyOrderAcrossEpochChange() {
        Fixture fixture = new Fixture(true, false);
        fixture.onRemove(event -> fixture.recipient.observedEpoch++);
        fixture.actor.despawnFrom(fixture.recipient);
        assertInstanceOf(RemoveEntityPacket.class, fixture.sent(1).getFirst());
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void packetCancellationWithoutViewChangeKeepsLegacyUntracking() {
        Fixture fixture = new Fixture(true, true);
        fixture.onRemove(event -> event.setCancelled());
        fixture.actor.despawnFrom(fixture.recipient);
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void recursiveRemovalDoesNotReenterItsOwnSendEvent() {
        Fixture fixture = new Fixture(true, true);
        fixture.onRemove(event -> fixture.actor.despawnFrom(fixture.recipient));
        fixture.actor.despawnFrom(fixture.recipient);
        assertInstanceOf(RemoveEntityPacket.class, fixture.sent(1).getFirst());
        verify(fixture.plugins, times(1)).callEvent(any(DataPacketSendEvent.class));
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void offThreadRemovalKeepsLegacyConcurrentTrackingPath() {
        Fixture fixture = new Fixture(true, true);
        when(fixture.server.isPrimaryThread()).thenReturn(false);
        fixture.onRemove(event -> fixture.recipient.observedEpoch++);
        fixture.actor.despawnFrom(fixture.recipient);
        assertInstanceOf(RemoveEntityPacket.class, fixture.sent(1).getFirst());
        assertFalse(fixture.actor.getViewers().containsKey(fixture.recipient.getLoaderId()));
    }

    @Test
    void terrainFamiliesCannotCrossAWorldChangeInTheSendEvent() {
        for (DataPacket packet : List.of(new LevelChunkPacket(), new SubChunkPacket(),
                new BlockEntityDataPacket(), new NetworkChunkPublisherUpdatePacket11920())) {
            Fixture fixture = new Fixture(true, true);
            Level target = mock(Level.class);
            fixture.onPacket(event -> fixture.recipient.setLevel(target));
            assertFalse(fixture.recipient.dataPacket(packet));
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void cachedTerrainBatchUsesTracksWithoutDecompressingItsBytes() {
        Fixture fixture = new Fixture(true, true);
        Level target = mock(Level.class);
        BatchPacket packet = new BatchPacket();
        packet.payload = new byte[]{1, 2, 3};
        packet.tracks = new BatchPacket.Track[]{new BatchPacket.Track(ProtocolInfo.SUB_CHUNK_PACKET, 3)};
        fixture.onPacket(event -> fixture.recipient.setLevel(target));
        assertFalse(fixture.recipient.dataPacket(packet));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void nonTerrainBatchAndUnknownBatchRetainTheirOriginalBehavior() {
        for (BatchPacket.Track[] tracks : new BatchPacket.Track[][]{
                null, {new BatchPacket.Track(ProtocolInfo.TEXT_PACKET, 3)}}) {
            Fixture fixture = new Fixture(true, true);
            Level target = mock(Level.class);
            BatchPacket packet = new BatchPacket(); packet.payload = new byte[]{1, 2, 3}; packet.tracks = tracks;
            fixture.onPacket(event -> fixture.recipient.setLevel(target));
            assertTrue(fixture.recipient.dataPacket(packet));
            BatchPacket sent = assertInstanceOf(BatchPacket.class, fixture.sent(1).getFirst());
            assertArrayEquals(packet.payload, sent.payload);
            assertArrayEquals(packet.tracks, sent.tracks);
        }
    }

    @Test
    void clonedTerrainInAReplacementSequenceIsDiscardedOnce() {
        Fixture fixture = new Fixture(true, true);
        Level target = mock(Level.class);
        AtomicInteger discarded = new AtomicInteger();
        fixture.onPacket(event -> {
            fixture.recipient.setLevel(target);
            event.setPacket(new PacketSequence(List.of(event.getPacket().clone()), () -> {}, discarded::incrementAndGet));
        });
        assertFalse(fixture.recipient.dataPacket(new SubChunkPacket()));
        assertEquals(1, discarded.get());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void anOriginalTerrainSequenceHasTheSameWorldBoundary() {
        Fixture fixture = new Fixture(true, true);
        Level target = mock(Level.class);
        AtomicInteger discarded = new AtomicInteger();
        PacketSequence packet = new PacketSequence(List.of(new SubChunkPacket()), () -> {}, discarded::incrementAndGet);
        fixture.onPacket(event -> fixture.recipient.setLevel(target));
        assertFalse(fixture.recipient.dataPacket(packet));
        assertEquals(1, discarded.get());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void anIndependentTextReplacementIsNotAnOldTerrainPacket() {
        Fixture fixture = new Fixture(true, true);
        Level target = mock(Level.class);
        TextPacket replacement = new TextPacket(); replacement.message = "Independent response";
        fixture.onPacket(event -> { fixture.recipient.setLevel(target); event.setPacket(replacement); });
        assertTrue(fixture.recipient.dataPacket(new SubChunkPacket()));
        assertSame(replacement, fixture.sent(1).getFirst());
    }

    @Test
    void aTypedIndependentBatchReplacementDiffersFromAnOpaqueReplacement() {
        for (boolean typed : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(true, true);
            Level target = mock(Level.class);
            BatchPacket replacement = new BatchPacket(); replacement.payload = new byte[]{1, 2, 3};
            if (typed) replacement.tracks = new BatchPacket.Track[]{new BatchPacket.Track(ProtocolInfo.TEXT_PACKET, 3)};
            fixture.onPacket(event -> { fixture.recipient.setLevel(target); event.setPacket(replacement); });
            assertEquals(typed, fixture.recipient.dataPacket(new SubChunkPacket()));
            if (typed) assertSame(replacement, fixture.sent(1).getFirst());
            else verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void aSameWorldPositionEpochDoesNotInvalidateTerrainContent() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.recipient.observedEpoch++);
        assertTrue(fixture.recipient.dataPacket(new SubChunkPacket()));
        assertInstanceOf(SubChunkPacket.class, fixture.sent(1).getFirst());
    }

    @Test
    void terrainSendDetectsRetirementButAllowsAlreadyRetiredCleanup() {
        Fixture changed = new Fixture(true, true);
        changed.onPacket(event -> changed.recipient.bindInputSession(null));
        assertFalse(changed.recipient.dataPacket(new SubChunkPacket()));
        verify(changed.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        Fixture cleanup = new Fixture(true, true);
        cleanup.recipient.bindInputSession(null);
        assertTrue(cleanup.recipient.dataPacket(new SubChunkPacket()));
        assertInstanceOf(SubChunkPacket.class, cleanup.sent(1).getFirst());
    }

    @Test
    void disabledOrOffThreadTerrainKeepsTheOldSendPath() {
        for (boolean enabled : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(true, enabled);
            when(fixture.server.isPrimaryThread()).thenReturn(!enabled);
            Level target = mock(Level.class);
            fixture.onPacket(event -> fixture.recipient.setLevel(target));
            assertTrue(fixture.recipient.dataPacket(new SubChunkPacket()));
            assertInstanceOf(SubChunkPacket.class, fixture.sent(1).getFirst());
        }
    }

    @Test
    void cancelledOldChunkCannotContinueSpawningFromTheNewWorld() {
        Fixture fixture = new Fixture(true, true);
        Level target = mock(Level.class);
        fixture.onPacket(event -> fixture.recipient.setLevel(target));
        fixture.recipient.sendChunk(0, 8, 8, 1, null, new LevelChunkPacket());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        verify(target, never()).getChunkBlockEntities(anyInt(), anyInt());
        verify(target, never()).getChunkEntities(anyInt(), anyInt());
    }

    private static final class Fixture {
        private final Server server = mock(Server.class);
        private final PluginManager plugins = mock(PluginManager.class);
        private final Level level = mock(Level.class);
        private final SourceInterface transport = mock(SourceInterface.class);
        private final ProbePlayer actor;
        private final ProbePlayer recipient;

        private Fixture(boolean actorEnabled, boolean recipientEnabled) {
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));
            when(server.getDefaultLevel()).thenReturn(level);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getViewDistance()).thenReturn(16);
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                global.when(Server::getInstance).thenReturn(server);
                actor = new ProbePlayer(transport, actorEnabled);
                recipient = new ProbePlayer(transport, recipientEnabled);
                recipient.bindInputSession(new ServerInputDispatcher.Session(recipient.getSessionId()));
            }
            actor.getViewers().put(recipient.getLoaderId(), recipient);
            when(level.getEntity(actor.getId())).thenReturn(actor);
        }

        private void onMove(Consumer<DataPacketSendEvent> callback) {
            doAnswer(invocation -> {
                DataPacketSendEvent event = invocation.getArgument(0);
                if (event.getPacket() instanceof MovePlayerPacket movement && movement.eid == actor.getId()) {
                    callback.accept(event);
                }
                return null;
            }).when(plugins).callEvent(any(DataPacketSendEvent.class));
        }

        private void onPacket(Consumer<DataPacketSendEvent> callback) {
            doAnswer(invocation -> {
                callback.accept(invocation.getArgument(0));
                return null;
            }).when(plugins).callEvent(any(DataPacketSendEvent.class));
        }

        private void onRemove(Consumer<DataPacketSendEvent> callback) {
            doAnswer(call -> {
                DataPacketSendEvent event = call.getArgument(0);
                if (event.getPacket() instanceof RemoveEntityPacket) callback.accept(event);
                return null;
            }).when(plugins).callEvent(any(DataPacketSendEvent.class));
        }

        private MovePlayerPacket movement() {
            MovePlayerPacket packet = new MovePlayerPacket();
            packet.eid = actor.getId();
            return packet;
        }

        private SetEntityDataPacket flags(Player owner, int property) {
            owner.getDataProperties().putLong(property, 0);
            SetEntityDataPacket packet = new SetEntityDataPacket();
            packet.eid = owner.getId();
            packet.metadata = new EntityMetadata().putLong(property, 0);
            return packet;
        }

        private List<DataPacket> sent(int count) {
            ArgumentCaptor<DataPacket> packets = ArgumentCaptor.forClass(DataPacket.class);
            verify(transport, times(count)).putPacket(eq(recipient), packets.capture());
            return packets.getAllValues();
        }
    }

    @Test
    void actorFlagsChangedInCallbackRejectTheOldMetadata() {
        Fixture fixture = new Fixture(true, true);
        SetEntityDataPacket packet = fixture.flags(fixture.actor, Entity.DATA_FLAGS);
        fixture.onPacket(event -> fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8));
        assertFalse(fixture.recipient.dataPacket(packet));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void selfFlagsChangedInCallbackRejectTheReplacedLocalEntityId() {
        Fixture fixture = new Fixture(true, true);
        SetEntityDataPacket packet = fixture.flags(fixture.recipient, Entity.DATA_FLAGS);
        fixture.onPacket(event -> fixture.recipient.getDataProperties().putLong(Entity.DATA_FLAGS, 8));
        assertFalse(fixture.recipient.dataPacket(packet));
    }

    @Test
    void extendedAndThirdFlagGroupsAreIndependentlyProtected() {
        for (int property : new int[]{Entity.DATA_FLAGS_EXTENDED, Entity.DATA_FLAGS_3}) {
            Fixture fixture = new Fixture(true, true);
            SetEntityDataPacket packet = fixture.flags(fixture.actor, property);
            fixture.onPacket(event -> fixture.actor.getDataProperties().putLong(property, 1));
            assertFalse(fixture.recipient.dataPacket(packet));
        }
    }

    @Test
    void unchangedAuthorityAllowsAViewSpecificFlagOverlay() {
        Fixture fixture = new Fixture(true, true);
        SetEntityDataPacket packet = fixture.flags(fixture.actor, Entity.DATA_FLAGS);
        packet.metadata.putLong(Entity.DATA_FLAGS, 1);
        assertTrue(fixture.recipient.dataPacket(packet));
        assertInstanceOf(SetEntityDataPacket.class, fixture.sent(1).getFirst());
    }

    @Test
    void unrelatedMetadataChangesDoNotInvalidateThisFlagGroup() {
        Fixture fixture = new Fixture(true, true);
        SetEntityDataPacket packet = fixture.flags(fixture.actor, Entity.DATA_FLAGS);
        fixture.onPacket(event -> fixture.actor.getDataProperties().putString(Entity.DATA_NAMETAG, "Updated"));
        assertTrue(fixture.recipient.dataPacket(packet));
    }

    @Test
    void recipientEpochChangeCannotDeliverOldFlags() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.recipient.observedEpoch++);
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void aHideInvalidatesOnlyTheAffectedObserver() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.actor.getViewers().remove(fixture.recipient.getLoaderId()));
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void aNestedSequenceCannotHideTheOldFlagsAndIsDiscardedOnce() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger discarded = new AtomicInteger();
        fixture.onPacket(event -> {
            fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8);
            event.setPacket(new PacketSequence(List.of(event.getPacket()), () -> {}, discarded::incrementAndGet));
        });
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
        assertEquals(1, discarded.get());
    }

    @Test
    void anIndependentTextReplacementKeepsItsOwnSemantics() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> {
            fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8);
            event.setPacket(new TextPacket());
        });
        assertTrue(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
        assertInstanceOf(TextPacket.class, fixture.sent(1).getFirst());
    }

    @Test
    void disabledEitherSideKeepsTheOriginalMetadataPath() {
        for (boolean actorEnabled : new boolean[]{true,false}) {
            Fixture fixture = new Fixture(actorEnabled, !actorEnabled);
            fixture.onPacket(event -> fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8));
            assertTrue(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
        }
    }

    @Test
    void actorEpochChangeCannotReuseTheOldFlags() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.actor.observedEpoch++);
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void anOpaqueBatchCannotBypassTheFlagSource() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> {
            fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8);
            event.setPacket(new BatchPacket());
        });
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void initialSequencesAlsoCaptureEachMetadataSource() {
        Fixture fixture = new Fixture(true, true);
        SetEntityDataPacket flags = fixture.flags(fixture.actor, Entity.DATA_FLAGS);
        fixture.onPacket(event -> fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8));
        assertFalse(fixture.recipient.dataPacket(new PacketSequence(List.of(new TextPacket(), flags), () -> {}, () -> {})));
    }

    @Test
    void recipientRetirementInvalidatesItsOldFlagsWithoutChangingWorld() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.recipient.bindInputSession(null));
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void actorDeathInvalidatesTheOuterFlagSnapshot() {
        Fixture fixture = new Fixture(true, true);
        fixture.onPacket(event -> fixture.actor.healthy = false);
        assertFalse(fixture.recipient.dataPacket(fixture.flags(fixture.actor, Entity.DATA_FLAGS)));
    }

    @Test
    void observerCallbackCannotRebaseTheSharedOldMetadataForSelf() {
        Fixture fixture = new Fixture(true, true);
        fixture.flags(fixture.actor, Entity.DATA_FLAGS);
        fixture.onPacket(event -> {
            if (event.getPlayer() == fixture.recipient) fixture.actor.getDataProperties().putLong(Entity.DATA_FLAGS, 8);
        });
        fixture.actor.sendData(new Player[]{fixture.recipient}, new EntityMetadata().putLong(Entity.DATA_FLAGS, 0));
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void motionChangedDuringObserverSendDropsTheOuterSequenceOnce() {
        Fixture fixture = new Fixture(true, true);
        AtomicInteger dropped = new AtomicInteger();
        SetEntityMotionPacket motion = new SetEntityMotionPacket();
        motion.eid = fixture.actor.getId();
        fixture.onPacket(event -> {
            fixture.actor.motionX = -0.03;
            event.setPacket(new PacketSequence(List.of(event.getPacket()), () -> {}, dropped::incrementAndGet));
        });
        assertFalse(fixture.recipient.dataPacket(motion));
        assertEquals(1, dropped.get());
        verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
    }

    @Test
    void selfMotionSourceSurvivesEntityIdReplacementButNotANewerMotion() {
        Fixture fixture = new Fixture(true, true);
        SetEntityMotionPacket motion = new SetEntityMotionPacket();
        motion.eid = fixture.recipient.getId();
        fixture.onPacket(event -> fixture.recipient.motionY = 0.1);
        assertFalse(fixture.recipient.dataPacket(motion));
    }

    @Test
    void independentSelfMotionReplacementDoesNotInheritTheObserversOldSource() {
        Fixture fixture = new Fixture(true, true);
        SetEntityMotionPacket motion = new SetEntityMotionPacket();
        motion.eid = fixture.actor.getId();
        fixture.onPacket(event -> {
            fixture.actor.motionX = -0.03;
            SetEntityMotionPacket independent = new SetEntityMotionPacket();
            independent.eid = fixture.recipient.getLocalEntityId();
            event.setPacket(independent);
        });
        assertTrue(fixture.recipient.dataPacket(motion));
        assertInstanceOf(SetEntityMotionPacket.class, fixture.sent(1).getFirst());
    }

    @Test
    void disabledMotionPublicationKeepsItsPreviousEventBehavior() {
        Fixture fixture = new Fixture(true, false);
        SetEntityMotionPacket motion = new SetEntityMotionPacket();
        motion.eid = fixture.actor.getId();
        fixture.onPacket(event -> fixture.actor.motionX = -0.03);
        assertTrue(fixture.recipient.dataPacket(motion));
    }

    // 仅离线发送入口夹具，不注册实体、不连接网络、不创建运行时测试玩家。
    private static final class ProbePlayer extends SynapsePlayer {
        private final boolean enabled;
        private long observedEpoch;
        private boolean healthy = true;

        private ProbePlayer(SourceInterface transport, boolean enabled) {
            super(transport, null, 0L, InetSocketAddress.createUnresolved("localhost", 0));
            this.enabled = enabled;
            this.isSynapseLogin = true;
            this.sessionId = UUID.randomUUID();
            this.setUniqueId(UUID.randomUUID());
            this.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }

        @Override public boolean isMainThreadInputEnabled() { return this.enabled; }
        @Override public long getMovementEpoch() { return this.observedEpoch; }
        @Override public boolean isOnline() { return true; }
        @Override public boolean isAlive() { return this.healthy; }
        @Override public boolean isNetEaseClient() { return false; }
        @Override public int getEntityViewDistance() { return 16; }
        @Override protected float getBaseOffset() { return 1.62f; }
    }
}
