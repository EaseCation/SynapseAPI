package org.itxtech.synapseapi;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.ServerInputDispatcher;
import io.netty.channel.Channel;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import cn.nukkit.network.protocol.AnimatePacket;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.itxtech.synapseapi.network.SynapseInterface;
import org.itxtech.synapseapi.network.protocol.spp.PlayerLoginPacket;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.itxtech.synapseapi.network.synlib.Session;
import org.itxtech.synapseapi.network.synlib.SynapseClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedirectAttributionTest {
    private static MockedStatic<Server> preparedServer(Server server) {
        // 独立运行此用例时，协议枚举同样需要已绑定的服务器。
        MockedStatic<Server> scope = mockStatic(Server.class);
        scope.when(Server::getInstance).thenReturn(server);
        return scope;
    }

    /**
     * 实际 Redirect 任务只按静态分类决定阶段：玩家处于未出生/死亡等原动态门禁状态时，
     * 已放行包仍在补充阶段执行，且准入不读取玩家状态；静态屏障上报子类型标签与 STATIC。
     */
    @Test
    void actualRedirectUsesStaticAdmissionOnlyAndReportsSubtypeLabel() {
        Server server = mock(Server.class, RETURNS_DEEP_STUBS);
        SynapseAPI api = mock(SynapseAPI.class);
        when(api.getServer()).thenReturn(server);
        when(api.isMainThreadInput()).thenReturn(true);
        doReturn(new Player[0]).when(server).getOnlinePlayerList();
        doReturn(Compressor.SNAPPY).when(server).getCompressor();
        AtomicBoolean primary = new AtomicBoolean(true);
        when(server.isPrimaryThread()).thenAnswer(call -> primary.get());
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 4096, 16, 4096, error -> fail(error));
        when(server.getInputDispatcher()).thenReturn(dispatcher);
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true);
        SynapseClient client = mock(SynapseClient.class);
        Session transport = mock(Session.class);
        when(client.getSession()).thenReturn(transport);
        when(transport.getChannel()).thenReturn(channel);
        AtomicReference<ServerInputDispatcher.Session> binding = new AtomicReference<>();
        UUID actor = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AnimatePacket animation = new AnimatePacket();
        animation.action = AnimatePacket.Action.SWING_ARM;
        // 资源包完成回复进入登录收尾，仍为静态屏障（M8）。
        ResourcePackClientResponsePacket16 exit = new ResourcePackClientResponsePacket16();
        exit.responseStatus = ResourcePackClientResponsePacket16.STATUS_COMPLETED;
        RedirectPacket swing = redirect(sessionId, new byte[]{1});
        RedirectPacket vehicle = redirect(sessionId, new byte[]{2});
        try (MockedStatic<Server> servers = preparedServer(server);
             MockedStatic<SynapseAPI> globals = mockStatic(SynapseAPI.class);
             MockedStatic<PacketRegister> packets = mockStatic(PacketRegister.class);
             MockedConstruction<SynapseInterface> interfaces = mockConstruction(SynapseInterface.class,
                     (source, context) -> when(source.getClient()).thenReturn(client));
             MockedConstruction<SynapseEntry.AsyncTicker> tickers = mockConstruction(SynapseEntry.AsyncTicker.class);
             MockedConstruction<SynapsePlayer116100> players = mockConstruction(SynapsePlayer116100.class,
                     (player, context) -> {
                         // 其余状态保持 mock 默认值：未出生、isAlive=false，相当于原 NOT_SPAWNED/DEAD 门禁。
                         when(player.getUniqueId()).thenReturn(actor);
                         when(player.getSessionId()).thenReturn(sessionId);
                         when(player.isAcceptingInputPackets()).thenReturn(true);
                         when(player.getInputSession()).thenAnswer(call -> binding.get());
                         doAnswer(call -> { binding.set(call.getArgument(0)); return null; }).when(player).bindInputSession(any(), any());
                     })) {
            servers.when(Server::getInstance).thenReturn(server);
            globals.when(SynapseAPI::getInstance).thenReturn(api);
            packets.when(() -> PacketRegister.getFullPacket(swing.mcpeBuffer, 975)).thenReturn(animation);
            packets.when(() -> PacketRegister.getFullPacket(vehicle.mcpeBuffer, 975)).thenReturn(exit);
            SynapseEntry entry = new SynapseEntry(api, "127.0.0.1", 10325, false, "0123456789abcdef", "Attribution contract");
            PlayerLoginPacket login = new PlayerLoginPacket();
            login.protocol = 975;
            login.uuid = actor;
            login.sessionId = sessionId;
            login.cachedLoginPacket = new byte[]{1};
            login.address = "127.0.0.1";
            login.port = 19132;
            entry.handleDataPacket(login);
            assertEquals(1, dispatcher.drainWorldTick(1));
            SynapsePlayer116100 player = players.constructed().getFirst();
            clearInvocations(player);

            primary.set(false);
            entry.handleDataPacket(swing);
            assertEquals(1, dispatcher.snapshot().queuedTasks());
            primary.set(true);
            assertEquals(1, dispatcher.drain(true, 2, Long.MAX_VALUE));
            verify(player).handleInputDataPacket(animation, 0L);
            verify(player, never()).isAlive();
            verify(player, never()).isSleeping();
            verify(player, never()).isSwimming();
            verify(player, never()).getAdventureSettings();
            assertEquals(0, dispatcher.snapshot().betweenTickBarriers());

            primary.set(false);
            entry.handleDataPacket(vehicle);
            primary.set(true);
            dispatcher.enableBarrierDiagnostics();
            assertEquals(0, dispatcher.drain(true, 2, Long.MAX_VALUE));
            ServerInputDispatcher.BarrierReport report = dispatcher.drainBarrierReport();
            assertEquals(1, report.types().size());
            assertEquals("ResourcePackClientResponsePacket16/COMPLETED", report.types().getFirst().type());
            assertEquals("STATIC", report.types().getFirst().reason());
            assertEquals(1, dispatcher.drainWorldTick(3));
            verify(player).handleInputDataPacket(exit, 0L);
            assertEquals(0, dispatcher.snapshot().queuedTasks());
        }
    }

    private static RedirectPacket redirect(UUID sessionId, byte[] buffer) {
        RedirectPacket redirect = new RedirectPacket();
        redirect.sessionId = sessionId;
        redirect.protocol = 975;
        redirect.mcpeBuffer = buffer;
        redirect.receivedNanos = 123L;
        return redirect;
    }
}
