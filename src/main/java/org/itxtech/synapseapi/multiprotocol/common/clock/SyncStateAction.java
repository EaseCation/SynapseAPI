package org.itxtech.synapseapi.multiprotocol.common.clock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SyncStateAction implements SyncClockAction {
    private static final Entry[] EMPTY = new Entry[0];

    @Default
    public Entry[] data = EMPTY;

    @Override
    public SyncClockActionType getType() {
        return SyncClockActionType.SYNC_STATE;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Entry {
        public long id;
        public int time;
        public boolean paused;
    }
}
