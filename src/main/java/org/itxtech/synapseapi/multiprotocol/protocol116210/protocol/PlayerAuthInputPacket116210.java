package org.itxtech.synapseapi.multiprotocol.protocol116210.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerActionPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.InventoryTransactionPacketInterface;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseSharedConstants;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.IPlayerAuthInputPacket;
import org.itxtech.synapseapi.multiprotocol.protocol116.protocol.PlayerAuthInputPacket116;

@Log4j2
@ToString
public class PlayerAuthInputPacket116210 extends Packet116210 implements InventoryTransactionPacketInterface, IPlayerAuthInputPacket {

    public static final int NETWORK_ID = ProtocolInfo.PLAYER_AUTH_INPUT_PACKET;

    public static final int INPUT_MODE_MOUSE_KEYBOARD = 1;
    public static final int INPUT_MODE_TOUCHSCREEN = 2;
    public static final int INPUT_MODE_GAME_PAD = 3;
    public static final int INPUT_MODE_MOTION_CONTROLLER = 4;

    public static final int FLAG_ASCEND = 0;
    public static final int FLAG_DESCEND = 1;
    public static final int FLAG_NORTH_JUMP = 2;
    public static final int FLAG_JUMP_DOWN = 3;
    public static final int FLAG_SPRINT_DOWN = 4;
    public static final int FLAG_CHANGE_HEIGHT = 5;
    public static final int FLAG_JUMPING = 6;
    public static final int FLAG_AUTO_JUMPING_IN_WATER = 7;
    public static final int FLAG_SNEAKING = 8;
    public static final int FLAG_SNEAK_DOWN = 9;
    public static final int FLAG_UP = 10;
    public static final int FLAG_DOWN = 11;
    public static final int FLAG_LEFT = 12;
    public static final int FLAG_RIGHT = 13;
    public static final int FLAG_UP_LEFT = 14;
    public static final int FLAG_UP_RIGHT = 15;
    public static final int FLAG_WANT_UP = 16;
    public static final int FLAG_WANT_DOWN = 17;
    public static final int FLAG_WANT_DOWN_SLOW = 18;
    public static final int FLAG_WANT_UP_SLOW = 19;
    public static final int FLAG_SPRINTING = 20;
    public static final int FLAG_ASCEND_SCAFFOLDING = 21;
    public static final int FLAG_DESCEND_SCAFFOLDING = 22;
    public static final int FLAG_SNEAK_TOGGLE_DOWN = 23;
    public static final int FLAG_PERSIST_SNEAK = 24;
    public static final int FLAG_START_SPRINTING = 25;
    public static final int FLAG_STOP_SPRINTING = 26;
    public static final int FLAG_START_SNEAKING = 27;
    public static final int FLAG_STOP_SNEAKING = 28;
    public static final int FLAG_START_SWIMMING = 29;
    public static final int FLAG_STOP_SWIMMING = 30;
    public static final int FLAG_START_JUMPING = 31;
    public static final int FLAG_START_GLIDING = 32;
    public static final int FLAG_STOP_GLIDING = 33;
    public static final int FLAG_PERFORM_ITEM_INTERACTION = 34;
    public static final int FLAG_PERFORM_BLOCK_ACTIONS = 35;
    public static final int FLAG_PERFORM_ITEM_STACK_REQUEST = 36;

    public static final int PLAY_MODE_NORMAL = 0;
    public static final int PLAY_MODE_TEASER = 1;
    public static final int PLAY_MODE_SCREEN = 2;
    public static final int PLAY_MODE_VIEWER = 3;
    public static final int PLAY_MODE_VR = 4;
    public static final int PLAY_MODE_PLACEMENT = 5;
    public static final int PLAY_MODE_LIVING_ROOM = 6;
    public static final int PLAY_MODE_EXIT_LEVEL = 7;
    public static final int PLAY_MODE_EXIT_LEVEL_LIVING_ROOM = 8;

