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
public class SetLevelGravity implements NetEaseJsonEvent {
    public final String eventName = "SET_LEVEL_GRAVITY";
    @Default
    public float gravity = -0.08f;
}
