package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetPlayerSize implements NetEaseJsonEvent {
    public final String eventName = "SET_PLAYER_SIZE";
    public long entityId;
    public Float width;
    public Float height;
}
