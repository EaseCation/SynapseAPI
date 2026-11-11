package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.item.Item;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.Network;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.BlockPickRequestPacket;
import cn.nukkit.network.protocol.BookEditPacket;
import cn.nukkit.network.protocol.ClientToServerHandshakePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.InteractPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * 全包准入 M2/M3：关窗、开背包等低风险容器包与无副作用/加入类包按包编号整包放行。
 * 分类按各目标协议实际注册的类核对（860 旧类、898 主力、975 JE、1001 新类）；
 * 队列语义（突发、前序屏障、同玩家顺序）与包类型无关，在此用代表包验证。
 */
class ContainerAndJoinAdmissionTest {
    private static final int[] TARGET_PROTOCOLS = {860, 898, 975, 1001};
    private static final int[] ADMITTED_IDS = {
            ProtocolInfo.CONTAINER_CLOSE_PACKET, ProtocolInfo.PLAYER_HOTBAR_PACKET,
            ProtocolInfo.BLOCK_PICK_REQUEST_PACKET, ProtocolInfo.ACTOR_PICK_REQUEST_PACKET,
            ProtocolInfo.BOOK_EDIT_PACKET, ProtocolInfo.ITEM_STACK_REQUEST_PACKET,
            ProtocolInfo.PACKET_WITHDRAW_FURNACE_XP,
            ProtocolInfo.SERVERBOUND_LOADING_SCREEN_PACKET, ProtocolInfo.SERVERBOUND_DIAGNOSTICS_PACKET,
            ProtocolInfo.UPDATE_CLIENT_OPTIONS_PACKET, ProtocolInfo.EMOTE_LIST_PACKET,
            ProtocolInfo.POSITION_TRACKING_DB_CLIENT_REQUEST_PACKET, ProtocolInfo.REQUEST_PERMISSIONS_PACKET,
            ProtocolInfo.ANVIL_DAMAGE_PACKET, ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET,
            ProtocolInfo.PACKET_NETEASE_JSON, ProtocolInfo.PACKET_SYNC_SKIN,
            ProtocolInfo.PACKET_VIOLATION_WARNING_PACKET, ProtocolInfo.SET_LOCAL_PLAYER_AS_INITIALIZED_PACKET,
            ProtocolInfo.BOSS_EVENT_PACKET, ProtocolInfo.PLAYER_SKIN_PACKET,
            ProtocolInfo.CLIENT_CACHE_STATUS_PACKET, ProtocolInfo.REQUEST_CHUNK_RADIUS_PACKET};

