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

    void setPitch(float pitch);
    void setYaw(float yaw);

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

    default boolean hasInventoryActionsField() {
        return false;
    }

    default boolean hasBlockActionsField() {
        return false;
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
