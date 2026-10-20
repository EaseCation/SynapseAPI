package org.itxtech.synapseapi;

import cn.nukkit.Server;
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
    @Test
    void onlyMouseOverIsEligibleAndOriginalReadinessStillApplies() {
        SynapsePlayer player = mock(SynapsePlayer.class);
        for (int action = InteractPacket113.ACTION_NONE; action <= InteractPacket113.ACTION_OPEN_INVENTORY; action++) {
            InteractPacket113 packet = packet(action, 1);
            assertEquals(action == InteractPacket113.ACTION_MOUSEOVER, BetweenTickPackets.supports(packet));
        }
        InteractPacket113 packet = packet(InteractPacket113.ACTION_MOUSEOVER, 1);
        assertFalse(BetweenTickPackets.canRunBetweenTicks(player, packet));
        when(player.canProcessInputStateBetweenTicks()).thenReturn(true);
        assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
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

    private static InteractPacket113 packet(int action, long target) {
        InteractPacket113 packet = new InteractPacket113();
        packet.action = action; packet.target = target;
        return packet;
    }

    private static final class Fixture {
        final Probe player = mock(Probe.class, CALLS_REAL_METHODS);
        final PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        final PluginManager plugins = mock(PluginManager.class);
        final Level level = mock(Level.class);
        final Entity target = mock(Entity.class, withSettings().extraInterfaces(EntityInteractable.class));
        final List<String> events = new ArrayList<>();
        final boolean enabled;
        boolean cancel;
        boolean retire;
        Fixture(boolean enabled) {
            this.enabled = enabled;
            Server server = mock(Server.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            player.configure(server, level, inventory);
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
                } else if (event instanceof PlayerMouseOverEntityEvent) events.add("mouseover");
                return null;
            }).when(plugins).callEvent(any());
        }
        void handle(InteractPacket113 packet) {
            if (enabled) player.handleInputDataPacket(packet, 0);
            else player.handleDataPacket(packet);
        }
    }

    static class Probe extends SynapsePlayer113 {
        String button;
        Probe() { super(null, null, 0L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, Level level, PlayerInventory inventory) {
            this.server = server; this.level = level; this.inventory = inventory;
            this.isSynapseLogin = true; this.spawned = true; this.button = "Initial";
        }
        @Override public void setButtonText(String text) { button = text; }
        Entity looked() { return lookAtEntity; }
    }
}
