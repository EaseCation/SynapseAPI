package org.itxtech.synapseapi;

import cn.nukkit.AdventureSettings;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.transaction.CraftingTransaction;
import cn.nukkit.inventory.transaction.EnchantTransaction;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBow;
import cn.nukkit.item.ItemBread;
import cn.nukkit.item.ItemCrossbow;
import org.itxtech.synapseapi.multiprotocol.common.PlayerAuthInputFlags;
import org.itxtech.synapseapi.multiprotocol.protocol116220.protocol.PlayerAuthInputPacket116220;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovementAttributionTest {
    private record Gate(InputBlockReason reason, Consumer<ItemActionAdmissionTest.TestPlayer> setup) { }

    @Test
    void individualGatesAndFirstFailureMatchTheOriginalShortCircuitOrder() {
        List<Gate> gates = List.of(
                new Gate(InputBlockReason.NOT_SPAWNED, p -> p.spawned(false)),
                new Gate(InputBlockReason.OFFLINE, p -> doReturn(false).when(p).isOnline()),
                new Gate(InputBlockReason.DEAD, p -> doReturn(false).when(p).isAlive()),
                new Gate(InputBlockReason.RIDING, p -> p.ride(mock(Entity.class))),
                new Gate(InputBlockReason.MOVEMENT_AUTHORITY_DISABLED, p -> doReturn(false).when(p).isServerAuthoritativeMovementEnabled()),
                new Gate(InputBlockReason.PACKET_SEQUENCE_UNSUPPORTED, p -> doReturn(false).when(p).supportsPacketSequences()),
                new Gate(InputBlockReason.INPUT_PROCESSOR_MISSING, p -> when(p.testServer().getPlayerInputProcessor()).thenReturn(null)),
                new Gate(InputBlockReason.SLEEPING, p -> doReturn(true).when(p).isSleeping()),
                new Gate(InputBlockReason.USING_JAVA_ITEM, p -> { doReturn(true).when(p).isUsingItem(); doReturn(true).when(p).isJavaClient(); }),
                new Gate(InputBlockReason.USING_OFFHAND_ITEM, p -> { doReturn(true).when(p).isUsingItem(); doReturn(true).when(p).isUsingOffhandItem(); }),
                new Gate(InputBlockReason.USING_ITEM_WITHOUT_INVENTORY, p -> { doReturn(true).when(p).isUsingItem(); p.useInventory(null); }),
                new Gate(InputBlockReason.USING_NON_STANDARD_BOW, p -> { doReturn(true).when(p).isUsingItem(); inventory(p, new ItemBread()); }),
                new Gate(InputBlockReason.SWIMMING, p -> doReturn(true).when(p).isSwimming()),
                new Gate(InputBlockReason.GLIDING, p -> doReturn(true).when(p).isGliding()),
                new Gate(InputBlockReason.CRAWLING, p -> doReturn(true).when(p).isCrawling()),
                new Gate(InputBlockReason.FLYING, p -> when(p.getAdventureSettings().get(AdventureSettings.Type.FLYING)).thenReturn(true)),
                new Gate(InputBlockReason.SPIN_ATTACK, p -> doReturn(true).when(p).getDataFlag(Entity.DATA_FLAG_SPIN_ATTACK)));
        for (int first = 0; first < gates.size(); first++) {
            for (int second = first; second < gates.size(); second++) {
                // 库存不存在与后续库存设置互斥；其余任意双条件均要求首个失败优先。
                if (first == 10 && second == 11) continue;
                ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
                inventory(player, new ItemBow());
                gates.get(second).setup.accept(player);
                gates.get(first).setup.accept(player);
                assertEquals(gates.get(first).reason, player.movementBlockReason());
                assertFalse(player.canProcessMovementBetweenTicks());
                verify(player, never()).onUpdate(anyInt());
                verify(player, never()).setUsingItem(anyBoolean());
            }
        }
    }

    private static void inventory(ItemActionAdmissionTest.TestPlayer p, Item item) {
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInHand()).thenReturn(item);
        p.useInventory(inventory);
    }

    @Test
    void bowExceptionAndEmbeddedActionKeepTheirExistingScope() {
        ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
        PlayerAuthInputPacket116220 input = mock(PlayerAuthInputPacket116220.class);
        assertEquals(InputBlockReason.NONE, BetweenTickPackets.movementBlockReason(player, input));
        doReturn(true).when(player).isUsingItem();
        inventory(player, new ItemBow());
        assertEquals(InputBlockReason.NONE, BetweenTickPackets.movementBlockReason(player, input));
        when(input.getUseItemData()).thenReturn(new UseItemData());
        assertEquals(InputBlockReason.USING_WITH_EMBEDDED_ACTION, BetweenTickPackets.movementBlockReason(player, input));
        when(input.getUseItemData()).thenReturn(null);
        when(input.hasFlag(PlayerAuthInputFlags.MISSED_SWING)).thenReturn(true);
        assertEquals(InputBlockReason.USING_WITH_EMBEDDED_ACTION, BetweenTickPackets.movementBlockReason(player, input));
        inventory(player, new ItemCrossbow());
        assertEquals(InputBlockReason.USING_NON_STANDARD_BOW, BetweenTickPackets.movementBlockReason(player, input));
        inventory(player, new ItemBow() { });
        assertEquals(InputBlockReason.USING_NON_STANDARD_BOW, BetweenTickPackets.movementBlockReason(player, input));
    }

    @Test
    void embeddedActionReportsTheActualPendingBoundaryWithoutRestrictingPureMovement() {
        List<Gate> gates = List.of(
                new Gate(InputBlockReason.INPUT_NOT_ACCEPTING, p -> doReturn(false).when(p).isAcceptingInputPackets()),
                new Gate(InputBlockReason.TELEPORT_PENDING, ItemActionAdmissionTest.TestPlayer::awaitTeleport),
                new Gate(InputBlockReason.CRAFTING_PENDING, p -> p.pendingCraft(mock(CraftingTransaction.class))),
                new Gate(InputBlockReason.ENCHANTING_PENDING, p -> p.pendingEnchant(mock(EnchantTransaction.class))),
                new Gate(InputBlockReason.REPAIR_PENDING, p -> p.pendingRepair(mock(RepairItemTransaction.class))));
        for (Gate gate : gates) {
            ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
            PlayerAuthInputPacket116220 input = mock(PlayerAuthInputPacket116220.class);
            gate.setup.accept(player);
            assertEquals(InputBlockReason.NONE, BetweenTickPackets.movementBlockReason(player, input));
            when(input.getUseItemData()).thenReturn(new UseItemData());
            assertEquals(gate.reason, BetweenTickPackets.movementBlockReason(player, input));
            assertFalse(BetweenTickPackets.canRunBetweenTicks(player, input));
        }
    }

    @Test
    void diagnosticDoesNotReadPastFirstFailure() {
        ItemActionAdmissionTest.TestPlayer player = ItemActionAdmissionTest.player();
        player.spawned(false);
        clearInvocations(player);
        assertEquals(InputBlockReason.NOT_SPAWNED, player.movementBlockReason());
        verify(player, never()).isOnline();
        verify(player, never()).isUsingItem();
        verify(player, never()).getAdventureSettings();
    }
}
