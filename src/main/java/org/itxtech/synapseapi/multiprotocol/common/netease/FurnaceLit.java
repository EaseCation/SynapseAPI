package org.itxtech.synapseapi.multiprotocol.common.netease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FurnaceLit implements NetEaseJsonEvent {
    public final String eventName = "FURNACE_LIT";
    public int x;
    public int y;
    public int z;
    public int lit;
}
