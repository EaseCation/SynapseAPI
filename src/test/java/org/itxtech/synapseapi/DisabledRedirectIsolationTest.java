package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.scheduler.ServerScheduler;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.SetEntityMotionPacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.ConnectionStatusPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLogoutPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisabledRedirectIsolationTest {
    @Test void bothOffSinglePacketsUseLegacyTicker() { check(false, false, false); }
    @Test void bothOffBatchUsesLegacyTicker() { check(false, false, true); }
    @Test void coreOnlySinglePacketsUseLegacyTicker() { check(true, false, false); }
    @Test void coreOnlyBatchUsesLegacyTicker() { check(true, false, true); }
    @Test void synapseOnlySinglePacketsUseLegacyTicker() { check(false, true, false); }
    @Test void synapseOnlyBatchUsesLegacyTicker() { check(false, true, true); }

    private static MockedStatic<Server> preparedServer(Server server) {
        // 协议枚举初始化会读取服务器，必须早于 PacketRegister 的静态 mock 建立。
        MockedStatic<Server> scope = mockStatic(Server.class);
        scope.when(Server::getInstance).thenReturn(server);
        return scope;
    }

    private void check(boolean coreEnabled, boolean synapseEnabled, boolean batched) {
        Server server = mock(Server.class, RETURNS_DEEP_STUBS);
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getServer()).thenReturn(server);
        when(api.getLogger()).thenReturn(mock(PluginLogger.class));
        when(api.isMainThreadInput()).thenReturn(synapseEnabled);
        when(server.getOnlinePlayerList()).thenReturn(new Player[0]);
        doReturn(Compressor.SNAPPY).when(server).getCompressor();
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 64, 4096, 64, 4096, error -> fail(error));
        when(server.getInputDispatcher()).thenReturn(coreEnabled ? dispatcher : null);
        AtomicReference<Runnable> worldTicker = new AtomicReference<>();
        ServerScheduler scheduler = server.getScheduler();
        doAnswer(call -> { worldTicker.set(call.getArgument(1)); return null; })
                .when(scheduler).scheduleRepeatingTask(any(Plugin.class), any(Runnable.class), eq(1));
        UUID actor = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        PlayerAuthInputPacket116220 input = new PlayerAuthInputPacket116220();
        List<DataPacket> decoded = List.of(input, new MobEquipmentPacket(), new InventoryTransactionPacket116(),
                new PlayerActionPacket119(), new AnimatePacket(), new NEPyRpcPacket16(), new SetEntityMotionPacket12070());
        List<Object> consumed = new ArrayList<>();
        try (MockedStatic<Server> servers = preparedServer(server);
             MockedStatic<SynapseAPI> globals = mockStatic(SynapseAPI.class);
             MockedStatic<PacketRegister> packets = mockStatic(PacketRegister.class);
             MockedStatic<SynapseEntry> entryStatics = mockStatic(SynapseEntry.class);
             MockedStatic<BetweenTickPackets> admission = mockStatic(BetweenTickPackets.class);
             MockedConstruction<SynapseInterface> interfaces = mockConstruction(SynapseInterface.class);
             MockedConstruction<SynapseEntry.AsyncTicker> tickers = mockConstruction(SynapseEntry.AsyncTicker.class);
             MockedConstruction<SynapsePlayer116100> players = mockConstruction(SynapsePlayer116100.class,
                     (player, context) -> {
                         when(player.getUniqueId()).thenReturn(actor);
                         when(player.getSessionId()).thenReturn(sessionId);
                         when(player.isOnline()).thenReturn(true);
                         player.lastAuthInputPacketTick = 100;
                         doAnswer(call -> { consumed.add(call.getArgument(0)); return null; })
                                 .when(player).handleDataPacket(any());
                         doAnswer(call -> { consumed.add("logout"); return null; })
                                 .when(player).close("", "test", true);
                     })) {
            servers.when(Server::getInstance).thenReturn(server);
            globals.when(SynapseAPI::getInstance).thenReturn(api);
            // 解码产物是测试边界；真实Entry、原Ticker与队列消费不替换。
            admission.when(() -> BetweenTickPackets.supports(any())).thenAnswer(call -> fail("Disabled entry reached admission"));
            SynapseEntry entry = new SynapseEntry(api, "127.0.0.1", 10325, false, "0123456789abcdef", "Disabled redirect contract");
            assertNotNull(worldTicker.get());
            ConnectionStatusPacket connected = new ConnectionStatusPacket();
            connected.type = ConnectionStatusPacket.TYPE_LOGIN_SUCCESS;
            entry.handleDataPacket(connected);
            PlayerLoginPacket login = new PlayerLoginPacket();
            login.protocol = 975;
            login.uuid = actor;
            login.sessionId = sessionId;
            login.cachedLoginPacket = new byte[]{1};
            login.address = "127.0.0.1";
            login.port = 19132;
            entry.handleDataPacket(login);
            assertTrue(players.constructed().isEmpty());
            worldTicker.get().run();
            assertEquals(1, players.constructed().size());
            SynapsePlayer player = players.constructed().getFirst();
            // 原关闭登录仍交接空 Session，不应把这个调用误判为新调度接入。
            verify(player).bindInputSession(isNull(), isNull());
            List<DataPacket> wirePackets;
            if (batched) {
                BatchPacket batch = new BatchPacket();
                entryStatics.when(() -> SynapseEntry.processBatch(batch, 975, false, (byte) 0)).thenReturn(decoded);
                wirePackets = List.of(batch);
            } else {
                wirePackets = decoded;
            }
            for (DataPacket packet : wirePackets) {
                RedirectPacket redirect = new RedirectPacket();
                redirect.sessionId = sessionId;
                redirect.protocol = 975;
                redirect.mcpeBuffer = new byte[]{1};
                packets.when(() -> PacketRegister.getFullPacket(redirect.mcpeBuffer, 975)).thenReturn(packet);
                entry.handleDataPacket(redirect);
            }
            PlayerLogoutPacket logout = new PlayerLogoutPacket();
            logout.sessionId = sessionId;
            logout.reason = "test";
            entry.handleDataPacket(logout);
            assertTrue(consumed.isEmpty());
            assertEquals(0, dispatcher.snapshot().queuedTasks());
            worldTicker.get().run();
            List<Object> expected = new ArrayList<>(decoded);
            expected.add("logout");
            assertEquals(expected, consumed);
            worldTicker.get().run();
            assertEquals(expected, consumed);
            assertEquals(100, player.lastAuthInputPacketTick);
            verify(player, never()).handleInputDataPacket(any(), anyLong());
            verify(player, never()).movementBlockReason();
            verify(player, never()).inputStateBlockReason();
            verify(player, never()).itemActionBlockReason();
            admission.verifyNoInteractions();
            assertEquals(0, dispatcher.snapshot().executedTasks());
        } finally {
            dispatcher.close();
        }
    }
}
