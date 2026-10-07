package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomAppearanceUuid implements NetEaseJsonEvent {
    public static final int TYPE_CRAFTING_TABLE = 0;
    public static final int TYPE_FURNACE = 1;
    public static final int TYPE_BED = 2;
    public static final int TYPE_CHEST = 3;

    public final String eventName = "CUSTOMAPPEARANCE_UUID";
    @Default
    public String uuid = "";
    public int type;
}
