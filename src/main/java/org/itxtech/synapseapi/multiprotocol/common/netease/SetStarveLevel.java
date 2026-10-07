package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetStarveLevel implements NetEaseJsonEvent {
    public final String eventName = "SET_STARVE_LEVEL";
    public long entityId;
    public int value;
}
