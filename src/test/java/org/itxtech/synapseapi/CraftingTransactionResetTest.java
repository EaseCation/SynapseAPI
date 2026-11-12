package org.itxtech.synapseapi;

import cn.nukkit.inventory.transaction.CraftingTransaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * 合成界面重置时作废未执行的多包合成（NI-CRAFT-STALE-TRANSACTION-01）：
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
}
