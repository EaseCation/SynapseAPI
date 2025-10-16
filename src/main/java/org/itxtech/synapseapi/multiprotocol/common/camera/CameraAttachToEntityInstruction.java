package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @since 1.21.120
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraAttachToEntityInstruction implements Cloneable {
    public long entityId;

    @Override
    public CameraAttachToEntityInstruction clone() {
        try {
            return (CameraAttachToEntityInstruction) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
