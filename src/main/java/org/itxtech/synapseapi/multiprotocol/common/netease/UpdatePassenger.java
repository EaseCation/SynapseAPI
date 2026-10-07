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
public class UpdatePassenger implements NetEaseJsonEvent {
    public final String eventName = "UPDATE_PASSENGER";
    public long entityId;
    @Default
    public long[] passengers = new long[0];
}
