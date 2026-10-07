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
public class AddPlayerBannedItem implements NetEaseJsonEvent {
    public final String eventName = "ADD_PLAYER_BANNED_ITEM";
    @Default
    public String[] identifiers = new String[0];
}
