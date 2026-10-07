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
public class SetHealthLevel implements NetEaseJsonEvent {
    public final String eventName = "SET_HEALTH_LEVEL";
    public long entityId;
    @Default
    public int value = 20;
}
