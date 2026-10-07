package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DisableGravityInLiquid implements NetEaseJsonEvent {
    public final String eventName = "DISABLE_GRAVITY_IN_LIQUID";
    public boolean disable;
}