    @BeforeAll
    static void protocols() {
        Block.init();
        Item.init();
        Attribute.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
            PacketRegister.init();
        }
    }

    @Test
    void registeredClassesOfEveryTargetProtocolAreAdmitted() {
        // 未按协议注册的编号回落到 Nukkit 原版注册表；这里只提供本组需要的原版类。
        Map<Integer, Supplier<DataPacket>> vanilla = Map.of(
                ProtocolInfo.BLOCK_PICK_REQUEST_PACKET, BlockPickRequestPacket::new,
                ProtocolInfo.BOOK_EDIT_PACKET, BookEditPacket::new,
                ProtocolInfo.PLAYER_HOTBAR_PACKET, PlayerHotbarPacket::new,
                ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET, ClientToServerHandshakePacket::new,
                ProtocolInfo.MOB_EQUIPMENT_PACKET, MobEquipmentPacket::new,
                ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET, ResourcePackClientResponsePacket::new);
        Server server = mock(Server.class);
        Network network = mock(Network.class);
        when(server.getNetwork()).thenReturn(network);
        when(network.getPacket(anyInt())).thenAnswer(call -> {
            Supplier<DataPacket> factory = vanilla.get((int) call.getArgument(0));
            return factory == null ? null : factory.get();
        });
        try (MockedStatic<Server> global = mockStatic(Server.class)) {
            global.when(Server::getInstance).thenReturn(server);
            for (int protocol : TARGET_PROTOCOLS) {
                for (int id : ADMITTED_IDS) {
                    DataPacket packet = PacketRegister.getPacket(id, protocol);
                    assertNotNull(packet, "protocol " + protocol + " id " + id);
                    assertEquals(id, packet.pid());
                    assertTrue(BetweenTickPackets.supports(packet), protocol + " " + packet.getClass().getSimpleName());
                }
                DataPacket equipment = PacketRegister.getPacket(ProtocolInfo.MOB_EQUIPMENT_PACKET, protocol);
                assertFalse(BetweenTickPackets.supports(equipment), "missing item keeps the barrier");
                DataPacket response = PacketRegister.getPacket(ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET, protocol);
                ResourcePackClientResponsePacket16 status = assertInstanceOf(ResourcePackClientResponsePacket16.class, response);
                for (byte value = 0; value <= ResourcePackClientResponsePacket16.STATUS_COMPLETED; value++) {
                    status.responseStatus = value;
                    assertEquals(value == ResourcePackClientResponsePacket16.STATUS_SEND_PACKS
                                    || value == ResourcePackClientResponsePacket16.STATUS_HAVE_ALL_PACKS,
                            BetweenTickPackets.supports(status), "resource pack status " + value);
                }
                DataPacket interact = PacketRegister.getPacket(ProtocolInfo.INTERACT_PACKET, protocol);
                // 860 及 JE 走旧类，898 起为新类；两者都只保留下车为屏障。
                if (protocol >= AbstractProtocol.PROTOCOL_121_130.getProtocolStart()) {
                    InteractPacket121130 modern = assertInstanceOf(InteractPacket121130.class, interact);
                    modern.action = InteractPacket121130.ACTION_OPEN_INVENTORY;
                    assertTrue(BetweenTickPackets.supports(modern));
                    modern.action = InteractPacket121130.ACTION_VEHICLE_EXIT;
                    assertFalse(BetweenTickPackets.supports(modern));
                } else {
                    InteractPacket113 legacy = assertInstanceOf(InteractPacket113.class, interact);
                    legacy.action = InteractPacket113.ACTION_OPEN_INVENTORY;
                    assertTrue(BetweenTickPackets.supports(legacy));
                    legacy.action = InteractPacket113.ACTION_VEHICLE_EXIT;
                    assertFalse(BetweenTickPackets.supports(legacy));
                }
            }
        }
    }

    @Test
    void burstOfAdmittedWorkDrainsBetweenTicksInOrder() {
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 64, 4096, 64, 4096,
                error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        DataPacket close = PacketRegister.getPacket(ProtocolInfo.CONTAINER_CLOSE_PACKET, 898);
        for (int i = 0; i < 20; i++) {
            int index = i;
            dispatcher.offer(session, context -> {
                assertTrue(context.betweenTicks());
                order.add("close" + index);
            }, 1, BetweenTickPackets.supports(close));
            dispatcher.offer(session, context -> order.add("follower" + index), 1, true);
        }
        assertEquals(40, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertEquals(0, dispatcher.snapshot().queuedTasks());
        assertEquals(0, dispatcher.snapshot().betweenTickBarriers());
        for (int i = 0; i < 20; i++) {
            assertEquals("close" + i, order.get(i * 2));
            assertEquals("follower" + i, order.get(i * 2 + 1));
        }
    }

    @Test
    void earlierBarrierStillHoldsAdmittedFollowers() {
        ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024,
                error -> fail(error));
        ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
        List<String> order = new ArrayList<>();
        InteractPacket121130 exit = new InteractPacket121130();
        exit.action = InteractPacket121130.ACTION_VEHICLE_EXIT;
        InteractPacket121130 open = new InteractPacket121130();
        open.action = InteractPacket121130.ACTION_OPEN_INVENTORY;
        dispatcher.offer(session, context -> order.add("exit"), 1, BetweenTickPackets.supports(exit));
        dispatcher.offer(session, context -> order.add("open"), 1, BetweenTickPackets.supports(open));
        assertEquals(0, dispatcher.drain(true, 7, Long.MAX_VALUE));
        assertTrue(order.isEmpty());
        assertEquals(2, dispatcher.drainWorldTick(8));
        assertEquals(List.of("exit", "open"), order);
    }
}
