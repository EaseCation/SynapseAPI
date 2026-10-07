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
public class SetJumpPower implements NetEaseJsonEvent {
    public final String eventName = "SET_JUMP_POWER";
    public long entityId;
    @Default
    public float value = 0.42f;
}
