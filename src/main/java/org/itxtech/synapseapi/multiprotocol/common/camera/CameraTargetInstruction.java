package org.itxtech.synapseapi.multiprotocol.common.camera;

import cn.nukkit.math.Vector3f;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * @since 1.21.20
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraTargetInstruction implements Cloneable {
    @Nullable
    public Vector3f centerOffset;
    public long entityId;

    @Override
    public CameraTargetInstruction clone() {
        try {
            return (CameraTargetInstruction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
