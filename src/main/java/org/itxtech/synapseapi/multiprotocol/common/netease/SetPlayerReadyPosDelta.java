package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetPlayerReadyPosDelta implements NetEaseJsonEvent {
    public final String eventName = "SET_PLAYER_READY_POS_DELTA";
    public long entityId;
    public float x;
    public float y;
    public float z;
}
