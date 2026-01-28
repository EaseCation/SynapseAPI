package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraAimAssistEntityPriority {
    public int presetIndex;
    public int categoryIndex;
    public int entityIndex;
    public int priorityValue;
}
