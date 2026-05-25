package org.itxtech.synapseapi.multiprotocol.protocol12630.protocol;

import cn.nukkit.block.Block;
import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.math.BlockFace;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.InventoryTransactionPacketInterface;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.LegacySetItemSlotData;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;

import javax.annotation.Nullable;

import static org.itxtech.synapseapi.SynapseSharedConstants.*;

@ToString
public class InventoryTransactionPacket12630 extends Packet12630 implements InventoryTransactionPacketInterface {
    private static final LegacySetItemSlotData[] EMPTY_LEGACY_DATA = new LegacySetItemSlotData[0];

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_MISMATCH = 1;
    public static final int TYPE_USE_ITEM = 2;
    public static final int TYPE_USE_ITEM_ON_ENTITY = 3;
    public static final int TYPE_RELEASE_ITEM = 4;

    /**
     * Right click item use on a surface like placing a block.
     */
    public static final int USE_ITEM_ACTION_CLICK_BLOCK = 0;
    /**
     * Start right click and hold style item use or potentially interact with nothing.
     */
    public static final int USE_ITEM_ACTION_CLICK_AIR = 1;
    /**
     * Block breaking like left click.
     */
    public static final int USE_ITEM_ACTION_BREAK_BLOCK = 2;
    /**
     * Left-click to instantaneously use an item instead of attacking.
     * Does not require a target block or entity.
     * @since 1.21.110
     */
    public static final int USE_ITEM_ACTION_USE_AS_ATTACK = 3;

    /**
     * Release right click and hold style item use, like firing a bow.
     */
    public static final int RELEASE_ITEM_ACTION_RELEASE = 0; //bow shoot
    /**
     * Finish right click and hold style item use, like charging a crossbow.
     */
    public static final int RELEASE_ITEM_ACTION_CONSUME = 1; //eat food, drink potion

    /**
     * Right click interact with entity.
     */
    public static final int USE_ITEM_ON_ENTITY_ACTION_INTERACT = 0;
    /**
     * Left click style attack of entity or elytra spin attack.
     */
    public static final int USE_ITEM_ON_ENTITY_ACTION_ATTACK = 1;
    public static final int USE_ITEM_ON_ENTITY_ACTION_ITEM_INTERACT = 2;


    public static final int ACTION_MAGIC_SLOT_DROP_ITEM = 0;
    public static final int ACTION_MAGIC_SLOT_PICKUP_ITEM = 1;

    public static final int ACTION_MAGIC_SLOT_CREATIVE_DELETE_ITEM = 0;
    public static final int ACTION_MAGIC_SLOT_CREATIVE_CREATE_ITEM = 1;

    public static final int USE_ITEM_TRIGGER_TYPE_UNKNOWN = 0;
    public static final int USE_ITEM_TRIGGER_TYPE_PLAYER_INPUT = 1;
    public static final int USE_ITEM_TRIGGER_TYPE_SIMULATION_TICK = 2;

    public int transactionType;
    public NetworkInventoryAction[] actions;
    public TransactionData transactionData;

    public int legacyRequestId;
    public LegacySetItemSlotData[] requestChangedSlots = EMPTY_LEGACY_DATA;

    /**
     * NOTE: THIS FIELD DOES NOT EXIST IN THE PROTOCOL, it's merely used for convenience for PocketMine-MP to easily
     * determine whether we're doing a crafting transaction.
     */
    public boolean isCraftingPart;
    public boolean isEnchantingPart;
    public boolean isRepairItemPart;

    public void setCraftingPart(boolean craftingPart) {
        isCraftingPart = craftingPart;
    }

    public boolean isCraftingPart() {
        return isCraftingPart;
    }

    public void setEnchantingPart(boolean enchantingPart) {
        isEnchantingPart = enchantingPart;
    }

    public boolean isEnchantingPart() {
        return isEnchantingPart;
    }

    @Override
    public void setRepairItemPart(boolean repairItemPart) {
        this.isRepairItemPart = repairItemPart;
    }

    @Override
    public boolean isRepairItemPart() {
        return this.isRepairItemPart;
    }

