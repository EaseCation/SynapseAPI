package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.ItemUseHand;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3f;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPickaxeIron;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.input.InboundContext;
import cn.nukkit.network.input.ServerInputDispatcher;
import cn.nukkit.network.input.ServerInputTask;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.LoginChainData;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket.PlayerBlockAction;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockActionAdmissionTest {
    @BeforeAll static void initialize(){Block.init();Item.init();}
    @Test void startAndAbortStayInOneOriginalHandler() { check("abort", true); }
    @Test void startAndPredictCompleteOnce() { check("predict", true); }
    @Test void nativeReversedPredictStartUsesOriginalCompatibilityOrder() { check("reverse", true); }
    @Test void startAndStopUseRecordedTargetOnce() { check("stop", true); }
    @Test void currentTargetContinuationKeepsOriginalStartTime() { check("continue", true); }
    @Test void interactionCancellationCannotStartOrBreak() { check("cancel", true); }
    @Test void interactionEpochChangeStopsFollowingBlockAction() { check("epoch", true); }
    @Test void rejectedMovementCannotStartBlockActions() { check("move-reject", true); }
    @Test void farStartDoesNotReadWorldOrBreak() { check("far", true); }
    @Test void disabledModeKeepsOriginalNativeOrder() { check("reverse", false); }

    @Test void embeddedBreakUsesOriginalTargetAndWorldOnce() { check("embedded-break", true); }
    @Test void embeddedBreakWithoutTargetCannotBreak() { check("embedded-no-target", true); }
    @Test void disabledEmbeddedBreakKeepsOriginalTargetAndWorld() { check("embedded-break", false); }

    private void check(String variant, boolean enabled) {
        ProbePlayer player = mock(ProbePlayer.class, CALLS_REAL_METHODS);
        Server server = mock(Server.class);
        PluginManager plugins = mock(PluginManager.class);
        Level level = mock(Level.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Block target = spy(Block.get(BlockID.STONE));
        Block side = mock(Block.class);
        target.x=1; target.y=0; target.z=0;
        when(target.getId()).thenReturn(BlockID.STONE);
        doReturn(side).when(target).getSide(any(BlockFace.class));
        doReturn(1f).when(target).getBreakTime(any(), eq(player));
        when(level.getBlock(any(Vector3.class), eq(false))).thenReturn(target);
        when(level.getBlock(any(Vector3.class))).thenReturn(target);
        when(level.getExtraBlock(any(Vector3.class))).thenReturn(side);
        Item item = new ItemPickaxeIron();
        when(inventory.getItemInHand()).thenReturn(item);
        player.configure(server, level, inventory);
        when(server.getPluginManager()).thenReturn(plugins);
        doReturn(enabled).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).isOnline(); doReturn(true).when(player).isAlive();
        doReturn(true).when(player).isAcceptingInputPackets(); doReturn(true).when(player).isCurrentInputPosition();
        doReturn(true).when(player).isServerAuthoritativeMovementEnabled();
        doReturn(true).when(player).callPacketReceiveEvent(any());
        doReturn(!variant.equals("move-reject")).when(player).commitReceivedMovement();
        doReturn(true).when(player).canInteract(any(Vector3.class), anyDouble());
        doReturn(variant.equals("reverse")).when(player).isNetEaseClient();
        doReturn(false).when(player).isSurvival();
        doReturn(mock(LoginChainData.class)).when(player).getLoginChainData();
        doNothing().when(player).onClientTickUpdated(anyLong());
        doNothing().when(player).setUsingItem(anyBoolean());
        doNothing().when(player).resetCraftingGridType();
        when(level.useBreakOn(any(Vector3.class), any(), eq(item), eq(player), eq(true))).thenReturn(item);
        doAnswer(call -> {
            PlayerInteractEvent event=call.getArgument(0);
            if (variant.equals("cancel")) event.setCancelled();
            if (variant.equals("epoch")) doReturn(false).when(player).isCurrentInputPosition();
            return null;
        }).when(plugins).callEvent(any(PlayerInteractEvent.class));
        int x=variant.equals("far")?100:1;
        PlayerBlockAction start=new PlayerBlockAction(PlayerActionPacket.ACTION_START_BREAK,x,0,0,BlockFace.UP.getIndex());
        int next= switch(variant) {
            case "predict","reverse","cancel","epoch" -> PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY;
            case "stop" -> PlayerActionPacket.ACTION_STOP_BREAK;
            case "continue" -> PlayerActionPacket.ACTION_BLOCK_CONTINUE_DESTROY;
            default -> PlayerActionPacket.ACTION_ABORT_BREAK;
        };
        PlayerBlockAction finish=new PlayerBlockAction(next,x,0,0,BlockFace.UP.getIndex());
        PlayerBlockAction[] actions=variant.equals("reverse")?new PlayerBlockAction[]{finish,start}:new PlayerBlockAction[]{start,finish};
        PlayerAuthInputPacket116220 packet=mock(PlayerAuthInputPacket116220.class);
        when(packet.pid()).thenReturn(ProtocolInfo.PLAYER_AUTH_INPUT_PACKET);
        if (variant.startsWith("embedded-")) {
            actions = variant.equals("embedded-no-target") ? new PlayerBlockAction[0] : new PlayerBlockAction[]{start};
            UseItemData data = new UseItemData();
            data.actionType = InventoryTransactionPacket116.USE_ITEM_ACTION_BREAK_BLOCK;
            data.blockPos = new BlockVector3(1, 0, 0);
            data.face = BlockFace.UP;
            data.clickPos = new Vector3f();
            data.itemInHand = item;
            when(packet.getUseItemData()).thenReturn(data);
            doReturn(false).when(player).isJavaClient();
            doReturn(ItemUseHand.MAIN_HAND).when(player).setItemInteractionHand(any());
        }
        when(packet.getBlockActions()).thenReturn(actions);
        BitSet flags=new BitSet();flags.set(PlayerAuthInputFlags.PERFORM_BLOCK_ACTIONS);
        if (variant.startsWith("embedded-")) flags.set(PlayerAuthInputFlags.PERFORM_ITEM_INTERACTION);
        when(packet.getInputFlags()).thenReturn(flags);when(packet.hasFlag(anyInt())).thenCallRealMethod();
        long before=System.currentTimeMillis();
        if(enabled){
            ServerInputDispatcher dispatcher=new ServerInputDispatcher(Thread.currentThread(),16,1024,16,1024,error->fail(error));
            assertTrue(BetweenTickPackets.supports(packet));List<String> order=new ArrayList<>();
            dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()),new ServerInputTask(){
                @Override public boolean canRunBetweenTicks(){return BetweenTickPackets.canRunBetweenTicks(player,packet);}
                @Override public void run(InboundContext context){assertTrue(context.betweenTicks());player.handleDataPacket(packet);order.add("block-actions");}
            },1,true);
            dispatcher.offer(new ServerInputDispatcher.Session(UUID.randomUUID()),context->order.add("follower"),1,true);
            assertEquals(2,dispatcher.drain(true,42,Long.MAX_VALUE));assertEquals(List.of("block-actions","follower"),order);
        }else player.handleDataPacket(packet);
        int breaks=List.of("predict","reverse","stop","embedded-break").contains(variant)?1:0;
        verify(level,times(breaks)).useBreakOn(any(Vector3.class),any(),eq(item),eq(player),eq(true));
        if(List.of("cancel","epoch","move-reject","far","embedded-no-target").contains(variant))assertNull(player.breakingBlock);
        else assertTrue(player.lastBreak>=before&&player.lastBreak<=System.currentTimeMillis());
        if(variant.equals("continue"))assertSame(target,player.breakingBlock);
        if(variant.equals("abort"))assertNull(player.breakingBlock);
        if(variant.equals("reverse"))assertSame(start,actions[0]);
        verify(player,never()).onUpdate(anyInt());
    }

    @Test void originalLevelFastBreakAndCancellationPreventDestruction() {
        for(boolean cancel:new boolean[]{false,true}){
            Level level=mock(Level.class);Server server=mock(Server.class);PluginManager plugins=mock(PluginManager.class);
            ProbePlayer player=mock(ProbePlayer.class);Block block=mock(Block.class);Item item=mock(Item.class);Vector3 position=new Vector3(1,0,0);
            when(level.getServer()).thenReturn(server);when(server.getPluginManager()).thenReturn(plugins);when(level.getBlock(position)).thenReturn(block);
            when(player.canDestroy(block,item)).thenReturn(true);when(player.isSurvival()).thenReturn(true);when(block.isBreakable(item)).thenReturn(true);when(block.getBreakTime(item,player)).thenReturn(2f);when(block.getDrops(item,player)).thenReturn(new Item[0]);player.lastBreak=System.currentTimeMillis();
            doAnswer(call->{BlockBreakEvent e=call.getArgument(0);assertTrue(e.isFastBreak());if(cancel)e.setCancelled();return null;}).when(plugins).callEvent(any(BlockBreakEvent.class));
            doCallRealMethod().when(level).useBreakOn(position,BlockFace.UP,item,player,true);
            assertNull(level.useBreakOn(position,BlockFace.UP,item,player,true));
            verify(block,never()).onBreak(any(),any());verify(item,never()).useOn(any(Block.class));
        }
    }

    static class ProbePlayer extends SynapsePlayer116 {
        ProbePlayer(){super(null,null,1L,new InetSocketAddress("127.0.0.1",19132));}
        void configure(Server server,Level level,PlayerInventory inventory){this.server=server;this.level=level;this.inventory=inventory;this.spawned=true;this.isSynapseLogin=true;}
        @Override protected float getBaseOffset(){return 0;}
    }
}
