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
public class OnOpenStore implements NetEaseJsonEvent {
    public final String eventName = "ON_OPEN_STORE";
    @Default
    public String category = "";
}
