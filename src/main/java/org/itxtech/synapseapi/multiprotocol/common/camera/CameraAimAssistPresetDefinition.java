package org.itxtech.synapseapi.multiprotocol.common.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CameraAimAssistPresetDefinition {
    public String identifier;
    @Default
    public String categories = "";
    @Default
    public String[] exclusions = new String[0];
    @Default
    public String[] liquidTargetingList = new String[0];
    @Default
    public String[] itemSettings = new String[0];
    @Nullable
    public String defaultItemSettings = "";
    @Nullable
    public String handSettings = "";
}
