package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.types.NetworkInventoryAction;

public interface IPlayerAuthInputPacket {

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

    default NetworkInventoryAction[] getInventoryActions() {
        return null;
    }

    default PlayerBlockAction[] getBlockActions() {
        return null;
    }

    default boolean hasInventoryActionsField() {
        return false;
    }

    default boolean hasBlockActionsField() {
        return false;
    }

    class PlayerBlockAction {

        public int action;
        public int x;
        public int y;
        public int z;
        public int face;

        public PlayerBlockAction(int action) {
            this.action = action;
        }

        public PlayerBlockAction(int action, int x, int y, int z, int face) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.z = z;
            this.face = face;
        }
    }
}
