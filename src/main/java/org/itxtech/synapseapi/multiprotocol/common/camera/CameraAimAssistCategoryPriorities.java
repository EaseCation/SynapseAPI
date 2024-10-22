package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraAimAssistCategoryPriorities {
    public int entities;
    public int blocks;
    public int entityDefault;
    public int blockDefault;
}