    public static final int STACK_REQUEST_TAKE = 0;
    public static final int STACK_REQUEST_PLACE = 1;
    public static final int STACK_REQUEST_SWAP = 2;
    public static final int STACK_REQUEST_DROP = 3;
    public static final int STACK_REQUEST_DESTROY = 4;
    public static final int STACK_REQUEST_CRAFTING_CONSUME_INPUT = 5;
    public static final int STACK_REQUEST_CRAFTING_MARK_SECONDARY_RESULT_SLOT = 6;
    public static final int STACK_REQUEST_LAB_TABLE_COMBINE = 7;
    public static final int STACK_REQUEST_BEACON_PAYMENT = 8;
    public static final int STACK_REQUEST_MINE_BLOCK = 9;
    public static final int STACK_REQUEST_CRAFTING_RECIPE = 10;
    public static final int STACK_REQUEST_CRAFTING_RECIPE_AUTO = 11;
    public static final int STACK_REQUEST_CREATIVE_CREATE = 12;
    public static final int STACK_REQUEST_CRAFTING_RECIPE_OPTIONAL = 13;
    public static final int STACK_REQUEST_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING = 14;
    public static final int STACK_REQUEST_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING = 15;

    public float x;
    public float y;
    public float z;
    public float pitch;
    public float yaw;
    public float headYaw;
    public float moveVecX;
    public float moveVecZ;
    public long inputFlags;
    public int inputMode;
    public int playMode;
    public float vrGazeDirectionX;
    public float vrGazeDirectionY;
    public float vrGazeDirectionZ;
    public long tick;
    public float deltaX;
    public float deltaY;
    public float deltaZ;

    public boolean hasNetworkIds;
    public NetworkInventoryAction[] inventoryActions;

    public int[] itemStackRequests; //TODO

    public PlayerBlockAction[] blockActions;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.pitch = this.getLFloat();
        this.yaw = this.getLFloat();
        Vector3f position = this.getVector3f();
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.moveVecX = this.getLFloat();
        this.moveVecZ = this.getLFloat();
        this.headYaw = this.getLFloat();
        this.inputFlags = this.getUnsignedVarLong();
        this.inputMode = (int) this.getUnsignedVarInt();
        this.playMode = (int) this.getUnsignedVarInt();
        if (this.playMode == PLAY_MODE_VR) {
            Vector3f vrGazeDirection = this.getVector3f();
            this.vrGazeDirectionX = vrGazeDirection.x;
            this.vrGazeDirectionY = vrGazeDirection.y;
            this.vrGazeDirectionZ = vrGazeDirection.z;
        }
        this.tick = this.getUnsignedVarLong();
        Vector3f delta = this.getVector3f();
        this.deltaX = delta.x;
        this.deltaY = delta.y;
        this.deltaZ = delta.z;

        boolean[] debugFlags;
        if (SynapseSharedConstants.MAC_DEBUG) {
            debugFlags = new boolean[3];
        }

        if ((this.inputFlags & (1L << FLAG_PERFORM_ITEM_INTERACTION)) != 0) {
            if (SynapseSharedConstants.MAC_DEBUG) debugFlags[0] = true;

            int legacyRequestId = (int) this.getUnsignedVarInt();
            if (legacyRequestId < -1 && (legacyRequestId & 1) == 0) {
                int size = (int) this.getUnsignedVarInt();
                for (int i = 0; i < size; i++) {
                    int containerId = this.getByte();

                    int length = (int) this.getUnsignedVarInt();
                    byte[] slots = this.get(length);

                }
            }

            this.hasNetworkIds = this.getBoolean();

            int size = (int) this.getUnsignedVarInt();
            this.inventoryActions = new NetworkInventoryAction[size];
            for (int i = 0; i < size; i++) {
                this.inventoryActions[i] = new NetworkInventoryAction().read(this, this);
            }
        }

