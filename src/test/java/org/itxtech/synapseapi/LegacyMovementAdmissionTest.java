package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.event.player.PlayerVehicleInputEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.PacketViolationReason;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerInputPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityAbsolutePacket15;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LegacyMovementAdmissionTest {
    @BeforeAll static void protocols() {
        Block.init();Item.init();Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
            PacketRegister.init();
        }
    }

    @Test void actualTargetRegistryAndWireKeepLegacyMessagesIntact() {
        Server server = mock(Server.class);Network network = mock(Network.class);
        when(server.getNetwork()).thenReturn(network);
        when(network.getPacket(ProtocolInfo.PLAYER_INPUT_PACKET)).thenAnswer(call -> new PlayerInputPacket());
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            for (int protocol : new int[]{859, 860, 898, 975}) {
                for (int kind = 0; kind < 3; kind++) {
                    DataPacket source = packet(kind);byte[] wire;
                    if (source instanceof PlayerInputPacket input) {
                        BinaryStream bytes = new BinaryStream();bytes.putUnsignedVarInt(input.pid());
                        bytes.putLFloat(input.motionX);bytes.putLFloat(input.motionY);bytes.putBoolean(input.jumping);bytes.putBoolean(input.sneaking);wire = bytes.getBuffer();
                    } else if (source instanceof MoveEntityAbsolutePacket15 move) {
                        // 目标客户端使用现代包头；1.5类的出站encode仍是旧三字节头，不能直接当现代入站线格式。
                        BinaryStream bytes = new BinaryStream();bytes.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());
                        bytes.putUnsignedVarInt(move.pid());bytes.putEntityRuntimeId(move.eid);bytes.putByte((byte) 0);
                        bytes.putLFloat(move.x);bytes.putLFloat(move.y);bytes.putLFloat(move.z);
                        bytes.putByte((byte) 0);bytes.putByte((byte) 0);bytes.putByte((byte) 0);wire = bytes.getBuffer();
                    } else {
                        source.setHelper(AbstractProtocol.fromRealProtocol(protocol).getHelper());source.tryEncode();wire = source.getBuffer();
                    }
                    DataPacket decoded = PacketRegister.getFullPacket(wire, protocol, false);
                    assertNotNull(decoded);assertEquals(source.getClass(), decoded.getClass());
                    assertEquals(wire.length, decoded.getOffset());assertTrue(BetweenTickPackets.supports(decoded));
                    if (decoded instanceof MovePlayerPacket116100NE move) assertEquals(1, move.x);
                    if (decoded instanceof MoveEntityAbsolutePacket15 move) assertEquals(7, move.eid);
                    if (decoded instanceof PlayerInputPacket input) {assertEquals(2, input.motionX);assertTrue(input.jumping);}
                }
            }
        }
    }

    @Test void modernAuthorityKeepsThreeOriginalRejectionsAndFollower() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(true, true);
            f.deliver(packet(kind), true);
            assertNull(f.player.pending());
            assertEquals(0, f.player.x);
            verifyNoInteractions(f.rideable);
            assertEquals(kind == 0 ? List.of() : List.of(kind == 1 ? "input_boat" : "input"), f.player.violations);
            verify(f.plugins, times(kind == 1 ? 0 : 1)).callEvent(any(DataPacketReceiveEvent.class));
            verify(f.processor, times(kind == 0 ? 1 : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        }
    }

    @Test void originalDisabledAuthorityRejectionsRemain() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(false, true);
            f.deliver(packet(kind), false);
            assertNull(f.player.pending());
            verifyNoInteractions(f.processor, f.rideable);
            assertEquals(kind == 0 ? List.of() : List.of(kind == 1 ? "input_boat" : "input"), f.player.violations);
        }
    }

    @Test void pendingPositionsRemainPendingUntilOriginalWorldConsumption() { pending(true); }
    @Test void disabledModeKeepsSamePendingPositionAndRotation() { pending(false); }
    private void pending(boolean enabled) {
        Fixture f = new Fixture(enabled, false);
        for (int i = 1; i <= 3; i++) {
            MovePlayerPacket116100NE p = (MovePlayerPacket116100NE) packet(0);
            p.x = i; p.yaw = i * 20;
            f.deliver(p, enabled);
            assertEquals(i, f.player.pending().x);
            assertEquals(0, f.player.x);
        }
        assertEquals(60, f.player.yaw);
        verify(f.processor, never()).onMovementCommitted(any(), any());
        verify(f.player, never()).onUpdate(anyInt());
    }

    @Test void cancelledPacketsKeepOriginalCancellation() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(true, false); f.cancel = true;
            f.deliver(packet(kind), true);
            assertNull(f.player.pending());
            verify(f.rideable, never()).onPlayerInput(any(), anyDouble(), anyDouble());
            verify(f.rideable, never()).onPlayerInput(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
            verify(f.processor, times(kind == 2 ? 0 : 1)).validateReceivedPacket(any(), any(), eq(true));
        }
    }

    @Test void validationRejectionDoesNotWritePendingOrVehicleMovement() {
        for (int kind = 0; kind < 2; kind++) {
            Fixture f = new Fixture(true, false);
            when(f.processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.REJECTED);
            f.deliver(packet(kind), true);
            assertNull(f.player.pending());
            verify(f.rideable, never()).onPlayerInput(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Test void allThreeUninitializedPacketsAreRejectedBeforeEvents() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(true, false); doReturn(false).when(f.player).isInitialized();
            f.deliver(packet(kind), true);
            verifyNoInteractions(f.plugins, f.processor, f.rideable);
            assertNull(f.player.pending());
        }
    }

    @Test void retiredSessionDoesNotExecuteAnyOriginalHandler() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(true, false); doReturn(false).when(f.player).isAcceptingInputPackets();
            f.deliver(packet(kind), true);
            verifyNoInteractions(f.plugins, f.processor, f.rideable);
        }
    }

    @Test void legacyVehicleAbsoluteKeepsDriverIdAndDistance() { vehicle(true); }
    @Test void disabledVehicleAbsoluteKeepsDriverIdAndDistance() { vehicle(false); }
    private void vehicle(boolean enabled) {
        for (String variant : List.of("normal", "wrong-id", "passenger", "far")) {
            Fixture f = new Fixture(enabled, false);
            MoveEntityAbsolutePacket15 p = (MoveEntityAbsolutePacket15) packet(1);
            if (variant.equals("wrong-id")) p.eid = 8;
            if (variant.equals("passenger")) when(f.vehicle.isControlling(f.player)).thenReturn(false);
            if (variant.equals("far")) p.x = 100;
            f.deliver(p, enabled);
            verify(f.rideable, times(variant.equals("normal") ? 1 : 0)).onPlayerInput(eq(f.player), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
            verify(f.vehicle, never()).onUpdate(anyInt());
            assertNull(f.player.pending());
        }
    }

    @Test void directionalVehicleInputKeepsOriginalClampAndEvent() { direction(true); }
    @Test void disabledDirectionalVehicleInputKeepsOriginalClampAndEvent() { direction(false); }
    private void direction(boolean enabled) {
        for (String variant : List.of("normal", "passenger", "dead", "zero")) {
            Fixture f = new Fixture(enabled, false);
            PlayerInputPacket p = (PlayerInputPacket) packet(2);
            if (variant.equals("passenger")) when(f.vehicle.isControlling(f.player)).thenReturn(false);
            if (variant.equals("dead")) doReturn(false).when(f.player).isAlive();
            if (variant.equals("zero")) p.motionX = p.motionY = 0;
            f.deliver(p, enabled);
            int count = variant.equals("normal") ? 1 : 0;
            verify(f.rideable, times(count)).onPlayerInput(f.player, 1, -1);
            verify(f.plugins, times(count)).callEvent(any(PlayerVehicleInputEvent.class));
            verifyNoInteractions(f.processor);
            verify(f.vehicle, never()).onUpdate(anyInt());
        }
    }

    @Test void teleportAndForceMovementKeepOriginalResponses() {
        for (boolean enabled : new boolean[]{true, false}) {
            for (boolean teleport : new boolean[]{true, false}) {
                Fixture f = new Fixture(enabled, false);f.player.awaitOriginalRevert(teleport);
                doNothing().when(f.player).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
                f.deliver(packet(0), enabled);
                assertNull(f.player.pending());assertEquals(0, f.player.x);
                verify(f.player, times(teleport ? 0 : 1)).sendPosition(any(), anyDouble(), anyDouble(), anyInt());
            }
        }
    }

    @Test void nonFiniteCoordinatesKeepOriginalClose() {
        for (int kind = 0; kind < 3; kind++) {
            Fixture f = new Fixture(true, false); DataPacket p = packet(kind);
            if (p instanceof MovePlayerPacket116100NE move) move.x = Float.NaN;
            else if (p instanceof MoveEntityAbsolutePacket15 move) move.x = Float.POSITIVE_INFINITY;
            else ((PlayerInputPacket) p).motionX = Float.NaN;
            f.deliver(p, true);
            assertEquals(1, f.player.closes);
            assertNull(f.player.pending());
        }
    }

    private static DataPacket packet(int kind) {
        if (kind == 0) {
            MovePlayerPacket116100NE p = new MovePlayerPacket116100NE();p.x = 1;p.y = 1.62f;return p;
        }
        if (kind == 1) {
            MoveEntityAbsolutePacket15 p = new MoveEntityAbsolutePacket15();p.eid = 7;p.x = 1;return p;
        }
        PlayerInputPacket p = new PlayerInputPacket();p.motionX = 2;p.motionY = -2;p.jumping = true;return p;
    }

    private static class Fixture {
        final Server server = mock(Server.class);
        final PluginManager plugins = mock(PluginManager.class);
        final PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        final Probe player = mock(Probe.class, CALLS_REAL_METHODS);
        final Entity vehicle = mock(Entity.class, withSettings().extraInterfaces(EntityRideable.class));
        final EntityRideable rideable = (EntityRideable) vehicle;
        final boolean enabled;
        boolean cancel;
        Fixture(boolean enabled, boolean authority) {
            this.enabled = enabled; player.configure(server, authority, vehicle);
            when(server.getPluginManager()).thenReturn(plugins);
            when(server.getPlayerInputProcessor()).thenReturn(processor);
            when(server.getLogger()).thenReturn(mock(MainLogger.class));
            when(server.isPrimaryThread()).thenReturn(true);
            when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenReturn(InputValidationResult.ACCEPTED);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            doReturn(true).when(player).isAlive();
            when(vehicle.getId()).thenReturn(7L);
            when(vehicle.isControlling(player)).thenReturn(true);
            doAnswer(call -> { if (cancel && call.getArgument(0) instanceof DataPacketReceiveEvent event) event.setCancelled(); return null; }).when(plugins).callEvent(any());
        }
        void deliver(DataPacket packet, boolean phase) {
            try (MockedStatic<Server> global = mockStatic(Server.class)) {
                global.when(Server::getInstance).thenReturn(server);
                if (!phase) { player.handleDataPacket(packet); return; }
                assertTrue(BetweenTickPackets.supports(packet));
                ServerInputDispatcher d = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
                when(server.getInputDispatcher()).thenReturn(d);
                ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
                List<String> order = new ArrayList<>();
                d.offer(session, new ServerInputTask() {
                    @Override public boolean canRunBetweenTicks() { return BetweenTickPackets.canRunBetweenTicks(player, packet); }
                    @Override public void run(InboundContext context) { assertTrue(context.betweenTicks());player.handleInputDataPacket(packet, player.getMovementEpoch());order.add("original"); }
                }, 1, true);
                d.offer(session, context -> order.add("follower"), 1, true);
                assertEquals(2, d.drain(true, 1, Long.MAX_VALUE));
                assertEquals(List.of("original", "follower"), order);
                assertEquals(0, d.snapshot().queuedTasks());
            }
        }
    }

    static class Probe extends SynapsePlayer116100 {
        List<String> violations;
        int closes;
        Probe() { super(null, null, 0L, new InetSocketAddress("127.0.0.1", 19132)); }
        void configure(Server server, boolean authority, Entity vehicle) {
            this.server = server;this.level = mock(Level.class);this.connected = true;this.spawned = true;this.isSynapseLogin = true;
            this.protocol = AbstractProtocol.PROTOCOL_126_20.getProtocolStart();this.serverAuthoritativeMovement = authority;this.riding = vehicle;
            this.violations = new ArrayList<>();this.temporalVector = new Vector3();
        }
        Vector3 pending() { return this.newPosition; }
        void awaitOriginalRevert(boolean teleport) {
            this.riding = null;
            if (teleport) this.teleportPosition = new Vector3();
            else this.forceMovement = new Vector3();
        }
        @Override public void close(String message, String reason) { this.closes++; }
        @Override public void onPacketViolation(PacketViolationReason reason, String tag) { this.violations.add(tag); }
    }
}
