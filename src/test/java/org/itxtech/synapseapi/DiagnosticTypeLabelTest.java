package org.itxtech.synapseapi;

import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.PlayerHotbarPacket;
import cn.nukkit.network.protocol.types.ContainerIds;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.InteractPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.InventoryTransactionPacket116;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.ContainerClosePacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol121.protocol.ContainerClosePacket121;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.InteractPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerHotbarPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.ResourcePackClientResponsePacket16;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 屏障诊断标签只读包字段；每个已知子类型对应一个常量，未知取值归 OTHER。
 */
class DiagnosticTypeLabelTest {
    private static final String TX = "InventoryTransactionPacket116/";

    @Test
    void unlabeledPacketsKeepClassName() {
        assertEquals("AnimatePacket", BetweenTickPackets.diagnosticType(new AnimatePacket()));
    }

    @Test
    void interactActionsAreLabeledPerProtocolClass() {
        List<String> names = List.of("NONE", "INTERACT", "DAMAGE", "VEHICLE_EXIT", "MOUSEOVER", "OPEN_NPC", "OPEN_INVENTORY");
        for (int action = 0; action < names.size(); action++) {
            InteractPacket121130 modern = new InteractPacket121130();
            modern.action = action;
            assertEquals("InteractPacket121130/" + names.get(action), BetweenTickPackets.diagnosticType(modern));
            InteractPacket113 legacy = new InteractPacket113();
            legacy.action = action;
            assertEquals("InteractPacket113/" + names.get(action), BetweenTickPackets.diagnosticType(legacy));
        }
        InteractPacket121130 unknown = new InteractPacket121130();
        unknown.action = 7;
        assertEquals("InteractPacket121130/OTHER", BetweenTickPackets.diagnosticType(unknown));
        unknown.action = -1;
        assertEquals("InteractPacket121130/OTHER", BetweenTickPackets.diagnosticType(unknown));
    }

    @Test
    void containerCloseWindowCategories() {
        int[][] cases = {{ContainerIds.INVENTORY, 0}, {ContainerIds.FIRST, 1}, {ContainerIds.LAST, 1},
                {ContainerIds.OFFHAND, 2}, {ContainerIds.NETEASE_UI, 2}, {ContainerIds.NONE, 3}, {110, 3}};
        String[] names = {"INVENTORY", "WINDOW", "FIXED", "OTHER"};
        for (int[] c : cases) {
            ContainerClosePacket121 modern = new ContainerClosePacket121();
            modern.windowId = c[0];
            assertEquals("ContainerClosePacket121/" + names[c[1]], BetweenTickPackets.diagnosticType(modern));
            ContainerClosePacket116100 legacy = new ContainerClosePacket116100();
            legacy.windowId = c[0];
            assertEquals("ContainerClosePacket116100/" + names[c[1]], BetweenTickPackets.diagnosticType(legacy));
        }
    }

    @Test
    void equipmentAndHotbarWindowCategories() {
        int[] windows = {ContainerIds.INVENTORY, ContainerIds.OFFHAND, ContainerIds.ARMOR};
        String[] names = {"INVENTORY", "OFFHAND", "OTHER"};
        for (int i = 0; i < windows.length; i++) {
            MobEquipmentPacket equipment = new MobEquipmentPacket();
            equipment.windowId = windows[i];
            assertEquals("MobEquipmentPacket/" + names[i], BetweenTickPackets.diagnosticType(equipment));
            PlayerHotbarPacket hotbar = new PlayerHotbarPacket();
            hotbar.windowId = windows[i];
            assertEquals("PlayerHotbarPacket/" + names[i], BetweenTickPackets.diagnosticType(hotbar));
            PlayerHotbarPacket14 legacy = new PlayerHotbarPacket14();
            legacy.windowId = windows[i];
            assertEquals("PlayerHotbarPacket14/" + names[i], BetweenTickPackets.diagnosticType(legacy));
        }
    }

    @Test
    void resourcePackResponseStatus() {
        String[] names = {"NONE", "REFUSED", "SEND_PACKS", "HAVE_ALL_PACKS", "COMPLETED", "OTHER"};
        for (int status = 0; status < names.length; status++) {
            ResourcePackClientResponsePacket16 response = new ResourcePackClientResponsePacket16();
            response.responseStatus = (byte) status;
            assertEquals("ResourcePackClientResponsePacket16/" + names[status], BetweenTickPackets.diagnosticType(response));
        }
    }

    @Test
    void transactionPartsTakePrecedence() {
        InventoryTransactionPacket116 packet = transaction(InventoryTransactionPacket116.TYPE_NORMAL, null);
        packet.isCraftingPart = true;
        assertEquals(TX + "CRAFT_PART", BetweenTickPackets.diagnosticType(packet));
        packet.isCraftingPart = false;
        packet.isEnchantingPart = true;
        assertEquals(TX + "ENCHANT_PART", BetweenTickPackets.diagnosticType(packet));
        packet.isEnchantingPart = false;
        packet.isRepairItemPart = true;
        assertEquals(TX + "REPAIR_PART", BetweenTickPackets.diagnosticType(packet));
    }

