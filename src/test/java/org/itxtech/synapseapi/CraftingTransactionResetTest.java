package org.itxtech.synapseapi;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerUIInventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 合成界面重置或收到客户端关窗包时作废未执行的多包合成（NI-CRAFT-STALE-TRANSACTION-01）：
 * 否则之后放入合成格的普通事务与合成分片都会并入残留事务，合成持续失败直到重新登录。
 */
class CraftingTransactionResetTest {
    @Test
    void resettingTheCraftingGridAbandonsThePendingCraft() {
        ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
        CraftingTransaction pending = mock(CraftingTransaction.class);
        player.pendingCraft(pending);
        assertSame(pending, player.pendingCraft());
        player.resetCraftingGridType();
        assertNull(player.pendingCraft());
    }

    @Test
    void clientCloseAbandonsThePendingCraftAndResyncsOnlyOwnInventories() {
        ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
        PlayerInventory inventory = mock(PlayerInventory.class);
        PlayerUIInventory ui = mock(PlayerUIInventory.class);
        player.useInventory(inventory);
        doReturn(ui).when(player).getUIInventory();
        player.pendingCraft(mock(CraftingTransaction.class));

        player.abandonPendingCraftOnClientClose();

        assertNull(player.pendingCraft());
        verify(inventory).sendContents(player);
        verify(ui).sendContents(player);
        verify(player, never()).sendAllInventories();
    }

    @Test
    void clientCloseWithoutPendingCraftSendsNothing() {
        ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
        PlayerInventory inventory = mock(PlayerInventory.class);
        player.useInventory(inventory);

        player.abandonPendingCraftOnClientClose();

        verify(inventory, never()).sendContents(player);
        verify(player, never()).getUIInventory();
    }
}
