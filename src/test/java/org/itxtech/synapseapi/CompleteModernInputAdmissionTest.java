package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.input.InputValidationResult;
import cn.nukkit.network.input.PlayerInputProcessor;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.ItemStackRequest;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompleteModernInputAdmissionTest {
    @BeforeAll
    static void initialize() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_20.getProtocolStart();
        }
    }

    @Test
    void realTargetDecodersKeepStackRequestAndEmoteWithoutStageBarrier() {
        for (AbstractProtocol protocol : new AbstractProtocol[]{AbstractProtocol.PROTOCOL_121_120, AbstractProtocol.PROTOCOL_121_124,
                AbstractProtocol.PROTOCOL_121_130, AbstractProtocol.PROTOCOL_126_20}) {
            boolean nativeClient = protocol != AbstractProtocol.PROTOCOL_126_20;
            PlayerAuthInputPacket116220 input = decode(protocol, nativeClient, 0);
            assertEquals(nativeClient ? 2 : 0, input.getNeteaseFlagsVersion());
            assertTrue(input.hasFlag(PlayerAuthInputFlags.EMOTING));
            assertTrue(input.hasFlag(PlayerAuthInputFlags.SNEAK_CURRENT_RAW));
            assertNotNull(input.getItemStackRequest());
            assertEquals(0, input.getItemStackRequest().actions.length);
            assertFalse(input.getInputFlags().get(66 + input.getNeteaseFlagsVersion()));
            assertTrue(BetweenTickPackets.supports(input));
        }
    }

    @Test
    void olderModernPacketClassKeepsItsOriginalWireLayout() {
        PlayerAuthInputPacket116220 input = decode(AbstractProtocol.PROTOCOL_116_220, false, 0);
        assertNotNull(input.getItemStackRequest());
        assertEquals(0, input.getItemStackRequest().actions.length);
        assertTrue(BetweenTickPackets.supports(input));
    }

    @Test
    void originalDecoderStillRejectsOversizedStackActionsBeforeAdmission() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> decode(AbstractProtocol.PROTOCOL_121_124, true, 61));
    }

    @Test
    void originalHandlerIgnoresStackRequestAndKeepsEmotingState() {
        check("stack", true);
    }

    @Test
    void containerActionIsConvertedButNeverCommitted() {
        check("container", true);
    }

    @Test
    void dropActionCannotBecomeAnInventoryCommit() {
        check("drop", true);
    }

    @Test
    void craftingMarkerCannotReviveDisabledTransactionCode() {
        check("craft", true);
    }

    @Test
    void allSpecialMarkersLeavePendingTransactionsUntouched() {
        check("special", true);
    }

    @Test
    void cancelledWholeInputStillValidatesOnceAndCannotReachPayload() {
        check("cancel", true);
    }

    @Test
    void rejectedMovementCannotReachPayloadConversion() {
        check("reject", true);
    }

    @Test
    void disabledOriginalHandlerKeepsIgnoredPayloadAndEmoting() {
        check("stack", false);
        check("container", false);
        check("special", false);
    }

    private void check(String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        PlayerInputProcessor processor = mock(PlayerInputProcessor.class);
        when(server.getPluginManager()).thenReturn(plugins);
        when(server.getPlayerInputProcessor()).thenReturn(processor);
        PlayerInventory inventory = new PlayerInventory(player);
        inventory.slots.put(0, new Item(ItemID.STONE, 0, 8));
        player.configure(server, inventory);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets();
        doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(false).when(player).isJavaClient();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        doNothing().when(player).onClientTickUpdated(anyLong());
        doReturn(inventory).when(player).getWindowById(anyInt());
        List<String> order = new ArrayList<>();
        doAnswer(call -> {
            order.add("event");
            if (variant.equals("cancel")) ((DataPacketReceiveEvent) call.getArgument(0)).setCancelled();
            return null;
        }).when(plugins).callEvent(any());
        when(processor.validateReceivedPacket(any(), any(), anyBoolean())).thenAnswer(call -> {
            assertEquals("event", order.getLast());
            order.add("validate");
            return InputValidationResult.ACCEPTED;
        });
        doAnswer(call -> { order.add("commit"); return !variant.equals("reject"); })
                .when(player).commitReceivedMovement();
        PlayerAuthInputPacket116220 packet = new PlayerAuthInputPacket116220();
        packet.inputFlags = new BitSet();
        packet.inputFlags.set(PlayerAuthInputFlags.EMOTING);
        packet.inputFlags.set(PlayerAuthInputFlags.PERFORM_ITEM_STACK_REQUEST);
        packet.itemStackRequest = mock(ItemStackRequest.class);
        NetworkInventoryAction action = spy(new NetworkInventoryAction());
        action.sourceType = NetworkInventoryAction.SOURCE_CONTAINER;
        action.windowId = 22;
        action.oldItem = new Item(ItemID.STONE, 0, 8);
        action.newItem = new Item(ItemID.DIAMOND, 0, 64);
        if (variant.equals("drop")) {
            action.sourceType = NetworkInventoryAction.SOURCE_WORLD;
            action.inventorySlot = InventoryTransactionPacket.ACTION_MAGIC_SLOT_DROP_ITEM;
        }
        if (variant.equals("craft")) {
            action.sourceType = NetworkInventoryAction.SOURCE_TODO;
            action.windowId = NetworkInventoryAction.SOURCE_TYPE_CRAFTING_RESULT;
        }
        List<InventoryAction> converted = new ArrayList<>();
        doAnswer(call -> {
            InventoryAction value = spy((InventoryAction) call.callRealMethod());
            converted.add(value);
            return value;
        }).when(action).createInventoryAction(player);
        if (!variant.equals("stack")) {
            packet.inventoryActions = new NetworkInventoryAction[]{action};
            packet.inputFlags.set(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION);
            packet.useItemData = new UseItemData();
            packet.useItemData.actionType = Integer.MAX_VALUE;
            packet.useItemData.face = BlockFace.UP;
            packet.useItemData.blockPos = new BlockVector3(0, 0, 0);
            packet.useItemData.clickPos = new Vector3f();
            packet.useItemData.itemInHand = action.oldItem;
        }
        packet.isCraftingPart = variant.equals("craft") || variant.equals("special");
        packet.isEnchantingPart = variant.equals("special");
        packet.isRepairItemPart = variant.equals("special");
        if (enabled) {
            assertTrue(BetweenTickPackets.supports(packet));
            assertTrue(BetweenTickPackets.canRunBetweenTicks(player, packet));
            ServerInputDispatcher dispatcher = new ServerInputDispatcher(Thread.currentThread(), 16, 1024, 16, 1024, error -> fail(error));
            ServerInputDispatcher.Session session = new ServerInputDispatcher.Session(UUID.randomUUID());
            dispatcher.offer(session, context -> {
                assertTrue(context.betweenTicks());
                player.handleDataPacket(packet);
            }, 1, true);
            dispatcher.offer(session, context -> order.add("follower"), 1, true);
            assertEquals(2, dispatcher.drain(true, 42, Long.MAX_VALUE));
        } else {
            player.handleDataPacket(packet);
            order.add("follower");
        }
        boolean payloadReached = !variant.equals("cancel") && !variant.equals("reject");
        assertEquals(!variant.equals("cancel"), player.emotingValue());
        assertEquals(payloadReached && !variant.equals("stack") ? 1 : 0, converted.size());
        assertEquals(ItemID.STONE, inventory.getItem(0).getId());
        assertEquals(8, inventory.getItem(0).getCount());
        for (InventoryAction value : converted) {
            verify(value, never()).execute(any());
            verify(value, never()).onPreExecute(any());
        }
        verifyNoInteractions(packet.itemStackRequest);
        player.assertPendingUnchanged();
        verify(processor, times(enabled ? 1 : 0)).validateReceivedPacket(any(), any(), anyBoolean());
        assertEquals("follower", order.getLast());
        verify(player, never()).onUpdate(anyInt());
    }

    private static PlayerAuthInputPacket116220 decode(AbstractProtocol protocol, boolean nativeClient, int actions) {
        BinaryStream wire = new BinaryStream();
        for (int i = 0; i < 8; i++) wire.putLFloat(0);
        int shift = nativeClient ? 2 : 0;
        BitSet flags = new BitSet();
        flags.set(PlayerAuthInputFlags.EMOTING);
        flags.set(PlayerAuthInputFlags.PERFORM_ITEM_STACK_REQUEST);
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            flags.set(PlayerAuthInputFlags.SNEAK_CURRENT_RAW + shift);
            flags.set(66 + shift);
        }
        // 使用真实线格式；最高组多余位由原解码器裁剪，不由调度器解释。
        for (int base = 0; base < flags.length(); base += 7) {
            int value = 0;
            for (int bit = 0; bit < 7; bit++) if (flags.get(base + bit)) value |= 1 << bit;
            wire.putByte((byte) (value | (base + 7 < flags.length() ? 128 : 0)));
        }
        wire.putUnsignedVarInt(PlayerAuthInputPacket116220.INPUT_MODE_MOUSE_KEYBOARD);
        wire.putUnsignedVarInt(PlayerAuthInputPacket116220.PLAY_MODE_NORMAL);
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119.getProtocolStart()) wire.putUnsignedVarInt(0);
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            wire.putLFloat(0); wire.putLFloat(0);
        }
        wire.putUnsignedVarLong(1);
        for (int i = 0; i < 3; i++) wire.putLFloat(0);
        if (nativeClient) wire.putBoolean(false);
        wire.putVarInt(-2);
        wire.putUnsignedVarInt(actions);
        wire.putUnsignedVarInt(0);
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_30.getProtocolStart()) wire.putLInt(0);
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_70.getProtocolStart()) {
            wire.putLFloat(0); wire.putLFloat(0);
        }
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            for (int i = 0; i < 3; i++) wire.putLFloat(0);
        }
        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            wire.putLFloat(0); wire.putLFloat(0);
        }
        PlayerAuthInputPacket116220 packet = new PlayerAuthInputPacket116220();
        packet.neteaseMode = nativeClient;
        packet.setHelper(protocol.getHelper());
        packet.setBuffer(wire.getBuffer(), 0);
        packet.decode();
        assertEquals(wire.getCount(), packet.getOffset());
        return packet;
    }

    static class ProbePlayer extends SynapsePlayer116 {
        private CraftingTransaction craft;
        private EnchantTransaction enchant;
        private RepairItemTransaction repair;

        ProbePlayer() {
            super(null, null, 1L, new InetSocketAddress("127.0.0.1", 19132));
        }

        void configure(Server server, PlayerInventory inventory) {
            this.server = server;
            this.inventory = inventory;
            this.isSynapseLogin = true;
            this.spawned = true;
            craftingTransaction = craft = mock(CraftingTransaction.class);
            enchantTransaction = enchant = mock(EnchantTransaction.class);
            repairItemTransaction = repair = mock(RepairItemTransaction.class);
        }

        boolean emotingValue() {
            return emoting;
        }

        void assertPendingUnchanged() {
            assertSame(craft, craftingTransaction);
            assertSame(enchant, enchantTransaction);
            assertSame(repair, repairItemTransaction);
            verifyNoInteractions(craft, enchant, repair);
        }
    }
}
