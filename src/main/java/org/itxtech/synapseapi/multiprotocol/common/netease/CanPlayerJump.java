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
public class CanPlayerJump implements NetEaseJsonEvent {
    public final String eventName = "CAN_PLAYER_JUMP";
    public long entityId;
    @Default
    public boolean value = true;
}
