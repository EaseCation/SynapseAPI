package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetPassengerIndex implements NetEaseJsonEvent {
    public final String eventName = "SET_PASSENGER_INDEX";
    public long entityId;
    public long passengerId;
    public int value;
}
