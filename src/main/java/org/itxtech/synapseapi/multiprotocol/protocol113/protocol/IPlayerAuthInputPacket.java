package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.inventory.transaction.data.UseItemData;
import cn.nukkit.network.protocol.types.InventoryTransactionPacketInterface;
import cn.nukkit.network.protocol.types.NetworkInventoryAction;
import lombok.ToString;

import javax.annotation.Nullable;

public interface IPlayerAuthInputPacket extends InventoryTransactionPacketInterface {

    float getX();
    float getY();
    float getZ();
    float getPitch();
    float getYaw();
    float getHeadYaw();
    float getMoveVecX();
    float getMoveVecZ();
    long getInputFlags();
    int getInputMode();
    int getPlayMode();
    float getVrGazeDirectionX();
    float getVrGazeDirectionY();
    float getVrGazeDirectionZ();
    long getTick();
    float getDeltaX();
    float getDeltaY();
    float getDeltaZ();

    @Nullable
    default NetworkInventoryAction[] getInventoryActions() {
        return null;
    }

    @Nullable
    default UseItemData getUseItemData() {
        return null;
    }

    @Nullable
    default PlayerBlockAction[] getBlockActions() {
        return null;
    }

    /**
     * @since 1.19.0
     */
    default int getInteractionModel() {
        return -1;
    }

    /**
     * @since 1.21.40
     */
    default float getInteractRotationX() {
        return 0;
    }

    /**
     * @since 1.21.40
     */
    default float getInteractRotationY() {
        return 0;
    }

    /**
     * @since 1.20.70
     */
    default float getVehiclePitch() {
        return 0;
    }

    /**
     * @since 1.20.70
     */
    default float getVehicleYaw() {
        return 0;
    }

    /**
     * @since 1.20.60
     */
    default long getPredictedVehicleEntityUniqueId() {
        return 0;
    }

    /**
     * @since 1.19.70
     */
    default float getAnalogMoveVecX() {
        return 0;
    }

    /**
     * @since 1.19.70
     */
    default float getAnalogMoveVecZ() {
        return 0;
    }

    /**
     * @since 1.21.40
     */
    default float getDirectionX() {
        return 0;
    }

    /**
     * @since 1.21.40
     */
    default float getDirectionY() {
        return 0;
    }

    /**
     * @since 1.21.40
     */
    default float getDirectionZ() {
        return 0;
    }

    @ToString
    class PlayerBlockAction {

        public int action;
        public int x;
        public int y;
        public int z;
        public int data;

        public PlayerBlockAction(int action) {
            this.action = action;
        }

        public PlayerBlockAction(int action, int x, int y, int z, int data) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.z = z;
            this.data = data;
        }
    }
}
