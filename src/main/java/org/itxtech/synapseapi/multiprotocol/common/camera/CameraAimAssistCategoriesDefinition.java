package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@ToString
public class CameraAimAssistCategoriesDefinition {
    public String identifier;
    @Default
    public CameraAimAssistCategoryDefinition[] categories = new CameraAimAssistCategoryDefinition[0];

    public CameraAimAssistCategoriesDefinition(String identifier, CameraAimAssistCategoryDefinition... categories) {
        this.identifier = identifier;
        this.categories = categories;
    }
}
