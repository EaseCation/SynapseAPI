package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetEntityAttackSpeed implements NetEaseJsonEvent {
    public final String eventName = "SET_ENTITY_ATTACK_SPEED";
    public long entityId;
    public float amplifier;
}
