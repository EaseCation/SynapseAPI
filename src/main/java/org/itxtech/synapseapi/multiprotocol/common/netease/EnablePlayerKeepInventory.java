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
public class EnablePlayerKeepInventory implements NetEaseJsonEvent {
    public final String eventName = "ENABLE_PLAYER_KEEP_INVENTORY";
    @JsonProperty("entity_id")
    public long entityId;
    public boolean enable;
}
