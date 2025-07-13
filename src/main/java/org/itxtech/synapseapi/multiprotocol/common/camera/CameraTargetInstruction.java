package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector3f;
import lombok.*;

import javax.annotation.Nullable;

/**
 * @since 1.21.20
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CameraTargetInstruction implements Cloneable {
    @Nullable
    public Vector3f centerOffset;
    public long entityId;

    public boolean remove;

    public CameraTargetInstruction(long entityId) {
        this.entityId = entityId;
    }

    public CameraTargetInstruction(long entityId, Vector3f centerOffset) {
        this.entityId = entityId;
        this.centerOffset = centerOffset;
    }

    public CameraTargetInstruction(boolean remove) {
        this.remove = remove;
    }

    @Override
    public CameraTargetInstruction clone() {
        try {
            return (CameraTargetInstruction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
