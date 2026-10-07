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
public class SetNameTagInfo implements NetEaseJsonEvent {
    public final String eventName = "SetNameTagInfo";
    @Default
    public String data = "";
}
