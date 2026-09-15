package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol12630.protocol.InventoryTransactionPacket12630;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BlockPlacementInventoryResyncTest {
    @BeforeAll
    static void initializeItems() {
        Block.init();
        Item.init();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            AbstractProtocol.PROTOCOL_126_30.getProtocolStart();
        }
    }

    @Test
    void modernClientFailureResyncsEvenWhenRequestCountMatchesServer() {
        verifyFailureResync(true);
    }

    @Test
    void legacyClientFailureResyncsEvenWhenRequestCountMatchesServer() {
        verifyFailureResync(false);
    }

    private void verifyFailureResync(boolean modern) {
        TestPlayer player = mock(TestPlayer.class, CALLS_REAL_METHODS);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Level level = mock(Level.class);
        int protocol = modern ? AbstractProtocol.PROTOCOL_126_30.getProtocolStart()
                : AbstractProtocol.PROTOCOL_121_20.getProtocolStart();
        player.configure(mock(Server.class), level, inventory, protocol);
        doReturn(true).when(player).isInitialized();
        doReturn(true).when(player).isAlive();
        doReturn(false).when(player).isJavaClient();
        doReturn(false).when(player).isNetEaseClient();
        doReturn(protocol).when(player).getProtocol();
        doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        doReturn(false).when(player).setDataFlag(anyInt(), anyBoolean());
        doReturn(true).when(player).canInteract(any(Vector3.class), anyDouble());
        Item held = Item.get(ItemID.STONE, 0, 2);
        when(inventory.getItemInHand()).thenReturn(held.clone());
        UseItemData data = new UseItemData();
        data.actionType = InventoryTransactionPacket.USE_ITEM_ACTION_CLICK_BLOCK;
        data.blockPos = new BlockVector3(0, 1, 0);
        data.face = BlockFace.UP;
        data.playerPos = new Vector3(0, 1, 0);
        data.clickPos = new Vector3f(0.5f, 1, 0.5f);
        data.itemInHand = held.clone();
        data.clientInteractPrediction = false;
        DataPacket packet;
        if (modern) {
            InventoryTransactionPacket12630 transaction = new InventoryTransactionPacket12630();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        } else {
            InventoryTransactionPacket116 transaction = new InventoryTransactionPacket116();
            transaction.transactionType = InventoryTransactionPacket.TYPE_USE_ITEM;
            transaction.transactionData = data;
            transaction.actions = new NetworkInventoryAction[0];
            packet = transaction;
        }
        player.handleDataPacket(packet);
        verify(inventory).sendHeldItem(player);
        verify(inventory, never()).setItemInHand(any());
        assertEquals(2, held.getCount());
    }

    static class TestPlayer extends SynapsePlayer116100 {
        TestPlayer(SourceInterface source, SynapseEntry entry, Long id, InetSocketAddress address) {
            super(source, entry, id, address);
        }

        void configure(Server server, Level level, PlayerInventory inventory, int protocol) {
            this.server = server;
            this.level = level;
            this.inventory = inventory;
            this.protocol = protocol;
            isSynapseLogin = true;
            spawned = true;
        }

        @Override
        protected boolean callPacketReceiveEvent(DataPacket packet) { return true; }
    }
}
