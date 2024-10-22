package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@ToString
public class CameraAimAssistCategoryDefinition {
    public String name;
    @Default
    public CameraAimAssistCategoryPriorities[] priorities = new CameraAimAssistCategoryPriorities[0];

    public CameraAimAssistCategoryDefinition(String name, CameraAimAssistCategoryPriorities... priorities) {
        this.name = name;
        this.priorities = priorities;
    }
}