    @Override
    public boolean hasNetworkIds() {
        return false;
    }

    @Override
    public int pid() {
        return ProtocolInfo.INVENTORY_TRANSACTION_PACKET;
    }

    @Override
    public void encode() {
        AbstractProtocol protocol = (AbstractProtocol) helper.getProtocol();

        this.reset();

        this.putVarInt(this.legacyRequestId);

        boolean legacyRequestIdValid = this.legacyRequestId < 0 || this.legacyRequestId != 0 && (this.legacyRequestId & 1) == 0;
        this.putBoolean(legacyRequestIdValid);
        if (legacyRequestIdValid) {
            this.putUnsignedVarInt(this.requestChangedSlots.length);
            for (LegacySetItemSlotData requestChangedSlot : this.requestChangedSlots) {
                this.putByte(requestChangedSlot.containerId);

                this.putUnsignedVarInt(requestChangedSlot.changedSlotIndexes.length);
                this.put(requestChangedSlot.changedSlotIndexes);
            }
        }

        boolean hasTransaction = this.transactionData != null;
        this.putBoolean(hasTransaction);
        if (!hasTransaction) {
            return;
        }
        this.putUnsignedVarInt(this.transactionType);

        boolean hasActions = this.actions != null;
        this.putBoolean(hasActions);
        if (hasActions) {
            this.putUnsignedVarInt(this.actions.length);
            for (NetworkInventoryAction action : this.actions) {
                action.writeCereal(this);
            }
        }

        switch (this.transactionType) {
            case TYPE_NORMAL:
            case TYPE_MISMATCH:
                break;
            case TYPE_USE_ITEM:
                UseItemData useItemData = (UseItemData) this.transactionData;

                this.putVarInt(useItemData.actionType);
                this.putByte(useItemData.triggerType);
                this.putBlockVector3(useItemData.blockPos);
                this.putBlockFace(useItemData.face);
                this.putVarInt(useItemData.hotbarSlot);
                this.putItemStack(useItemData.itemInHand);
                this.putVector3f(useItemData.playerPos.asVector3f());
                this.putVector3f(useItemData.clickPos);
                Block block = useItemData.block;
                this.putUnsignedVarInt(block != null ? AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, neteaseMode, block.getId(), block.getItemSerializationMeta()) : 0);
                this.putByte(useItemData.clientInteractPrediction ? 1 : 0);
                this.putByte(useItemData.clientCooldownState ? 1 : 0);
                break;
            case TYPE_USE_ITEM_ON_ENTITY:
                UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) this.transactionData;

                this.putEntityRuntimeId(useItemOnEntityData.entityRuntimeId);
                this.putVarInt(useItemOnEntityData.actionType);
                this.putVarInt(useItemOnEntityData.hotbarSlot);
                this.putItemStack(useItemOnEntityData.itemInHand);
                this.putVector3f(useItemOnEntityData.playerPos.asVector3f());
                this.putVector3f(useItemOnEntityData.clickPos.asVector3f());
                break;
            case TYPE_RELEASE_ITEM:
                ReleaseItemData releaseItemData = (ReleaseItemData) this.transactionData;

