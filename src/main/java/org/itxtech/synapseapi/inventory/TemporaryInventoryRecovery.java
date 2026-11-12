package org.itxtech.synapseapi.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * 保存关闭时尚未撤离的临时物品；恢复过程只在登录或主动打开物品栏时尝试一次。
 */
public final class TemporaryInventoryRecovery extends BaseInventory {
    public static final String NBT_KEY = "AuthoritativeInventoryRecovery";

    private final Player player;
    private boolean restoring;

    public TemporaryInventoryRecovery(Player player, ListTag<CompoundTag> contents) {
        super(player, InventoryType.UI, Collections.emptyMap(), contents.size());
        this.player = player;
        for (int index = 0; index < contents.size(); index++) {
            Item item = NBTIO.getItemHelper(contents.get(index).clone());
            if (!item.isNull()) {
                slots.put(index, item);
            }
        }
    }

    public static ListTag<CompoundTag> snapshot(List<Inventory> temporaryInventories,
                                                @Nullable TemporaryInventoryRecovery recovery) {
        ListTag<CompoundTag> saved = new ListTag<>(NBT_KEY);
        if (recovery != null) {
            appendContents(saved, recovery);
        }
        for (Inventory inventory : temporaryInventories) {
            appendContents(saved, inventory);
        }
        return saved;
    }

    private static void appendContents(ListTag<CompoundTag> saved, Inventory inventory) {
        for (Item item : inventory.getContents().values()) {
            if (!item.isNull()) {
                saved.add(NBTIO.putItemHelper(item.clone()).clone());
            }
        }
    }

    public boolean restore() {
        if (restoring || isEmpty()) {
            return false;
        }
        restoring = true;
        try {
            TransactionBuilder builder = new TransactionBuilder();
            TransactionBuilderInventory target = builder.getInventory(player.getInventory());
            TransactionBuilderInventory pending = builder.getInventory(this);
            for (Int2ObjectMap.Entry<Item> entry : getContents().int2ObjectEntrySet()) {
                Item[] remaining = target.addItem(entry.getValue());
                if (remaining.length == 0) {
                    pending.clear(entry.getIntKey());
                } else {
                    pending.setItem(entry.getIntKey(), remaining[0]);
                }
            }
            List<InventoryAction> actions = builder.generateActions();
            return !actions.isEmpty() && new InventoryTransaction(player, actions).execute();
        } finally {
            restoring = false;
        }
    }

    @Override
    public void sendSlot(int index, Player... players) {
    }

    @Override
    public void sendContents(Player... players) {
    }
}