    @Test
    void normalTransactionSources() {
        int container = NetworkInventoryAction.SOURCE_CONTAINER;
        int creative = NetworkInventoryAction.SOURCE_CREATIVE;
        int world = NetworkInventoryAction.SOURCE_WORLD;
        int todo = NetworkInventoryAction.SOURCE_TODO;
        assertEquals(TX + "NORMAL_CONTAINER", normal(container, container));
        assertEquals(TX + "NORMAL_CONTAINER", normal());
        assertEquals(TX + "NORMAL_CREATIVE", normal(creative, container));
        assertEquals(TX + "NORMAL_DROP", normal(container, world));
        assertEquals(TX + "NORMAL_DROP", normal(todo, world));
        assertEquals(TX + "NORMAL_SPECIAL", normal(container, todo));
        assertEquals(TX + "NORMAL_SPECIAL", normal(creative, NetworkInventoryAction.SOURCE_CRAFT_SLOT));
        InventoryTransactionPacket116 broken = transaction(InventoryTransactionPacket116.TYPE_NORMAL, null);
        broken.actions = null;
        assertEquals(TX + "OTHER", BetweenTickPackets.diagnosticType(broken));
        broken.actions = new NetworkInventoryAction[]{null};
        assertEquals(TX + "OTHER", BetweenTickPackets.diagnosticType(broken));
    }

    @Test
    void mismatchAndActionTypes() {
        assertEquals(TX + "MISMATCH", label(InventoryTransactionPacket116.TYPE_MISMATCH, null));
        InventoryTransactionPacket116 mismatch = transaction(InventoryTransactionPacket116.TYPE_MISMATCH, null);
        mismatch.actions = actions(NetworkInventoryAction.SOURCE_CONTAINER);
        assertEquals(TX + "MISMATCH_ACTIONS", BetweenTickPackets.diagnosticType(mismatch));

        String[] use = {"USE_CLICK_BLOCK", "USE_CLICK_AIR", "USE_BREAK_BLOCK", "USE_AS_ATTACK", "OTHER"};
        for (int action = 0; action < use.length; action++) {
            UseItemData data = new UseItemData();
            data.actionType = action;
            assertEquals(TX + use[action], label(InventoryTransactionPacket116.TYPE_USE_ITEM, data));
        }
        String[] entity = {"ENTITY_INTERACT", "ENTITY_ATTACK", "ENTITY_ITEM_INTERACT", "OTHER"};
        for (int action = 0; action < entity.length; action++) {
            UseItemOnEntityData data = new UseItemOnEntityData();
            data.actionType = action;
            assertEquals(TX + entity[action], label(InventoryTransactionPacket116.TYPE_USE_ITEM_ON_ENTITY, data));
        }
        String[] release = {"RELEASE", "CONSUME", "OTHER"};
        for (int action = 0; action < release.length; action++) {
            ReleaseItemData data = new ReleaseItemData();
            data.actionType = action;
            assertEquals(TX + release[action], label(InventoryTransactionPacket116.TYPE_RELEASE_ITEM, data));
        }
        assertEquals(TX + "OTHER", label(InventoryTransactionPacket116.TYPE_USE_ITEM, new ReleaseItemData()));
        assertEquals(TX + "OTHER", label(9, null));
    }

    @Test
    void labelsAreSharedConstants() {
        InteractPacket121130 first = new InteractPacket121130();
        first.action = InteractPacket121130.ACTION_OPEN_INVENTORY;
        InteractPacket121130 second = new InteractPacket121130();
        second.action = InteractPacket121130.ACTION_OPEN_INVENTORY;
        assertSame(BetweenTickPackets.diagnosticType(first), BetweenTickPackets.diagnosticType(second));
        assertSame(normal(NetworkInventoryAction.SOURCE_CONTAINER), normal(NetworkInventoryAction.SOURCE_CONTAINER));
    }

    private static String normal(int... sources) {
        InventoryTransactionPacket116 packet = transaction(InventoryTransactionPacket116.TYPE_NORMAL, null);
        packet.actions = actions(sources);
        return BetweenTickPackets.diagnosticType(packet);
    }

    private static String label(int type, TransactionData data) {
        return BetweenTickPackets.diagnosticType(transaction(type, data));
    }

    private static InventoryTransactionPacket116 transaction(int type, TransactionData data) {
        InventoryTransactionPacket116 packet = new InventoryTransactionPacket116();
        packet.transactionType = type;
        packet.transactionData = data;
        packet.actions = new NetworkInventoryAction[0];
        return packet;
    }

    private static NetworkInventoryAction[] actions(int... sources) {
        NetworkInventoryAction[] actions = new NetworkInventoryAction[sources.length];
        for (int i = 0; i < sources.length; i++) {
            actions[i] = new NetworkInventoryAction();
            actions[i].sourceType = sources[i];
        }
        return actions;
    }
}