                this.putVarInt(releaseItemData.actionType);
                this.putVarInt(releaseItemData.hotbarSlot);
                this.putItemStack(releaseItemData.itemInHand);
                this.putVector3f(releaseItemData.headRot.asVector3f());
                break;
            default:
                throw new RuntimeException("Unknown transaction type " + this.transactionType);
        }
    }

    @Override
    public void decode() {
        AbstractProtocol protocol = (AbstractProtocol) helper.getProtocol();

        this.legacyRequestId = this.getVarInt();

        if (this.getBoolean()) { // legacyRequestIdValid
            int length = (int) this.getUnsignedVarInt();
            if (length > 10) {
                throw new IndexOutOfBoundsException("Too many slot sync requests in inventory transaction");
            }
            this.requestChangedSlots = new LegacySetItemSlotData[length];
            for (int i = 0; i < length; i++) {
                LegacySetItemSlotData requestChangedSlot = new LegacySetItemSlotData();

                requestChangedSlot.containerId = this.getByte();

                int slotCount = (int) this.getUnsignedVarInt();
                requestChangedSlot.changedSlotIndexes = this.get(slotCount);
                if (requestChangedSlot.changedSlotIndexes.length != slotCount) {
                    throw new ArrayIndexOutOfBoundsException("array length mismatch");
                }

                this.requestChangedSlots[i] = requestChangedSlot;
            }
        }

        if (!this.getBoolean()) { // hasTransaction
            return;
        }
        this.transactionType = (int) this.getUnsignedVarInt();

        if (this.getBoolean()) { // hasActions
            int count = (int) this.getUnsignedVarInt();
            if (count > (SERVER_AUTHORITATIVE_INVENTORY ? 50 : 100)) {
                throw new IndexOutOfBoundsException("Too many actions in inventory transaction");
            }
            this.actions = new NetworkInventoryAction[count];
            for (int i = 0; i < this.actions.length; i++) {
                this.actions[i] = new NetworkInventoryAction().readCereal(this, this);
            }
        }

        switch (this.transactionType) {
            case TYPE_NORMAL:
            case TYPE_MISMATCH:
                break;
            case TYPE_USE_ITEM:
                UseItemData itemData = new UseItemData();

                itemData.actionType = this.getVarInt();
                itemData.triggerType = this.getByte();
                itemData.blockPos = this.getBlockVector3();
                itemData.face = this.getBlockFace();
                itemData.hotbarSlot = this.getVarInt();
                itemData.itemInHand = this.getItemStack();
                itemData.playerPos = this.getVector3f().asVector3();
                itemData.clickPos = this.getVector3f();
                int blockRuntimeId = (int) this.getUnsignedVarInt();
                int blockFullId = AdvancedGlobalBlockPalette.getLegacyId(protocol, neteaseMode, blockRuntimeId);
                if (blockFullId != -1) {
                    itemData.block = Block.fromFullId(blockFullId);
                }
                itemData.clientInteractPrediction = this.getByte() != 0;
                itemData.clientCooldownState = this.getByte() != 0;

                this.transactionData = itemData;
                break;
            case TYPE_USE_ITEM_ON_ENTITY:
                UseItemOnEntityData useItemOnEntityData = new UseItemOnEntityData();

                useItemOnEntityData.entityRuntimeId = this.getEntityRuntimeId();
                useItemOnEntityData.actionType = this.getVarInt();
                useItemOnEntityData.hotbarSlot = this.getVarInt();
                useItemOnEntityData.itemInHand = this.getItemStack();
                useItemOnEntityData.playerPos = this.getVector3f().asVector3();
                useItemOnEntityData.clickPos = this.getVector3f().asVector3();

                this.transactionData = useItemOnEntityData;
                break;
            case TYPE_RELEASE_ITEM:
                ReleaseItemData releaseItemData = new ReleaseItemData();

                releaseItemData.actionType = getVarInt();
                releaseItemData.hotbarSlot = getVarInt();
                releaseItemData.itemInHand = getItemStack();
                releaseItemData.headRot = this.getVector3f().asVector3();

                this.transactionData = releaseItemData;
                break;
            default:
                throw new RuntimeException("Unknown transaction type " + this.transactionType);
        }
    }

    @Override
    @Nullable
    public BlockFace getBlockFace() {
        int face = this.getByte();
        return face != 255 ? BlockFace.fromIndex(face) : null;
    }

    @Override
    public void putBlockFace(@Nullable BlockFace face) {
        this.putByte(face != null ? face.getIndex() : 255);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        InventoryTransactionPacket packet = (InventoryTransactionPacket) pk;
        this.transactionType = packet.transactionType;
        this.actions = packet.actions;
        this.transactionData = packet.transactionData;
        this.legacyRequestId = packet.legacyRequestId;
        this.isCraftingPart = packet.isCraftingPart;
        this.isEnchantingPart = packet.isEnchantingPart;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InventoryTransactionPacket.class;
    }
}
