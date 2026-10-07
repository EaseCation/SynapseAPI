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
public class AddContainerMix implements NetEaseJsonEvent {
    public final String eventName = "ADD_CONTAINER_MIX";
    @Default
    public String input = "";
    @Default
    public String reagent = "";
    @Default
    public String output = "";
}
