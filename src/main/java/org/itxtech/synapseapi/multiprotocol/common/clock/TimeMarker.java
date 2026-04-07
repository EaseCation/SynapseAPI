package org.itxtech.synapseapi.multiprotocol.common.clock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TimeMarker {
    static final TimeMarker[] EMPTY = new TimeMarker[0];

    public long id;
    public String name;
    public int time;
    public Integer period;
}
