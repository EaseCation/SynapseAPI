package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetPlayerPosition implements NetEaseJsonEvent {
    public final String eventName = "SET_PLAYER_POSITION";
    public long entityId;
    public float x;
    public float y;
    public float z;
}
