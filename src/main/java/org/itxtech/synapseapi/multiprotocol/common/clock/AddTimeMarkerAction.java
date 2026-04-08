package org.itxtech.synapseapi.multiprotocol.common.clock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddTimeMarkerAction implements SyncClockAction {
    public long id;
    public TimeMarker[] markers;

    @Override
    public SyncClockActionType getType() {
        return SyncClockActionType.ADD_TIME_MARKER;
    }
}
