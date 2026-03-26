package org.itxtech.synapseapi.multiprotocol.common.clock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RemoveTimeMarkerAction implements SyncClockAction {
    public long id;
    public long[] markerIds;

    @Override
    public SyncClockActionType getType() {
        return SyncClockActionType.REMOVE_TIME_MARKER;
    }
}
