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
public class CameraAimAssistPreset {
    public String identifier;
    /**
     * @deprecated 1.21.60
     */
    @Deprecated
    @Default
    public String categories = "";
    @Default
    public String[] exclusions = new String[0];
    @Default
    public String[] liquidTargetingList = new String[0];
    @Default
    public ItemSetting[] itemSettings = new ItemSetting[0];
    @Nullable
    public String defaultItemSettings = "";
    @Nullable
    public String handSettings = "";

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ItemSetting {
        public String identifier;
        public String category;
    }
}
