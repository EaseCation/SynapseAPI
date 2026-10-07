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
public class SyncFishingLineMax implements NetEaseJsonEvent {
    public final String eventName = "SYNC_FISHING_LINE_MAX";
    public long entityId;
    @Default
    public String itemName = "";
    public float maxLength;
}
