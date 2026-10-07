package org.itxtech.synapseapi.multiprotocol.common.netease;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ControlEntityAi implements NetEaseJsonEvent {
    public final String eventName = "CONTROL_ENTITY_AI";
    public long entityId;
    @JsonProperty("isAiCloseAi")
    public boolean aiCloseAi;
    @JsonProperty("isFreezeAnim")
    public boolean freezeAnim;
}