        if ((this.inputFlags & (1L << FLAG_PERFORM_ITEM_STACK_REQUEST)) != 0) {
            if (SynapseSharedConstants.MAC_DEBUG) debugFlags[1] = true;

            int requestId = this.getVarInt();

            int size = (int) this.getUnsignedVarInt();
            this.itemStackRequests = new int[size];
            for (int i = 0; i < size; i++) {
                int type = this.getByte();
                switch (type) {
                    case STACK_REQUEST_TAKE:
                        int count = this.getByte();
                        Object source = this.getStackRequestSlotInfo();
                        Object destination = this.getStackRequestSlotInfo();
                        break;
                    case STACK_REQUEST_PLACE:
                        count = this.getByte();
                        source = this.getStackRequestSlotInfo();
                        destination = this.getStackRequestSlotInfo();
                        break;
                    case STACK_REQUEST_SWAP:
                        source = this.getStackRequestSlotInfo();
                        destination = this.getStackRequestSlotInfo();
                        break;
                    case STACK_REQUEST_DROP:
                        count = this.getByte();
                        source = this.getStackRequestSlotInfo();
                        boolean unknownBool3 = this.getBoolean();
                        break;
                    case STACK_REQUEST_DESTROY:
                        count = this.getByte();
                        source = this.getStackRequestSlotInfo();
                        break;
                    case STACK_REQUEST_CRAFTING_CONSUME_INPUT:
                        count = this.getByte();
                        source = this.getStackRequestSlotInfo();
                        break;
                    case STACK_REQUEST_CRAFTING_MARK_SECONDARY_RESULT_SLOT:
                        int slot = this.getByte();
                        break;
                    case STACK_REQUEST_LAB_TABLE_COMBINE:
                        break;
                    case STACK_REQUEST_BEACON_PAYMENT:
                        int primaryEffect = this.getVarInt();
                        int secondaryEffect = this.getVarInt();
                        break;
                    case STACK_REQUEST_MINE_BLOCK:
                        int unknown9 = this.getVarInt();
                        int predictedDurability = this.getVarInt();
                        int stackId = this.getVarInt();
                        break;
                    case STACK_REQUEST_CRAFTING_RECIPE:
                        int recipeNetworkId = (int) this.getUnsignedVarInt();
                        break;
                    case STACK_REQUEST_CRAFTING_RECIPE_AUTO:
                        recipeNetworkId = (int) this.getUnsignedVarInt();
                        break;
                    case STACK_REQUEST_CREATIVE_CREATE:
                        int creativeItemNetworkId = (int) this.getUnsignedVarInt();
                        break;
                    case STACK_REQUEST_CRAFTING_RECIPE_OPTIONAL:
                        recipeNetworkId = (int) this.getUnsignedVarInt();
                        int filteredStringIndex = this.getLInt();
                        break;
                    case STACK_REQUEST_CRAFTING_NON_IMPLEMENTED_DEPRECATED_ASK_TY_LAING:
                        break;
                    case STACK_REQUEST_CRAFTING_RESULTS_DEPRECATED_ASK_TY_LAING:
                        int length = (int) this.getUnsignedVarInt();
                        for (int j = 0; j < length; j++) {
                            Item resultItem = this.getSlot();
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Unhandled item stack request action type: " + type);
                }
                this.itemStackRequests[i] = type;
            }

            size = (int) this.getUnsignedVarInt();
            for (int i = 0; i < size; i++) {
                String filteredString = this.getString();

            }
        }

        if ((this.inputFlags & (1L << FLAG_PERFORM_BLOCK_ACTIONS)) != 0) {
            if (SynapseSharedConstants.MAC_DEBUG) debugFlags[2] = true;

            int size = (int) this.getUnsignedVarInt();
            this.blockActions = new PlayerBlockAction[size];
            for (int i = 0; i < size; i++) {
                int actionType = this.getVarInt();
                PlayerBlockAction action = new PlayerBlockAction(actionType);

                switch (actionType) {
                    case PlayerActionPacket.ACTION_START_BREAK:
                    case PlayerActionPacket.ACTION_ABORT_BREAK:
                    case PlayerActionPacket.ACTION_CONTINUE_BREAK:
                    case PlayerActionPacket.ACTION_BLOCK_PREDICT_DESTROY:
                    case PlayerActionPacket.ACTION_BLOCK_CONTINUE_DESTROY:
                        BlockVector3 pos = this.getBlockVector3();
                        action.x = pos.x;
                        action.y = pos.y;
                        action.z = pos.z;

                        action.face = this.getVarInt();
                }

                this.blockActions[i] = action;
            }
        }

        if (SynapseSharedConstants.MAC_DEBUG) {
            String debug = "";
            if (debugFlags[0]) debug += "PERFORM_ITEM_INTERACTION | ";
            if (debugFlags[1]) debug += "PERFORM_ITEM_STACK_REQUEST | ";
            if (debugFlags[2]) debug += "PERFORM_BLOCK_ACTIONS | ";
            if (!debug.isEmpty()) log.debug("{} {}", debug, this);
        }
    }

