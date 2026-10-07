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
public class SyncFishingLineColor implements NetEaseJsonEvent {
    public final String eventName = "SYNC_FISHING_LINE_COLOR";
    public long entityId;
    @Default
    public String itemName = "";
    public float r;
    public float g;
    public float b;
}
