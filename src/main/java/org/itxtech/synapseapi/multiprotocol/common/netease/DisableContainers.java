package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DisableContainers implements NetEaseJsonEvent {
    public final String eventName = "DISABLE_CONTAINERS";
    public boolean disable;
}
