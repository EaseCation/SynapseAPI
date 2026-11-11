package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.event.Event;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.level.Level;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginManager;
import org.itxtech.synapseapi.filtertext.FilterTextService;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.TextPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.TextPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.TextPacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.TextPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol126.protocol.TextPacket126;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.NetworkStackLatencyPacket19;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TextAdmissionTest {
    @BeforeAll
    static void initializeProtocols() {
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_121_130.getProtocolStart();
            AbstractProtocol.PROTOCOL_126.getProtocolStart();
        }
        // 滤词服务静态初始化读取插件词表；先以空词表完成初始化，测试只替换投递入口。
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getResource(any())).thenAnswer(call -> new ByteArrayInputStream(new byte[0]));
        try (MockedStatic<SynapseAPI> global = mockStatic(SynapseAPI.class)) {
            global.when(SynapseAPI::getInstance).thenReturn(api);
            assertNotNull(FilterTextService.getInstance());
        }
    }

    @Test
    void onlyAsyncFilteredTextClassesAreEligible() {
        for (DataPacket packet : List.of(new TextPacket126(), new TextPacket121130(), new TextPacket121(),
                new TextPacket116100(), new TextPacket116100NE())) {
            assertTrue(BetweenTickPackets.supports(packet), packet.getClass().getSimpleName());
        }
        // 原版 Nukkit 文本 handler 同步执行聊天，继续作为队头屏障。
        assertFalse(BetweenTickPackets.supports(new TextPacket()));
    }

    @Test
    void chatOnlyQueuesFilterInBothModesWithoutSynchronousChat() {
        for (boolean enabled : new boolean[]{false, true}) {
            for (boolean modern : new boolean[]{false, true}) {
                Fixture fixture = new Fixture(enabled, modern);
                fixture.handle(chat(modern, "hello"));
                assertEquals(List.of("receive"), fixture.events);
                assertEquals(List.of("hello"), fixture.filtered);
                verify(fixture.player, never()).preChat(any());
                verify(fixture.player, never()).chat(any());
            }
        }
    }

    @Test
    void nonChatTypesOnlyFireReceiveEventInBothModes() {
        for (boolean enabled : new boolean[]{false, true}) {
            for (boolean modern : new boolean[]{false, true}) {
                Fixture fixture = new Fixture(enabled, modern);
                DataPacket packet = modern ? raw126("x") : raw121130("x");
                fixture.handle(packet);
                assertEquals(List.of("receive"), fixture.events);
                assertEquals(List.of(), fixture.filtered);
            }
        }
    }

    @Test
    void cancelledRetiredDeadOrUnspawnedSenderNeverReachesFilter() {
        for (boolean enabled : new boolean[]{false, true}) {
            for (String state : List.of("cancelled", "retired", "dead", "unspawned")) {
                if (!enabled && state.equals("retired")) continue;
                Fixture fixture = new Fixture(enabled, true);
                if (state.equals("cancelled")) fixture.cancel = true;
                if (state.equals("retired")) fixture.retire = true;
                if (state.equals("dead")) doReturn(false).when(fixture.player).isAlive();
                if (state.equals("unspawned")) fixture.player.spawned = false;
                fixture.handle(chat(true, "blocked"));
                assertEquals(List.of("receive"), fixture.events, state);
                assertEquals(List.of(), fixture.filtered, state);
            }
        }
    }

    @Test
    void supplementalTextRunsWholeHandlerBeforeFollowingLatency() {
        for (boolean baseline : new boolean[]{true, false}) {
            Fixture fixture = new Fixture(true, true);
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            List<Boolean> phases = new ArrayList<>();
            DataPacket text = chat(true, "fast");
            fixture.offer(dispatcher, session, text, !baseline && BetweenTickPackets.supports(text), phases);
            fixture.offer(dispatcher, session, new NetworkStackLatencyPacket19(), true, phases);
            assertEquals(baseline ? 0 : 2, dispatcher.drain(true, 7, Long.MAX_VALUE));
            if (baseline) assertEquals(2, dispatcher.drainWorldTick(8));
            assertEquals(List.of("receive", "filter", "latency"), fixture.order);
            assertEquals(List.of(!baseline, !baseline), phases);
            assertEquals(List.of("fast"), fixture.filtered);
        }
    }

    @Test
    void earlierWorldBarrierStillHoldsTextAndFollowers() {
        Fixture fixture = new Fixture(true, true);
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 8, 1024, 8, 1024, error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<Boolean> phases = new ArrayList<>();
        DataPacket text = chat(true, "after");
        fixture.offer(dispatcher, session, new TextPacket(), false, phases);
        fixture.offer(dispatcher, session, text, BetweenTickPackets.supports(text), phases);
        fixture.offer(dispatcher, session, new NetworkStackLatencyPacket19(), true, phases);
        assertEquals(0, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(3, dispatcher.drainWorldTick(8));
        assertEquals(List.of("barrier", "receive", "filter", "latency"), fixture.order);
    }

    @Test
    void sustainedTextBurstNoLongerDefersFollowingInput() {
        // 复现生产风暴：1.21.130 客户端每 tick 一个非聊天文本包。
        Fixture fixture = new Fixture(true, false);
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 512, 65536, 512, 65536, error -> fail(error));
        ServerInputDispatcher.Session spammer = new ServerInputDispatcher.Session(UUID.randomUUID());
        ServerInputDispatcher.Session other = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<Boolean> phases = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DataPacket text = raw121130("spam");
            fixture.offer(dispatcher, spammer, text, BetweenTickPackets.supports(text), phases);
            fixture.offer(dispatcher, other, new NetworkStackLatencyPacket19(), true, phases);
        }
        assertEquals(40, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        assertTrue(phases.stream().allMatch(Boolean::booleanValue));
        assertEquals(List.of(), fixture.filtered);
    }

    private static DataPacket chat(boolean modern, String message) {
        if (modern) {
            TextPacket126 packet = new TextPacket126();
            packet.type = TextPacket126.TYPE_CHAT;
            packet.message = message;
            return packet;
        }
        TextPacket121130 packet = new TextPacket121130();
        packet.type = TextPacket121130.TYPE_CHAT;
        packet.message = message;
        return packet;
    }

    private static TextPacket126 raw126(String message) {
        TextPacket126 packet = new TextPacket126();
        packet.type = TextPacket126.TYPE_RAW;
        packet.message = message;
        return packet;
    }

    private static TextPacket121130 raw121130(String message) {
        TextPacket121130 packet = new TextPacket121130();
        packet.type = TextPacket121130.TYPE_RAW;
        packet.message = message;
        return packet;
    }

    private static final class Fixture {
        final Probe player = mock(Probe.class, CALLS_REAL_METHODS);
        final Server server = mock(Server.class);
        final PluginManager plugins = mock(PluginManager.class);
        final List<String> events = new ArrayList<>();
        final List<String> filtered = new ArrayList<>();
        final List<String> order = new ArrayList<>();
        final Thread mainThread = Thread.currentThread();
        final boolean enabled;
        boolean cancel;
        boolean retire;

        Fixture(boolean enabled, boolean modern) {
            this.enabled = enabled;
            player.configure(server, mock(Level.class), mock(PlayerInventory.class), modern);
            doReturn(true).when(player).isInitialized();
            doReturn(true).when(player).isOnline();
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getPluginManager()).thenReturn(plugins);
            doReturn(enabled).when(player).isMainThreadInputEnabled();
            doReturn(true).when(player).isAcceptingInputPackets();
            doReturn(true).when(player).isAlive();
            doReturn(false).when(player).isClosed();
            doAnswer(call -> {
                Event event = call.getArgument(0);
                if (event instanceof DataPacketReceiveEvent received) {
                    events.add("receive");
                    order.add("receive");
                    if (cancel) received.setCancelled();
                    if (retire) doReturn(false).when(player).isAcceptingInputPackets();
                }
                return null;
            }).when(plugins).callEvent(any());
        }

        void handle(DataPacket packet) {
            try (MockedStatic<FilterTextService> service = mockStatic(FilterTextService.class)) {
                service.when(() -> FilterTextService.filter(any(), any(), anyBoolean(), any())).thenAnswer(call -> {
                    // 只登记投递；真实聊天出口在世界周期的滤词 tick 执行。
                    assertSame(player, call.getArgument(1));
                    assertTrue(call.<Boolean>getArgument(2));
                    filtered.add(call.getArgument(0));
                    order.add("filter");
                    return null;
                });
                if (enabled) player.handleInputDataPacket(packet, 0);
                else player.handleDataPacket(packet);
            }
        }

        void offer(ServerInputDispatcher dispatcher, ServerInputDispatcher.Session session, DataPacket packet,
                   boolean supported, List<Boolean> phases) {
            assertEquals(ServerInputDispatcher.OfferResult.ACCEPTED, dispatcher.offer(session, new ServerInputTask() {

                @Override
                public void run(InboundContext context) {
                    assertSame(mainThread, Thread.currentThread());
                    phases.add(context.betweenTicks());
                    if (packet instanceof TextPacket) order.add("barrier");
                    else if (packet instanceof NetworkStackLatencyPacket19) order.add("latency");
                    else handle(packet);
                }
            }, 1, supported));
        }
    }

    static class Probe extends SynapsePlayer116100 {
        Probe() {
            super(null, null, 0L, new InetSocketAddress("127.0.0.1", 19132));
        }

        void configure(Server server, Level level, PlayerInventory inventory, boolean modern) {
            this.protocol = (modern ? AbstractProtocol.PROTOCOL_126 : AbstractProtocol.PROTOCOL_121_130).getProtocolStart();
            this.server = server;
            this.level = level;
            this.inventory = inventory;
            this.isSynapseLogin = true;
            this.spawned = true;
        }
    }
}
