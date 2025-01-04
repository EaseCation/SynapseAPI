package org.itxtech.synapseapi.multiprotocol.protocol116.protocol;

import cn.nukkit.inventory.transaction.data.ReleaseItemData;
import cn.nukkit.inventory.transaction.data.TransactionData;
import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.InventoryTransactionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.InventoryTransactionPacketInterface;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.inventory.LegacySetItemSlotData;
import org.itxtech.synapseapi.utils.ClassUtils;

import static org.itxtech.synapseapi.SynapseSharedConstants.SERVER_AUTHORITATIVE_INVENTORY;

@ToString
public class InventoryTransactionPacket116 extends Packet116 implements InventoryTransactionPacketInterface {
    private static final LegacySetItemSlotData[] EMPTY_LEGACY_DATA = new LegacySetItemSlotData[0];

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_MISMATCH = 1;
    public static final int TYPE_USE_ITEM = 2;
    public static final int TYPE_USE_ITEM_ON_ENTITY = 3;
    public static final int TYPE_RELEASE_ITEM = 4;

    public static final int USE_ITEM_ACTION_CLICK_BLOCK = 0;
    public static final int USE_ITEM_ACTION_CLICK_AIR = 1;
    public static final int USE_ITEM_ACTION_BREAK_BLOCK = 2;

    public static final int RELEASE_ITEM_ACTION_RELEASE = 0; //bow shoot
    public static final int RELEASE_ITEM_ACTION_CONSUME = 1; //eat food, drink potion

    public static final int USE_ITEM_ON_ENTITY_ACTION_INTERACT = 0;
    public static final int USE_ITEM_ON_ENTITY_ACTION_ATTACK = 1;


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

    public boolean hasNetworkIds;
    public int legacyRequestId;
    public LegacySetItemSlotData[] requestChangedSlots = EMPTY_LEGACY_DATA;

