package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.data.ServerConfiguration;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.dialogue.NPCDialoguePlayerHandler;
import org.itxtech.synapseapi.dialogue.NPCDialogueScene;
import org.itxtech.synapseapi.dialogue.NPCDialogueState;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol11710.protocol.NpcDialoguePacket11710;
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
import static org.mockito.Mockito.*;

class NpcDialogueSendOwnershipTest {
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
    void cancelledOuterOpenCannotOverwriteNestedScene() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneB, event -> {
                event.setCancelled();
                fixture.handler().openDialogue(fixture.sceneC);
            });
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void cancelledOuterCloseCannotClearNestedScene() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneA, event -> {
                event.setCancelled();
                fixture.handler().openDialogue(fixture.sceneC);
            });
            fixture.handler().closeDialogue();
            assertNotNull(fixture.handler().getState());
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void uncancelledReentryDropsOnlyStaleOuterPacket() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneB, event -> fixture.handler().openDialogue(fixture.sceneC));
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void firstOpenReentryProtectsTheNullStateSource() {
        try (Fixture fixture = new Fixture(true, false)) {
            fixture.onScene(fixture.sceneB, event -> fixture.handler().openDialogue(fixture.sceneC, fixture.entity, "nested"));
            fixture.handler().openDialogue(fixture.sceneB, fixture.entity, "outer");
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void callbackRetirementRejectsOuterOpenBeforeTransport() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            fixture.onScene(fixture.sceneB, event -> fixture.player.bindInputSession(null));
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(original, fixture.handler().getState());
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void closeRetirementPreventsTheFollowingOtherHostOpen() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            fixture.onScene(fixture.sceneA, event -> fixture.player.bindInputSession(null));
            Entity other = mock(Entity.class);
            when(other.getId()).thenReturn(8L);
            fixture.handler().openDialogue(fixture.sceneB, other, "other");
            assertSame(original, fixture.handler().getState());
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
            verify(fixture.plugins, times(1)).callEvent(any(DataPacketSendEvent.class));
        }
    }

    @Test
    void nestedCloseSuccessorPreventsTheFollowingOtherHostOpen() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneA, event -> fixture.handler().openDialogue(fixture.sceneC));
            Entity other = mock(Entity.class);
            when(other.getId()).thenReturn(8L);
            fixture.handler().openDialogue(fixture.sceneB, other, "other");
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void seamlessOpenCannotWriteAfterCallbackRetirement() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            fixture.onScene(fixture.sceneB, event -> fixture.player.bindInputSession(null));
            fixture.handler().openDialogueSeamless(fixture.sceneB, fixture.entity, "next");
            assertSame(original, fixture.handler().getState());
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void anAlreadyRetiredHandlerStartsNoSendEvent() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            fixture.player.bindInputSession(null);
            fixture.handler().openDialogue(fixture.sceneB);
            fixture.handler().openDialogueSeamless(fixture.sceneB, fixture.entity, "next");
            fixture.handler().closeDialogue();
            assertSame(original, fixture.handler().getState());
            verify(fixture.plugins, never()).callEvent(any(DataPacketSendEvent.class));
        }
    }

    @Test
    void cancellationAloneKeepsOriginalApiBookkeeping() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneB, DataPacketSendEvent::setCancelled);
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(fixture.sceneB, fixture.handler().getState().currentScene());
            fixture.handler().closeDialogue();
            assertNull(fixture.handler().getState());
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void nestedSameSourceSequenceIsDiscardedOnce() {
        try (Fixture fixture = new Fixture(true)) {
            AtomicInteger innerDropped = new AtomicInteger();
            AtomicInteger outerDropped = new AtomicInteger();
            AtomicInteger appended = new AtomicInteger();
            fixture.onScene(fixture.sceneB, event -> {
                fixture.handler().openDialogue(fixture.sceneC);
                PacketSequence inner = new PacketSequence(List.of(event.getPacket()), appended::incrementAndGet, innerDropped::incrementAndGet);
                event.setPacket(new PacketSequence(List.of(inner), appended::incrementAndGet, outerDropped::incrementAndGet));
            });
            fixture.handler().openDialogue(fixture.sceneB);
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
            assertEquals(1, innerDropped.get());
            assertEquals(1, outerDropped.get());
            assertEquals(0, appended.get());
        }
    }

    @Test
    void independentReplacementDoesNotInheritNpcPacketSource() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            fixture.onScene(fixture.sceneB, event -> {
                fixture.player.bindInputSession(null);
                event.setPacket(new TextPacket());
            });
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(original, fixture.handler().getState());
            assertInstanceOf(TextPacket.class, fixture.sent(1).getFirst());
        }
    }

    @Test
    void activePositionEpochChangeKeepsNotification() {
        try (Fixture fixture = new Fixture(true)) {
            fixture.onScene(fixture.sceneB, event -> fixture.player.observedEpoch++);
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(fixture.sceneB, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneB.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void disabledModeKeepsLegacyNestedOpenOverwrite() {
        try (Fixture fixture = new Fixture(false)) {
            fixture.onScene(fixture.sceneB, event -> {
                event.setCancelled();
                fixture.handler().openDialogue(fixture.sceneC);
            });
            fixture.handler().openDialogue(fixture.sceneB);
            assertSame(fixture.sceneB, fixture.handler().getState().currentScene());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
        }
    }

    @Test
    void eventFailureDoesNotCommitAStateOrEnqueuePacket() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialogueState original = fixture.handler().getState();
            IllegalStateException failure = new IllegalStateException("Probe send failed");
            fixture.onScene(fixture.sceneB, event -> { throw failure; });
            assertSame(failure, assertThrows(IllegalStateException.class, () -> fixture.handler().openDialogue(fixture.sceneB)));
            assertSame(original, fixture.handler().getState());
            verify(fixture.transport, never()).putPacket(any(Player.class), any(DataPacket.class));
        }
    }

    @Test
    void aReplacedHandlerOwnsItsNewSceneAndOldHandlerStops() {
        try (Fixture fixture = new Fixture(true)) {
            NPCDialoguePlayerHandler old = fixture.handler();
            NPCDialogueState original = old.getState();
            fixture.onScene(fixture.sceneB, event -> {
                fixture.player.dialogueHandler = new NPCDialoguePlayerHandler(fixture.player);
                fixture.handler().openDialogue(fixture.sceneC, fixture.entity, "new owner");
            });
            old.openDialogue(fixture.sceneB);
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
            assertSame(original, old.getState());
            assertEquals(fixture.sceneC.getSceneName(), fixture.sentDialogue(1).sceneName);
            old.closeDialogue();
            assertSame(fixture.sceneC, fixture.handler().getState().currentScene());
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final Server server = mock(Server.class);
        private final PluginManager plugins = mock(PluginManager.class);
        private final SourceInterface transport = mock(SourceInterface.class);
        private final Entity entity = mock(Entity.class);
        private final NPCDialogueScene sceneA = new NPCDialogueScene("A");
        private final NPCDialogueScene sceneB = new NPCDialogueScene("B");
        private final NPCDialogueScene sceneC = new NPCDialogueScene("C");
        private final ProbePlayer player;
        private final MockedStatic<SynapseAPI> apis;

        private Fixture(boolean enabled) { this(enabled, true); }

        private Fixture(boolean enabled, boolean initialize) {
            when(server.getConfiguration()).thenReturn(mock(ServerConfiguration.class));
            when(server.getDefaultLevel()).thenReturn(mock(Level.class));
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getViewDistance()).thenReturn(16);
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                global.when(Server::getInstance).thenReturn(server);
                player = new ProbePlayer(transport, enabled);
                player.bindInputSession(new ServerInputDispatcher.Session(player.getSessionId()));
            }
            SynapseAPI api = mock(SynapseAPI.class);
            when(api.getLogger()).thenReturn(mock(PluginLogger.class));
            apis = mockStatic(SynapseAPI.class);
            apis.when(SynapseAPI::getInstance).thenReturn(api);
            when(entity.getId()).thenReturn(7L);
            if (initialize) handler().openDialogue(sceneA, entity, "host");
            clearInvocations(transport, plugins);
        }

        private NPCDialoguePlayerHandler handler() { return player.getNpcDialoguePlayerHandler(); }

        private void onScene(NPCDialogueScene scene, Consumer<DataPacketSendEvent> callback) {
            doAnswer(call -> {
                DataPacketSendEvent event = call.getArgument(0);
                if (event.getPacket() instanceof NpcDialoguePacket11710 packet && packet.sceneName.equals(scene.getSceneName())) callback.accept(event);
                return null;
            }).when(plugins).callEvent(any(DataPacketSendEvent.class));
        }

        private List<DataPacket> sent(int count) {
            ArgumentCaptor<DataPacket> packets = ArgumentCaptor.forClass(DataPacket.class);
            verify(transport, times(count)).putPacket(eq(player), packets.capture());
            return packets.getAllValues();
        }

        private NpcDialoguePacket11710 sentDialogue(int count) { return assertInstanceOf(NpcDialoguePacket11710.class, sent(count).getLast()); }

        @Override
        public void close() { apis.close(); }
    }

    private static final class ProbePlayer extends SynapsePlayer {
        private final boolean enabled;
        private long observedEpoch;
        private NPCDialoguePlayerHandler dialogueHandler = new NPCDialoguePlayerHandler(this);

        private ProbePlayer(SourceInterface transport, boolean enabled) {
            super(transport, null, 0L, InetSocketAddress.createUnresolved("localhost", 0));
            this.enabled = enabled;
            this.isSynapseLogin = true;
            this.sessionId = UUID.randomUUID();
            this.setUniqueId(UUID.randomUUID());
            this.protocol = AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        }

        @Override public boolean isMainThreadInputEnabled() { return enabled; }
        @Override public NPCDialoguePlayerHandler getNpcDialoguePlayerHandler() { return dialogueHandler; }
        @Override public long getMovementEpoch() { return observedEpoch; }
        @Override public boolean isOnline() { return true; }
        @Override public boolean isNetEaseClient() { return false; }
    }
}
