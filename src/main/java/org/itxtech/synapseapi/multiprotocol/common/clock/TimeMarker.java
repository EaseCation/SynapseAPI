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
    public static final TimeMarker[] OVERWORLD = {
            new TimeMarker(2625810911898139949L, "minecraft:sunrise", 23000, 24000),
            new TimeMarker(4918950784056990566L, "minecraft:night", 13000, 24000),
            new TimeMarker(6827470627776846754L, "minecraft:noon", 6000, 24000),
            new TimeMarker(-7184653752370368672L, "minecraft:midnight", 18000, 24000),
            new TimeMarker(-4807795260250801598L, "minecraft:day", 1000, 24000),
            new TimeMarker(-1781951082890426794L, "minecraft:sunset", 12000, 24000),
    };

    static final TimeMarker[] EMPTY = new TimeMarker[0];

    public long id;
    public String name;
    public int time;
    public Integer period;
}
