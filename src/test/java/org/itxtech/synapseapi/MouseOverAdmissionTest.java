package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.math.Vector3f;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.InteractPacket121130;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityInteractable;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.event.Event;
import cn.nukkit.event.player.PlayerMouseOverEntityEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MouseOverAdmissionTest {
    @BeforeAll
    static void initializeProtocol() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_121_130.getProtocolStart();
        }
    }

    @Test
    void modernMouseOverKeepsOtherActionsAsBarriers() {
        for (int action = InteractPacket121130.ACTION_NONE; action <= InteractPacket121130.ACTION_OPEN_INVENTORY; action++) {
            assertEquals(action == InteractPacket121130.ACTION_MOUSEOVER, BetweenTickPackets.supports(modernPacket(action, 0)));
        }
    }

    @Test
    void modernSelectAndClearRunWholeBeforeFollowingInput() {
        Fixture fixture = new Fixture(true, true);
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        InteractPacket121130 select = modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 1);
        InteractPacket121130 clear = modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 0);
        assertTrue(BetweenTickPackets.supports(select));
        assertTrue(BetweenTickPackets.canRunBetweenTicks(fixture.player, select));
        dispatcher.offer(session, context -> { assertTrue(context.betweenTicks()); fixture.handle(select); }, 1, true);
        dispatcher.offer(session, context -> { assertSame(fixture.target, fixture.player.looked()); fixture.handle(clear); }, 1, true);
        dispatcher.offer(session, context -> { assertNull(fixture.player.looked()); fixture.events.add("follower"); }, 1, true);
        assertEquals(3, dispatcher.drain(true, 1, Long.MAX_VALUE));
        assertEquals(List.of("receive", "mouseover", "receive", "follower"), fixture.events);
        assertEquals("Hand prompt", fixture.player.button);
        verify(fixture.processor, times(2)).validateReceivedPacket(eq(fixture.player), any(), anyBoolean());
    }

    @Test
    void modernCancellationAndRetirementKeepOriginalRejection() {
        for (boolean retire : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(true, true);
            fixture.cancel = !retire;
            fixture.retire = retire;
            fixture.handle(modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 1));
            assertNull(fixture.player.looked());
            assertEquals("Initial", fixture.player.button);
            assertEquals(List.of("receive"), fixture.events);
            verify(fixture.processor, times(retire ? 0 : 1)).validateReceivedPacket(eq(fixture.player), any(), eq(true));
        }
    }

    @Test
    void modernDisabledAndMissingTargetKeepOriginalHandler() {
        Fixture fixture = new Fixture(false, true);
        fixture.handle(modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 1));
        assertSame(fixture.target, fixture.player.looked());
        fixture.handle(modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 0));
        assertNull(fixture.player.looked());
        when(fixture.level.getEntity(1)).thenReturn(null);
        fixture.handle(modernPacket(InteractPacket121130.ACTION_MOUSEOVER, 1));
        assertEquals(List.of("receive", "mouseover", "receive", "receive"), fixture.events);
        verifyNoInteractions(fixture.processor);
    }

    @Test
    void modernDecoderPreservesOptionalPositionAndTarget() {
        for (boolean position : new boolean[]{false, true}) {
            BinaryStream wire = new BinaryStream();
            wire.putByte((byte) InteractPacket121130.ACTION_MOUSEOVER);
            wire.putEntityRuntimeId(7);
            wire.putBoolean(position);
            if (position) wire.putVector3f(new Vector3f(1, 2, 3));
            InteractPacket121130 packet = new InteractPacket121130();
            packet.setBuffer(wire.getBuffer(), 0);
            packet.decode();
            assertEquals(7, packet.target);
            assertEquals(position, packet.position != null);
            assertTrue(BetweenTickPackets.supports(packet));
        }
    }

    private static InteractPacket121130 modernPacket(int action, long target) {
        InteractPacket121130 packet = new InteractPacket121130();
        packet.action = action;
        packet.target = target;
        return packet;
    }

    @Test
    void onlyMouseOverIsStaticallyEligible() {
        for (int action = InteractPacket113.ACTION_NONE; action <= InteractPacket113.ACTION_OPEN_INVENTORY; action++) {
            InteractPacket113 packet = packet(action, 1);
            assertEquals(action == InteractPacket113.ACTION_MOUSEOVER, BetweenTickPackets.supports(packet));
        }
    }

    @Test
    void anEarlierBarrierStillPreventsMouseOverAndFollowingInputFromOvertaking() {
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024,
                exception -> fail(exception));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        dispatcher.offer(session, context -> order.add("barrier"), 1, BetweenTickPackets.supports(new TextPacket()));
        dispatcher.offer(session, context -> order.add("mouseover"), 1,
                BetweenTickPackets.supports(packet(InteractPacket113.ACTION_MOUSEOVER, 1)));
        dispatcher.offer(session, context -> order.add("following"), 1, true);
        assertEquals(0, dispatcher.drain(true, 1, Long.MAX_VALUE));
        assertEquals(3, dispatcher.drainWorldTick(2));
        assertEquals(List.of("barrier", "mouseover", "following"), order);
    }

    @Test
    void originalHandlerSelectsAndClearsTargetInBothModes() {
        for (boolean enabled : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(enabled);
            fixture.handle(packet(InteractPacket113.ACTION_MOUSEOVER, 1));
            assertSame(fixture.target, fixture.player.looked());
            assertEquals("Target prompt", fixture.player.button);
            assertEquals(List.of("receive", "mouseover"), fixture.events);
            fixture.handle(packet(InteractPacket113.ACTION_MOUSEOVER, 0));
            assertNull(fixture.player.looked());
            assertEquals("Hand prompt", fixture.player.button);
            assertEquals(List.of("receive", "mouseover", "receive"), fixture.events);
            verify(fixture.processor, times(enabled ? 2 : 0)).validateReceivedPacket(eq(fixture.player), any(), anyBoolean());
        }
    }

    @Test
    void cancelledPacketDoesNotChangePromptOrInvokeNpcHook() {
        Fixture fixture = new Fixture(true);
        fixture.cancel = true;
        fixture.handle(packet(InteractPacket113.ACTION_MOUSEOVER, 1));
        assertNull(fixture.player.looked());
        assertEquals("Initial", fixture.player.button);
        assertEquals(List.of("receive"), fixture.events);
        verify(fixture.processor).validateReceivedPacket(eq(fixture.player), any(), eq(true));
    }

    @Test
    void retirementInReceiveCallbackStopsTheOldInput() {
        Fixture fixture = new Fixture(true);
        fixture.retire = true;
        fixture.handle(packet(InteractPacket113.ACTION_MOUSEOVER, 1));
        assertNull(fixture.player.looked());
        assertEquals("Initial", fixture.player.button);
        assertEquals(List.of("receive"), fixture.events);
        verifyNoInteractions(fixture.processor);
    }

    @Test
    void missingTargetDoesNotInvokeNpcHook() {
        Fixture fixture = new Fixture(true);
        when(fixture.level.getEntity(1)).thenReturn(null);
        fixture.handle(packet(InteractPacket113.ACTION_MOUSEOVER, 1));
        assertNull(fixture.player.looked());
        assertEquals(List.of("receive"), fixture.events);
    }

    @Test
    void lifecycleRejectionRunsWithoutHoldingFollowingInput() {
        for (boolean modern : new boolean[]{false, true}) {
            for (boolean enabled : new boolean[]{false, true}) {
                for (String state : List.of("dead", "unspawned", "uninitialized", "retired")) {
                    Fixture fixture = new Fixture(enabled, modern);
                    fixture.handle(hover(modern, 1));
                    assertSame(fixture.target, fixture.player.looked());
                    fixture.events.clear();
                    clearInvocations(fixture.processor, fixture.plugins);
                    if (state.equals("dead")) doReturn(false).when(fixture.player).isAlive();
                    if (state.equals("unspawned")) fixture.player.spawned = false;
                    if (state.equals("uninitialized")) doReturn(false).when(fixture.player).isInitialized();
                    if (state.equals("retired")) doReturn(false).when(fixture.player).isAcceptingInputPackets();
                    if (!enabled && state.equals("retired")) continue;
                    fixture.deliver(hover(modern, 0));
                    assertSame(fixture.target, fixture.player.looked());
                    assertEquals("Target prompt", fixture.player.button);
                    boolean received = state.equals("dead") || state.equals("unspawned");
                    assertEquals(received ? List.of("receive") : List.of(), fixture.events);
                    verify(fixture.processor, times(enabled && received ? 1 : 0)).validateReceivedPacket(eq(fixture.player), any(), eq(false));
                    verify(fixture.player, never()).onUpdate(anyInt());
                }
            }
        }
    }

    @Test
    void unrelatedInputCapabilitiesDoNotGateOriginalHover() {
        for (boolean modern : new boolean[]{false, true}) {
            for (String state : List.of("no-sequence", "no-processor", "offline")) {
                Fixture fixture = new Fixture(true, modern);
                fixture.handle(hover(modern, 1));
                fixture.events.clear();
                clearInvocations(fixture.processor);
                if (state.equals("no-sequence")) doReturn(false).when(fixture.player).supportsPacketSequences();
                if (state.equals("no-processor")) when(fixture.server.getPlayerInputProcessor()).thenReturn(null);
                if (state.equals("offline")) doReturn(false).when(fixture.player).isOnline();
                fixture.deliver(hover(modern, 0));
                assertNull(fixture.player.looked());
                assertEquals("Hand prompt", fixture.player.button);
                assertEquals(List.of("receive"), fixture.events);
                verify(fixture.processor, times(state.equals("no-processor") ? 0 : 1)).validateReceivedPacket(eq(fixture.player), any(), eq(false));
            }
        }
    }

    @Test
    void queuedCancellationAndValidationRejectionDoNotClearTarget() {
        for (boolean modern : new boolean[]{false, true}) {
            for (boolean cancelled : new boolean[]{false, true}) {
                Fixture fixture = new Fixture(true, modern);
                fixture.handle(hover(modern, 1));
                fixture.events.clear();
                fixture.cancel = cancelled;
                if (!cancelled) when(fixture.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
                fixture.deliver(hover(modern, 0));
                assertSame(fixture.target, fixture.player.looked());
                assertEquals("Target prompt", fixture.player.button);
                assertEquals(List.of("receive"), fixture.events);
            }
        }
    }

    @Test
    void callbackDeathOrRetirementStillStopsOriginalHover() {
        for (boolean modern : new boolean[]{false, true}) {
            for (boolean retire : new boolean[]{false, true}) {
                Fixture fixture = new Fixture(true, modern);
                fixture.handle(hover(modern, 1));
                fixture.events.clear();
                fixture.retire = retire;
                fixture.dieOnReceive = !retire;
                fixture.deliver(hover(modern, 0));
                assertSame(fixture.target, fixture.player.looked());
                assertEquals(List.of("receive"), fixture.events);
            }
        }
    }

    @Test
    void nonHoverActionsCannotBorrowRelaxedDynamicEligibility() {
        for (boolean modern : new boolean[]{false, true}) {
            Fixture fixture = new Fixture(true, modern);
            doReturn(false).when(fixture.player).isAlive();
            for (int action = InteractPacket113.ACTION_NONE; action <= InteractPacket113.ACTION_OPEN_INVENTORY; action++) {
                if (action == InteractPacket113.ACTION_MOUSEOVER) continue;
                DataPacket packet = modern ? modernPacket(action, 0) : packet(action, 0);
                assertFalse(BetweenTickPackets.supports(packet));
                assertFalse(BetweenTickPackets.canRunBetweenTicks(fixture.player, packet));
            }
        }
    }

    private static DataPacket hover(boolean modern, long target) {
        return modern ? modernPacket(InteractPacket121130.ACTION_MOUSEOVER, target) : packet(InteractPacket113.ACTION_MOUSEOVER, target);
    }

    private static InteractPacket113 packet(int action, long target) {
        InteractPacket113 packet = new InteractPacket113();
        packet.action = action; packet.target = target;
        return packet;
    }

    private static final class Fixture {
        final Probe player = mock(Probe.class, CALLS_REAL_METHODS);
        final Server server = mock(Server.class);
        final PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        final PluginManager plugins = mock(PluginManager.class);
        final Level level = mock(Level.class);
        final Entity target = mock(Entity.class, withSettings().extraInterfaces(EntityInteractable.class));
        final List<String> events = new ArrayList<>();
        final boolean enabled;
        boolean cancel;
        boolean retire;
        boolean dieOnReceive;
        Fixture(boolean enabled) {
            this(enabled, false);
        }
        Fixture(boolean enabled, boolean modern) {
            this.enabled = enabled;
            PlayerInventory inventory = mock(PlayerInventory.class);
            player.configure(server, level, inventory, modern);
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).supportsPacketSequences();
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getPlayerInputProcessor()).thenReturn(processor);
            when(processor.validateReceivedPacket(eq(player), any(), anyBoolean())).thenReturn(InputValidationResult.ACCEPTED);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isAlive();
            doReturn(false).when(player).isClosed();
            doReturn(true).when(player).setDataProperty(any(EntityData.class));
            when(level.getEntity(1)).thenReturn(target);
            when(target.isAlive()).thenReturn(true);
            when(((EntityInteractable) target).getInteractButtonText(player)).thenReturn("Target prompt");
            Item hand = mock(Item.class);
            when(inventory.getItemInHand()).thenReturn(hand);
            when(hand.getInteractText(player)).thenReturn("Hand prompt");
            doAnswer(call -> {
                Event event = call.getArgument(0);
                if (event instanceof DataPacketReceiveEvent received) {
                    events.add("receive");
                    if (cancel) received.setCancelled();
                    if (retire) doReturn(false).when(player).isAcceptingInputPackets();
                    if (dieOnReceive) doReturn(false).when(player).isAlive();
                } else if (event instanceof PlayerMouseOverEntityEvent) events.add("mouseover");
                return null;
            }).when(plugins).callEvent(any());
        }
        void deliver(DataPacket packet) {
            if (!enabled) { handle(packet); return; }
            assertTrue(BetweenTickPackets.supports(packet));
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
            when(server.getInputDispatcher()).thenReturn(dispatcher);
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            List<String> order = new ArrayList<>();
            dispatcher.offer(session, new ServerInputTask() {
                public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                public void run(InboundContext context) {
                    assertTrue(context.betweenTicks());
                    handle(packet);
                    order.add("hover");
                }
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 1, Long.MAX_VALUE));
            assertEquals(List.of("hover", "follower"), order);
        }
        void handle(DataPacket packet) {
            if (enabled) player.handleInputDataPacket(packet, 0);
            else player.handleDataPacket(packet);
        }
    }

    static class Probe extends SynapsePlayer116100 {
        String button;
        Probe() { super(null, null, 0L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, PlayerInventory inventory, boolean modern) {
            this.protocol = (modern ? AbstractProtocol.PROTOCOL_121_130 : AbstractProtocol.PROTOCOL_121_20).getProtocolStart();
            this.server = server; this.level = level; this.inventory = inventory;
            this.isSynapseLogin = true; this.spawned = true; this.button = "Initial";
        }
        @Override public void setButtonText(String text) { button = text; }
        Entity looked() { return lookAtEntity; }
    }
}
