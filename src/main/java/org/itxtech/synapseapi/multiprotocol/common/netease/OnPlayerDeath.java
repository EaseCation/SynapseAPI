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
public class OnPlayerDeath implements NetEaseJsonEvent {
    public final String eventName = "ON_PLAYER_DEATH";
    @Default
    public String deadName = "";
    @Default
    public String message = "";
    @Default
    public String[] params = {};
}
