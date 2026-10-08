package org.itxtech.synapseapi;

import cn.nukkit.Server;
import cn.nukkit.inventory.InventorySlotReference;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlockItemSourceTest {
    private final BlockPlacementInventoryResyncTest.TestPlayer player = mock(BlockPlacementInventoryResyncTest.TestPlayer.class, CALLS_REAL_METHODS);
    private final PlayerInventory inventory = mock(PlayerInventory.class);
    private final InventorySlotReference source = mock(InventorySlotReference.class);
    private final Level level = mock(Level.class);
    private final Vector3 position = new Vector3(2, 10, 2);
    private final Item item = new Item(ItemID.STONE, 0, 8);

    @BeforeEach
    void prepare() {
        player.configure(mock(Server.class), level, inventory, 0);
        doReturn(true).when(player).isMainThreadInputEnabled();
        doReturn(true).when(player).canContinueItemUse(anyLong());
        doReturn(true).when(player).isOnline();
        doReturn(true).when(player).isInputSessionActive();
        doReturn(true).when(player).isSurvivalLike();
        doReturn(true).when(player).dataPacket(any());
        when(inventory.captureHeldItem()).thenReturn(source);
        when(source.getSnapshot()).thenAnswer(call -> item.clone());
        when(source.isCurrent()).thenReturn(true);
        when(source.isSelectedBy(inventory)).thenReturn(true);
        when(source.setItem(any())).thenReturn(true);
        when(player.getViewers()).thenReturn(Map.of());
    }

    @Test
    void consumptionAfterWorldEffectUsesTheOriginalSourceEvenIfSelectionChanged() {
        doAnswer(call -> {
            assertTrue(((BooleanSupplier) call.getArgument(8)).getAsBoolean());
            when(source.isSelectedBy(inventory)).thenReturn(false);
            Item remaining = item.clone();
            remaining.setCount(7);
            return remaining;
        }).when(level).useItemOn(eq(position), same(item), eq(BlockFace.UP), eq(0F), eq(0F), eq(0F), same(player), isNull(), any(BooleanSupplier.class));
        Item result = place();
        assertNotNull(result);
        assertEquals(7, result.getCount());
        verify(source).setItem(argThat(written -> written.getCount() == 7));
        verify(inventory, never()).setItemInHand(any());
    }

    @Test
    void cancelledOrInvalidatedWorldActionCannotConsumeEitherSlot() {
        doAnswer(call -> {
            when(source.isSelectedBy(inventory)).thenReturn(false);
            assertFalse(((BooleanSupplier) call.getArgument(8)).getAsBoolean());
            return null;
        }).when(level).useItemOn(eq(position), same(item), eq(BlockFace.UP), eq(0F), eq(0F), eq(0F), same(player), isNull(), any(BooleanSupplier.class));
        assertNull(place());
        verify(source, never()).setItem(any());
        verify(source).sendContents(player);
    }

    @Test
    void staleSourceBeforeTheActionNeverCallsTheWorld() {
        when(source.isCurrent()).thenReturn(false);
        assertNull(place());
        verifyNoInteractions(level);
        verify(source, never()).setItem(any());
    }

    @Test
    void replacedInventoryAtWritebackDoesNotRepeatTheWorldEffect() {
        Item remaining = item.clone();
        remaining.setCount(7);
        when(source.setItem(any())).thenReturn(false);
        doReturn(remaining).when(level).useItemOn(eq(position), same(item), eq(BlockFace.UP), eq(0F), eq(0F), eq(0F), same(player), isNull(), any(BooleanSupplier.class));
        assertSame(remaining, place());
        verify(level, times(1)).useItemOn(eq(position), same(item), eq(BlockFace.UP), eq(0F), eq(0F), eq(0F), same(player), isNull(), any(BooleanSupplier.class));
        verify(source).sendContents(player);
        verify(inventory, never()).setItemInHand(any());
    }

    @Test
    void disabledModeKeepsTheOriginalUnguardedLevelCall() {
        doReturn(false).when(player).isMainThreadInputEnabled();
        place();
        verify(level).useItemOn(position, item, BlockFace.UP, 0, 0, 0, player, null);
        verify(inventory, never()).captureHeldItem();
        verifyNoInteractions(source);
    }

    private Item place() {
        return player.useItemOnBlock(position, item, BlockFace.UP, 0, 0, 0, null);
    }
}
