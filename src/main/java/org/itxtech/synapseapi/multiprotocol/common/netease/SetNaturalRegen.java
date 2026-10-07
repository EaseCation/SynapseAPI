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
public class SetNaturalRegen implements NetEaseJsonEvent {
    public final String eventName = "SET_NATURAL_REGEN";
    public long entityId;
    @Default
    public boolean value = true;
}
