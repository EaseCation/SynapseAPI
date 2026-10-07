package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SetLockPassenger implements NetEaseJsonEvent {
    public final String eventName = "SET_LOCK_PASSENGER";
    public long entityId;
    public boolean value;
}