    /**
     * NOTE: THIS FIELD DOES NOT EXIST IN THE PROTOCOL, it's merely used for convenience for PocketMine-MP to easily
     * determine whether we're doing a crafting transaction.
     */
    public boolean isCraftingPart = false;
    public boolean isEnchantingPart = false;
    public boolean isRepairItemPart = false;

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
        return this.hasNetworkIds;
    }

    @Override
    public int pid() {
        return ProtocolInfo.INVENTORY_TRANSACTION_PACKET;
    }

    @Override
    public void encode() {
        //TODO 暂时先这样写吧, 应用层需要改的地方太多非常恶心 -- 04/17/2021
        AbstractProtocol protocol = (AbstractProtocol) helper.getProtocol();
        boolean field_hasNetworkIds = protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_116_220.getProtocolStart();

        this.reset();

        this.putVarInt(this.legacyRequestId);
        if (this.legacyRequestId != 0) {
            this.putUnsignedVarInt(this.requestChangedSlots.length);
            for (LegacySetItemSlotData requestChangedSlot : this.requestChangedSlots) {
                this.putByte(requestChangedSlot.containerId);

                this.putUnsignedVarInt(requestChangedSlot.changedSlotIndexes.length);
                this.put(requestChangedSlot.changedSlotIndexes);
            }
        }

        this.putUnsignedVarInt(this.transactionType);

        if (field_hasNetworkIds) this.putBoolean(this.hasNetworkIds);

        this.putUnsignedVarInt(this.actions.length);
        for (NetworkInventoryAction action : this.actions) {
            action.write(this, this);
        }

        switch (this.transactionType) {
            case TYPE_NORMAL:
            case TYPE_MISMATCH:
                break;
            case TYPE_USE_ITEM:
                UseItemData useItemData = (UseItemData) this.transactionData;

                this.putUnsignedVarInt(useItemData.actionType);
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    this.putUnsignedVarInt(useItemData.triggerType);
                }
                this.putBlockVector3(useItemData.blockPos);
                this.putBlockFace(useItemData.face);
                this.putVarInt(useItemData.hotbarSlot);
                this.putSlot(useItemData.itemInHand);
                this.putVector3f(useItemData.playerPos.asVector3f());
                this.putVector3f(useItemData.clickPos);
                this.putUnsignedVarInt(useItemData.blockId);
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    this.putUnsignedVarInt(useItemData.clientInteractPrediction ? 1 : 0);
                }
                break;
            case TYPE_USE_ITEM_ON_ENTITY:
                UseItemOnEntityData useItemOnEntityData = (UseItemOnEntityData) this.transactionData;

                this.putEntityRuntimeId(useItemOnEntityData.entityRuntimeId);
                this.putUnsignedVarInt(useItemOnEntityData.actionType);
                this.putVarInt(useItemOnEntityData.hotbarSlot);
                this.putSlot(useItemOnEntityData.itemInHand);
                this.putVector3f(useItemOnEntityData.playerPos.asVector3f());
                this.putVector3f(useItemOnEntityData.clickPos.asVector3f());
                break;
            case TYPE_RELEASE_ITEM:
                ReleaseItemData releaseItemData = (ReleaseItemData) this.transactionData;

                this.putUnsignedVarInt(releaseItemData.actionType);
                this.putVarInt(releaseItemData.hotbarSlot);
                this.putSlot(releaseItemData.itemInHand);
                this.putVector3f(releaseItemData.headRot.asVector3f());
                break;
            default:
                throw new RuntimeException("Unknown transaction type " + this.transactionType);
        }
    }

    @Override
    public void decode() {
        AbstractProtocol protocol = (AbstractProtocol) helper.getProtocol();
        boolean field_hasNetworkIds = protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_116_220.getProtocolStart();

        this.legacyRequestId = this.getVarInt();
        if (this.legacyRequestId != 0) {
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

        this.transactionType = (int) this.getUnsignedVarInt();

        if (field_hasNetworkIds) this.hasNetworkIds = this.getBoolean();

        int count = (int) this.getUnsignedVarInt();
        if (count > (SERVER_AUTHORITATIVE_INVENTORY ? 50 : 100)) {
            throw new IndexOutOfBoundsException("Too many actions in inventory transaction");
        }
        this.actions = new NetworkInventoryAction[count];
        for (int i = 0; i < this.actions.length; i++) {
            this.actions[i] = new NetworkInventoryAction().read(this, this);
        }

        switch (this.transactionType) {
            case TYPE_NORMAL:
            case TYPE_MISMATCH:
                //Regular ComplexInventoryTransaction doesn't read any extra data
                break;
            case TYPE_USE_ITEM:
                UseItemData itemData = new UseItemData();

                itemData.actionType = (int) this.getUnsignedVarInt();
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    itemData.triggerType = (int) this.getUnsignedVarInt();
                }
                itemData.blockPos = this.getBlockVector3();
                itemData.face = this.getBlockFace();
                itemData.hotbarSlot = this.getVarInt();
                itemData.itemInHand = this.getSlot();
                itemData.playerPos = this.getVector3f().asVector3();
                itemData.clickPos = this.getVector3f();
                itemData.blockId = (int) this.getUnsignedVarInt();
                if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
                    itemData.clientInteractPrediction = this.getUnsignedVarInt() != 0;
                }

                this.transactionData = itemData;
                break;
            case TYPE_USE_ITEM_ON_ENTITY:
                UseItemOnEntityData useItemOnEntityData = new UseItemOnEntityData();

                useItemOnEntityData.entityRuntimeId = this.getEntityRuntimeId();
                useItemOnEntityData.actionType = (int) this.getUnsignedVarInt();
                useItemOnEntityData.hotbarSlot = this.getVarInt();
                useItemOnEntityData.itemInHand = this.getSlot();
                useItemOnEntityData.playerPos = this.getVector3f().asVector3();
                useItemOnEntityData.clickPos = this.getVector3f().asVector3();

                this.transactionData = useItemOnEntityData;
                break;
            case TYPE_RELEASE_ITEM:
                ReleaseItemData releaseItemData = new ReleaseItemData();

                releaseItemData.actionType = (int) getUnsignedVarInt();
                releaseItemData.hotbarSlot = getVarInt();
                releaseItemData.itemInHand = getSlot();
                releaseItemData.headRot = this.getVector3f().asVector3();

                this.transactionData = releaseItemData;
                break;
            default:
                throw new RuntimeException("Unknown transaction type " + this.transactionType);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, InventoryTransactionPacket.class);

        InventoryTransactionPacket packet = (InventoryTransactionPacket) pk;
        this.transactionType = packet.transactionType;
        this.actions = packet.actions;
        this.transactionData = packet.transactionData;
        this.hasNetworkIds = packet.hasNetworkIds;
        this.legacyRequestId = packet.legacyRequestId;
        this.isCraftingPart = packet.isCraftingPart;
        this.isEnchantingPart = packet.isEnchantingPart;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return InventoryTransactionPacket.class;
    }
}