    @Override
    public void encode() {

    }

    //TODO
    public Object getStackRequestSlotInfo() {

        int type = this.getByte();
        int slot = this.getByte();
        int stackNetworkId = this.getVarInt();

        return null;
    }

    @Override
    public DataPacket toDefault() {
        PlayerAuthInputPacket116 pk = new PlayerAuthInputPacket116();
        pk.x = this.x;
        pk.y = this.y;
        pk.z = this.z;
        pk.pitch = this.pitch;
        pk.yaw = this.yaw;
        pk.headYaw = this.headYaw;
        pk.moveVecX = this.moveVecX;
        pk.moveVecZ = this.moveVecZ;
        pk.inputFlags = this.inputFlags;
        pk.inputMode = this.inputMode;
        pk.playMode = this.playMode;
        pk.vrGazeDirectionX = this.vrGazeDirectionX;
        pk.vrGazeDirectionY = this.vrGazeDirectionY;
        pk.vrGazeDirectionZ = this.vrGazeDirectionZ;
        pk.tick = this.tick;
        pk.deltaX = this.deltaX;
        pk.deltaY = this.deltaY;
        pk.deltaZ = this.deltaZ;
        return pk;
    }

    @Override
    public void setCraftingPart(boolean craftingPart) {

    }

    @Override
    public boolean isCraftingPart() {
        return false;
    }

    @Override
    public void setEnchantingPart(boolean enchantingPart) {

    }

    @Override
    public boolean isEnchantingPart() {
        return false;
    }

    @Override
    public void setRepairItemPart(boolean repairItemPart) {

    }

    @Override
    public boolean isRepairItemPart() {
        return false;
    }

    @Override
    public boolean hasNetworkIds() {
        return this.hasNetworkIds;
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public float getMoveVecX() {
        return this.moveVecX;
    }

    @Override
    public float getMoveVecZ() {
        return this.moveVecZ;
    }

    @Override
    public long getInputFlags() {
        return this.inputFlags;
    }

    @Override
    public int getInputMode() {
        return this.inputMode;
    }

    @Override
    public int getPlayMode() {
        return this.playMode;
    }

    @Override
    public float getVrGazeDirectionX() {
        return this.vrGazeDirectionX;
    }

    @Override
    public float getVrGazeDirectionY() {
        return this.vrGazeDirectionY;
    }

    @Override
    public float getVrGazeDirectionZ() {
        return this.vrGazeDirectionZ;
    }

    @Override
    public long getTick() {
        return this.tick;
    }

    @Override
    public float getDeltaX() {
        return this.deltaX;
    }

    @Override
    public float getDeltaY() {
        return this.deltaY;
    }

    @Override
    public float getDeltaZ() {
        return this.deltaZ;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public NetworkInventoryAction[] getInventoryActions() {
        return this.inventoryActions;
    }

    @Override
    public PlayerBlockAction[] getBlockActions() {
        return this.blockActions;
    }

    @Override
    public boolean hasInventoryActionsField() {
        return true;
    }

    @Override
    public boolean hasBlockActionsField() {
        return true;
    }
}
