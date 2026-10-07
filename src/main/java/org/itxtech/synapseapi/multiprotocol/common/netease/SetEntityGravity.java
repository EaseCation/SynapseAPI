package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetEntityGravity implements NetEaseJsonEvent {
    public final String eventName = "SET_ENTITY_GRAVITY";
    public long entityId;
    public float gravity;
}
