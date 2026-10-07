package org.itxtech.synapseapi.multiprotocol.common.netease;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeliverLobbyGood implements NetEaseJsonServerEvent {
    public final String eventName = "DELIVER_LOBBY_GOOD";
    @Default
    public String uid = "";
    @Default
    public String resid = "";
    @Nullable
    public String[] data;
}
