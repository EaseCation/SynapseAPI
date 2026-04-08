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
public class InitializeRegistryAction implements SyncClockAction {
    private static final Entry[] EMPTY = new Entry[0];

    @Default
    public Entry[] data = EMPTY;

    @Override
    public SyncClockActionType getType() {
        return SyncClockActionType.INITIALIZE_REGISTRY;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Entry {
        public long id;
        public String name;
        public int time;
        public boolean paused;
        @Default
        public TimeMarker[] markers = TimeMarker.EMPTY;
    }
}
